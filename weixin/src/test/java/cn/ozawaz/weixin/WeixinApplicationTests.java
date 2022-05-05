package cn.ozawaz.weixin;

import cn.ozawaz.weixin.config.WxPayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
