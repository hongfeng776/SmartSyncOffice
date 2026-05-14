package com.smartsync.auth.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String moduleName() default "";
    String operationType() default "";
    String description() default "";
}