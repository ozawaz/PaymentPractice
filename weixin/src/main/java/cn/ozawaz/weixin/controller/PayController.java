package cn.ozawaz.weixin.controller;

import cn.ozawaz.weixin.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.controller
 * @since JDK1.8
 */
@RestController
@RequestMapping("/api/pay")
@CrossOrigin
@Api(tags="支付管理")
public class PayController {

    @ApiOperation("测试接口")
    @GetMapping("/test")
    public Result test(){
        return Result.ok()
                .data("message", "hello")
                .data("now", new Date());
    }

}
