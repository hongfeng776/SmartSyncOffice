package com.smartsync.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String realName;

    private String ipAddress;

    private String ipLocation;

    private String browser;

    private String operatingSystem;

    private String deviceType;

    private Integer loginStatus;

    private String failureReason;

    private LocalDateTime loginTime;

    private String token;
}
