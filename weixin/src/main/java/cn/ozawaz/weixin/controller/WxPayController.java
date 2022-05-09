package cn.ozawaz.weixin.controller;

import cn.ozawaz.weixin.common.Result;
import cn.ozawaz.weixin.service.WxPayService;
import cn.ozawaz.weixin.util.HttpUtils;
import cn.ozawaz.weixin.util.JsonUtils;
import cn.ozawaz.weixin.util.WechatPay2ValidatorForRequest;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.controller
 * @since JDK1.8
 */
@CrossOrigin
@RestController
@RequestMapping("/api/wx-pay")
@Api(tags = "网站微信支付")
@Slf4j
public class WxPayController {

    private WxPayService wxPayService;
    @Resource
    private Verifier verifier;

    @Autowired
    public void setWxPayService(WxPayService wxPayService) {
        this.wxPayService = wxPayService;
    }

    /**
     * Native下单
     *
     * @param productId 产品id
     * @return 返回支付二维码连接和订单号
     * @throws Exception 异常
     */
    @ApiOperation("调用统一下单API，生成支付二维码")
    @PostMapping("/native/{productId}")
    public Result nativePay(@PathVariable Long productId) throws Exception {
        log.info("发起支付请求");
        // 支付二维码连接和订单号
        Map<String, Object> map = wxPayService.nativePay(productId);
        return Result.ok().setData(map);
    }

    /**
     * 支付通知
     * 微信支付通过支付通知接口将用户支付成功消息通知给商户
     */
    @ApiOperation("支付通知")
    @PostMapping("/native/notify")
    @SuppressWarnings("unchecked")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException {
        // 应答对象
        Map<String, String> map = new HashMap<>(2);
        // 处理通知参数
        String body = HttpUtils.readData(request);
        Map<String, Object> bodyMap = JsonUtils.jsonStringToMap(body);
        // id
        String requestId = (String) bodyMap.get("id");
        log.info("支付通知的id ===> {}", bodyMap.get("id"));
        log.info("支付通知的完整数据 ===> {}", body);
        // 签名的验证
        WechatPay2ValidatorForRequest validator
                = new WechatPay2ValidatorForRequest(verifier, body, requestId);
        // 验签没通过
        if (!validator.validate(request)) {
            log.error("验签没通过");
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "验签没通过");
            return JsonUtils.mapToJsonString(map);
        }
        log.info("验签通过");
        // 处理订单
        wxPayService.processOrder(bodyMap);
        //成功应答：成功应答必须为200或204，否则就是失败应答
        response.setStatus(200);
        map.put("code", "SUCCESS");
        map.put("message", "成功");
        return JsonUtils.mapToJsonString(map);
    }

    /**
     * 用户取消订单
     * @param orderNo 订单号
     * @return 返回消息
     * @throws Exception 异常
     */
    @ApiOperation("用户取消订单")
    @PostMapping("/cancel/{orderNo}")
    public Result cancel(@PathVariable String orderNo) throws Exception {
        log.info("取消订单");
        wxPayService.cancelOrder(orderNo);
        return Result.ok().setMessage("订单已取消");
    }

    /**
     * 查询订单
     * @param orderNo 订单号
     * @return 返回订单状态
     * @throws Exception 异常
     */
    @ApiOperation("查询订单：测试订单状态用")
    @GetMapping("query/{orderNo}")
    public Result queryOrder(@PathVariable String orderNo) throws Exception {
        log.info("查询订单");
        String bodyAsString = wxPayService.queryOrder(orderNo);
        return Result.ok().setMessage("查询成功").data("bodyAsString", bodyAsString);
    }

    @ApiOperation("申请退款")
    @PostMapping("/refunds/{orderNo}/{reason}")
    public Result refunds(@PathVariable String orderNo, @PathVariable String reason) throws Exception {
        log.info("申请退款");
        wxPayService.refunds(orderNo, reason);
        return Result.ok();
    }
}
