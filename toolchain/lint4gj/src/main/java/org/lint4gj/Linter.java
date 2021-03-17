/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import org.lint4gj.linters.AsyncMethodNaming;
import org.lint4gj.linters.IllegalViewModelMethod;
import org.lint4gj.linters.InvalidLinterSuppression;
import org.lint4gj.linters.ViewClassInViewModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Linter {

    protected static final String SUPPRESS_LINTER_ANNOTATION_NAME = "SuppressLinter";

    private static Class<? extends Linter>[] linters;

    @SuppressWarnings("unchecked")
    public static Class<? extends Linter>[] getLinters() {
        if (linters == null) {
            linters =
                new Class[] {
                    InvalidLinterSuppression.class,
                    IllegalViewModelMethod.class,
                    AsyncMethodNaming.class,
                    ViewClassInViewModel.class
                };
        }

        return linters;
    }

    final void scan(CompilationUnit compilationUnit, ErrorHandler errorHandler) {
        List<TypeDeclaration<?>> typeDecls = compilationUnit.getTypes();
        if (typeDecls.isEmpty()) {
            return;
        }

        Set<String> seenBefore = new HashSet<>();

        for (TypeDeclaration<?> typeDecl : typeDecls) {
            if (!typeDecl.isClassOrInterfaceDeclaration()) {
                continue;
            }

            ClassOrInterfaceDeclaration classDecl = typeDecl.asClassOrInterfaceDeclaration();
            scanType(
                classDecl,
                message -> {
                    if (!seenBefore.contains(message)) {
                        seenBefore.add(message);
                        errorHandler.handle(Linter.this, message);
                    }
                },
                message -> {
                    if (!seenBefore.contains(message)) {
                        seenBefore.add(message);
                        errorHandler.handle(new InvalidLinterSuppression(), message);
                    }
                });
        }
    }

    /**
     * Allows linters to perform class-level inspection. This method is called whenever a class declaration is
     * encountered in the source code.
     */
    protected void classEncounter(ClassOrInterfaceDeclaration classDecl, Consumer<String> errorHandler) {}

    /**
     * Allows linters to perform field-level inspection. This method is called whenever a field declaration is
     * encountered in the source code.
     */
    protected void fieldEncounter(
            ClassOrInterfaceDeclaration classDecl, FieldDeclaration fieldDecl, Consumer<String> errorHandler) {}

    /**
     * Allows linters to perform method-level inspection. This method is called whenever a method declaration is
     * encountered in the source code.
     */
    protected void methodEncounter(
            ClassOrInterfaceDeclaration classDecl, MethodDeclaration methodDecl, Consumer<String> errorHandler) {}

    private void scanType(
            ClassOrInterfaceDeclaration classDecl,
            Consumer<String> errorHandler,
            Consumer<String> notEncounteredHandler) {
        classEncounter(classDecl, errorHandler);

        for (Node child : classDecl.getChildNodes()) {
            if (child instanceof FieldDeclaration) {
                FieldDeclaration fieldDecl = (FieldDeclaration)child;
                SuppressionInfo suppressionInfo = getSuppressionInfo(fieldDecl);
                ExpectedDiagnosticHandler expectedDiagnosticHandler =
                    new ExpectedDiagnosticHandler(suppressionInfo, errorHandler);
                fieldEncounter(classDecl, fieldDecl, expectedDiagnosticHandler);
                checkExpectedError(fieldDecl, suppressionInfo, expectedDiagnosticHandler, notEncounteredHandler);
            } else if (child instanceof MethodDeclaration) {
                MethodDeclaration methodDecl = (MethodDeclaration)child;
                SuppressionInfo suppressionInfo = getSuppressionInfo(methodDecl);
                ExpectedDiagnosticHandler expectedDiagnosticHandler =
                    new ExpectedDiagnosticHandler(suppressionInfo, errorHandler);
                methodEncounter(classDecl, methodDecl, expectedDiagnosticHandler);
                checkExpectedError(methodDecl, suppressionInfo, expectedDiagnosticHandler, notEncounteredHandler);
            } else if (child instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration childClassDecl = (ClassOrInterfaceDeclaration)child;
                SuppressionInfo suppressionInfo = getSuppressionInfo(childClassDecl);
                ExpectedDiagnosticHandler expectedDiagnosticHandler =
                    new ExpectedDiagnosticHandler(suppressionInfo, errorHandler);
                scanType((ClassOrInterfaceDeclaration)child, errorHandler, notEncounteredHandler);
                checkExpectedError(classDecl, suppressionInfo, expectedDiagnosticHandler, notEncounteredHandler);
            }
        }
    }

    private void checkExpectedError(
            Node node,
            SuppressionInfo suppressionInfo,
            ExpectedDiagnosticHandler expectedDiagnosticHandler,
            Consumer<String> notEncounteredHandler) {
        if (suppressionInfo.isLocal() && !expectedDiagnosticHandler.hasOccurred()) {
            notEncounteredHandler.accept(
                Utils.getFullyQualifiedName(node)
                    + ": "
                    + getClass().getSimpleName()
                    + " is suppressed, but not encountered.");
        }
    }

    @SuppressWarnings("unchecked")
    private SuppressionInfo getSuppressionInfo(Node node) {
        boolean suppressedLocally = false;
        boolean suppressedByInheritance = false;

        if (node instanceof NodeWithAnnotations) {
            suppressedLocally =
                parseAnnotation(((NodeWithAnnotations)node).getAnnotationByName(SUPPRESS_LINTER_ANNOTATION_NAME))
                    .contains(getClass());
            node = node.getParentNode().orElse(null);
        }

        while (node != null) {
            if (node instanceof NodeWithAnnotations) {
                suppressedByInheritance =
                    parseAnnotation(((NodeWithAnnotations)node).getAnnotationByName(SUPPRESS_LINTER_ANNOTATION_NAME))
                        .contains(getClass());

                if (suppressedByInheritance) {
                    break;
                }
            }

            node = node.getParentNode().orElse(null);
        }

        return new SuppressionInfo(suppressedLocally, suppressedLocally || suppressedByInheritance);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Set<Class<? extends Linter>> parseAnnotation(Optional<AnnotationExpr> annotationExpr) {
        AnnotationExpr annotation = annotationExpr.orElse(null);
        if (annotation == null) {
            return Collections.emptySet();
        }

        String[] value = null;
        List<Node> children = annotation.getChildNodes();
        for (int i = 1; i < children.size(); ++i) {
            if (children.get(i) instanceof MemberValuePair) {
                MemberValuePair pair = (MemberValuePair)children.get(i);
                if ("value".equals(pair.getNameAsString())) {
                    if (pair.getValue().isStringLiteralExpr()) {
                        value = new String[] {pair.getValue().asStringLiteralExpr().getValue()};
                    } else if (pair.getValue().isArrayInitializerExpr()) {
                        List<Expression> values = pair.getValue().asArrayInitializerExpr().getValues();
                        value = new String[values.size()];
                        for (int j = 0; j < values.size(); ++j) {
                            if (values.get(j).isStringLiteralExpr()) {
                                value[j] = values.get(j).asStringLiteralExpr().getValue();
                            }
                        }
                    }

                    break;
                }
            }
        }

        if (value == null) {
            return Collections.emptySet();
        }

        Set<Class<? extends Linter>> list = new HashSet<>();
        for (Class<? extends Linter> linter : Linter.getLinters()) {
            for (String val : value) {
                if (linter.getSimpleName().equals(val)) {
                    list.add(linter);
                }
            }
        }

        return list;
    }

}
