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
            codeBuilder.appendLine(
                "abstract class %s extends %s implements %s {",
                NameHelper.getModelBaseTypeName(classNode),
                classNode.hasAbstractModifier()
                    ? NameHelper.getModelBaseTypeName(classNode.getInheritedClass().get(), otherClasses)
                    : NameHelper.getModelTypeName(classNode.getInheritedClass().get(), otherClasses),
                NameHelper.getInterfaceTypeName(classNode));
        } else {
            codeBuilder.appendLine(
                "abstract class %s implements %s, Mergeable<%s> {",
                NameHelper.getModelBaseTypeName(classNode),
                NameHelper.getInterfaceTypeName(classNode),
                NameHelper.getInterfaceTypeName(classNode));
        }

        codeBuilder.indent();
        codeBuilder.appendLines(new PropertyDeclarationsGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new PropertyGettersGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new ParameterlessConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new ConvertingConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new DeserializingConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new GettersGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new MergeGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new IsDirtyGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new GetObjectDataGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new EqualsGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new HashCodeGenerator(classNode, otherClasses).toString());
        codeBuilder.unindent().appendLine("}");
    }

    @Override
    public String toString() {
        return codeBuilder.toString();
    }

    private static class PropertyDeclarationsGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        PropertyDeclarationsGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            if (classNode.getClassChain(otherClasses).size() == 1) {
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
                        "private final TrackingAsyncObjectProperty<AsyncObservable%s<%s>> %s =",
                        typeNode.getCollectionTypeName(), typeNode.getGenericType().getTypeName(), fieldNode.getName())
                    .indent()
                    .appendLine(
                        "new TrackingAsyncObjectProperty<AsyncObservable%s<%s>>(",
                        typeNode.getCollectionTypeName(), typeNode.getGenericType().getTypeName());
            } else {
                codeBuilder
                    .appendLine(
                        "private final TrackingAsync%sProperty<%s> %s =",
                        typeNode.getCollectionTypeName(), typeNode.getGenericType().getTypeName(), fieldNode.getName())
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
                .indent()
                .appendLine(".synchronizationContext(PlatformSynchronizationContext.getInstance())");

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
                .appendLine("private final TrackingAsync%sProperty %s =", propertyTypeName, fieldNode.getName())
                .indent()
                .appendLine("new TrackingAsync%sProperty(", propertyTypeName)
                .indent()
                .appendLine("this,")
                .appendLine("new PropertyMetadata.Builder<%s>()", metadataTypeName)
                .indent()
                .appendLine(".synchronizationContext(PlatformSynchronizationContext.getInstance())");

            if (fieldNode.getDefaultValue().isPresent()) {
                codeBuilder.appendLine(".initialValue(%s)", fieldNode.getDefaultValue().get());
            }

            codeBuilder.appendLine(".create());").unindent().unindent().unindent();
        }

        private void emitObjectProperty(FieldNode fieldNode, String propertyTypeName) {
            codeBuilder
                .appendLine("private final TrackingAsyncObjectProperty<%s> %s =", propertyTypeName, fieldNode.getName())
                .indent()
                .appendLine("new TrackingAsyncObjectProperty<%s>(", propertyTypeName)
                .indent()
                .appendLine("this,")
                .appendLine("new PropertyMetadata.Builder<%s>()", propertyTypeName)
                .indent()
                .appendLine(".synchronizationContext(PlatformSynchronizationContext.getInstance())");

            if (fieldNode.getDefaultValue().isPresent()) {
                codeBuilder.appendLine(".initialValue(%s)", fieldNode.getDefaultValue().get());
            }

            codeBuilder.appendLine(".create());").unindent().unindent().unindent();
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
            emitSimplePropertyGetter(
                fieldNode,
                typeNode.getBoxedTypeName(),
                typeNode.getBoxedTypeName().equals("Boolean") ? "Boolean" : "Number");
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            emitObjectPropertyGetter(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            if (typeNode.getTypeName().equals("String")) {
                emitSimplePropertyGetter(fieldNode, "String", "String");
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
            if (typeNode.isValueSemantics()) {
                codeBuilder
                    .indent()
                    .appendLine(
                        "public %sAsyncObjectProperty<AsyncObservable%s<%s>> %sProperty() {",
                        fieldNode.isReadOnly() ? "ReadOnly" : "",
                        typeNode.getCollectionTypeName(),
                        typeNode.getGenericType().getTypeName(),
                        fieldNode.getName())
                    .indent()
                    .appendLine("return %s;", fieldNode.getName())
                    .indent()
                    .appendLine("}");
            } else {
                codeBuilder
                    .indent()
                    .appendLine(
                        "public %sAsync%sProperty<%s> %sProperty() { ",
                        fieldNode.isReadOnly() ? "ReadOnly" : "",
                        typeNode.getCollectionTypeName(),
                        typeNode.getGenericType().getTypeName(),
                        fieldNode.getName())
                    .indent()
                    .appendLine("return %s;", fieldNode.getName())
                    .indent()
                    .appendLine("}");
            }

            codeBuilder.unindent().unindent().unindent();
        }

        private void emitSimplePropertyGetter(FieldNode fieldNode, String propertyTypeName, String metadataTypeName) {
            codeBuilder
                .indent()
                .appendLine(
                    "public %sAsync%sProperty %sProperty() {",
                    (fieldNode.isReadOnly() ? "ReadOnly" : ""), propertyTypeName, fieldNode.getName())
                .indent()
                .appendLine("return %s;", fieldNode.getName())
                .unindent()
                .appendLine("}");
        }

        private void emitObjectPropertyGetter(FieldNode fieldNode, String propertyTypeName) {
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

        ParameterlessConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder.appendLine("%s() {", NameHelper.getModelBaseTypeName(classNode)).indent();

            if (classChain.size() == 1) {
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
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {}

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class ConvertingConstructorGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private final List<ClassNode> otherClasses;
        private final ClassNode classNode;

        ConvertingConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            this.classNode = classNode;
            this.otherClasses = otherClasses;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder
                .appendLine(
                    "%s(%s source) {",
                    NameHelper.getModelBaseTypeName(classNode), NameHelper.getInterfaceTypeName(classNode))
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super(source);");
            } else {
                codeBuilder.appendLine("this.id = source.getId();");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            codeBuilder.appendLine(
                "this.%s.update(source.%s(), %s::new);",
                fieldNode.getName(), NameHelper.getGetterName(fieldNode), NameHelper.getModelTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            String modelTypeName = NameHelper.getModelTypeName(typeNode.getGenericType());
            String getterName = NameHelper.getGetterName(fieldNode);

            if (typeNode.getGenericType() instanceof GeneratedTypeNode) {
                if (!typeNode.getGenericDerivedTypes().isEmpty()) {
                    codeBuilder
                        .appendLine(
                            "try (LockedCollection<%s> lockedCollection = this.%s.lock()) {",
                            modelTypeName, fieldNode.getName())
                        .indent()
                        .appendLine(
                            "for (%s item : source.%s()) {",
                            NameHelper.getInterfaceTypeName(typeNode.getGenericType()), getterName)
                        .indent();

                    for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                        String interfaceTypeName = NameHelper.getInterfaceTypeName(derivedTypeName, otherClasses);
                        codeBuilder.appendLine(
                            "if (item instanceof %s) { lockedCollection.add(new %s((%s)item)); continue; }",
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
                    codeBuilder.appendLine(
                        "CollectionHelper.updateAll(this.%s, source.%s(), %s::new);",
                        fieldNode.getName(), getterName, modelTypeName);
                }
            } else if (typeNode.isValueSemantics()) {
                codeBuilder.appendLine(
                    "CollectionHelper.updateAll(this.%s, source.%s());", fieldNode.getName(), getterName);
            } else {
                codeBuilder.appendLine(
                    "CollectionHelper.updateAll(this.%s, source.%s(), value -> %s);",
                    fieldNode.getName(), getterName, typeNode.getGenericType().findAttributeValue("copy", "value"));
            }
        }

        private void visitSimpleField(FieldNode fieldNode, TypeNode typeNode) {
            codeBuilder.appendLine(
                "this.%s.update(source.%s());", fieldNode.getName(), NameHelper.getGetterName(fieldNode));
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
                .appendLine("%s(DeserializationContext context) {", NameHelper.getModelBaseTypeName(classNode))
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super(context);");
            } else {
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
            if (classNode.getClassChain(otherClasses).size() == 1) {
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
                    .indent()
                    .appendLine("if (!this.id.equals(newValue.getId()))")
                    .indent()
                    .appendLine("throw new IllegalArgumentException(\"ID mismatch.\");")
                    .unindent();
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
                "this.%s.merge(newValue.%s(), strategy, %s::new);",
                fieldNode.getName(), NameHelper.getGetterName(fieldNode), NameHelper.getModelTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (typeNode.getGenericType() instanceof UnknownTypeNode
                    || typeNode.getGenericType() instanceof KnownTypeNode) {
                if (typeNode.isValueSemantics()) {
                    codeBuilder.appendLine(
                        "CollectionHelper.updateAll(this.%s, newValue.%s());",
                        fieldNode.getName(), NameHelper.getGetterName(fieldNode));
                } else {
                    codeBuilder.appendLine(
                        "this.%s.merge(newValue.%s(), strategy, value -> %s, (source, target) -> %s);",
                        fieldNode.getName(),
                        NameHelper.getGetterName(fieldNode),
                        typeNode.getGenericType().findAttributeValue("copy", "value"),
                        typeNode.getGenericType().findAttributeValue("merge", "{}"));
                }
            } else if (typeNode.getGenericType() instanceof GeneratedTypeNode
                    && !typeNode.getGenericDerivedTypes().isEmpty()) {
                codeBuilder
                    .appendLine(
                        "this.%s.merge(newValue.%s(), strategy, value -> {",
                        fieldNode.getName(), NameHelper.getGetterName(fieldNode))
                    .indent();

                for (String deriveTypeName : typeNode.getGenericDerivedTypes()) {
                    String modelTypeName = NameHelper.getModelTypeName(deriveTypeName, otherClasses);
                    String interfaceTypeName = NameHelper.getInterfaceTypeName(deriveTypeName, otherClasses);
                    codeBuilder.appendLine(
                        "if (value instanceof %s) return new %s((%s)value);",
                        interfaceTypeName, modelTypeName, interfaceTypeName);
                }

                codeBuilder
                    .appendLine("throw new IllegalArgumentException(")
                    .indent()
                    .appendLine(
                        "String.format(\"%s does not support %s values of the derived type %%s.\", "
                            + "value.getClass().getSimpleName()));",
                        NameHelper.getModelTypeName(classNode), NameHelper.getModelTypeName(typeNode.getGenericType()))
                    .unindent()
                    .unindent()
                    .appendLine("}, (s, t) -> s.merge(t, strategy));");
            } else {
                codeBuilder.appendLine(
                    "this.%s.merge(newValue.%s(), strategy, %s::new, (s, t) -> s.merge(t, strategy));",
                    fieldNode.getName(),
                    NameHelper.getGetterName(fieldNode),
                    NameHelper.getModelTypeName(typeNode.getGenericType()));
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

    private static class GetObjectDataGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        GetObjectDataGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder
                .appendLine("@Override")
                .appendLine("public void getObjectData(SerializationContext context) {")
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super.getObjectData(context);");
            } else {
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
            codeBuilder.appendLine(
                "PropertySerializationHelper.writeObject(context, this.%s, %s.class);",
                fieldNode.getName(), typeNode.getTypeName());
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
            codeBuilder.appendLine(
                "PropertySerializationHelper.writeObject(context, this.%s, %s.class);",
                fieldNode.getName(), NameHelper.getModelTypeName(typeNode));
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
            } else {
                codeBuilder.append("Objects.equals(id, other.id)");
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

    private static class HashCodeGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        HashCodeGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder.appendLine("@Override").appendLine("public int hashCode() {").indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("int result = super.hashCode();");
            } else {
                codeBuilder.appendLine("int result = this.id.hashCode();");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.appendLine("return result;").unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            emitPrimitiveHashCode(fieldNode, typeNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            emitHashCode(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            emitHashCode(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            emitHashCode(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            emitHashCode(fieldNode);
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            emitHashCode(fieldNode);
        }

        private void emitPrimitiveHashCode(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            codeBuilder.appendLine(
                "result = 31 * result + %s.hashCode(this.%s.get());", typeNode.getBoxedTypeName(), fieldNode.getName());
        }

        private void emitHashCode(FieldNode fieldNode) {
            codeBuilder.appendLine(
                "{ Object v = this.%s.get(); result = 31 * result + (v == null ? 0 : v.hashCode()); }",
                fieldNode.getName());
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

}
