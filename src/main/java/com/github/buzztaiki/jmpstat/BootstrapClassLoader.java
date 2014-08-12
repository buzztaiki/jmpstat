package com.github.buzztaiki.jmpstat;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BootstrapClassLoader extends URLClassLoader {
    private final String packageName;
    private BootstrapClassLoader(String packageName, URL[] urls) {
        super(urls);
        this.packageName = packageName;
    }

    public static BootstrapClassLoader of(final String main) throws IOException {
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

        return AccessController.doPrivileged(new PrivilegedAction<BootstrapClassLoader>() {
            @Override
            public BootstrapClassLoader run() {
                return new BootstrapClassLoader(main, urls.toArray(new URL[0]));
            }
        });
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!name.startsWith(packageName)) {
            return super.loadClass(name, resolve);
        }

        // load class from self instead of parent loader
        Class<?> klass = findLoadedClass(name);
        if (klass != null) {
            return klass;
        }

        klass = findClass(name);
        if (klass == null) {
            return super.loadClass(name, resolve);
        }

        if (resolve) {
            resolveClass(klass);
        }
        return klass;
    }

}
