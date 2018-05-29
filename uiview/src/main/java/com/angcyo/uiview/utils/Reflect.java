package com.angcyo.uiview.utils;

import android.text.TextUtils;

import com.angcyo.library.utils.L;
import com.angcyo.uiview.recycler.RBaseViewHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by angcyo on 2016-11-26.
 */

public class Reflect {

    /**
     * 从一个对象中, 获取指定的成员对象
     */
    public static Object getMember(Object target, String member) {
        if (target == null) {
            return null;
        }
        return getMember(target.getClass(), target, member);
    }

    public static Object getMember(Class<?> cls, Object target, String member) {
        Object result = null;
        try {
            Field memberField = cls.getDeclaredField(member);
            memberField.setAccessible(true);
            result = memberField.get(target);
        } catch (Exception e) {
            //L.i("错误:" + cls.getSimpleName() + " ->" + e.getMessage());
        }
        return result;
    }

    /**
     * 只判断String类型的字段, 是否相等
     */
    public static boolean areContentsTheSame(Object target1, Object target2) {
        if (target1 == null || target2 == null) {
            return false;
        }
        if (!target1.getClass().isAssignableFrom(target2.getClass()) ||
                !target2.getClass().isAssignableFrom(target1.getClass())) {
            return false;
        }

        boolean result = true;
        try {
            Field[] declaredFields1 = target1.getClass().getDeclaredFields();
            Field[] declaredFields2 = target2.getClass().getDeclaredFields();
            for (int i = 0; i < declaredFields1.length; i++) {
                declaredFields1[i].setAccessible(true);
                Object v1 = declaredFields1[i].get(target1);

                declaredFields2[i].setAccessible(true);
                Object v2 = declaredFields2[i].get(target2);

                if (v1 instanceof String && v2 instanceof String) {
                    result = TextUtils.equals(((String) v1), (String) v2);
                } else if (v1 instanceof Number && v2 instanceof Number) {
                    result = v1 == v2;
                } else {
                    result = false;
                }

                if (!result) {
                    break;
                }
            }
        } catch (Exception e) {
            L.i("错误: ->" + e.getMessage());
        }
        return result;
    }

    public static void setMember(Class<?> cls, Object target, String member, Object value) {
        try {
            Field memberField = cls.getDeclaredField(member);
            memberField.setAccessible(true);
            memberField.set(target, value);
        } catch (Exception e) {
            L.e("错误:" + e.getMessage());
        }
    }

    public static void setMember(Object target, String member, Object value) {
        setMember(target.getClass(), target, member, value);
    }

    /**
     * 获取调用堆栈上一级的方法名称
     */
    public static String getMethodName() {
        final StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
        return stackTraceElements[1].getMethodName();
    }

    /**
     * 通过类对象，运行指定方法
     *
     * @param obj        类对象
     * @param methodName 方法名
     * @param params     参数值
     * @return 失败返回null
     */
    public static Object invokeMethod(Object obj, String methodName, Object... params) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        return invokeMethod(clazz, methodName, methodName, params);
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?>[] paramTypes, Object... params) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        return invokeMethod(clazz, obj, methodName, paramTypes, params);
    }

    public static Object invokeMethod(Class<?> cls, Object obj, String methodName, Object... params) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        try {
            Class<?>[] paramTypes = null;
            if (params != null && params.length > 0) {
                paramTypes = new Class[params.length];
                for (int i = 0; i < params.length; ++i) {
                    paramTypes[i] = params[i].getClass();
                }
            }
            Method method = cls.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            Object invoke = method.invoke(obj, params);
            return invoke;
        } catch (Exception e) {
            L.e("错误:" + e.getMessage());
        }
        return null;
    }

    public static Object invokeMethod(Class<?> cls, Object obj, String methodName, Class<?>[] paramTypes, Object... params) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        try {
            Method method = cls.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            Object invoke = method.invoke(obj, params);
            return invoke;
        } catch (Exception e) {
            L.e("错误:" + e.getMessage());
        }
        return null;
    }

    /**
     * 通过反射, 获取obj对象的 指定成员变量的值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
                L.e("错误:" + e.getMessage());
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 设置字段的值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return;
            } catch (Exception e) {
                L.e("错误:" + e.getMessage());
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 通过类型, 创建实例
     */
    public static <T> T newObject(Class cls) {
        T obj = null;
        try {
            Constructor constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            obj = (T) constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 打印对象所有的字段, 和方法, 以及值
     *
     * @param onlyPublic 仅输出public 声明的属性
     */
    public static String logObject(Object object, boolean onlyPublic) {
        /*
         *
         * getFields()与getDeclaredFields()区别:
         * getFields()只能访问类中声明为公有的字段,私有的字段它无法访问，能访问从其它类继承来的公有方法.
         * getDeclaredFields()能访问类中所有的字段,与public,private,protect无关，不能访问从其它类继承来的方法

         * getMethods()与getDeclaredMethods()区别:
         * getMethods()只能访问类中声明为公有的方法,私有的方法它无法访问,能访问从其它类继承来的公有方法.
         * getDeclaredFields()能访问类中所有的字段,与public,private,protect无关,不能访问从其它类继承来的方法

         * getConstructors()与getDeclaredConstructors()区别:
         * getConstructors()只能访问类中声明为public的构造函数.
         * getDeclaredConstructors()能访问类中所有的构造函数,与public,private,protect无关
         *
         * */

        Field[] fields;
        Method[] methods;
        if (onlyPublic) {
            fields = object.getClass().getFields();
            methods = object.getClass().getMethods();
        } else {
            fields = object.getClass().getDeclaredFields();
            methods = object.getClass().getDeclaredMethods();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(object.getClass().getSimpleName());
        builder.append("\n");

        builder.append("字段");
        builder.append("(");
        builder.append(fields.length);
        builder.append("):\n");
        for (Field f : fields) {
            builder.append(f.getName());
            builder.append(":");
            try {
                f.setAccessible(true);

                Object value = f.get(object);

                logList(builder, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            builder.append("\n");
        }

        builder.append("\n方法");
        builder.append("(");
        builder.append(methods.length);
        builder.append("):\n");
        for (Method m : methods) {
            builder.append(m.getName());
            builder.append(":");
            try {
                m.setAccessible(true);

                Class<?>[] parameterTypes = m.getParameterTypes();
                if (parameterTypes == null || parameterTypes.length <= 0) {
                    logList(builder, m.invoke(object));
                } else {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        builder.append(i);
                        builder.append(",");
                        builder.append(parameterTypes[i].getSimpleName());
                        builder.append(";");
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    private static void logList(StringBuilder builder, Object object) {
        if (object instanceof List) {
            for (int i = 0; i < ((List) object).size(); i++) {
                builder.append(i);
                builder.append(",");
                builder.append(((List) object).get(i));
                builder.append(";");
            }
        } else {
            builder.append(object);
        }
    }

    /**
     * 填充对象
     */
    public static void fill(Object from, Object to) {
        RBaseViewHolder.fill(from, to);
    }
}
