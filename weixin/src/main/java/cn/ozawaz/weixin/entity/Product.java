package cn.ozawaz.weixin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ozawa
 */
@Setter
@Getter
@TableName("t_product")
public class Product extends BaseEntity{

    /**
     * 商品名称
     */
    private String title;

    /**
     * 价格（分）
     */
    private Integer price;
}
