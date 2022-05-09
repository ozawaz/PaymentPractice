package cn.ozawaz.weixin.service;

import cn.ozawaz.weixin.entity.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author ozawa
 */
public interface RefundInfoService extends IService<RefundInfo> {

    /**
     * 根据订单号和退款理由创建退款单
     * @param orderNo 订单号
     * @param reason 退款理由
     * @return 退款单
     */
    RefundInfo createRefundByOrderNo(String orderNo, String reason);

    /**
     * 根据退款API返回的信息，更新退款单
     * @param bodyAsString 信息
     */
    void updateRefund(String bodyAsString);
}
