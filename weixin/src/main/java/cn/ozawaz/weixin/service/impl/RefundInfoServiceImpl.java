package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.entity.RefundInfo;
import cn.ozawaz.weixin.mapper.RefundInfoMapper;
import cn.ozawaz.weixin.service.OrderInfoService;
import cn.ozawaz.weixin.service.RefundInfoService;
import cn.ozawaz.weixin.util.JsonUtils;
import cn.ozawaz.weixin.util.OrderNoUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author ozawa
 */
@Service
@Slf4j
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    private OrderInfoService orderInfoService;

    @Autowired
    public void setOrderInfoService(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    @Override
    public RefundInfo createRefundByOrderNo(String orderNo, String reason) {
        log.info("创建退款单记录");
        // 根据订单号获取订单信息
        OrderInfo orderInfo = orderInfoService.getOrderByOrderNo(orderNo);
        // 根据订单信息和退款理由生成退款订单
        RefundInfo refundInfo = getRefundInfo(orderInfo, reason);
        log.info("保存退款单记录");
        // 保存退款信息
        this.save(refundInfo);

        return refundInfo;
    }

    @Override
    @SuppressWarnings("all")
    public void updateRefund(String bodyAsString) {
        // 信息转换成map
        HashMap<String, String> map = JsonUtils.jsonStringToMap(bodyAsString);
        // 退款单号
        String refundNo = map.get("out_refund_no");
        // 找到对应的退款单
        RefundInfo refundInfo = this.lambdaQuery().eq(RefundInfo::getRefundNo, refundNo).one();
        // 根据map中的字段更新退款信息
        updateRefundInfoByMap(bodyAsString, map, refundInfo);
    }

    /**
     * 根据map中的字段更新退款信息
     * @param bodyAsString 全部响应结果
     * @param map map
     * @param refundInfo 退款单
     */
    @SuppressWarnings("all")
    private void updateRefundInfoByMap(String bodyAsString, HashMap<String, String> map, RefundInfo refundInfo) {
        // 微信支付退款单号
        refundInfo.setRefundId(map.get("refund_id"));
        // 查询退款和申请退款的退款状态
        if (map.get("status") != null) {
            // 退款状态
            refundInfo.setRefundStatus(map.get("status"));
            // 存入全部响应结果
            refundInfo.setContentReturn(bodyAsString);
        }
        // 退款回调中的回调参数
        if (map.get("refund_status") != null) {
            // 退款状态
            refundInfo.setRefundStatus(map.get("refund_status"));
            // 存入全部响应结果
            refundInfo.setContentNotify(bodyAsString);
        }
        // 更新
        this.lambdaUpdate().eq(RefundInfo::getRefundNo, refundInfo.getRefundNo()).update(refundInfo);
    }

    /**
     * 根据订单信息和退款理由生成退款订单
     * @param orderInfo 订单信息
     * @param reason 退款理由
     * @return 退款订单
     */
    private RefundInfo getRefundInfo(OrderInfo orderInfo, String reason) {
        RefundInfo refundInfo = new RefundInfo();
        // 订单编号
        refundInfo.setOrderNo(orderInfo.getOrderNo());
        // 退款单编号
        refundInfo.setRefundNo(OrderNoUtils.getRefundNo());
        // 原订单金额(分)
        refundInfo.setTotalFee(orderInfo.getTotalFee());
        // 退款金额(分)
        refundInfo.setRefund(orderInfo.getTotalFee());
        // 退款原因
        refundInfo.setReason(reason);

        return refundInfo;
    }
}
