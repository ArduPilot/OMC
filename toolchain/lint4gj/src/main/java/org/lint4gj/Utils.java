/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    static int indexOfDifference(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return -1;
        }

        if (cs1 == null || cs2 == null) {
            return 0;
        }

        int i;
        for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                break;
            }
        }

        if (i < cs2.length() || i < cs1.length()) {
            return i;
        }

        return -1;
    }

    public static String getFullyQualifiedName(Node node) {
        if (node instanceof ClassOrInterfaceDeclaration) {
            return ((ClassOrInterfaceDeclaration)node).getFullyQualifiedName().orElseThrow();
        }

        if (node instanceof MethodDeclaration) {
            return ((ClassOrInterfaceDeclaration)node.getParentNode().orElseThrow())
                    .getFullyQualifiedName()
                    .orElseThrow()
                + "."
                + ((MethodDeclaration)node).getNameAsString();
        }

        if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDecl = (FieldDeclaration)node;
            List<VariableDeclarator> variables = fieldDecl.getVariables();
            return ((ClassOrInterfaceDeclaration)fieldDecl.getParentNode().orElseThrow())
                    .getFullyQualifiedName()
                    .orElseThrow()
                + "."
                + (variables.size() == 1
                    ? variables.get(0).getNameAsString()
                    : "("
                        + variables.stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.joining(","))
                        + ")");
        }

        throw new IllegalArgumentException();
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    static String[] exec(String command, File workingDir) {
        try {
            Process process = Runtime.getRuntime().exec(command, null, workingDir);

            try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return stdInput.lines().toArray(String[]::new);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
