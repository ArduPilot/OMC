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
    private final List<TypeNode> implementedInterfaces;
    private final boolean abstractClass;
    private final boolean inheritsBaseModel;
    private final boolean immutable;
    private final List<FieldNode> fields;

    public ClassNode(
            String className,
            String inheritedClass,
            boolean abstractClass,
            boolean inheritsBaseModel,
            boolean immutable,
            List<TypeNode> implementedInterfaces,
            List<FieldNode> fields) {
        this.className = className;
        this.inheritedClass = Optional.ofNullable(inheritedClass);
        this.abstractClass = abstractClass;
        this.inheritsBaseModel = inheritsBaseModel;
        this.immutable = immutable;
        this.implementedInterfaces = implementedInterfaces;
        this.fields = fields;
    }

    public String getClassName() {
        return className;
    }

    public Optional<String> getInheritedClass() {
        return inheritedClass;
    }

    public List<TypeNode> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public boolean isAbstractClass() {
        return abstractClass;
    }

    public boolean inheritsBaseModel() {
        return inheritsBaseModel;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public List<FieldNode> getFields() {
        return fields;
    }

    public boolean implementsInterface(String interfaceName) {
        return implementsInterface(interfaceName, false);
    }

    public boolean implementsInterface(String interfaceName, boolean matchGenericType) {
        for (TypeNode typeNode : implementedInterfaces) {
            if (!matchGenericType && typeNode instanceof UnknownGenericTypeNode) {
                if (interfaceName.equals(((UnknownGenericTypeNode)typeNode).getOuterTypeName())) {
                    return true;
                }
            }

            if (typeNode.getTypeName().equals(interfaceName)) {
                return true;
            }
        }

        return false;
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
