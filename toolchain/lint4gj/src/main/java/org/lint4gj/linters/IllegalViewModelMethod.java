/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj.linters;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.lint4gj.Linter;
import org.lint4gj.Utils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class IllegalViewModelMethod extends Linter {

    private static final String INTERFACE_ViewModel = "ViewModel";
    private static final String CLASS_ViewModelBase = "ViewModelBase";
    private static final String CLASS_DialogViewModel = "DialogViewModel";

    private static final Pattern BEAN_GETTER = Pattern.compile("^(get|is|has|can)[A-Z]+.*(?<!Property)$");
    private static final Pattern BEAN_SETTER = Pattern.compile("^(set)[A-Z]+.*(?<!Property)$");
    private static final Pattern PROPERTY_GETTER = Pattern.compile("^(?!(get|set|is|has|can)[A-Z])[a-z].*Property$");

    private static final MethodSpec[] WHITE_LIST =
        new MethodSpec[] {
            new MethodSpec("initialize", "void", new String[0]),
            new MethodSpec("initializeViewModel", "void", new String[0]),
            new MethodSpec("initializeViewModel", "void", new String[] {MethodSpec.ANY}),
            new MethodSpec("onClosing", "void", new String[0]),
        };

    @Override
    public void methodEncounter(
            ClassOrInterfaceDeclaration classDecl, MethodDeclaration methodDecl, Consumer<String> errorHandler) {
        if (!isViewModelClass(classDecl) || methodDecl.getAccessSpecifier() == AccessSpecifier.PRIVATE) {
            return;
        }

        boolean valid = isWhiteListed(methodDecl);

        if (!valid) {
            valid = isSimpleGetter(methodDecl);
        }

        if (!valid) {
            valid = isSimpleSetter(methodDecl);
        }

        if (!valid) {
            errorHandler.accept(
                Utils.getFullyQualifiedName(methodDecl)
                    + ": Non-private methods defined on view model classes must be simple"
                    + " getters or setters and follow bean/property naming conventions.");
        }
    }

    private boolean isViewModelClass(ClassOrInterfaceDeclaration classDecl) {
        boolean isViewModelClass =
            classDecl
                .getImplementedTypes()
                .stream()
                .anyMatch(type -> INTERFACE_ViewModel.equals(type.getName().getIdentifier()));

        isViewModelClass |=
            classDecl
                .getExtendedTypes()
                .stream()
                .anyMatch(
                    type ->
                        CLASS_ViewModelBase.equals(type.getName().getIdentifier())
                            || CLASS_DialogViewModel.equals(type.getName().getIdentifier()));

        return isViewModelClass;
    }

    private boolean isWhiteListed(MethodDeclaration methodDecl) {
        for (MethodSpec methodSpec : WHITE_LIST) {
            if (methodSpec.match(methodDecl)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSimpleGetter(MethodDeclaration methodDecl) {
        if (methodDecl.getType().isVoidType()) {
            return false;
        }

        if (!methodDecl.getParameters().isEmpty()) {
            return false;
        }

        String name = methodDecl.getNameAsString();
        if (!BEAN_GETTER.matcher(name).matches() && !PROPERTY_GETTER.matcher(name).matches()) {
            return false;
        }

        Optional<BlockStmt> block = methodDecl.getBody();
        if (block.isEmpty()) {
            return true;
        }

        if (block.get().getStatements().size() != 1) {
            return false;
        }

        return block.get().getStatement(0).isReturnStmt();
    }

    private boolean isSimpleSetter(MethodDeclaration methodDecl) {
        if (!methodDecl.getType().isVoidType()) {
            return false;
        }

        if (methodDecl.getParameters().size() != 1) {
            return false;
        }

        String name = methodDecl.getNameAsString();
        if (!BEAN_SETTER.matcher(name).matches()) {
            return false;
        }

        Optional<BlockStmt> block = methodDecl.getBody();
        if (block.isEmpty()) {
            return true;
        }

        return block.get().getStatements().size() == 1;
    }

    private static class MethodSpec {
        static final String ANY = "<any>";
        static final String[] ANY_PARAMS = new String[0];

        private final String name;
        private final String returnType;
        private final String[] paramTypes;

        MethodSpec(String name, String returnType, String[] paramTypes) {
            this.name = name;
            this.returnType = returnType;
            this.paramTypes = paramTypes;
        }

        @SuppressWarnings("StringEquality")
        boolean match(MethodDeclaration methodDecl) {
            if (name != ANY && !methodDecl.getNameAsString().equals(name)) {
                return false;
            }

            if (returnType != ANY && !methodDecl.getType().asString().equals(returnType)) {
                return false;
            }

            if (paramTypes == ANY_PARAMS) {
                return true;
            }

            if (paramTypes.length != methodDecl.getParameters().size()) {
                return false;
            }

            for (int i = 0; i < paramTypes.length; ++i) {
                if (paramTypes[i] != ANY && !methodDecl.getParameter(i).getNameAsString().equals(paramTypes[i])) {
                    return false;
                }
            }

            return true;
        }
    }

}
