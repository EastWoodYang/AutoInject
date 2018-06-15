package com.eastwood.common.autoinject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface AutoArrow {
    String model();

    int priority() default 0;

    boolean context() default false;
}