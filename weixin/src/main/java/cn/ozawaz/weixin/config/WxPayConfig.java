package cn.ozawaz.weixin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.config
 * @since JDK1.8
 */
@Configuration
@Setter
@Getter
@PropertySource("classpath:wxpay.properties")
@ConfigurationProperties(prefix="wxpay")
public class WxPayConfig {

    // 商户号
    private String mchId;

    // 商户API证书序列号
    private String mchSerialNo;

    // 商户私钥文件
    private String privateKeyPath;

    // APIv3密钥
    private String apiV3Key;

    // APPID
    private String appid;

    // 微信服务器地址
    private String domain;

    // 接收结果通知地址
    private String notifyDomain;
}

