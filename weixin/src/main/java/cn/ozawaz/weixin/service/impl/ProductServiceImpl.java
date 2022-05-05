package cn.ozawaz.weixin.service.impl;

import cn.ozawaz.weixin.entity.Product;
import cn.ozawaz.weixin.mapper.ProductMapper;
import cn.ozawaz.weixin.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
