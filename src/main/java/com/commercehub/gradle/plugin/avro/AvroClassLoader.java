package com.commercehub.gradle.plugin.avro;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;

public class AvroClassLoader extends URLClassLoader {

    private String currentPath = "";

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    public AvroClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public AvroClassLoader(URL[] urls) {
        super(urls);
    }

    public AvroClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public URL getResource(String name) {
        URL result = super.getResource(name);
        if (result == null) {
            name = findPathInAvdlJar(name);
            result = super.getResource(name);
        }
        for (URL url : this.getURLs()) {
            System.out.println(url.toString());
        }
        if (name.contains("/")) currentPath = name.substring(0, name.lastIndexOf("/"));
        return result;
    }

    private String findPathInAvdlJar(String targetPath) {
        String[] currentPathSegments = currentPath.split("/");
        String[] targetPathSegments = targetPath.split("/");

        int endOfCurrentPath = currentPathSegments.length;
        int startOfTargetPath = 0;

        for (String s : targetPathSegments) {
            if (s.equals("..")) {
                endOfCurrentPath--;
                startOfTargetPath++;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < endOfCurrentPath; i++) {
            sb.append(currentPathSegments[i]).append("/");
        }
        for (int i = startOfTargetPath; i < targetPathSegments.length - 1; i++) {
            sb.append(targetPathSegments[i]).append("/");
        }

        sb.append(targetPathSegments[targetPathSegments.length - 1]);

        return sb.toString();
    }

}
