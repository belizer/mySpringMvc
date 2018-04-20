package com.belizer.spring.annotation;

import java.lang.annotation.*;

@Target({ElementType.CONSTRUCTOR,ElementType.FIELD,ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
