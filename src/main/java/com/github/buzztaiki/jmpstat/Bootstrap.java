package com.github.buzztaiki.jmpstat;

import java.lang.reflect.InvocationTargetException;

public class Bootstrap {
    public static void main(String[] args) throws Throwable {
        ClassLoader cl = BootstrapClassLoader.of("com.github.buzztaiki.jmpstat.");
        Class<?> mainClass = cl.loadClass("com.github.buzztaiki.jmpstat.Main");
        try {
            mainClass.getMethod("main", String[].class).invoke(mainClass, new Object[]{args});
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
