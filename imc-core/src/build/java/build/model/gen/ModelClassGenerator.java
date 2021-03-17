/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.gen;

import build.model.ast.ClassNode;
import build.model.ast.CollectionNode;
import build.model.ast.EnumTypeNode;
import build.model.ast.FieldNode;
import build.model.ast.GeneratedTypeNode;
import build.model.ast.KnownTypeNode;
import build.model.ast.PrimitiveTypeNode;
import build.model.ast.TypeNode;
import build.model.ast.UnknownGenericTypeNode;
import build.model.ast.UnknownTypeNode;
import java.util.List;

public class ModelClassGenerator {

    private final CodeBuilder codeBuilder = new CodeBuilder();

    public ModelClassGenerator(
            String packageName, String[] imports, ClassNode classNode, List<ClassNode> otherClasses) {
        codeBuilder.appendLine("package " + packageName + ";");

        for (String imp : imports) {
            codeBuilder.appendLine("import " + imp + ";");
        }

        if (classNode.getInheritedClass().isPresent()) {
            codeBuilder.append(
                "abstract class %s extends %s",
                NameHelper.getModelBaseTypeName(classNode),
                classNode.inheritsBaseModel()
                    ? NameHelper.getModelBaseTypeName(classNode.getInheritedClass().get(), otherClasses)
                    : NameHelper.getModelTypeName(classNode.getInheritedClass().get(), otherClasses));

            CommonCode.implementsInterfaces(
                codeBuilder,
                NameHelper.getModelTypeName(classNode),
                classNode.getClassChain(otherClasses),
                NameHelper.getInterfaceTypeName(classNode));

            codeBuilder.indent();
        } else {
            codeBuilder.append("abstract class %s extends PropertyObject", NameHelper.getModelBaseTypeName(classNode));

            CommonCode.implementsInterfaces(
                codeBuilder,
                NameHelper.getModelTypeName(classNode),
                classNode.getClassChain(otherClasses),
                NameHelper.getInterfaceTypeName(classNode),
                String.format("Mergeable<%s>", NameHelper.getInterfaceTypeName(classNode)));

            codeBuilder.indent();
        }

        codeBuilder.appendLines(new PropertyDeclarationsGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new HierarchySupportGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new ParameterlessConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new ConvertingConstructorGenerator(classNode, otherClasses, true).toString());
        codeBuilder.appendLines(new ConvertingConstructorGenerator(classNode, otherClasses, false).toString());
        codeBuilder.appendLines(new DeserializingConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new PropertyGettersGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new GettersGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new MergeGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new IsDirtyGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new SerializeGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new EqualsGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new HashCodeGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new CloneGenerator(classNode).toString());
        codeBuilder.unindent().appendLine("}");
    }

    @Override
    public String toString() {
        return codeBuilder.toString();
    }

    private static class PropertyDeclarationsGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        PropertyDeclarationsGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            if (classNode.implementsInterface("Identifiable") && classNode.getClassChain(otherClasses).size() == 1) {
                codeBuilder.appendLine("private final UUID id;");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            emitSimpleProperty(
                fieldNode,
                typeNode.getBoxedTypeName(),
                typeNode.getBoxedTypeName().equals("Boolean") ? "Boolean" : "Number");
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            emitObjectProperty(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            if (typeNode.getTypeName().equals("String")) {
                emitSimpleProperty(fieldNode, "String", "String");
            } else {
                emitObjectProperty(fieldNode, typeNode.getTypeName());
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            emitObjectProperty(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            emitObjectProperty(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (typeNode.isValueSemantics()) {
                codeBuilder
                    .appendLine(
                        "%s final TrackingAsyncObjectProperty<AsyncObservable%s<%s>> %s =",
                        fieldNode.getAccessModifier(),
                        typeNode.getCollectionTypeName(),
                        typeNode.getGenericType().getTypeName(),
                        fieldNode.getName())
                    .indent()
                    .appendLine(
                        "new TrackingAsyncObjectProperty<AsyncObservable%s<%s>>(",
                        typeNode.getCollectionTypeName(), typeNode.getGenericType().getTypeName());
            } else {
                codeBuilder
                    .appendLine(
                        "%s final TrackingAsync%sProperty<%s> %s =",
                        fieldNode.getAccessModifier(),
                        typeNode.getCollectionTypeName(),
                        typeNode.getGenericType().getTypeName(),
                        fieldNode.getName())
                    .indent()
                    .appendLine(
                        "new TrackingAsync%sProperty<%s>(",
                        typeNode.getCollectionTypeName(), typeNode.getGenericType().getTypeName());
            }

            codeBuilder
                .indent()
                .appendLine("this,")
                .appendLine(
                    "new PropertyMetadata.Builder<AsyncObservable%s<%s>>()",
                    typeNode.getCollectionTypeName(), typeNode.getGenericType().getTypeName())
                .indent();

            if (fieldNode.getDefaultValue().isPresent()) {
                codeBuilder.appendLine(".initialValue(%s)", fieldNode.getDefaultValue().get());
            } else {
                codeBuilder.appendLine(
                    ".initialValue(FXAsyncCollections.observableArray%s())", typeNode.getCollectionTypeName());
            }

            codeBuilder.appendLine(".create());").unindent().unindent().unindent();
        }

        private void emitSimpleProperty(FieldNode fieldNode, String propertyTypeName, String metadataTypeName) {
            codeBuilder
                .appendLine(
                    "%s final TrackingAsync%sProperty %s =",
                    fieldNode.getAccessModifier(), propertyTypeName, fieldNode.getName())
                .indent()
                .append("new TrackingAsync%sProperty(", propertyTypeName);

            if (fieldNode.getDefaultValue().isPresent()) {
                codeBuilder
                    .newLine()
                    .indent()
                    .appendLine("this,")
                    .appendLine(
                        "new PropertyMetadata.Builder<%s>().initialValue(%s).create());",
                        metadataTypeName, fieldNode.getDefaultValue().get())
                    .unindent();
            } else {
                codeBuilder.appendLine("this);");
            }

            codeBuilder.unindent();
        }

        private void emitObjectProperty(FieldNode fieldNode, String propertyTypeName) {
            codeBuilder
                .appendLine(
                    "%s final TrackingAsyncObjectProperty<%s> %s =",
                    fieldNode.getAccessModifier(), propertyTypeName, fieldNode.getName())
                .indent()
                .append("new TrackingAsyncObjectProperty<%s>(", propertyTypeName);

            if (fieldNode.getDefaultValue().isPresent()) {
                codeBuilder
                    .newLine()
                    .indent()
                    .appendLine("this,")
                    .appendLine(
                        "new PropertyMetadata.Builder<%s>().initialValue(%s).create());",
                        propertyTypeName, fieldNode.getDefaultValue().get())
                    .unindent();
            } else {
                codeBuilder.appendLine("this);");
            }

            codeBuilder.unindent();
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class HierarchySupportGenerator {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        HierarchySupportGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            if (classNode.getClassChain(otherClasses).size() != 1) {
                return;
            }

            String parentTypeName = null;
            for (TypeNode typeNode : classNode.getImplementedInterfaces()) {
                if (typeNode instanceof UnknownGenericTypeNode) {
                    UnknownGenericTypeNode genericTypeNode = (UnknownGenericTypeNode)typeNode;
                    if (genericTypeNode.getOuterTypeName().equals("Hierarchical")) {
                        parentTypeName = genericTypeNode.getGenericType().getTypeName();
                        break;
                    }
                }
            }

            if (parentTypeName != null) {
                codeBuilder
                    .appendLine("private %s parent;", parentTypeName)
                    .appendLine("@Override")
                    .appendLine("public %s getParent() {", parentTypeName)
                    .indent()
                    .appendLine("return parent;")
                    .unindent()
                    .appendLine("}");

                codeBuilder
                    .appendLine("void setParent(%s parent) {", parentTypeName)
                    .indent()
                    .appendLine("if (this.parent != null && parent != null)")
                    .indent()
                    .appendLine(
                        "throw new IllegalStateException(\"Parent already set for %s.\");",
                        NameHelper.getModelTypeName(classNode))
                    .unindent()
                    .appendLine("this.parent = parent;")
                    .unindent()
                    .appendLine("}");
            }
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class PropertyGettersGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        PropertyGettersGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            emitSimplePropertyGetter(fieldNode, typeNode.getBoxedTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            emitObjectPropertyGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            if (typeNode.getTypeName().equals("String")) {
                emitSimplePropertyGetter(fieldNode, "String");
            } else {
                emitObjectPropertyGetter(fieldNode, typeNode.getTypeName());
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            emitObjectPropertyGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            emitObjectPropertyGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (Boolean.parseBoolean(fieldNode.getType().findAttributeValue("deprecated", "false"))) {
                codeBuilder.appendLine("@Deprecated");
            }

            if (typeNode.isValueSemantics()) {
                codeBuilder
                    .appendLine(
                        "public %sAsyncObjectProperty<AsyncObservable%s<%s>> %sProperty() {",
                        fieldNode.isReadOnly() ? "ReadOnly" : "",
                        typeNode.getCollectionTypeName(),
                        typeNode.getGenericType().getTypeName(),
                        fieldNode.getName())
                    .indent()
                    .appendLine("return %s;", fieldNode.getName())
                    .unindent()
                    .appendLine("}");
            } else {
                codeBuilder
                    .appendLine(
                        "public %sAsync%sProperty<%s> %sProperty() { ",
                        fieldNode.isReadOnly() ? "ReadOnly" : "",
                        typeNode.getCollectionTypeName(),
                        typeNode.getGenericType().getTypeName(),
                        fieldNode.getName())
                    .indent()
                    .appendLine("return %s;", fieldNode.getName())
                    .unindent()
                    .appendLine("}");
            }
        }

        private void emitSimplePropertyGetter(FieldNode fieldNode, String propertyTypeName) {
            if (Boolean.parseBoolean(fieldNode.getType().findAttributeValue("deprecated", "false"))) {
                codeBuilder.appendLine("@Deprecated");
            }

            codeBuilder
                .appendLine(
                    "public %sAsync%sProperty %sProperty() {",
                    (fieldNode.isReadOnly() ? "ReadOnly" : ""), propertyTypeName, fieldNode.getName())
                .indent()
                .appendLine("return %s;", fieldNode.getName())
                .unindent()
                .appendLine("}");
        }

        private void emitObjectPropertyGetter(FieldNode fieldNode, String propertyTypeName) {
            if (Boolean.parseBoolean(fieldNode.getType().findAttributeValue("deprecated", "false"))) {
                codeBuilder.appendLine("@Deprecated");
            }

            codeBuilder
                .appendLine(
                    "public %sAsyncObjectProperty<%s> %sProperty() {",
                    (fieldNode.isReadOnly() ? "ReadOnly" : ""), propertyTypeName, fieldNode.getName())
                .indent()
                .appendLine("return %s;", fieldNode.getName())
                .unindent()
                .appendLine("}");
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class ParameterlessConstructorGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private final List<ClassNode> otherClasses;

        ParameterlessConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            this.otherClasses = otherClasses;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder.appendLine("%s() {", NameHelper.getModelBaseTypeName(classNode)).indent();

            if (classNode.implementsInterface("Identifiable") && classChain.size() == 1) {
                codeBuilder.appendLine("this.id = UUID.randomUUID();");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {}

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {}

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {}

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {}

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {}

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (isHierarchical(typeNode, otherClasses)) {
                codeBuilder
                    .appendLine("HierarchyHelper.setParent(this.%s, this);", fieldNode.getName())
                    .appendLine("this.%s.addListener(new HierarchyHelper.Listener<>(this));", fieldNode.getName());
            }
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class ConvertingConstructorGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private final List<ClassNode> otherClasses;
        private final ClassNode classNode;
        private boolean sourceIsModelClass;

        ConvertingConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses, boolean sourceIsModelClass) {
            this.classNode = classNode;
            this.otherClasses = otherClasses;
            this.sourceIsModelClass = sourceIsModelClass;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);
            String modelBaseType = NameHelper.getModelBaseTypeName(classNode);

            codeBuilder
                .appendLine(
                    "%s(%s source) {",
                    modelBaseType, sourceIsModelClass ? modelBaseType : NameHelper.getSnapshotTypeName(classNode))
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super(source);");
            } else if (classNode.implementsInterface("Identifiable")) {
                codeBuilder.appendLine("this.id = source.getId();");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            if (sourceIsModelClass) {
                String modelTypeName = NameHelper.getModelTypeName(typeNode);
                codeBuilder.appendLine(
                    "this.%s.init(source.%s, (Function<%s, %s>)%s::new);",
                    fieldNode.getName(), fieldNode.getName(), modelTypeName, modelTypeName, modelTypeName);
            } else {
                codeBuilder.appendLine(
                    "this.%s.init(source.%s(), %s::new);",
                    fieldNode.getName(), NameHelper.getGetterName(fieldNode), NameHelper.getModelTypeName(typeNode));
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            String modelTypeName = NameHelper.getModelTypeName(typeNode.getGenericType());
            String getterName = NameHelper.getGetterName(fieldNode);

            if (typeNode.getGenericType() instanceof GeneratedTypeNode) {
                if (!typeNode.getGenericDerivedTypes().isEmpty()) {
                    if (sourceIsModelClass) {
                        codeBuilder
                            .appendLine(
                                "try (LockedCollection<%s> targetCollection = this.%s.lock(); "
                                    + "LockedCollection<%s> sourceCollection = source.%s().lock()) {",
                                modelTypeName, fieldNode.getName(), modelTypeName, getterName)
                            .indent()
                            .appendLine(
                                "for (%s item : sourceCollection) {",
                                NameHelper.getInterfaceTypeName(typeNode.getGenericType()), getterName)
                            .indent();
                    } else {
                        codeBuilder
                            .appendLine(
                                "try (LockedCollection<%s> targetCollection = this.%s.lock()) {",
                                modelTypeName, fieldNode.getName())
                            .indent()
                            .appendLine(
                                "for (%s item : source.%s()) {",
                                NameHelper.getInterfaceTypeName(typeNode.getGenericType()), getterName)
                            .indent();
                    }

                    for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                        String interfaceTypeName = NameHelper.getInterfaceTypeName(derivedTypeName, otherClasses);
                        codeBuilder.appendLine(
                            "if (item instanceof %s) { targetCollection.add(%s.copy((%s)item)); continue; }",
                            interfaceTypeName,
                            NameHelper.getModelTypeName(derivedTypeName, otherClasses),
                            interfaceTypeName);
                    }

                    codeBuilder
                        .appendLine("throw new IllegalArgumentException(")
                        .indent()
                        .appendLine(
                            "String.format(\"%s does not support %s values of the derived type %%s.\", "
                                + "item.getClass().getSimpleName()));",
                            NameHelper.getModelTypeName(classNode),
                            NameHelper.getModelTypeName(typeNode.getGenericType()))
                        .unindent()
                        .unindent()
                        .appendLine("}")
                        .unindent()
                        .appendLine("}");
                } else {
                    if (sourceIsModelClass) {
                        codeBuilder.appendLine(
                            "CollectionHelper.initAll(this.%s, source.%s, %s::new);",
                            fieldNode.getName(), fieldNode.getName(), modelTypeName);
                    } else {
                        codeBuilder.appendLine(
                            "CollectionHelper.initAll(this.%s, source.%s(), %s::new);",
                            fieldNode.getName(), getterName, modelTypeName);
                    }
                }
            } else if (typeNode.isValueSemantics()) {
                codeBuilder.appendLine(
                    "CollectionHelper.initAll(this.%s, source.%s());", fieldNode.getName(), getterName);
            } else {
                codeBuilder.appendLine(
                    "CollectionHelper.initAll(this.%s, source.%s(), value -> %s);",
                    fieldNode.getName(), getterName, typeNode.getGenericType().findAttributeValue("copy", "value"));
            }

            if (isHierarchical(typeNode, otherClasses)) {
                codeBuilder
                    .appendLine("HierarchyHelper.setParent(this.%s, this);", fieldNode.getName())
                    .appendLine("this.%s.addListener(new HierarchyHelper.Listener<>(this));", fieldNode.getName());
            }
        }

        private void visitSimpleField(FieldNode fieldNode) {
            if (sourceIsModelClass) {
                codeBuilder.appendLine("this.%s.init(source.%s);", fieldNode.getName(), fieldNode.getName());
            } else {
                codeBuilder.appendLine(
                    "this.%s.init(source.%s());", fieldNode.getName(), NameHelper.getGetterName(fieldNode));
            }
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class DeserializingConstructorGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private final List<ClassNode> otherClasses;

        DeserializingConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            this.otherClasses = otherClasses;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder
                .appendLine("%s(CompositeDeserializationContext context) {", NameHelper.getModelBaseTypeName(classNode))
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super(context);");
            } else if (classNode.implementsInterface("Identifiable")) {
                codeBuilder.appendLine("this.id = UUID.fromString(context.readString(\"id\"));");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().append("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getBoxedTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            codeBuilder.appendLine(
                "PropertySerializationHelper.readObject(context, this.%s, %s.class);",
                fieldNode.getName(), NameHelper.getModelTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            codeBuilder.appendLine(
                "PropertySerializationHelper.readEnum(context, this.%s, %s.class);",
                fieldNode.getName(), typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            codeBuilder.appendLine(
                "PropertySerializationHelper.readObject(context, this.%s, %s.class);",
                fieldNode.getName(), NameHelper.getModelTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            String typeName =
                typeNode.getGenericType() instanceof GeneratedTypeNode
                    ? NameHelper.getModelTypeName(typeNode.getGenericType())
                    : typeNode.getGenericType().getTypeName();

            if (typeNode.getGenericDerivedTypes().isEmpty()) {
                codeBuilder.appendLine(
                    "PropertySerializationHelper.read%s(context, this.%s, %s.class);",
                    typeNode.getCollectionTypeName(), fieldNode.getName(), typeName);
            } else {
                codeBuilder.append(
                    "PropertySerializationHelper.readPolymorphic%s(context, this.%s, %s.class",
                    typeNode.getCollectionTypeName(), fieldNode.getName(), typeName);

                for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                    codeBuilder.append(", %s.class", NameHelper.getModelTypeName(derivedTypeName, otherClasses));
                }

                codeBuilder.appendLine(");");
            }

            if (isHierarchical(typeNode, otherClasses)) {
                codeBuilder
                    .appendLine("HierarchyHelper.setParent(this.%s, this);", fieldNode.getName())
                    .appendLine("this.%s.addListener(new HierarchyHelper.Listener<>(this));", fieldNode.getName());
            }
        }

        private void visitSimpleField(FieldNode fieldNode, String typeName) {
            codeBuilder.appendLine(
                "PropertySerializationHelper.read%s(context, this.%s);", typeName, fieldNode.getName());
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class GettersGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        GettersGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            if (classNode.implementsInterface("Identifiable") && classNode.getClassChain(otherClasses).size() == 1) {
                codeBuilder
                    .appendLine("@Override")
                    .appendLine("public UUID getId() {")
                    .indent()
                    .appendLine("return this.id;")
                    .unindent()
                    .appendLine("}");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            emitGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            emitGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            emitGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            emitGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            emitGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            String typeName =
                typeNode.getGenericType() instanceof GeneratedTypeNode
                    ? NameHelper.getModelTypeName(typeNode.getGenericType())
                    : typeNode.getGenericType().getTypeName();

            emitGetter(fieldNode, String.format("AsyncObservable%s<%s>", typeNode.getCollectionTypeName(), typeName));
        }

        private void emitGetter(FieldNode fieldNode, String typeName) {
            if (Boolean.parseBoolean(fieldNode.getType().findAttributeValue("deprecated", "false"))) {
                codeBuilder.appendLine("@Deprecated");
            }

            codeBuilder
                .appendLine("@Override")
                .appendLine("public %s %s() {", typeName, NameHelper.getGetterName(fieldNode))
                .indent()
                .appendLine("return %s.get();", fieldNode.getName())
                .unindent()
                .appendLine("}");
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class MergeGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private final ClassNode classNode;
        private final List<ClassNode> otherClasses;

        MergeGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            this.classNode = classNode;
            this.otherClasses = otherClasses;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder.appendLine("@Override");

            if (classChain.size() > 1) {
                String baseIntfName = NameHelper.getInterfaceTypeName(classChain.get(0));
                String superIntfName = NameHelper.getInterfaceTypeName(classChain.get(classChain.size() - 1));

                codeBuilder
                    .appendLine("public void merge(%s newValueBase, MergeStrategy strategy) {", baseIntfName)
                    .indent()
                    .appendLine("if (newValueBase == null)")
                    .indent()
                    .appendLine("throw new IllegalArgumentException(\"Value cannot be null.\");")
                    .unindent()
                    .appendLine("if (!(newValueBase instanceof %s))", NameHelper.getInterfaceTypeName(classNode))
                    .indent()
                    .appendLine(
                        "throw new IllegalArgumentException(newValueBase.getClass().getName() + \" cannot be merged into %s.\");",
                        NameHelper.getModelBaseTypeName(classNode))
                    .unindent()
                    .appendLine("super.merge(newValueBase, strategy);")
                    .appendLine("%s newValue = (%s)newValueBase;", superIntfName, superIntfName);
            } else {
                codeBuilder
                    .appendLine(
                        "public void merge(%s newValue, MergeStrategy strategy) {",
                        NameHelper.getInterfaceTypeName(classNode))
                    .indent();

                if (classNode.implementsInterface("Identifiable")) {
                    codeBuilder
                        .appendLine("if (!this.id.equals(newValue.getId()))")
                        .indent()
                        .appendLine("throw new IllegalArgumentException(\"ID mismatch.\");")
                        .unindent();
                }
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            codeBuilder.appendLine(
                "this.%s.merge(newValue.%s(), strategy, %s::copy);",
                fieldNode.getName(),
                NameHelper.getGetterName(fieldNode),
                NameHelper.getModelTypeName(typeNode.getTypeName(), otherClasses));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (typeNode.getGenericType() instanceof UnknownTypeNode
                    || typeNode.getGenericType() instanceof KnownTypeNode) {
                if (typeNode.isValueSemantics()) {
                    codeBuilder.appendLine(
                        "CollectionHelper.merge(this.%s, newValue.%s(), strategy, value -> value);",
                        fieldNode.getName(), NameHelper.getGetterName(fieldNode));
                } else {
                    codeBuilder.appendLine(
                        "this.%s.merge(newValue.%s(), strategy, value -> %s, (source, target) -> %s);",
                        fieldNode.getName(),
                        NameHelper.getGetterName(fieldNode),
                        typeNode.getGenericType().findAttributeValue("copy", "value"),
                        typeNode.getGenericType().findAttributeValue("merge", "{}"));
                }
            } else if (typeNode.getGenericType() instanceof GeneratedTypeNode) {
                String modelName = NameHelper.getModelTypeName(typeNode.getGenericType());

                if (typeNode.isValueSemantics()) {
                    codeBuilder.appendLine(
                        "CollectionHelper.merge(this.%s, newValue.%s(), strategy, %s::copy);",
                        fieldNode.getName(), NameHelper.getGetterName(fieldNode), modelName);
                } else if (!typeNode.getGenericDerivedTypes().isEmpty()) {
                    codeBuilder
                        .appendLine(
                            "this.%s.merge(newValue.%s(), strategy, value -> {",
                            fieldNode.getName(), NameHelper.getGetterName(fieldNode))
                        .indent();
                    codeBuilder.appendLine("if (value == null) return null;");

                    for (String deriveTypeName : typeNode.getGenericDerivedTypes()) {
                        String derivedModelBaseName = NameHelper.getModelBaseTypeName(deriveTypeName, otherClasses);
                        String derivedInterfaceName = NameHelper.getInterfaceTypeName(deriveTypeName, otherClasses);
                        codeBuilder.appendLine(
                            "if (value instanceof %s) return %s.copy((%s)value);",
                            derivedInterfaceName, derivedModelBaseName, derivedInterfaceName);
                    }

                    codeBuilder
                        .appendLine("throw new IllegalArgumentException(")
                        .indent()
                        .appendLine(
                            "String.format(\"%s does not support %s values of the derived type %%s.\", "
                                + "value.getClass().getSimpleName()));",
                            NameHelper.getModelTypeName(classNode), modelName)
                        .unindent()
                        .unindent()
                        .appendLine("}, (s, t) -> s.merge(t, strategy));");
                } else {
                    codeBuilder.appendLine(
                        "this.%s.merge(newValue.%s(), strategy, %s::copy, (s, t) -> s.merge(t, strategy));",
                        fieldNode.getName(), NameHelper.getGetterName(fieldNode), modelName);
                }
            } else {
                throw new RuntimeException("Can't generate code for " + typeNode.getTypeName());
            }
        }

        private void visitSimpleField(FieldNode fieldNode) {
            codeBuilder.appendLine(
                "this.%s.merge(newValue.%s(), strategy);", fieldNode.getName(), NameHelper.getGetterName(fieldNode));
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class IsDirtyGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private boolean itemIncludesOperator = false;

        IsDirtyGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder.appendLine("@Override");
            codeBuilder.appendLine("public boolean isDirty() {").indent();
            codeBuilder.append("return").indent();

            if (classChain.size() > 1) {
                codeBuilder.newLine().append("super.isDirty()");
                itemIncludesOperator = true;
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.appendLine(";").unindent().unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            visitSimpleField(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            visitSimpleField(fieldNode);
        }

        private void visitSimpleField(FieldNode fieldNode) {
            if (itemIncludesOperator) {
                codeBuilder.newLine().append("|| this.%s.isDirty()", fieldNode.getName());
            } else {
                codeBuilder.newLine().append("this.%s.isDirty()", fieldNode.getName());
                itemIncludesOperator = true;
            }
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class SerializeGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        SerializeGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder
                .appendLine("@Override")
                .appendLine("public void serialize(CompositeSerializationContext context) {")
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super.serialize(context);");
            } else if (classNode.implementsInterface("Identifiable")) {
                codeBuilder.appendLine("context.writeString(\"id\", this.id.toString());");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getBoxedTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            codeBuilder.appendLine("PropertySerializationHelper.writeObject(context, this.%s);", fieldNode.getName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            codeBuilder.appendLine(
                "PropertySerializationHelper.writeEnum(context, this.%s, %s.class);",
                fieldNode.getName(), NameHelper.getModelTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            codeBuilder.appendLine("PropertySerializationHelper.writeObject(context, this.%s);", fieldNode.getName());
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            codeBuilder.appendLine(
                "PropertySerializationHelper.write%s%s(context, this.%s, %s.class);",
                typeNode.getGenericDerivedTypes().isEmpty() ? "" : "Polymorphic",
                typeNode.getCollectionTypeName(),
                fieldNode.getName(),
                NameHelper.getModelTypeName(typeNode.getGenericType()));
        }

        private void visitSimpleField(FieldNode fieldNode, String typeName) {
            codeBuilder.appendLine(
                "PropertySerializationHelper.write%s(context, this.%s);", typeName, fieldNode.getName());
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class EqualsGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        EqualsGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);
            String modelTypeName = NameHelper.getModelBaseTypeName(classNode);

            codeBuilder.appendLine("@Override");
            codeBuilder.appendLine("public boolean equals(Object obj) {");
            codeBuilder.indent().appendLine("if (this == obj) return true;");
            codeBuilder
                .appendLine("if (!(obj instanceof %s)) return false;", modelTypeName)
                .appendLine("%s other = (%s)obj;", modelTypeName, modelTypeName)
                .appendLine("return")
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.append("super.equals(other)");
            } else if (classNode.implementsInterface("Identifiable")) {
                codeBuilder.append("Objects.equals(id, other.id)");
            } else {
                codeBuilder.append("true");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.appendLine(";").unindent().unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            emitEqualsOperator(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            emitEqualsMethod(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            emitEqualsMethod(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            emitEqualsOperator(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            emitEqualsMethod(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            emitEqualsMethod(fieldNode);
        }

        private void emitEqualsOperator(FieldNode fieldNode) {
            codeBuilder.newLine().append("&& %s.get() == other.%s.get()", fieldNode.getName(), fieldNode.getName());
        }

        private void emitEqualsMethod(FieldNode fieldNode) {
            codeBuilder
                .newLine()
                .append("&& Objects.equals(%s.get(), other.%s.get())", fieldNode.getName(), fieldNode.getName());
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class HashCodeGenerator {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        HashCodeGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            if (classChain.size() == 1 && classNode.implementsInterface("Identifiable")) {
                codeBuilder
                    .appendLine("@Override")
                    .appendLine("public int hashCode() {")
                    .indent()
                    .appendLine("return id.hashCode();")
                    .unindent()
                    .appendLine("}");
            }
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class CloneGenerator {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        CloneGenerator(ClassNode classNode) {
            if (classNode.isAbstractClass()) {
                return;
            }

            String interfaceTypeName = NameHelper.getInterfaceTypeName(classNode);
            String modelTypeName = NameHelper.getModelTypeName(classNode);
            String snapshotTypeName = NameHelper.getSnapshotTypeName(classNode);

            codeBuilder
                .appendLine("public static %s copy(%s source) {", modelTypeName, interfaceTypeName)
                .indent()
                .appendLine("if (source == null) {")
                .indent()
                .appendLine("return null;")
                .unindent()
                .appendLine("}")
                .appendLine("if (source instanceof %s) {", modelTypeName)
                .indent()
                .appendLine("return new %s((%s)source);", modelTypeName, modelTypeName)
                .unindent()
                .appendLine("}")
                .appendLine("if (source instanceof %s) {", snapshotTypeName)
                .indent()
                .appendLine("return new %s((%s)source);", modelTypeName, snapshotTypeName)
                .unindent()
                .appendLine("}")
                .appendLine("throw new IllegalArgumentException(\"Unexpected type: \" + source.getClass().getName());")
                .unindent()
                .appendLine("}");
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static boolean isHierarchical(TypeNode typeNode, List<ClassNode> classes) {
        if (Boolean.parseBoolean(typeNode.findAttributeValue("hierarchical", "false"))) {
            return true;
        }

        if (typeNode instanceof CollectionNode) {
            typeNode = ((CollectionNode)typeNode).getGenericType();
        } else if (typeNode instanceof UnknownGenericTypeNode) {
            typeNode = ((UnknownGenericTypeNode)typeNode).getGenericType();
        }

        for (ClassNode classNode : classes) {
            if (classNode.getClassName().equals(typeNode.getTypeName())
                    && (classNode.implementsInterface("Hierarchical"))) {
                return true;
            }
        }

        return false;
    }

}
