package cn.ozawaz.weixin.controller;

import cn.ozawaz.weixin.common.Result;
import cn.ozawaz.weixin.service.WxPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Autowired
    public void setWxPayService(WxPayService wxPayService) {
        this.wxPayService = wxPayService;
    }

    /**
     * Native下单
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
}
