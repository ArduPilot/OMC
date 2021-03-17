/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj.linters;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.lint4gj.Linter;
import org.lint4gj.Utils;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AsyncMethodNaming extends Linter {

    private static final String[] FUTURE_TYPES = new String[] {"Future", "ListenableFuture", "SettableFuture"};
    private static final Pattern FUTURE_GETTER = Pattern.compile("^get([A-Z].*)?Future$");

    @Override
    protected void methodEncounter(
            ClassOrInterfaceDeclaration classDecl, MethodDeclaration methodDecl, Consumer<String> errorHandler) {
        if (!methodDecl.getType().isClassOrInterfaceType()) {
            if (methodDecl.getNameAsString().endsWith("Async")) {
                errorHandler.accept(
                    Utils.getFullyQualifiedName(methodDecl)
                        + ": Methods that do not return a future must not end in -Async.");
            }

            return;
        }

        boolean found = false;
        String name = methodDecl.getType().asClassOrInterfaceType().getNameAsString();
        for (String futureName : FUTURE_TYPES) {
            if (name.equals(futureName)) {
                found = true;
                break;
            }
        }

        if (!found && methodDecl.getNameAsString().endsWith("Async")) {
            errorHandler.accept(
                Utils.getFullyQualifiedName(methodDecl)
                    + ": Methods that do not return a future must not end in -Async.");
        } else if (found && !isValidMethod(methodDecl.getNameAsString())) {
            errorHandler.accept(
                Utils.getFullyQualifiedName(methodDecl) + ": Methods that return a future must end in -Async.");
        }
    }

    private boolean isValidMethod(String name) {
        return name.endsWith("Async") || FUTURE_GETTER.matcher(name).matches();
    }

}
