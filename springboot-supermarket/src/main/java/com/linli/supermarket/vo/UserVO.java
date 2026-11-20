package com.linli.supermarket.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 会员资料 / 登录结果。 */
@Data
public class UserVO {

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer gender;
    private Integer points;
    private Integer status;
    /** 登录成功后签发的 Token（仅登录接口返回）。 */
    private String token;
    private LocalDateTime createdAt;
}
