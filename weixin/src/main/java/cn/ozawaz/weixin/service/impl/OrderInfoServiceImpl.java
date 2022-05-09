package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.entity.Product;
import cn.ozawaz.weixin.enums.OrderStatus;
import cn.ozawaz.weixin.mapper.OrderInfoMapper;
import cn.ozawaz.weixin.service.OrderInfoService;
import cn.ozawaz.weixin.service.ProductService;
import cn.ozawaz.weixin.util.OrderNoUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author ozawa
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    private ProductService productService;

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public OrderInfo createOrderByProductId(Long productId) {
        // 查找已存在但是未支付的订单
        OrderInfo orderInfo = getNoPayOrderByProductId(productId);
        // 假如存在直接返回
        if (orderInfo != null) {
            return orderInfo;
        }
        // 找到对应的产品
        Product product = productService.getById(productId);
        // 订单信息
        orderInfo = createOrderInfo(product, productId);
        // 保存
        this.save(orderInfo);
        // 返回
        return orderInfo;
    }

    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        // 查询订单
        OrderInfo orderInfo = this.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one();
        // 更新
        orderInfo.setCodeUrl(codeUrl);
        this.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).update(orderInfo);
    }

    @Override
    public List<OrderInfo> listOrderByCreateTimeDesc() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<OrderInfo>().orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus type) {
        log.info("更新订单状态");
        OrderInfo orderInfo = this.lambdaQuery().eq(OrderInfo::getOrderNo, orderNo).one();
        orderInfo.setOrderStatus(type.getType());
        this.lambdaUpdate().eq(OrderInfo::getOrderNo, orderNo).update(orderInfo);
    }

    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes) {
        // minutes分钟之前的时间
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));
        return this.lambdaQuery()
                .eq(OrderInfo::getOrderStatus, OrderStatus.NOTPAY.getType())
                .le(OrderInfo::getCreateTime, instant)
                .list();
    }

    /**
     * 根据商品id查询未支付订单
     * 防止重复创建订单对象
     * @param productId 商品id
     * @return 返回订单对象
     */
    private OrderInfo getNoPayOrderByProductId(Long productId) {
        return this.lambdaQuery()
                .eq(OrderInfo::getProductId, productId)
                .eq(OrderInfo::getOrderStatus, OrderStatus.NOTPAY.getType())
                .one();
    }

    /**
     * 新建订单对象
     * @param product 商品信息
     * @param productId 商品id
     * @return 返回订单对象
     */
    private OrderInfo createOrderInfo(Product product, Long productId) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle())
                .setOrderNo(OrderNoUtils.getOrderNo())
                .setProductId(productId)
                .setTotalFee(product.getPrice())
                .setOrderStatus(OrderStatus.NOTPAY.getType());
        return orderInfo;
    }
}
