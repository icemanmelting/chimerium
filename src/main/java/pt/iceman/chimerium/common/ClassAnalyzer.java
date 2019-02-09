package pt.iceman.chimerium.common;

public class ClassAnalyzer {
    public static Class getRootClass(Class<?> c) {
        Class<?> spr = c.getSuperclass();

        if (spr.equals(Object.class)) {
            return c;
        } else {
            return getRootClass(spr);
        }
    }
}
