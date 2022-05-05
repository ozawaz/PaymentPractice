package cn.ozawaz.weixin.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.config
 * @since JDK1.8
 */
@Configuration
@MapperScan("cn.ozawaz.weixin.mapper")
@EnableTransactionManagement
public class MybatisPlusConfig {
}
