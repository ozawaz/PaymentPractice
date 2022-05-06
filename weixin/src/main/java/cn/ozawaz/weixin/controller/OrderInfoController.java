package cn.ozawaz.weixin.controller;

import cn.ozawaz.weixin.common.Result;
import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.enums.OrderStatus;
import cn.ozawaz.weixin.service.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.controller
 * @since JDK1.8
 */
@CrossOrigin
@Api(tags = "商品订单管理")
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {

    private OrderInfoService orderInfoService;

    @Autowired
    public void setOrderInfoService(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public Result list(){
        List<OrderInfo> list = orderInfoService.listOrderByCreateTimeDesc();
        return Result.ok().data("list", list);
    }

    /**
     * 查询本地订单状态
     */
    @ApiOperation("查询本地订单状态")
    @GetMapping("/query-order-status/{orderNo}")
    public Result queryOrderStatus(@PathVariable String orderNo) {
        OrderInfo orderInfo = orderInfoService.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one();
        String orderStatus = orderInfo.getOrderStatus();
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            return Result.ok();
        }
        return Result.ok().setCode(101).setMessage("支付中...");
    }
}
