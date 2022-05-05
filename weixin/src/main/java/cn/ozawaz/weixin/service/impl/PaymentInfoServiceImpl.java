package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.entity.PaymentInfo;
import cn.ozawaz.weixin.mapper.PaymentInfoMapper;
import cn.ozawaz.weixin.service.PaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

}
