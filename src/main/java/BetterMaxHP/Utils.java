package BetterMaxHP;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Utils {
    public static Method findMethod(Class clz, String methodName, Class... parameterTypes)
            throws NoSuchMethodException {
        try {
            return clz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class superClass = clz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return findMethod(superClass, methodName, parameterTypes);
        }
    }

    public static Field findField(Class clz, String fieldName) throws NoSuchFieldException {
        try {
            return clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return findField(superClass, fieldName);
        }
    }

    public static Object invokeMethod(Object object, Method method, Object... args)
            throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(object, args);
    }

    public static <A> A getField(Object obj, String name) {
        Field field;
        A result;
        try {
            field = findField(obj.getClass(), name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            result = (A) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <A> void setField(Object obj, String name, A value) {
        Field field;
        try {
            field = findField(obj.getClass(), name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
