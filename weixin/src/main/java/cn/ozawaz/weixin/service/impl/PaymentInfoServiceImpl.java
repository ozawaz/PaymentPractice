package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.entity.PaymentInfo;
import cn.ozawaz.weixin.enums.PayType;
import cn.ozawaz.weixin.mapper.PaymentInfoMapper;
import cn.ozawaz.weixin.service.PaymentInfoService;
import cn.ozawaz.weixin.util.JsonUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ozawa
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Override
    @SuppressWarnings("all")
    public void createPaymentInfo(String plainText) {
        log.info("记录支付日志");

        // 转换明文
        HashMap<String, Object> map = JsonUtils.getMap(plainText);
        // 订单号
        String orderNo = (String)map.get("out_trade_no");
        // 微信支付订单号
        String transactionId = (String)map.get("transaction_id");
        // 交易类型
        String tradeType = (String)map.get("trade_type");
        // 交易状态
        String tradeState = (String)map.get("trade_state");
        // 订单金额
        Map<String, Object> amount = (Map) map.get("amount");
        // 用户支付金额
        Integer payerTotal = (Integer) amount.get("payer_total");
        // 支付日志
        PaymentInfo paymentInfo = getPaymentInfo(orderNo, transactionId, tradeType, tradeState, payerTotal, plainText);
        // 记录支付日志
        this.save(paymentInfo);
    }

    private PaymentInfo getPaymentInfo(String orderNo, String transactionId, String tradeType,
                                       String tradeState, Integer payerTotal, String plainText) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderNo(orderNo);
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        paymentInfo.setPayerTotal(payerTotal);
        paymentInfo.setContent(plainText);
        return paymentInfo;
    }
}
