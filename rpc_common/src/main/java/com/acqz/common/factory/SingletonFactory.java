package com.acqz.common.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author haofeng
 * @date 2023/2/25 14:29
 * @description 单例工厂
 */

public class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException("");
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return c.cast(OBJECT_MAP.get(key));
        } else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key, v -> {
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }));
        }
    }

    public static <T> T getInstance(Class<T> c, Object... args) {
        if (c == null) {
            throw new IllegalArgumentException("");
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            return c.cast(OBJECT_MAP.get(key));
        } else {
            T instance;
            try {
                Constructor<T> constructor = c.getDeclaredConstructor(getParameterTypes(args));
                instance = constructor.newInstance(args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            OBJECT_MAP.put(key, instance);
            return instance;
        }
    }

    private static Class<?>[] getParameterTypes(Object[] args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return parameterTypes;
    }
}
