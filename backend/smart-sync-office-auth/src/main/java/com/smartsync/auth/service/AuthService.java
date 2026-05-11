package com.smartsync.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsync.api.dto.LoginDTO;
import com.smartsync.api.dto.UserDTO;
import com.smartsync.api.exception.BusinessException;
import com.smartsync.api.result.ResultCode;
import com.smartsync.auth.entity.SysPermission;
import com.smartsync.auth.entity.SysUser;
import com.smartsync.auth.mapper.SysUserMapper;
import com.smartsync.auth.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginLogService loginLogService;

    public Map<String, Object> login(LoginDTO loginDTO, HttpServletRequest request) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername())
        );

        String failureReason = null;

        if (user == null) {
            failureReason = "用户不存在";
            loginLogService.saveLoginLog(request, null, null, false, failureReason);
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (user.getStatus() == 0) {
            failureReason = "用户已禁用";
            loginLogService.saveLoginLog(request, user, null, false, failureReason);
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            failureReason = "密码错误";
            loginLogService.saveLoginLog(request, user, null, false, failureReason);
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        user.setLastLoginTime(java.time.LocalDateTime.now());
        userMapper.updateById(user);

        loginLogService.saveLoginLog(request, user, token, true, null);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", 86400);
        return result;
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
