package com.github.buzztaiki.jmpstat;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BootstrapClassLoader extends URLClassLoader {
    private BootstrapClassLoader(URL[] urls) {
        super(urls);
    }

    public static BootstrapClassLoader get() throws IOException {
        Path libDir = Paths.get(System.getProperty("java.home"), "../lib").toRealPath();
        List<Path> jars = Arrays.asList(
            Paths.get(System.getProperty("java.class.path")).toRealPath(),
            libDir.resolve("tools.jar"),
            libDir.resolve("jconsole.jar"));

        for (Path jar : jars) {
            if (!Files.isReadable(jar)) {
                throw new IOException(jar + " not found");
            }
        }

        final List<URL> urls = new ArrayList<>();
        for (Path jar : jars) {
            urls.add(jar.toUri().toURL());
        }

        return new BootstrapClassLoader(urls.toArray(new URL[0]));
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> klass = findLoadedClass(name);
        if (klass == null) {
            try {
                klass = findClass(name);
            } catch (ClassNotFoundException e) {
                klass = getParent().loadClass(name);
            }
        }

        if (resolve) {
            resolveClass(klass);
        }
        return klass;
    }
}
