package com.smartsync.auth.controller;

import com.smartsync.api.dto.LoginDTO;
import com.smartsync.api.dto.UserDTO;
import com.smartsync.api.result.Result;
import com.smartsync.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @GetMapping("/user/info")
    public Result<UserDTO> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return Result.success(authService.getUserInfo(username));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
