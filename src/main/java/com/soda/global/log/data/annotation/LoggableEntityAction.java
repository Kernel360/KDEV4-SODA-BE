package com.soda.global.log.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoggableEntityAction {
    String action(); // "CREATE", "UPDATE", "DELETE"
    Class<?> entityClass();
}