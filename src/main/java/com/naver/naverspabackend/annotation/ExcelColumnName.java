package com.naver.naverspabackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumnName {

    String headerName() default "";
    boolean upload() default false;
    boolean download() default true;
    String type() default "common";

    int width() default 256;
}
