package cn.ozawaz.weixin.util;

import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;

/**
 * @author ozawa
 * @version 1.0
 * @date 2022
 * @description cn.ozawaz.weixin.util
 * @since JDK1.8
 */
public class WechatPay2ValidatorForRequest {
    protected static final Logger log = LoggerFactory.getLogger(WechatPay2Validator.class);
    protected static final long REQUEST_EXPIRED_MINUTES = 5L;
    protected final Verifier verifier;
    protected final String body;
    protected final String requestId;

    public WechatPay2ValidatorForRequest(Verifier verifier, String body, String requestId) {
        this.verifier = verifier;
        this.body = body;
        this.requestId = requestId;
    }

    protected static IllegalArgumentException parameterError(String message, Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException("parameter error: " + message);
    }

    @SuppressWarnings("all")
    protected static IllegalArgumentException verifyFail(String message, Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException("signature verify fail: " + message);
    }

    public final boolean validate(HttpServletRequest request) {
        try {
            // 处理请求参数
            this.validateParameters(request);
            // 构造验签名串
            String message = this.buildMessage(request);
            // 平台证书序列号
            String serial = request.getHeader("Wechatpay-Serial");
            // 数字签名
            String signature = request.getHeader("Wechatpay-Signature");
            // 开始验签
            if (!this.verifier.verify(serial, message.getBytes(StandardCharsets.UTF_8), signature)) {
                throw verifyFail("serial=[%s] message=[%s] sign=[%s], request-id=[%s]",
                        serial, message, signature, requestId);
            } else {
                return true;
            }
        } catch (IllegalArgumentException var5) {
            log.warn(var5.getMessage());
            return false;
        }
    }

    protected final void validateParameters(HttpServletRequest request) {
        String[] headers = new String[]{"Wechatpay-Serial", "Wechatpay-Signature", "Wechatpay-Nonce", "Wechatpay-Timestamp"};
        String header = null;

        for (String headerName : headers) {
            header = request.getHeader(headerName);
            if (header == null) {
                throw parameterError("empty [%s], request-id=[%s]", headerName, requestId);
            }
        }
        // 时间戳
        String timestampStr = header;
        // 判断请求是否过期
        try {
            Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestampStr));
            // 拒绝过期请求
            if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= REQUEST_EXPIRED_MINUTES) {
                throw parameterError("timestamp=[%s] expires, request-id=[%s]", timestampStr, requestId);
            }
        } catch (NumberFormatException | DateTimeException var10) {
            throw parameterError("invalid timestamp=[%s], request-id=[%s]", timestampStr, requestId);
        }
    }

    protected final String buildMessage(HttpServletRequest request) {
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        return timestamp + "\n" + nonce + "\n" + body + "\n";
    }
}
