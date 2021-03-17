/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj.linters;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import org.lint4gj.Linter;
import org.lint4gj.Maintainer;
import org.lint4gj.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InvalidLinterSuppression extends Linter {

    private final Maintainer[] maintainers;

    public InvalidLinterSuppression() {
        this.maintainers = new Maintainer[0];
    }

    @SuppressWarnings("unused")
    public InvalidLinterSuppression(Maintainer[] maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    protected void classEncounter(ClassOrInterfaceDeclaration classDecl, Consumer<String> errorHandler) {
        Optional<AnnotationExpr> annotationExpr = classDecl.getAnnotationByName(SUPPRESS_LINTER_ANNOTATION_NAME);
        if (annotationExpr.isEmpty()) {
            return;
        }

        validateAnnotation(classDecl, annotationExpr.get(), errorHandler);
    }

    @Override
    protected void methodEncounter(
            ClassOrInterfaceDeclaration classDecl, MethodDeclaration methodDecl, Consumer<String> errorHandler) {
        Optional<AnnotationExpr> annotationExpr = methodDecl.getAnnotationByName(SUPPRESS_LINTER_ANNOTATION_NAME);
        if (annotationExpr.isEmpty()) {
            return;
        }

        validateAnnotation(methodDecl, annotationExpr.get(), errorHandler);
    }

    private void validateAnnotation(Node node, AnnotationExpr annotationExpr, Consumer<String> errorHandler) {
        List<String> unknownLinters = new ArrayList<>();
        String[] value = null;
        String reviewer = "", justification = "";
        List<Node> children = annotationExpr.getChildNodes();
        for (int i = 1; i < children.size(); ++i) {
            if (children.get(i) instanceof MemberValuePair) {
                MemberValuePair pair = (MemberValuePair)children.get(i);
                if ("value".equals(pair.getNameAsString())) {
                    if (pair.getValue().isStringLiteralExpr()) {
                        String name = pair.getValue().asStringLiteralExpr().getValue();
                        if (isKnownLinter(name)) {
                            value = new String[] {name};
                        } else {
                            unknownLinters.add(name);
                        }
                    } else if (pair.getValue().isArrayInitializerExpr()) {
                        List<Expression> values = pair.getValue().asArrayInitializerExpr().getValues();
                        value = new String[values.size()];
                        for (int j = 0; j < values.size(); ++j) {
                            if (values.get(j).isStringLiteralExpr()) {
                                String name = values.get(j).asStringLiteralExpr().getValue();
                                if (isKnownLinter(name)) {
                                    value[j] = name;
                                } else {
                                    unknownLinters.add(name);
                                }
                            } else {
                                unknownLinters.add(values.get(j).toString());
                            }
                        }
                    } else {
                        unknownLinters.add(pair.getValue().toString());
                    }
                } else if ("reviewer".equals(pair.getNameAsString())) {
                    reviewer = pair.getValue().asStringLiteralExpr().getValue();
                } else if ("justification".equals(pair.getNameAsString())) {
                    justification = pair.getValue().asStringLiteralExpr().getValue();
                }
            }
        }

        if (value == null || !unknownLinters.isEmpty()) {
            errorHandler.accept(
                Utils.getFullyQualifiedName(node)
                    + ": Unknown linter"
                    + (unknownLinters.size() > 1 ? "s: " : ": ")
                    + (unknownLinters.isEmpty() ? "<empty>" : String.join(", ", unknownLinters)));
        }

        if (reviewer.length() < 2) {
            errorHandler.accept(
                Utils.getFullyQualifiedName(node)
                    + ": Please specify a value of at least two characters for the 'reviewer' field.");
        }

        if (maintainers.length > 0) {
            String reviewerCopy = reviewer;
            boolean found =
                Arrays.stream(maintainers).anyMatch(m -> m.equals(Maintainer.NONE) || m.equalsName(reviewerCopy));

            if (!found) {
                if (maintainers.length == 1) {
                    errorHandler.accept(
                        Utils.getFullyQualifiedName(node)
                            + ": Suppressions can only be approved by the maintainer of this source code file: "
                            + maintainers[0].getName());
                } else {
                    errorHandler.accept(
                        Utils.getFullyQualifiedName(node)
                            + ": Suppressions can only be approved by the maintainers of this source code file: "
                            + Arrays.stream(maintainers).map(Maintainer::getName).collect(Collectors.joining(",")));
                }
            }
        }

        if (justification.length() < 9) {
            errorHandler.accept(
                Utils.getFullyQualifiedName(node)
                    + ": Please specify a value of at least nine characters for the 'justification' field.");
        }
    }

    private boolean isKnownLinter(String name) {
        for (Class<? extends Linter> linterClass : Linter.getLinters()) {
            if (linterClass == getClass()) {
                continue;
            }

            if (linterClass.getSimpleName().equals(name)) {
                return true;
            }
        }

        return false;
    }

}
