package cn.ozawaz.weixin.service;

import cn.ozawaz.weixin.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    /**
     * 查询订单列表，并倒序查询
     * @return 返回订单列表
     */
    List<OrderInfo> listOrderByCreateTimeDesc();
}
