package com.belizer.spring.annotation;

import java.lang.annotation.*;

/**
 * 表示该注解作用的范围 作用的范围有包，类型(类，接口，枚举，注解)
 * ，类型成员（成员变量，方法，构造方法，枚举值），方法参数，本地变量（如循环变量，catch参数）
 */
@Target({ElementType.TYPE})
/**
 * Retention 保留 表示该注解会保留到哪个阶段
 * 有三个值
 * Retention.SOURCE 只保留在源码阶段，编译时会被忽略
 * Retention.CLASS 保留到字节码阶段，但会被JVM在运行时忽略
 * Retention.RUNTIME 保留到运行阶段
 */
@Retention(RetentionPolicy.RUNTIME)

@Documented
public @interface Controller {
    String value() default "";
}
