package cn.ozawaz.weixin.service;

import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.service
 * @since JDK1.8
 */
public interface WxPayService {

    /**
     * Native下单
     * @param productId 产品id
     * @return 返回支付二维码连接和订单号
     * @throws Exception 异常
     */
    Map<String, Object> nativePay(Long productId) throws Exception;

    /**
     * 处理订单
     * @param bodyMap 参数
     * @throws GeneralSecurityException 异常
     */
    void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException;

    /**
     * 用户取消订单
     * @param orderNo 订单号
     * @throws Exception 异常
     */
    void cancelOrder(String orderNo) throws Exception;
}
