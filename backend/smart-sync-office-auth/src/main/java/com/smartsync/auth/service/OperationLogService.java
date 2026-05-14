package com.smartsync.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsync.auth.entity.SysOperationLog;
import com.smartsync.auth.mapper.SysOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final SysOperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Async
    public void saveOperationLog(String moduleName, String operationType, String description,
                                  HttpServletRequest request, Object requestParams,
                                  Object responseResult, Integer status, String errorMsg,
                                  long costTime) {
        try {
            SysOperationLog log = new SysOperationLog();
            log.setModuleName(moduleName);
            log.setOperationType(operationType);
            log.setDescription(description);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                log.setOperator(authentication.getName());
            }

            log.setIpAddress(getClientIp(request));
            if (request != null) {
                log.setRequestMethod(request.getMethod());
                log.setRequestUrl(request.getRequestURI());
            }

            if (requestParams != null) {
                log.setRequestParams(objectMapper.writeValueAsString(requestParams));
            }

            if (responseResult != null) {
                log.setResponseResult(objectMapper.writeValueAsString(responseResult));
            }

            log.setStatus(status);
            log.setErrorMsg(errorMsg);
            log.setCostTime(costTime);
            log.setCreateTime(LocalDateTime.now());

            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}