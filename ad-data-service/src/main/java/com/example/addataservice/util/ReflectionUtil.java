package com.example.addataservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * ReflectionUtil 提供反射工具方法。
 */
public class ReflectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);

    public static Object getFieldValue(Object node, String fieldName) {
        try {
            String getterName = "get" + capitalize(fieldName);
            Method method = node.getClass().getMethod(getterName);
            return method.invoke(node);
        } catch (Exception e) {
            try {
                Field field = node.getClass().getField(fieldName);
                return field.get(node);
            } catch (Exception ex) {
                logger.warn("Failed to access field {}: {}", fieldName, ex.getMessage());
                return null;
            }
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}