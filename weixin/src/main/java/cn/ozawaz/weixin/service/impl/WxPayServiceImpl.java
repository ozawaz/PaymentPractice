package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.config.WxPayConfig;
import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.enums.OrderStatus;
import cn.ozawaz.weixin.enums.wxpay.WxApiCode;
import cn.ozawaz.weixin.enums.wxpay.WxApiType;
import cn.ozawaz.weixin.enums.wxpay.WxNotifyType;
import cn.ozawaz.weixin.service.WxPayService;
import cn.ozawaz.weixin.util.OrderNoUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    public void setWxPayConfig(WxPayConfig wxPayConfig) {
        this.wxPayConfig = wxPayConfig;
    }

    @Autowired
    public void setWxPayClient(CloseableHttpClient wxPayClient) {
        this.wxPayClient = wxPayClient;
    }

    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {
        log.info("生成订单");

        // 生成订单
        OrderInfo orderInfo = createOrderInfo(productId);
        //TODO：存入数据库

        // 调用订单api，获取二维码地址和订单号
        return callApi(orderInfo);
    }

    /**
     * 创建订单
     * @param productId 产品id
     * @return 返回订单
     */
    private OrderInfo createOrderInfo(Long productId) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTitle("test")
                .setOrderNo(OrderNoUtils.getOrderNo())
                .setProductId(productId)
                .setTotalFee(1)
                .setOrderStatus(OrderStatus.NOTPAY.getType());
        return orderInfo;
    }

    /**
     * 调用API
     */
    private Map<String, Object> callApi(OrderInfo orderInfo) throws Exception {
        // 请求body参数
        String jsonParams = getCallParameter(orderInfo);
        log.info("请求参数：" + jsonParams);
        // 请求
        HttpPost httpPost = getHttpPost(jsonParams);
        // 完成签名并执行请求
        return callHttpPost(httpPost, orderInfo.getOrderNo());
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
     * @return 返回封装好的请求
     */
    private HttpPost getHttpPost(String jsonParams) {
        String uri = wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType());
        HttpPost httpPost = new HttpPost(uri);
        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        return httpPost;
    }

    /**
     * 完成签名并执行请求，返回地址和订单号
     * @param httpPost 请求
     * @param orderNo 订单号
     * @return 返回地址和订单号
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object>  callHttpPost(HttpPost httpPost, String orderNo) throws Exception{
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
            Map<String, Object> map = new HashMap<>(2);
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderNo);
            return map;
        }
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
