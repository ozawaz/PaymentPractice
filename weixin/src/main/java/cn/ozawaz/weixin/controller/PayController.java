package cn.ozawaz.weixin.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class PayController {


    @GetMapping("/test")
    public String test(){
        return "hello";
    }

}
