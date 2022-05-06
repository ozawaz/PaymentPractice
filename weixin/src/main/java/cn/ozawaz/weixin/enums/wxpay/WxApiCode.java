package cn.ozawaz.weixin.enums.wxpay;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.enums.wxpay
 * @since JDK1.8
 */
@AllArgsConstructor
@Getter
public enum WxApiCode {

    /**
     * 成功，有响应体
     */
    SUCCESS_BODY(200),
    /**
     * 成功，但是没有响应体
     */
    SUCCESS_NOBODY(204);

    /**
     * 响应码
     */
    private final Integer code;
}
