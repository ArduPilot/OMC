/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ClassNode {

    private final String className;
    private final Optional<String> inheritedClass;
    private final boolean abstractModifier;
    private final List<FieldNode> fields;

    public ClassNode(String className, String inheritedClass, boolean abstractModifier, List<FieldNode> fields) {
        this.className = className;
        this.inheritedClass = Optional.ofNullable(inheritedClass);
        this.abstractModifier = abstractModifier;
        this.fields = fields;
    }

    public String getClassName() {
        return className;
    }

    public Optional<String> getInheritedClass() {
        return inheritedClass;
    }

    public boolean hasAbstractModifier() {
        return abstractModifier;
    }

    public List<FieldNode> getFields() {
        return fields;
    }

    /**
     * Gets the inheritance chain of the current class, starting from the bottom up. The current class will be at the
     * end of the list.
     */
    public List<ClassNode> getClassChain(List<ClassNode> otherClasses) {
        List<ClassNode> classes = new ArrayList<>();

        if (inheritedClass.isPresent()) {
            Optional<ClassNode> cls =
                otherClasses.stream().filter(c -> c.getClassName().equals(inheritedClass.get())).findFirst();

            if (cls.isEmpty()) {
                throw new RuntimeException("Unknown superclass: " + inheritedClass.get());
            }

            classes.addAll(cls.get().getClassChain(otherClasses));
        }

        classes.add(this);
        return classes;
    }

}
