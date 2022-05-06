package cn.ozawaz.weixin.service;

import cn.ozawaz.weixin.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author ozawa
 */
public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 保存订单
     * @param productId 产品id
     * @return 返回保存的订单
     */
    OrderInfo createOrderByProductId(Long productId);

    /**
     * 存储订单二维码
     * @param orderNo 订单号
     * @param codeUrl 二维码
     */
    void saveCodeUrl(String orderNo, String codeUrl);

}
