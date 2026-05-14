package com.smartsync.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsync.api.result.Result;
import com.smartsync.auth.entity.SysUser;
import com.smartsync.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<Map<String, Object>> getUserPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Page<SysUser> userPage = userService.getUserPage(page, size, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", userPage.getRecords());
        result.put("total", userPage.getTotal());
        result.put("page", userPage.getCurrent());
        result.put("size", userPage.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        return Result.success(userService.createUser(user));
    }

    @PutMapping
    public Result<SysUser> updateUser(@RequestBody SysUser user) {
        return Result.success(userService.updateUser(user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    public Result<List<String>> getUserRoles(@PathVariable Long id) {
        return Result.success(userService.getUserRoles(id));
    }
}
