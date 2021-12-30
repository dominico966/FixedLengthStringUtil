package com.dominico966.util.vo.fixedString.annotation.handler;

import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.DataTypeHandler;

public enum DataType {
    STRING(String.class, DefaultDataTypeHandler.PadConfig.RIGHT_PADDING, ' '),
    FLOAT(Float.class, DefaultDataTypeHandler.PadConfig.LEFT_PADDING, '0', "[0-9+-. ]+"),
    INTEGER (Integer.class, DefaultDataTypeHandler.PadConfig.LEFT_PADDING, '0', "[0-9+-]+");


    private DataTypeHandler dataTypeHandler;

    DataType(Class<?> javaType, int padConfig, char padChar) {
        this.dataTypeHandler = new DefaultDataTypeHandler(javaType, padConfig, padChar);
    }

    DataType(Class<?> javaType, int padConfig, char padChar, String regex) {
        this.dataTypeHandler = new DefaultDataTypeHandler(javaType, padConfig, padChar, regex);
    }

    public DataTypeHandler getHandler() {
        return this.dataTypeHandler;
    }
}
