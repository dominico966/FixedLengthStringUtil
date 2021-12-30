package com.dominico966.util.vo.fixedString.annotation.handler;

import com.dominico966.util.vo.fixedString.annotation.handler.cast.CastHandlerMapper;
import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.*;

import java.util.regex.Pattern;

public class DefaultDataTypeHandler implements DataTypeHandler {
    public static class PadConfig {
        public static int LEFT_PADDING = 0x000000;
        public static int RIGHT_PADDING = 0x000001;
    }

    final Class<?> javaType;
    final int padConfig;
    final char padChar;
    Pattern regexPattern = Pattern.compile(".+", Pattern.DOTALL);
    LengthDefiner lengthDefiner = LengthDefiner.DEFAULT;

    public DefaultDataTypeHandler(Class<?> javaType, int padConfig, char padChar) {
        this.javaType = javaType;
        this.padConfig = padConfig;
        this.padChar = padChar;
    }

    public DefaultDataTypeHandler(Class<?> javaType, int padConfig, char padChar, String regex) {
        this(javaType, padConfig, padChar);
        this.regexPattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    public DefaultDataTypeHandler(Class<?> javaType, int padConfig, char padChar, String regex, LengthDefiner lengthDefiner) {
        this.javaType = javaType;
        this.padConfig = padConfig;
        this.padChar = padChar;
        this.regexPattern = Pattern.compile(regex, Pattern.DOTALL);
        this.lengthDefiner = lengthDefiner;
    }

    @Override
    public boolean isLeftPadding() {
        return this.padConfig == PadConfig.LEFT_PADDING;
    }

    @Override
    public boolean isRightPadding() {
        return this.padConfig == PadConfig.RIGHT_PADDING;
    }

    @Override
    public int defineLength(Object contextObject, String lengthString) {
        return this.lengthDefiner.calculate(contextObject, lengthString);
    }

    @Override
    public boolean isValidValue(String value) {
        return regexPattern.matcher(value).matches();
    }

    @Override
    public <F, T> CastHandler<F, T> getCastHandler(Class<F> originType, Class<T> serializeType) {
        return CastHandlerMapper.getCastHandler(originType, serializeType);
    }

    @Override
    public Class<?> getJavaType() {
        return this.javaType;
    }

}
