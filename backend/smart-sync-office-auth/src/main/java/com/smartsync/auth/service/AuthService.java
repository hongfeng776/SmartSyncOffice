package com.smartsync.auth.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsync.api.dto.LoginDTO;
import com.smartsync.api.dto.UserDTO;
import com.smartsync.api.exception.BusinessException;
import com.smartsync.api.result.ResultCode;
import com.smartsync.auth.entity.SysUser;
import com.smartsync.auth.mapper.SysUserMapper;
import com.smartsync.auth.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginLogService loginLogService;
    private final CaptchaService captchaService;

    @Value("${login.max-fail-count:5}")
    private int maxFailCount;

    @Value("${login.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    private static final Pattern INTERNAL_IP_PATTERN = Pattern.compile(
        "^(127\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.|0:0:0:0:0:0:0:1|::1)"
    );

    @Transactional
    public Map<String, Object> login(LoginDTO loginDTO, HttpServletRequest request) {
        log.info("开始登录请求: username={}", loginDTO.getUsername());
        
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername())
        );

        String ipAddress = getClientIp(request);
        log.info("登录IP地址: {}", ipAddress);

        if (user == null) {
            log.warn("用户不存在: username={}", loginDTO.getUsername());
            loginLogService.saveLoginLog(request, null, null, false, "用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        log.info("查询到用户: id={}, username={}, loginFailCount={}, lockTime={}", 
            user.getId(), user.getUsername(), user.getLoginFailCount(), user.getLockTime());

        if (isAccountLocked(user)) {
            long remainingMinutes = getRemainingLockMinutes(user);
            log.warn("账号已锁定: username={}, 剩余解锁时间={}分钟", user.getUsername(), remainingMinutes);
            loginLogService.saveLoginLog(request, user, null, false, "账号已锁定");
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED.getCode(), 
                "账号已被锁定，请在" + remainingMinutes + "分钟后重试");
        }

        if (user.getStatus() == 0) {
            log.warn("用户已禁用: username={}", user.getUsername());
            loginLogService.saveLoginLog(request, user, null, false, "用户已禁用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        if (shouldRequireCaptcha(user)) {
            log.info("需要验证码验证: loginFailCount={}", user.getLoginFailCount());
            boolean captchaValid = captchaService.validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode());
            log.info("验证码校验结果: {}", captchaValid);
            
            if (!captchaValid) {
                incrementLoginFailCount(user);
                loginLogService.saveLoginLog(request, user, null, false, "验证码错误");
                throw new BusinessException(ResultCode.CAPTCHA_ERROR);
            }
        }

        boolean passwordMatch = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        log.info("密码校验结果: {}", passwordMatch);
        
        if (!passwordMatch) {
            incrementLoginFailCount(user);
            
            int currentFailCount = user.getLoginFailCount() != null ? user.getLoginFailCount() : 0;
            int remainingAttempts = maxFailCount - currentFailCount;
            String failureReason = "密码错误";
            if (remainingAttempts > 0 && remainingAttempts <= 2) {
                failureReason = "密码错误，剩余" + remainingAttempts + "次尝试机会";
            }
            
            log.warn("密码错误: username={}, 当前失败次数={}, 剩余尝试次数={}", 
                user.getUsername(), currentFailCount, remainingAttempts);
            
            loginLogService.saveLoginLog(request, user, null, false, failureReason);
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        boolean differentLocation = checkDifferentLocation(user, ipAddress);
        log.info("异地登录检测结果: {}", differentLocation);

        resetLoginFailCount(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setLastLoginLocation(getSimpleLocation(ipAddress));
        userMapper.updateById(user);
        log.info("用户登录成功: username={}, token已生成", user.getUsername());

        loginLogService.saveLoginLog(request, user, token, true, null);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", 86400);
        result.put("differentLocation", differentLocation);
        
        if (differentLocation) {
            result.put("lastLoginIp", user.getLastLoginIp() != null ? user.getLastLoginIp() : "未知");
            result.put("lastLoginTime", user.getLastLoginTime() != null ? user.getLastLoginTime().toString() : "首次登录");
            result.put("currentLoginIp", ipAddress);
        }
        
        return result;
    }

    private boolean isAccountLocked(SysUser user) {
        if (user.getLockTime() == null) {
            return false;
        }
        LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
        boolean locked = LocalDateTime.now().isBefore(unlockTime);
        if (locked) {
            log.info("账号锁定状态检查: lockTime={}, unlockTime={}", user.getLockTime(), unlockTime);
        }
        return locked;
    }

    private long getRemainingLockMinutes(SysUser user) {
        if (user.getLockTime() == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
        if (now.isAfter(unlockTime)) {
            return 0;
        }
        long seconds = java.time.Duration.between(now, unlockTime).getSeconds();
        return Math.max(1, (seconds + 59) / 60);
    }

    private boolean shouldRequireCaptcha(SysUser user) {
        int failCount = user.getLoginFailCount() != null ? user.getLoginFailCount() : 0;
        return failCount >= 3;
    }

    private void incrementLoginFailCount(SysUser user) {
        int failCount = user.getLoginFailCount() != null ? user.getLoginFailCount() : 0;
        failCount++;
        user.setLoginFailCount(failCount);
        user.setUpdateTime(LocalDateTime.now());
        
        log.info("增加登录失败计数: username={}, failCount={}, maxFailCount={}", 
            user.getUsername(), failCount, maxFailCount);
        
        if (failCount >= maxFailCount) {
            user.setLockTime(LocalDateTime.now());
            log.warn("账号已被锁定: username={}, lockTime={}", user.getUsername(), user.getLockTime());
        }
        
        userMapper.updateById(user);
        log.info("用户状态已更新到数据库: id={}, failCount={}, lockTime={}", 
            user.getId(), user.getLoginFailCount(), user.getLockTime());
    }

    private void resetLoginFailCount(SysUser user) {
        user.setLoginFailCount(0);
        user.setLockTime(null);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("重置登录失败计数: username={}", user.getUsername());
    }

    private boolean checkDifferentLocation(SysUser user, String currentIp) {
        if (user.getLastLoginIp() == null) {
            log.info("首次登录，不触发异地登录检测: username={}", user.getUsername());
            return false;
        }
        
        String lastIp = user.getLastLoginIp();
        if (lastIp.equals(currentIp)) {
            log.info("IP相同，不触发异地登录: lastIp={}, currentIp={}", lastIp, currentIp);
            return false;
        }
        
        String lastIpSection = getIpSection(lastIp);
        String currentIpSection = getIpSection(currentIp);
        
        if (isInternalIp(lastIp) && isInternalIp(currentIp)) {
            log.info("都是内网IP，不触发异地登录: lastIp={}, currentIp={}", lastIp, currentIp);
            return false;
        }
        
        boolean different = !lastIpSection.equals(currentIpSection);
        log.info("IP网段比较: lastSection={}, currentSection={}, different={}", 
            lastIpSection, currentIpSection, different);
        
        return different;
    }

    private boolean isInternalIp(String ip) {
        if (ip == null) {
            return true;
        }
        return INTERNAL_IP_PATTERN.matcher(ip).find();
    }

    private String getIpSection(String ip) {
        if (ip == null) {
            return "";
        }
        String[] parts = ip.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return ip;
    }

    private String getSimpleLocation(String ip) {
        if (ip == null) {
            return "未知";
        }
        if (isInternalIp(ip)) {
            return "内网IP";
        }
        return "外网IP: " + getIpSection(ip);
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
        }
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public UserDTO getUserInfo(String username) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = userMapper.selectPermissionCodesByUserId(user.getId());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setRealName(user.getRealName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setRoles(roles);
        userDTO.setPermissions(permissions);

        return userDTO;
    }

    public List<Map<String, Object>> getUserMenus(List<String> permissionCodes) {
        List<Map<String, Object>> menus = new ArrayList<>();
        
        List<Map<String, Object>> constantMenus = new ArrayList<>();
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("path", "/dashboard");
        dashboard.put("name", "Dashboard");
        Map<String, Object> dashboardMeta = new HashMap<>();
        dashboardMeta.put("title", "系统首页");
        dashboardMeta.put("icon", "HomeFilled");
        dashboard.put("meta", dashboardMeta);
        constantMenus.add(dashboard);
        
        List<Map<String, Object>> adminMenus = new ArrayList<>();
        if (permissionCodes.contains("system")) {
            Map<String, Object> systemMenu = new HashMap<>();
            systemMenu.put("path", "/system");
            systemMenu.put("name", "System");
            Map<String, Object> systemMeta = new HashMap<>();
            systemMeta.put("title", "系统管理");
            systemMeta.put("icon", "Setting");
            systemMenu.put("meta", systemMeta);
            
            List<Map<String, Object>> systemChildren = new ArrayList<>();
            if (permissionCodes.contains("system:user")) {
                Map<String, Object> userMenu = new HashMap<>();
                userMenu.put("path", "/system/user");
                Map<String, Object> userMeta = new HashMap<>();
                userMeta.put("title", "用户管理");
                userMeta.put("icon", "User");
                userMenu.put("meta", userMeta);
                systemChildren.add(userMenu);
            }
            if (permissionCodes.contains("system:role")) {
                Map<String, Object> roleMenu = new HashMap<>();
                roleMenu.put("path", "/system/role");
                Map<String, Object> roleMeta = new HashMap<>();
                roleMeta.put("title", "角色管理");
                roleMeta.put("icon", "UserFilled");
                roleMenu.put("meta", roleMeta);
                systemChildren.add(roleMenu);
            }
            if (permissionCodes.contains("system:permission")) {
                Map<String, Object> permMenu = new HashMap<>();
                permMenu.put("path", "/system/permission");
                Map<String, Object> permMeta = new HashMap<>();
                permMeta.put("title", "权限管理");
                permMeta.put("icon", "Lock");
                permMenu.put("meta", permMeta);
                systemChildren.add(permMenu);
            }
            systemMenu.put("children", systemChildren);
            adminMenus.add(systemMenu);
        }
        
        List<Map<String, Object>> commonMenus = new ArrayList<>();
        if (permissionCodes.contains("todo")) {
            Map<String, Object> todoMenu = new HashMap<>();
            todoMenu.put("path", "/todo");
            Map<String, Object> todoMeta = new HashMap<>();
            todoMeta.put("title", "待办事项");
            todoMeta.put("icon", "TodoList");
            todoMenu.put("meta", todoMeta);
            commonMenus.add(todoMenu);
        }
        if (permissionCodes.contains("file")) {
            Map<String, Object> fileMenu = new HashMap<>();
            fileMenu.put("path", "/file");
            Map<String, Object> fileMeta = new HashMap<>();
            fileMeta.put("title", "文件管理");
            fileMeta.put("icon", "Folder");
            fileMenu.put("meta", fileMeta);
            commonMenus.add(fileMenu);
        }
        if (permissionCodes.contains("profile")) {
            Map<String, Object> profileMenu = new HashMap<>();
            profileMenu.put("path", "/profile");
            Map<String, Object> profileMeta = new HashMap<>();
            profileMeta.put("title", "个人中心");
            profileMeta.put("icon", "User");
            profileMenu.put("meta", profileMeta);
            commonMenus.add(profileMenu);
        }
        
        menus.addAll(constantMenus);
        menus.addAll(adminMenus);
        menus.addAll(commonMenus);
        
        return menus;
    }
}
