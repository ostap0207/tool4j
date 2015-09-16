package com.tool4j;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface Option {

    String value();

    Class type() default String.class;

    boolean hasArg() default true;

    boolean isRequired() default false;

}
