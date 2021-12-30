package com.dominico966.util.vo.fixedString.annotation.handler.cast;

import com.dominico966.util.vo.fixedString.annotation.exception.CastHandlerNotFoundException;
import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.CastHandler;
import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.CastHandler;
import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.CastHandler;
import net.jodah.typetools.TypeResolver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class CastHandlerMapper {
    private static final Set<CastHandler<?, ?>> CAST_HANDLERS = new HashSet<>();

    static {
        CAST_HANDLERS.add((CastHandler<Short, String>) String::valueOf);
        CAST_HANDLERS.add((CastHandler<Byte, String>) String::valueOf);
        CAST_HANDLERS.add((CastHandler<Integer, String>) String::valueOf);
        CAST_HANDLERS.add((CastHandler<Long, String>) String::valueOf);
        CAST_HANDLERS.add((CastHandler<Float, String>) String::valueOf);
        CAST_HANDLERS.add((CastHandler<Double, String>) String::valueOf);
        CAST_HANDLERS.add((CastHandler<BigDecimal, String>) BigDecimal::toPlainString);
        CAST_HANDLERS.add((CastHandler<BigInteger, String>) BigInteger::toString);

        CAST_HANDLERS.add((CastHandler<String, Short>) Short::parseShort);
        CAST_HANDLERS.add((CastHandler<String, Byte>) Byte::parseByte);
        CAST_HANDLERS.add((CastHandler<String, Integer>) Integer::parseInt);
        CAST_HANDLERS.add((CastHandler<String, Long>) Long::parseLong);
        CAST_HANDLERS.add((CastHandler<String, Float>) Float::parseFloat);
        CAST_HANDLERS.add((CastHandler<String, Double>) Double::parseDouble);
        CAST_HANDLERS.add((CastHandler<String, BigDecimal>) BigDecimal::new);
        CAST_HANDLERS.add((CastHandler<String, BigInteger>) BigInteger::new);
    }

    public static <F, T> CastHandler<F, T> getCastHandler(Class<F> castFrom, Class<T> castTo) {
        for(CastHandler<?,?> handler : CAST_HANDLERS) {
            Class<?> clazz = handler.getClass();
            Class<?>[] genericTypes = TypeResolver.resolveRawArguments(CastHandler.class, clazz);

            Class<?> expectedCastFrom = genericTypes[0];
            Class<?> expectedCastTo = genericTypes[1];

            Class<?> boxedCastFrom = tryToGetBoxedType(castFrom);
            Class<?> boxedCastTo = tryToGetBoxedType(castTo);

            if(expectedCastFrom == boxedCastFrom && expectedCastTo == boxedCastTo) {
                return (CastHandler<F, T>) handler;
            }
        }

        throw new CastHandlerNotFoundException();
    }

    private static Class<?> tryToGetBoxedType(Class<?> clazz) {
        if(clazz == Short.TYPE) {
            return Short.class;
        } else if(clazz == Byte.TYPE) {
            return Byte.class;
        } else if(clazz == Integer.TYPE) {
            return Integer.class;
        } else if(clazz == Long.TYPE) {
            return Long.class;
        } else if(clazz == Float.TYPE) {
            return Float.class;
        } else if(clazz == Double.TYPE) {
            return Double.class;
        } else {
            return clazz;
        }
    }
}
