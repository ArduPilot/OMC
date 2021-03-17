/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.imageio;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnnotatedSvgClassLoader extends ClassLoader {

    static class Annotation {
        private final String key;
        private final String value;

        private Annotation(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    private static class AnnotationList extends ArrayList<Annotation> {
        @Override
        public String toString() {
            var stringBuilder = new StringBuilder();
            boolean first = true;

            for (var annotation : this) {
                if (!first) {
                    stringBuilder.append(";");
                }

                stringBuilder.append(annotation.getKey());
                stringBuilder.append("=");
                stringBuilder.append(annotation.getValue());
                first = false;
            }

            return stringBuilder.toString();
        }
    }

    private static Logger LOGGER = LoggerFactory.getLogger(AnnotatedSvgClassLoader.class);

    private final ClassLoader classLoader;
    private final List<Annotation> currentAnnotations = new ArrayList<>();

    AnnotatedSvgClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public URL getResource(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        currentAnnotations.clear();

        if (isAnnotatedSvg(path)) {
            String annotatedFileName = getFileName(path);
            var parts = annotatedFileName.split("\\(");
            if (parts.length != 2) {
                LOGGER.warn("Malformed SVG annotation in path '" + path + "'.");
                return classLoader.getResource(path);
            }

            var directory = path.substring(0, path.length() - annotatedFileName.length());
            URL url = classLoader.getResource(directory + parts[0] + ".svg");
            if (url == null) {
                return null;
            }

            try {
                var list = new AnnotationList();

                var annotations = parts[1].substring(0, parts[1].indexOf(")"));
                for (var annotation : annotations.split(";")) {
                    parts = annotation.split("=");
                    if (parts.length != 2) {
                        LOGGER.warn("Malformed SVG annotation in path '" + path + "'.");
                        return classLoader.getResource(path);
                    }

                    list.add(new Annotation(parts[0], parts[1]));
                }

                currentAnnotations.addAll(list);
                return new URL(url.toExternalForm() + "#" + list);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        return classLoader.getResource(path);
    }

    List<Annotation> getCurrentAnnotations() {
        return currentAnnotations;
    }

    private String getFileName(String path) {
        for (int i = path.length() - 1; i >= 0; --i) {
            if (path.charAt(i) == '/') {
                return path.substring(i + 1);
            }
        }

        return path;
    }

    private boolean isAnnotatedSvg(String name) {
        int length = name.length();
        if (length < 4) {
            return false;
        }

        char c = name.charAt(length - 1);
        if (c != 'g' && c != 'G') {
            return false;
        }

        c = name.charAt(length - 2);
        if (c != 'v' && c != 'V') {
            return false;
        }

        c = name.charAt(length - 3);
        if (c != 's' && c != 'S') {
            return false;
        }

        c = name.charAt(length - 4);
        if (c != '.') {
            return false;
        }

        boolean closingBraceFound = false;
        for (int i = length - 5; i >= 0; --i) {
            if (name.charAt(i) == '/') {
                return false;
            }

            if (!closingBraceFound && name.charAt(i) == ')') {
                closingBraceFound = true;
            } else if (closingBraceFound && name.charAt(i) == '(') {
                return true;
            }
        }

        return false;
    }

}
