package com.dominico966.util.vo.fixedString.annotation;

import com.dominico966.util.vo.fixedString.annotation.handler.DataType;
import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.DataTypeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FixedLengthString {
    String value();
    int order();
    DataType dataType() default DataType.STRING;
}
