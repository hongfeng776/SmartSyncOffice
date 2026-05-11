package com.smartsync.api.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    
    USER_NOT_EXIST(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已禁用"),
    TOKEN_INVALID(1004, "Token无效"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    
    CAPTCHA_ERROR(1006, "验证码错误或已过期"),
    CAPTCHA_EXPIRED(1007, "验证码已过期"),
    ACCOUNT_LOCKED(1008, "账号已被锁定，请稍后重试"),
    
    USERNAME_EXISTS(1010, "用户名已存在"),
    ROLE_NOT_EXIST(1020, "角色不存在"),
    
    DIFFERENT_LOCATION_LOGIN(2001, "异地登录");

    private final Integer code;
    private final String message;
}
