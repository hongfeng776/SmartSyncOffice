package com.smartsync.auth.controller;

import com.smartsync.api.result.Result;
import com.smartsync.auth.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/generate")
    public Result<Map<String, Object>> generateCaptcha() {
        return Result.success(captchaService.generateCaptcha());
    }
}
