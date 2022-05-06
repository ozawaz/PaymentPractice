package cn.ozawaz.weixin.controller;

import cn.ozawaz.weixin.common.Result;
import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.service.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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

}
