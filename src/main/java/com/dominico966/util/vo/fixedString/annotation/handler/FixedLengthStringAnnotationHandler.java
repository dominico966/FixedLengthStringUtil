package com.dominico966.util.vo.fixedString.annotation.handler;

import com.dominico966.util.vo.fixedString.annotation.util.ObjectUtil;
import com.dominico966.util.vo.fixedString.annotation.FixedLengthIterableString;
import com.dominico966.util.vo.fixedString.annotation.FixedLengthString;
import com.dominico966.util.vo.fixedString.annotation.FixedLengthStringLength;
import com.dominico966.util.vo.fixedString.annotation.exception.CastHandlerNotFoundException;
import com.dominico966.util.vo.fixedString.annotation.exception.DeclaredFieldTypeNotMatchException;
import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.CastHandler;
import com.dominico966.util.vo.fixedString.annotation.handler.interfaces.DataTypeHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FixedLengthStringAnnotationHandler {

    private static final Comparator<Field> ANNOTATED_FIELD_COMPARATOR = (o1, o2) -> {
        int order1, order2;
        if(o1.getAnnotation(FixedLengthIterableString.class) == null) {
            order1 = o1.getAnnotation(FixedLengthString.class).order();
        } else {
            order1 = o1.getAnnotation(FixedLengthIterableString.class).order();
        }

        if(o2.getAnnotation(FixedLengthIterableString.class) == null) {
            order2 = o2.getAnnotation(FixedLengthString.class).order();
        } else {
            order2 = o2.getAnnotation(FixedLengthIterableString.class).order();
        }

        return Integer.compare(order1, order2);
    };

    public static final int MESSAGE_PREFIX_OFFSET = 0;

    public static <T> T deserializeFixedStringAnnotatedInstance(InputStream inputStream, T vo) throws ReflectiveOperationException, DeclaredFieldTypeNotMatchException, CastHandlerNotFoundException {
        Field[] fields = ObjectUtil.getAnnotatedDeclaredFields(vo.getClass(), new Class[] {FixedLengthString.class, FixedLengthIterableString.class}, true);

        Arrays.sort(fields, ANNOTATED_FIELD_COMPARATOR);

        byte[] receivedLengthCutMessage;
        ByteArrayInputStream innerCopyInputStream = null;

        // 노말한 로직 먼저 처리
        for (Field dataField : fields) {
            FixedLengthString fixedLengthStringAnnotation = dataField.getAnnotation(FixedLengthString.class);
            FixedLengthStringLength fixedLengthStringLengthAnnotation = dataField.getAnnotation(FixedLengthStringLength.class);
            FixedLengthIterableString fixedLengthIterableStringAnnotation = dataField.getAnnotation(FixedLengthIterableString.class);

            if (fixedLengthIterableStringAnnotation != null) {
                // 리스트, 배열 형의 경우 처리
                Class<?> iterableObjectType = dataField.getType();

                Field sizeField = vo.getClass().getDeclaredField(fixedLengthIterableStringAnnotation.value());

                sizeField.setAccessible(true);
                int size = Integer.parseInt(sizeField.get(vo).toString());
                sizeField.setAccessible(false);

                dataField.setAccessible(true);
                if (iterableObjectType.isArray()) {
                    Class<?> componentType = iterableObjectType.getComponentType();

                    // Array 객체 생성
                    Object arrayInstance = Array.newInstance(componentType, size);
                    dataField.set(vo, arrayInstance);

                    // Array Component 객체 생성
                    for (int i = 0; i < size; i++) {
                        Object componentInstance = deserializeFixedStringAnnotatedInstance(innerCopyInputStream, componentType.newInstance());
                        Array.set(arrayInstance, i, componentInstance);
                    }

                } else if (iterableObjectType.isAssignableFrom(List.class)) {
                    Class<?> componentType = ObjectUtil.getDeclaredListFieldGenericType(dataField);

                    // 리스트 객체 생성
                    List<?> listInstance = ObjectUtil.createGenericArrayList(componentType);
                    dataField.set(vo, listInstance);

                    // 리시트 엘리먼트 객체 생성
                    for (int i = 0; i < size; i++) {
                        Object componentInstance = deserializeFixedStringAnnotatedInstance(innerCopyInputStream, componentType.newInstance());
                        List.class.getDeclaredMethod("add", Object.class).invoke(listInstance, componentInstance);
                    }
                }
            } else {
                DataType dataType = fixedLengthStringAnnotation.dataType();
                DataTypeHandler dataTypeHandler = dataType.getHandler();

                String value;
                try {
                    int fixedLength = (int) dataTypeHandler.defineLength(vo, fixedLengthStringAnnotation.value());
                    byte[] read;

                    if (innerCopyInputStream == null) {
                        // FixedLengthStringLength 어노테이션 읽기 전에는 소켓의 inputStream 에서 직접 읽기
                        read = readNBytes(inputStream, fixedLength);
                    } else {
                        // FixedLengthStringLength 어노테이션을 읽어 내부 inputStream 이 생성되면 거기서 읽기
                        read = readNBytes(innerCopyInputStream, fixedLength);
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(read).order(ByteOrder.BIG_ENDIAN);
                    CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
                    value = charBuffer.toString().trim();

                    if (fixedLengthStringLengthAnnotation != null) {
                        // FixedLengthStringLength 어노테이션을 읽으면 전문을 전체길이로 읽어 내부 inputStream 을 생성
                        int receivedLength = Integer.parseInt(value);
                        receivedLengthCutMessage = readNBytes(inputStream, receivedLength);
                        innerCopyInputStream = new ByteArrayInputStream(receivedLengthCutMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    value = "";
                }

                dataField.setAccessible(true);
                CastHandler<String, ?> castHandler = dataTypeHandler.getCastHandler(String.class, dataTypeHandler.getJavaType());
                Object dataFieldValue = castHandler.cast(value);
                dataField.set(vo, dataFieldValue);
            }
        }

        if (innerCopyInputStream != null && !(innerCopyInputStream.available() > 0)) {
            try {
                innerCopyInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return vo;
    }

    public static ByteBuffer serializeFixedLengthStringAnnotatedInstance(StringBuilder stringBuilder, Object vo) throws ReflectiveOperationException {
        Field[] fields = ObjectUtil.getAnnotatedDeclaredFields(vo.getClass(), new Class[]{FixedLengthString.class, FixedLengthIterableString.class}, true);

        Arrays.sort(fields, ANNOTATED_FIELD_COMPARATOR);

        for (Field dataField : fields) {
            FixedLengthStringLength fixedLengthStringLengthAnnotation = dataField.getAnnotation(FixedLengthStringLength.class);
            FixedLengthIterableString fixedLengthIterableStringAnnotation = dataField.getAnnotation(FixedLengthIterableString.class);

            dataField.setAccessible(true);
            if (fixedLengthStringLengthAnnotation != null) {
                int messageFullLength = getFixedMessageLength(vo) - MESSAGE_PREFIX_OFFSET;
                dataField.set(vo, messageFullLength);
            }

            if (fixedLengthIterableStringAnnotation != null) {
                Class<?> iterableObjectType = dataField.getType();

                dataField.setAccessible(true);
                if (iterableObjectType.isArray()) {
                    // Array 객체 get
                    Object arrayInstance = dataField.get(vo);
                    int size = Array.getLength(arrayInstance);

                    // Array Component 객체 생성
                    for (int i = 0; i < size; i++) {
                        serializeFixedLengthStringAnnotatedInstance(stringBuilder, Array.get(arrayInstance, i));
                    }

                } else if (iterableObjectType.isAssignableFrom(List.class)) {
                    // 리스트 객체 생성
                    List<?> listInstance = (List<?>) dataField.get(vo);

                    // 리시트 엘리먼트 객체 생성
                    for (Object o : listInstance) {
                        serializeFixedLengthStringAnnotatedInstance(stringBuilder, o);
                    }
                }
            } else {
                FixedLengthString fixedLengthStringAnnotation = dataField.getAnnotation(FixedLengthString.class);
                DataType dataType = fixedLengthStringAnnotation.dataType();
                DataTypeHandler dataTypeHandler = dataType.getHandler();
                CastHandler<?, String> castHandler = dataTypeHandler.getCastHandler(dataTypeHandler.getJavaType(), String.class);
                Method castMethod = castHandler.getClass().getDeclaredMethod("cast", dataTypeHandler.getJavaType());

                Object fieldValue = dataField.get(vo);
                String stringValue = (String) castMethod.invoke(castHandler, fieldValue);
                int fixedLength = (int) dataTypeHandler.defineLength(vo, fixedLengthStringAnnotation.value());
                String fixedLengthString = padString(stringValue, fixedLength);
                stringBuilder.append(fixedLengthString);
            }

        }

        return StandardCharsets.UTF_8.encode(stringBuilder.toString());
    }

    public static String padString(String string, int length) {
        return String.format("%1$" + length + "s", string);
    }

    public static String padString(String string, int length, char padWith) {
        return padString(string, length).replace(' ', padWith);
    }

    public static int sumOfFixedLengthStringAnnotatedFieldLength(Object vo) {
        int sumOfFixedLength = 0;
        for (Field field : ObjectUtil.getAnnotatedDeclaredFields(vo.getClass(), FixedLengthString.class, true)) {
            FixedLengthString FixedLengthString = field.getAnnotation(FixedLengthString.class);
            sumOfFixedLength += FixedLengthString.dataType().getHandler().defineLength(vo, FixedLengthString.value());
        }

        return sumOfFixedLength;
    }

    public static int sumOfFixedLengthStringAnnotatedFieldLength(Object vo, Class<?> dataClass) {
        int sumOfFixedLength = 0;
        for (Field field : ObjectUtil.getAnnotatedDeclaredFields(dataClass, FixedLengthString.class, true)) {
            FixedLengthString fixedLengthString = field.getAnnotation(FixedLengthString.class);
            sumOfFixedLength += fixedLengthString.dataType().getHandler().defineLength(vo, fixedLengthString.value());
        }

        return sumOfFixedLength;
    }

    public static int getFixedMessageLength(Object vo) {
        int sumOfFixedLength = sumOfFixedLengthStringAnnotatedFieldLength(vo);

        for (Field f : ObjectUtil.getAnnotatedDeclaredFields(vo.getClass(), FixedLengthIterableString.class, true)) {
            int size;
            Class<?> componentType;
            try {
                f.setAccessible(true);
                if (f.getType().isArray()) {
                    componentType = f.getType().getComponentType();
                    size = Array.getLength(f.get(vo));

                } else if (f.getType().isAssignableFrom(List.class)) {
                    componentType = ObjectUtil.getDeclaredListFieldGenericType(f);
                    size = (int) List.class.getMethod("size").invoke(f.get(vo));

                } else {
                    continue;
                }
            } catch (ReflectiveOperationException e) {
                continue;
            } finally {
                f.setAccessible(false);
            }

            int iterableClassFixedLengthStringLength = sumOfFixedLengthStringAnnotatedFieldLength(vo, componentType) * size;
            sumOfFixedLength += iterableClassFixedLengthStringLength;
        }

        return sumOfFixedLength;
    }

    private static byte[] readNBytes(InputStream is, int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, 8192)];
            int nread = 0;

            while( (n = is.read(buf, nread, Math.min(buf.length - nread, remaining))) > 0 ) {
                nread += n;
                remaining -=n;
            }

            if(nread >0) {
                if ( Integer.MAX_VALUE - total < nread ) {
                    throw new OutOfMemoryError("Required array size too large");
                }

                total += nread;
                if(result == null) {
                    result = buf;
                } else {
                    if(bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }

        } while ( n > 0 && remaining > 0 );

        if( bufs == null ) {
            if ( result == null ) {
                return new byte[0];
            }
            return result.length == total ? result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for( byte[] b : bufs ) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }
}
