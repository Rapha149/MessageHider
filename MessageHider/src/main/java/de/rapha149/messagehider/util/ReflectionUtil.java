package de.rapha149.messagehider.util;

import de.rapha149.messagehider.Updates;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionUtil {

    private static String version;
    private static String craftbukkitPackage;
    private static String nmsPackage;

    public static String TO_PLAIN_TEXT;

    public static boolean load() {
        Matcher matcher = Pattern.compile("v\\d+_\\d+_R\\d+").matcher(Bukkit.getServer().getClass().getPackage().getName());
        if (matcher.find()) {
            version = matcher.group();
            craftbukkitPackage = "org.bukkit.craftbukkit." + version + ".";
            nmsPackage = "net.minecraft.server." + version + ".";

            if (Updates.isBukkitVersionAboveOrEqualTo("1.13"))
                TO_PLAIN_TEXT = "getString";
            else if (Updates.isBukkitVersionAboveOrEqualTo("1.9"))
                TO_PLAIN_TEXT = "toPlainText";
            else if (Updates.isBukkitVersionAboveOrEqualTo("1.8"))
                TO_PLAIN_TEXT = "c";
            return true;
        }
        return false;
    }

    public static Class<?> getClass(boolean nms, String className) {
        try {
            return Class.forName((nms ? nmsPackage : craftbukkitPackage) + className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object invokeStaticMethod(boolean nms, String className, String method, Object... parameters) {
        return invokeStaticMethod(nms, className, method, Arrays.stream(parameters).map(Param::new).toArray(Param[]::new));
    }

    public static Object invokeStaticMethod(boolean nms, String className, String method, Param... parameters) {
        try {
            Class<?> c = Class.forName((nms ? nmsPackage : craftbukkitPackage) + className);
            Class<?>[] params = Arrays.stream(parameters).map(param -> param.clazz).toArray(Class<?>[]::new);

            Method m = c.getMethod(method, params);
            m.setAccessible(true);
            return m.invoke(null, Arrays.stream(parameters).map(param -> param.value).toArray());
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object invokeMethod(Object obj, String method, Object... parameters) {
        return invokeMethod(obj, method, Arrays.stream(parameters).map(Param::new).toArray(Param[]::new));
    }

    public static Object invokeMethod(Object obj, String method, Param... parameters) {
        try {
            Class<?> c = obj.getClass();
            Class<?>[] params = Arrays.stream(parameters).map(param -> param.clazz).toArray(Class<?>[]::new);

            Method m = c.getMethod(method, params);
            m.setAccessible(true);
            return m.invoke(obj, Arrays.stream(parameters).map(param -> param.value).toArray());
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object newInstance(boolean nms, String className, Object... parameters) {
        return newInstance(nms, className, Arrays.stream(parameters).map(Param::new).toArray(Param[]::new));
    }

    public static Object newInstance(boolean nms, String className, Param... parameters) {
        try {
            Class<?> c = Class.forName((nms ? nmsPackage : craftbukkitPackage) + className);
            Class<?>[] params = Arrays.stream(parameters).map(param -> param.clazz).toArray(Class<?>[]::new);

            Constructor<?> constructor = c.getConstructor(params);
            constructor.setAccessible(true);
            return constructor.newInstance(Arrays.stream(parameters).map(param -> param.value).toArray());
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getField(Object obj, String fieldName) {
        return getField(obj.getClass(), obj, fieldName);
    }

    public static Object getStaticField(Class<?> c, String fieldName) {
        return getField(c, null, fieldName);
    }

    public static Object getStaticField(String className, String fieldName) {
        try {
            return getField(Class.forName(className), null, fieldName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getField(Class<?> c, Object obj, String fieldName) {
        try {
            Field field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Param {

        private Class<?> clazz;
        private Object value;

        public Param(Object value) {
            clazz = value.getClass();
            this.value = value;
        }

        public Param(Class<?> clazz, Object value) {
            this.clazz = clazz;
            this.value = value;
        }

        public Param(String className, Object value) {
            try {
                this.clazz = Class.forName(className);
                this.value = value;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public Param(boolean nms, String className, Object value) {
            try {
                this.clazz = Class.forName((nms ? nmsPackage : craftbukkitPackage) + className);
                this.value = value;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
