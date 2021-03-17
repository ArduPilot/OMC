/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.lint4gj.linters.InvalidLinterSuppression;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;

class CodeScanner {

    static {
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(new ReflectionTypeSolver(false));
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
    }

    private final File file;
    private final String source;
    private final Maintainer[] maintainers;
    private final Set<Class<? extends Linter>> suppressions;

    CodeScanner(File file) throws IOException {
        this.file = file;
        this.source = Files.readString(file.toPath());
        this.maintainers = new Maintainer[0];
        this.suppressions = Collections.emptySet();
    }

    CodeScanner(File file, Maintainer[] maintainers, Set<Class<? extends Linter>> suppressions) throws IOException {
        this.file = file;
        this.source = Files.readString(file.toPath());
        this.maintainers = maintainers;
        this.suppressions = suppressions;
    }

    CodeScanner(String source, Maintainer[] maintainers, Set<Class<? extends Linter>> suppressions) {
        this.file = null;
        this.source = source;
        this.maintainers = maintainers;
        this.suppressions = suppressions;
    }

    void scan(ErrorHandler errorHandler, InfoHandler infoHandler) {
        CompilationUnit compilationUnit = StaticJavaParser.parse(source);

        for (Class<? extends Linter> linterClass : Linter.getLinters()) {
            if (suppressions.contains(linterClass)) {
                try {
                    if (linterClass == InvalidLinterSuppression.class) {
                        errorHandler.handle(
                            linterClass.getConstructor(Maintainer[].class).newInstance((Object)maintainers),
                            InvalidLinterSuppression.class.getSimpleName() + " cannot be suppressed.");
                    } else {
                        if (file != null) {
                            infoHandler.handle(
                                linterClass.getConstructor().newInstance(),
                                "Inspection is suppressed for " + file.getAbsolutePath());
                        }

                        continue;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }

            try {
                if (linterClass == InvalidLinterSuppression.class) {
                    linterClass
                        .getConstructor(Maintainer[].class)
                        .newInstance((Object)maintainers)
                        .scan(compilationUnit, errorHandler);
                } else {
                    linterClass.getConstructor().newInstance().scan(compilationUnit, errorHandler);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

}
