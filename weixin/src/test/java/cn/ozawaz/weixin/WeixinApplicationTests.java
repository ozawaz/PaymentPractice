package cn.ozawaz.weixin;

import cn.ozawaz.weixin.config.WxPayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;

@SpringBootTest
class WeixinApplicationTests {

    @Autowired
    private WxPayConfig wxPayConfig;

    @Test
    void contextLoads() {
    }

    @Test
    void testGetConfig() {
        String mchId = wxPayConfig.getMchId();
        System.out.println(mchId);
    }

    @Test
    void testGetPrivateKey() {
        // 获取私钥地址
        String privateKeyPath = wxPayConfig.getPrivateKeyPath();
        // 获取私钥
        PrivateKey privateKey = wxPayConfig.getPrivateKey(privateKeyPath);
        System.out.println(privateKey);
    }
}
