package cn.ozawaz.weixin.task;

import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.service.OrderInfoService;
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

    @Autowired
    public void setOrderInfoService(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    /**
     * 从第0秒开始每隔30秒执行1次，查询创建超过5分钟，并且未支付的订单
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() {
        log.info("orderConfirm 被执行......");
        // 查询创建超过5分钟，并且未支付的订单
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(5);
        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ===> {}", orderNo);
        }
    }
}
