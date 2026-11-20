package com.linli.supermarket.service.impl;

import com.linli.supermarket.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 演示短信服务：固定验证码校验，发送仅打日志。
 *
 * <p>默认启用（{@code app.sms.mode=demo}）。生产接入真实短信后，提供另一个
 * {@code SmsService} 实现并设置 {@code app.sms.mode=real} 即可替换。</p>
 */
@Service
@ConditionalOnProperty(name = "app.sms.mode", havingValue = "demo", matchIfMissing = true)
public class DemoSmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(DemoSmsServiceImpl.class);

    @Value("${app.auth.demo-verify-code:123456}")
    private String demoCode;

    @Override
    public boolean verify(String phone, String code) {
        return demoCode != null && demoCode.equals(code);
    }

    @Override
    public void send(String phone, String code) {
        log.info("[DEMO SMS] 向 {} 发送验证码 {}", maskPhone(phone), code);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
