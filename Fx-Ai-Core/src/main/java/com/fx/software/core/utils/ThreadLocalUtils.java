package com.fx.software.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @FileName ThreadLocalUtils
 * @Description
 * @Author fx
 * @date 2026-01-15
 */
public class ThreadLocalUtils {

    public static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal();

    public ThreadLocalUtils() {
    }

    public static void set(String key, Object value) {
        Map<String, Object> map = (Map)threadLocal.get();
        if (map == null) {
            map = new HashMap(16);
            threadLocal.set(map);
        }

        ((Map)map).put(key, value);
    }

    public static boolean isThreadLocalEmpty() {
        Map<String, Object> map = (Map)threadLocal.get();
        return map == null ? true : map.isEmpty();
    }

    public static Map getAll() {
        Map<String, Object> map = (Map)threadLocal.get();
        if (map == null) {
            map = new HashMap(16);
            threadLocal.set(map);
        }

        return (Map)map;
    }

    public static Object get(String key) {
        Map<String, Object> map = (Map)threadLocal.get();
        if (map == null) {
            map = new HashMap(16);
            threadLocal.set(map);
        }

        return ((Map)map).get(key);
    }

    private static String returnObjectValue(Object value) {
        return value == null ? null : value.toString();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
