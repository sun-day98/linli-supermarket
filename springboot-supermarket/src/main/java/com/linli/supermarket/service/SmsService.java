package com.linli.supermarket.service;

/**
 * 短信验证码服务抽象（生产化 seam）。
 *
 * <p>演示实现直接校验固定验证码；生产可实现真实短信服务商（发送 + 校验 + 风控），
 * 通过 Spring 替换 Bean 即可，无需改鉴权主流程。</p>
 */
public interface SmsService {

    /** 校验手机号对应的验证码，失败返回 false。 */
    boolean verify(String phone, String code);

    /** 发送验证码（演示实现仅记录日志）。 */
    void send(String phone, String code);
}
