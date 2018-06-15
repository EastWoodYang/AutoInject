package com.eastwood.common.autoinject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
public @interface AutoTarget {
    String[] name() default {}; // default value is method name
}