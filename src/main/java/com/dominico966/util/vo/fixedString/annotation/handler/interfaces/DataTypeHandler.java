package com.dominico966.util.vo.fixedString.annotation.handler.interfaces;

public interface DataTypeHandler {

    boolean isLeftPadding();

    boolean isRightPadding();

    int defineLength(Object contextObject, String lengthString);

    boolean isValidValue(String value);

    <F, T> CastHandler<F, T> getCastHandler(Class<F> originType, Class<T> serializeType);

    Class<?> getJavaType();
}
