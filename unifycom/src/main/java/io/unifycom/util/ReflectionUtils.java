package io.unifycom.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.Converter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    public static void copyProperties(Object from, Object to) {

        copyProperties(from, to, null);
    }

    public static void copyProperties(Object from, Object to, Converter converter) {

        boolean useConverter = converter != null;
        BeanCopier beanCopier = BeanCopier.create(from.getClass(), to.getClass(), useConverter);
        beanCopier.copy(from, to, converter);
    }

    public static <T> T clone(Object source, Class<T> destinationClass) {

        if (ObjectUtils.anyNull(source, destinationClass)) {

            throw new IllegalArgumentException("Source object or destination class cannot be null.");
        }

        T target = Reflect.onClass(destinationClass).create().get();
        copyProperties(source, target);

        return target;
    }

    public static <T, E> List<T> clone(Collection<E> source, Class<T> destinationClass) {

        if (Objects.isNull(destinationClass)) {

            throw new IllegalArgumentException("Destination class cannot be null.");
        }

        List<T> result = new ArrayList<>();

        if (CollectionUtils.isEmpty(source)) {

            return result;
        }

        source.forEach(p -> result.add(clone(p, destinationClass)));

        return result;
    }

    public static <T> List<T> findFields(Class<T> clazz) {

        return findFields(clazz, false);
    }

    public static <T> List<T> findStaticFields(Class<T> clazz) {

        return findFields(clazz, true);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> findFields(Class<T> clazz, boolean isStatic) {

        List<T> result = new ArrayList<>();
        Field[] fields = clazz.getFields();

        if (ArrayUtils.isNotEmpty(fields)) {
            Arrays.stream(fields).filter(field -> field.getType() == clazz && isStatic && Modifier.isStatic(field.getModifiers())).forEach(field -> {
                try {
                    result.add((T)field.get(clazz));
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }
            });
            Arrays.stream(fields).filter(field -> field.getType() == clazz && !isStatic).forEach(field -> {
                try {
                    result.add((T)field.get(clazz));
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }
            });
        }

        return result;
    }

    /**
     * 获取 sourceClazz 中所有 targetClazz 类型的静态类成员
     */
    @SuppressWarnings("unchecked")
    public static <T, V> List<V> findStaticFields(Class<T> sourceClazz, Class<V> targetClazz) {

        List<V> result = new ArrayList<>();
        Field[] fields = sourceClazz.getFields();
        if (ArrayUtils.isNotEmpty(fields)) {

            for (Field field : fields) {

                if (field.getType() == targetClazz) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    try {
                        result.add((V)field.get(targetClazz));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }

        return result;
    }

    public static <T> Class<T> findSubTypeAssignableFromGenericInterface(Class<?> clazz, Class<T> genericInterface) {

        List<Type> genericInterfaces = findGenericInterfaces(clazz);

        for (Type genericType : genericInterfaces) {

            if (!(genericType instanceof ParameterizedType)) {

                continue;
            }

            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            for (Type actualTypeArgument : actualTypeArguments) {

                if (actualTypeArgument instanceof ParameterizedType) {

                    Type rawType = ((ParameterizedType)actualTypeArgument).getRawType();
                    Class<T> actualClass = findActualClass(rawType, genericInterface);

                    if (actualClass != null) {

                        return actualClass;
                    }
                }

                return findActualClass(actualTypeArgument, genericInterface);
            }
        }

        return null;
    }

    private static <T> Class<T> findActualClass(Type parameterizedType, Class<T> genericInterface) {

        if (!(parameterizedType instanceof Class)) {

            return null;
        }

        @SuppressWarnings("unchecked") Class<T> actualClass = (Class<T>)parameterizedType;
        if (!genericInterface.isAssignableFrom(actualClass)) {

            return null;
        }

        return actualClass;
    }

    private static List<Type> findGenericInterfaces(Class<?> clazz) {

        List<Type> genericInterfaces = new ArrayList<>(10);

        genericInterfaces.add(clazz.getGenericSuperclass());
        Collections.addAll(genericInterfaces, clazz.getGenericInterfaces());

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            genericInterfaces.addAll(findGenericInterfaces(clazz.getSuperclass()));
        }

        return genericInterfaces;
    }
}
