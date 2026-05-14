package com.smartsync.auth.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.smartsync.auth.entity.SysLoginLog;
import com.smartsync.auth.entity.SysUser;
import com.smartsync.auth.mapper.SysLoginLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    @Async
    public void saveLoginLog(HttpServletRequest request, SysUser user, String token, boolean success, String failureReason) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            
            if (user != null) {
                loginLog.setUserId(user.getId());
                loginLog.setUsername(user.getUsername());
                loginLog.setRealName(user.getRealName());
            }
            
            loginLog.setIpAddress(getClientIp(request));
            loginLog.setBrowser(getBrowser(request));
            loginLog.setOperatingSystem(getOperatingSystem(request));
            loginLog.setDeviceType(getDeviceType(request));
            loginLog.setLoginStatus(success ? 1 : 0);
            loginLog.setFailureReason(failureReason);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setToken(StrUtil.sub(token, 0, 50));
            
            loginLogMapper.insert(loginLog);
            log.info("登录日志已保存: username={}, ip={}, status={}", 
                loginLog.getUsername(), loginLog.getIpAddress(), loginLog.getLoginStatus());
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("获取本机IP失败", e);
                }
            }
        }
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getBrowser(HttpServletRequest request) {
        String userAgentStr = request.getHeader("User-Agent");
        if (StrUtil.isBlank(userAgentStr)) {
            return "Unknown";
        }
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        String browser = userAgent.getBrowser().getName();
        String version = userAgent.getVersion();
        return StrUtil.isNotBlank(version) ? browser + " " + version : browser;
    }

    private String getOperatingSystem(HttpServletRequest request) {
        String userAgentStr = request.getHeader("User-Agent");
        if (StrUtil.isBlank(userAgentStr)) {
            return "Unknown";
        }
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        return userAgent.getOs().getName();
    }

    private String getDeviceType(HttpServletRequest request) {
        String userAgentStr = request.getHeader("User-Agent");
        if (StrUtil.isBlank(userAgentStr)) {
            return "Unknown";
        }
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        if (userAgent.isMobile()) {
            return "Mobile";
        } else if (userAgent.isTablet()) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
}
