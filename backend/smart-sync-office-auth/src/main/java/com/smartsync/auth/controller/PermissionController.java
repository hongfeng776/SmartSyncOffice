package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.SysPermission;
import com.smartsync.auth.entity.SysRole;
import com.smartsync.auth.mapper.SysPermissionMapper;
import com.smartsync.auth.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PermissionController {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @GetMapping("/roles")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1)
        );
        return Result.success(roles);
    }

    @GetMapping("/permissions")
    public Result<List<SysPermission>> getAllPermissions() {
        List<SysPermission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSortOrder)
        );
        return Result.success(permissions);
    }
}
