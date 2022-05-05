package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.entity.OrderInfo;
import cn.ozawaz.weixin.mapper.OrderInfoMapper;
import cn.ozawaz.weixin.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

}
