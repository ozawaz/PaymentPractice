package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.config.WxPayConfig;
import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.enums.OrderStatus;
import cn.ozawaz.weixin.enums.wxpay.WxApiCode;
import cn.ozawaz.weixin.enums.wxpay.WxApiType;
import cn.ozawaz.weixin.enums.wxpay.WxNotifyType;
import cn.ozawaz.weixin.enums.wxpay.WxTradeState;
import cn.ozawaz.weixin.service.OrderInfoService;
import cn.ozawaz.weixin.service.PaymentInfoService;
import cn.ozawaz.weixin.service.WxPayService;
import cn.ozawaz.weixin.util.JsonUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.service.impl
 * @since JDK1.8
 */
@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    private WxPayConfig wxPayConfig;
    private CloseableHttpClient wxPayClient;
    private OrderInfoService orderInfoService;
    private PaymentInfoService paymentInfoService;
    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    public void setWxPayConfig(WxPayConfig wxPayConfig) {
        this.wxPayConfig = wxPayConfig;
    }

    @Autowired
    public void setWxPayClient(CloseableHttpClient wxPayClient) {
        this.wxPayClient = wxPayClient;
    }

    @Autowired
    public void setOrderInfoService(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    @Autowired
    public void setPaymentInfoService(PaymentInfoService paymentInfoService) {
        this.paymentInfoService = paymentInfoService;
    }

    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {
        log.info("生成订单");

        // 生成订单
        OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId);
        // 判断订单是否已经存在，并返回二维码信息
        Map<String, Object> map = isOrderInfoExist(orderInfo);
        if (map != null) {
            return map;
        }

        // 调用订单api，获取二维码地址和订单号
        return callApi(orderInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("处理订单");
        // 解密获取明文
        String plainText = decryptFromResource(bodyMap);
        // 转换明文
        HashMap<String, Object> map = JsonUtils.getMap(plainText);
        // 获取订单号
        String orderNo = (String) map.get("out_trade_no");
        /*在对业务数据进行状态检查和处理之前，
        要采用数据锁进行并发控制，
        以避免函数重入造成的数据混乱*/
        // 尝试获取锁：
        // 成功获取则立即返回true，获取失败则立即返回false。不必一直等待锁的释放
        if (lock.tryLock()) {
            try {
                // 更新订单状态
                if (updateOrderInfo(orderNo)) {
                    return;
                }
                // 记录支付日志
                paymentInfoService.createPaymentInfo(plainText);
            } finally {
                // 要主动释放锁
                lock.unlock();
            }
        }
    }

    @Override
    public void cancelOrder(String orderNo) throws Exception {
        // 调用微信支付的关单接口
        closeOrder(orderNo);
        // 更新商户端的订单状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);
    }

    @Override
    public String queryOrder(String orderNo) throws Exception {
        log.info("查单接口调用 ===> {}", orderNo);

        // 地址
        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());

        // 请求对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        return callHttpReturn(Collections.singletonList(httpGet));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void checkOrderStatus(String orderNo) throws Exception {
        // 调用查询接口，获取json字符串
        String jsonString = queryOrder(orderNo);
        // 改成map对象
        HashMap<String, Object> map = JsonUtils.getMap(jsonString);
        // 获取订单状态
        Object tradeState = map.get("trade_state");
        // 根据订单状态进行对应的处理
        detailOrder(tradeState, jsonString, orderNo);
    }

    /**
     * 根据订单状态，更新对应订单
     * @param tradeState 订单状态
     * @param jsonString 查询订单结果
     * @param orderNo 订单号
     */
    private void detailOrder(Object tradeState, String jsonString, String orderNo) throws Exception {
        if (WxTradeState.SUCCESS.getType().equals(tradeState)) {
            log.warn("核实订单已支付 ===> {}", orderNo);
            // 如果确认订单已支付则更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            // 记录支付日志
            paymentInfoService.createPaymentInfo(jsonString);
        } else {
            log.warn("核实订单未支付 ===> {}", orderNo);
            // 如果订单未支付，则调用关单接口
            closeOrder(orderNo);
            // 更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }
    }

    /**
     * 关单接口调用
     * @param orderNo 订单号
     */
    private void closeOrder(String orderNo) throws Exception {
        log.info("关单接口的调用，订单号 ===> {}", orderNo);

        // 组装json请求体
        Map<String, String> paramsMap = new HashMap<>(1);
        paramsMap.put("mchid", wxPayConfig.getMchId());
        String jsonParams = JsonUtils.mapToJsonString(paramsMap);
        log.info("请求参数 ===> {}", jsonParams);

        // 创建远程请求对象
        String url = String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpPost httpPost = getHttpPost(jsonParams, url);

        // 完成签名并执行请求
        callHttp(Collections.singletonList(httpPost));
    }

    /**
     * 更新订单
     * @param orderNo 订单号
     */
    private boolean updateOrderInfo(String orderNo) {
        log.info("更新订单状态");
        OrderInfo orderInfo = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one();
        // 防止被删除的订单的回调通知的调用
        if (orderInfo == null){
            return false;
        }
        // 处理重复通知
        // 保证接口调用的幂等性：无论接口被调用多少次，产生的结果是一致的
        String orderStatus = orderInfo.getOrderStatus();
        // 判断支付状态
        if (!orderStatus.equals(OrderStatus.NOTPAY.getType())) {
            return false;
        }
        orderInfo.setOrderStatus(OrderStatus.SUCCESS.getType());

        // 模拟通知并发
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        orderInfoService.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).update(orderInfo);
        return true;
    }

    /**
     * 对称解密
     * @param bodyMap 参数
     * @return 返回明文
     */
    @SuppressWarnings("all")
    private String decryptFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {
        log.info("密文解密");

        // 通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        // 数据密文
        String ciphertext = resourceMap.get("ciphertext");
        // 随机串
        String nonce = resourceMap.get("nonce");
        // 附加数据
        String associatedData = resourceMap.get("associated_data");
        log.info("密文 ===> {}", ciphertext);
        // 解密工具类
        AesUtil aesUtil = new
                AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        // 解密成明文
        String plainText =
                aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),
                        nonce.getBytes(StandardCharsets.UTF_8),
                        ciphertext);
        log.info("明文 ===> {}", plainText);
        return plainText;
    }

    /**
     * 判断订单是否存在，并返回二维码信息
     * @param orderInfo 订单信息
     * @return 返回二维码信息
     */
    private Map<String, Object> isOrderInfoExist(OrderInfo orderInfo) {
        String codeUrl = orderInfo.getCodeUrl();
        if(!StringUtils.isEmpty(codeUrl)){
            log.info("订单已存在，二维码已保存");
            // 返回二维码信息
            return getMap(codeUrl, orderInfo.getOrderNo());
        }
        return null;
    }

    /**
     * 调用API
     */
    private Map<String, Object> callApi(OrderInfo orderInfo) throws Exception {
        // 请求body参数
        String jsonParams = getCallParameter(orderInfo);
        log.info("请求参数：" + jsonParams);
        // 请求地址
        String url = wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType());
        // 请求
        HttpPost httpPost = getHttpPost(jsonParams, url);
        // 完成签名并执行请求
        return callHttpPost(httpPost, orderInfo);
    }

    /**
     * 获取请求参数
     * @param orderInfo 订单信息
     * @return 返回json参数
     */
    private String getCallParameter(OrderInfo orderInfo) {
        Map<String, Object> paramsMap = new HashMap<>(6);
        // 应用ID
        paramsMap.put("appid", wxPayConfig.getAppid());
        // 直连商户号
        paramsMap.put("mchid", wxPayConfig.getMchId());
        // 商品描述
        paramsMap.put("description", orderInfo.getTitle());
        // 商户订单号
        paramsMap.put("out_trade_no", orderInfo.getOrderNo());
        // 通知地址,要求必须为https地址。
        paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        // 订单金额
        Map<String, Object> amount = new HashMap<>(1);
        amount.put("total", orderInfo.getTotalFee());
        paramsMap.put("amount", amount);
        return new JSONObject(paramsMap).toString();
    }

    /**
     * 根据请求参数，封装请求
     * @param jsonParams 请求参数
     * @param url 请求地址
     * @return 返回封装好的请求
     */
    private HttpPost getHttpPost(String jsonParams, String url) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        return httpPost;
    }

    /**
     * 完成签名并执行请求，返回地址和订单号
     * @param httpPost 请求
     * @param orderInfo 订单对象
     * @return 返回地址和订单号
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object>  callHttpPost(HttpPost httpPost, OrderInfo orderInfo) throws Exception{
        try (CloseableHttpResponse response = wxPayClient.execute(httpPost)) {
            // 响应体
            String bodyAsString = EntityUtils.toString(response.getEntity());
            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            // 处理响应
            detailResponse(statusCode, bodyAsString);
            // 响应结果
            HashMap<String, String> resultMap = JSON.parseObject(bodyAsString, HashMap.class);
            // 二维码
            String codeUrl = resultMap.get("code_url");
            // 保存二维码
            orderInfoService.saveCodeUrl(orderInfo.getOrderNo(), codeUrl);
            // 返回参数
            return getMap(codeUrl, orderInfo.getOrderNo());
        }
    }

    /**
     * 完成签名并执行请求
     * @param http 请求
     */
    private void callHttp(List<? extends HttpUriRequest> http) throws Exception{
        try (CloseableHttpResponse response = wxPayClient.execute(http.get(0))) {
            // 响应体
            String bodyAsString = "无返回体";
            if (response.getEntity() != null) {
                bodyAsString = EntityUtils.toString(response.getEntity());
            }
            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            // 处理响应
            detailResponse(statusCode, bodyAsString);
        }
    }

    /**
     * 完成签名并执行请求，返回响应体
     * @param http 请求
     */
    private String callHttpReturn(List<? extends HttpUriRequest> http) throws Exception{
        try (CloseableHttpResponse response = wxPayClient.execute(http.get(0))) {
            // 响应体
            String bodyAsString = "无返回体";
            if (response.getEntity() != null) {
                bodyAsString = EntityUtils.toString(response.getEntity());
            }
            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            // 处理响应
            detailResponse(statusCode, bodyAsString);
            // 返回
            return bodyAsString;
        }
    }

    /**
     * 根据二维码和订单号生成对应信息
     * @param codeUrl 二维码
     * @param orderNo 订单号
     * @return 返回对应信息
     */
    private Map<String, Object> getMap(String codeUrl, String orderNo) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("codeUrl", codeUrl);
        map.put("orderNo", orderNo);
        return map;
    }

    /**
     * 处理响应
     * @param statusCode 响应码
     * @param bodyAsString 响应体
     * @throws Exception 异常
     */
    private void detailResponse(int statusCode, String bodyAsString) throws Exception{
        // 处理成功
        if (statusCode == WxApiCode.SUCCESS_BODY.getCode()) {
            log.info("成功, 返回结果 = " + bodyAsString);
        } else if (statusCode == WxApiCode.SUCCESS_NOBODY.getCode()) {
            // 处理成功，无返回Body
            log.info("成功");
        } else {
            log.info("Native下单失败,响应码 = " + statusCode + ",返回结果 = " + bodyAsString);
            throw new IOException("请求失败");
        }
    }
}
