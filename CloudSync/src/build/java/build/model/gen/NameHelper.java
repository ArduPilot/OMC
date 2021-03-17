/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.gen;

import build.model.ast.ClassNode;
import build.model.ast.FieldNode;
import build.model.ast.GeneratedTypeNode;
import build.model.ast.TypeNode;
import build.model.ast.UnknownTypeNode;
import java.util.List;

public class NameHelper {

    private static final String INTERFACE_NAME = "I%s";
    private static final String CLASS_NAME = "%s";
    private static final String BASE_CLASS_NAME = "Abstract%s";
    private static final String SNAPSHOT_CLASS_NAME = "%sSnapshot";

    public static String getGetterName(FieldNode fieldNode) {
        String nameUppercase = Character.toUpperCase(fieldNode.getName().charAt(0)) + fieldNode.getName().substring(1);
        TypeNode typeNode = fieldNode.getType();

        if (typeNode instanceof UnknownTypeNode && ((UnknownTypeNode)typeNode).getTypeName().equals("boolean")) {
            return "is" + nameUppercase;
        }

        return "get" + nameUppercase;
    }

    public static String getModelTypeName(TypeNode typeNode) {
        if (typeNode instanceof GeneratedTypeNode) {
            return String.format(CLASS_NAME, typeNode.getTypeName());
        }

        return typeNode.getTypeName();
    }

    public static String getModelTypeName(String className, List<ClassNode> classes) {
        for (ClassNode node : classes) {
            if (node.getClassName().equals(className)) {
                return getModelTypeName(node);
            }
        }

        throw new IllegalArgumentException("Unknown class name: " + className);
    }

    public static String getInterfaceTypeName(String className, List<ClassNode> classes) {
        for (ClassNode node : classes) {
            if (node.getClassName().equals(className)) {
                return getInterfaceTypeName(node);
            }
        }

        throw new IllegalArgumentException("Unknown class name: " + className);
    }

    public static String getModelBaseTypeName(String className, List<ClassNode> classes) {
        for (ClassNode node : classes) {
            if (node.getClassName().equals(className)) {
                return getModelBaseTypeName(node);
            }
        }

        throw new IllegalArgumentException("Unknown class name: " + className);
    }

    public static String getSnapshotTypeName(String className, List<ClassNode> classes) {
        for (ClassNode node : classes) {
            if (node.getClassName().equals(className)) {
                return getSnapshotTypeName(node);
            }
        }

        throw new IllegalArgumentException("Unknown class name: " + className);
    }

    public static String getModelTypeName(ClassNode classNode) {
        return String.format(CLASS_NAME, classNode.getClassName());
    }

    public static String getModelBaseTypeName(ClassNode classNode) {
        return String.format(BASE_CLASS_NAME, classNode.getClassName());
    }

    public static String getInterfaceTypeName(ClassNode classNode) {
        return String.format(INTERFACE_NAME, classNode.getClassName());
    }

    public static String getInterfaceTypeName(TypeNode typeNode) {
        return String.format(INTERFACE_NAME, typeNode.getTypeName());
    }

    public static String getSnapshotTypeName(ClassNode classNode) {
        return String.format(SNAPSHOT_CLASS_NAME, classNode.getClassName());
    }

    public static String getSnapshotTypeName(TypeNode typeNode) {
        return String.format(SNAPSHOT_CLASS_NAME, typeNode.getTypeName());
    }

    public static String getModelFileName(ClassNode classNode) {
        return getModelBaseTypeName(classNode) + ".java";
    }

    public static String getInterfaceFileName(ClassNode classNode) {
        return getInterfaceTypeName(classNode) + ".java";
    }

    public static String getSnapshotFileName(ClassNode classNode) {
        return getSnapshotTypeName(classNode) + ".java";
    }

}
