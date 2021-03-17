/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj.linters;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import org.lint4gj.Linter;
import org.lint4gj.Utils;

import java.util.List;
import java.util.function.Consumer;

public class ViewClassInViewModel extends Linter {

    private static final String INTERFACE_ViewModel = "ViewModel";
    private static final String CLASS_ViewModelBase = "ViewModelBase";
    private static final String CLASS_DialogViewModel = "DialogViewModel";
    private static final List<String> VIEW_PACKAGES = List.of("javafx.scene.", "javafx.stage.");

    @Override
    protected void fieldEncounter(
            ClassOrInterfaceDeclaration classDecl, FieldDeclaration fieldDecl, Consumer<String> errorHandler) {
        if (!isViewModelClass(classDecl)) {
            return;
        }

        try {
            resolvedTypeEncounter(fieldDecl, fieldDecl.resolve().getType(), errorHandler);
        } catch (RuntimeException ignored) {
        }
    }

    @Override
    protected void methodEncounter(
            ClassOrInterfaceDeclaration classDecl, MethodDeclaration methodDecl, Consumer<String> errorHandler) {
        if (!isViewModelClass(classDecl)) {
            return;
        }

        try {
            resolvedTypeEncounter(methodDecl, methodDecl.getType().resolve(), errorHandler);
        } catch (RuntimeException ignored) {
        }
    }

    private void resolvedTypeEncounter(Node node, ResolvedType type, Consumer<String> errorHandler) {
        if (type.isReferenceType()) {
            String name = type.asReferenceType().getQualifiedName();
            for (String pkg : VIEW_PACKAGES) {
                if (name.startsWith(pkg)) {
                    errorHandler.accept(
                        Utils.getFullyQualifiedName(node) + ": " + name + " can not be used in view model classes.");
                    break;
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

}
