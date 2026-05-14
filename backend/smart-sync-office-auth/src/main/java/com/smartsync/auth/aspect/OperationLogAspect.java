package com.smartsync.auth.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsync.auth.annotation.OperationLog;
import com.smartsync.auth.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.smartsync.auth.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);

        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
        } catch (Exception e) {
            log.warn("获取HttpServletRequest失败", e);
        }

        Object[] args = joinPoint.getArgs();
        String requestParams = null;
        try {
            if (args != null && args.length > 0) {
                requestParams = objectMapper.writeValueAsString(args);
            }
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
        }

        Object result = null;
        Integer status = 1;
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            try {
                if (operationLogAnnotation != null) {
                    operationLogService.saveOperationLog(
                            operationLogAnnotation.moduleName(),
                            operationLogAnnotation.operationType(),
                            operationLogAnnotation.description(),
                            request,
                            requestParams,
                            result,
                            status,
                            errorMsg,
                            costTime
                    );
                }
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
    }
}