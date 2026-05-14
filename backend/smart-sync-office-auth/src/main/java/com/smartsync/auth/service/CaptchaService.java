package com.smartsync.auth.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CaptchaService {

    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();
    private final Map<String, Long> captchaExpire = new ConcurrentHashMap<>();
    
    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000;

    public Map<String, Object> generateCaptcha() {
        cleanExpiredCaptcha();
        
        RandomGenerator randomGenerator = new RandomGenerator("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", 4);
        
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 4);
        captcha.setGenerator(randomGenerator);
        
        String code = captcha.getCode();
        String base64Image = captcha.getImageBase64Data();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        captchaStore.put(uuid, code.toLowerCase());
        captchaExpire.put(uuid, System.currentTimeMillis() + CAPTCHA_EXPIRE_TIME);
        
        Map<String, Object> result = new HashMap<>();
        result.put("captchaKey", uuid);
        result.put("captchaImage", "data:image/png;base64," + base64Image);
        result.put("expireTime", CAPTCHA_EXPIRE_TIME / 1000);
        
        return result;
    }

    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaCode == null) {
            return false;
        }
        
        cleanExpiredCaptcha();
        
        String storedCode = captchaStore.get(captchaKey);
        if (storedCode == null) {
            return false;
        }
        
        captchaStore.remove(captchaKey);
        captchaExpire.remove(captchaKey);
        
        return storedCode.equals(captchaCode.toLowerCase());
    }

    private void cleanExpiredCaptcha() {
        long now = System.currentTimeMillis();
        captchaExpire.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                captchaStore.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
}
