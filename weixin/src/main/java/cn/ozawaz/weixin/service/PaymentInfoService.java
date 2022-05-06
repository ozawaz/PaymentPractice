package cn.ozawaz.weixin.service;

import cn.ozawaz.weixin.entity.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author ozawa
 */
public interface PaymentInfoService extends IService<PaymentInfo> {

    /**
     * 根据明文保存支付日志
     * @param plainText 明文
     */
    void createPaymentInfo(String plainText);
}
