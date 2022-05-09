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

    /**
     * 查询订单
     * @param orderNo 订单号
     * @return 返回订单状态
     * @throws Exception 异常
     */
    String queryOrder(String orderNo) throws Exception;

    /**
     * 核实订单状态：调用微信支付查单接口
     * @param orderNo 订单号
     * @throws Exception 异常
     */
    void checkOrderStatus(String orderNo) throws Exception;

    /**
     * 根据订单号和退款理由，申请退款
     * @param orderNo 订单号
     * @param reason 退款理由
     * @throws Exception 异常
     */
    void refunds(String orderNo, String reason) throws Exception;

    /**
     * 查询退款接口调用
     * @param refundNo 退单号
     * @return 返回信息
     * @throws Exception 异常
     */
    String queryRefund(String refundNo) throws Exception;
}
