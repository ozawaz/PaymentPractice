package cn.ozawaz.weixin.task;

import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.entity.RefundInfo;
import cn.ozawaz.weixin.service.OrderInfoService;
import cn.ozawaz.weixin.service.RefundInfoService;
import cn.ozawaz.weixin.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.task
 * @since JDK1.8
 */
@Slf4j
@Component
public class WxPayTask {

    private OrderInfoService orderInfoService;
    private WxPayService wxPayService;
    private RefundInfoService refundInfoService;

    @Autowired
    public void setOrderInfoService(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    @Autowired
    public void setWxPayService(WxPayService wxPayService) {
        this.wxPayService = wxPayService;
    }

    @Autowired
    public void setRefundInfoService(RefundInfoService refundInfoService) {
        this.refundInfoService = refundInfoService;
    }

    /**
     * 从第0秒开始每隔30秒执行1次，查询创建超过5分钟，并且未支付的订单
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() throws Exception {
        log.info("orderConfirm 被执行......");
        // 查询创建超过5分钟，并且未支付的订单
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(5);
        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ===> {}", orderNo);
            // 核实订单状态：调用微信支付查单接口
            wxPayService.checkOrderStatus(orderNo);
        }
    }

    /**
     * 从第0秒开始每隔30秒执行1次，查询创建超过5分钟，并且未成功的退款单
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void refundConfirm() throws Exception {
        log.info("refundConfirm 被执行......");
        // 找出申请退款超过5分钟并且未成功的退款单
        List<RefundInfo> refundInfoList =
                refundInfoService.getNoRefundOrderByDuration(5);
        for (RefundInfo refundInfo : refundInfoList) {
            String refundNo = refundInfo.getRefundNo();
            log.warn("超时未退款的退款单号 ===> {}", refundNo);
            // 核实订单状态：调用微信支付查询退款接口
            wxPayService.checkRefundStatus(refundNo);
        }
    }
}
