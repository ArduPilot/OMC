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
import build.model.ast.UnknownTypeNode;
import java.util.List;

public class SnapshotClassGenerator {

    private final CodeBuilder codeBuilder = new CodeBuilder();

    public SnapshotClassGenerator(
            String packageName, String[] imports, ClassNode classNode, List<ClassNode> otherClasses) {
        codeBuilder.appendLine("package " + packageName + ";");

        for (String imp : imports) {
            codeBuilder.appendLine("import " + imp + ";");
        }

        if (classNode.getInheritedClass().isPresent()) {
            codeBuilder.append(
                "public class %s extends %s",
                NameHelper.getSnapshotTypeName(classNode),
                NameHelper.getSnapshotTypeName(classNode.getInheritedClass().get(), otherClasses));
        } else {
            codeBuilder.append("public class %s", NameHelper.getSnapshotTypeName(classNode));
        }

        CommonCode.implementsInterfaces(
            codeBuilder,
            NameHelper.getSnapshotTypeName(classNode),
            classNode.getClassChain(otherClasses),
            NameHelper.getInterfaceTypeName(classNode));

        codeBuilder.indent();
        codeBuilder.appendLines(new FieldDeclarationsGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new AllArgsConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new ConvertingConstructorGenerator(classNode, otherClasses, false).toString());
        if (!classNode.isImmutable()) {
            codeBuilder.appendLines(new ConvertingConstructorGenerator(classNode, otherClasses, true).toString());
        }

        codeBuilder.appendLines(new DeserializingConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new GettersGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new SerializeGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new EqualsGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new HashCodeGenerator(classNode, otherClasses).toString());
        codeBuilder.unindent().appendLine("}");
    }

    @Override
    public String toString() {
        return codeBuilder.toString();
    }

    private static class FieldDeclarationsGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        FieldDeclarationsGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            if (classNode.implementsInterface("Identifiable") && classNode.getClassChain(otherClasses).size() == 1) {
                codeBuilder.appendLine("private final UUID id;");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            emitField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            emitField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            emitField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            emitField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            emitField(fieldNode, NameHelper.getSnapshotTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (typeNode.getGenericType() instanceof GeneratedTypeNode) {
                emitField(
                    fieldNode,
                    String.format(
                        "%s<%s>",
                        typeNode.getCollectionTypeName(), NameHelper.getSnapshotTypeName(typeNode.getGenericType())));
            } else {
                emitField(fieldNode, fieldNode.getType().getTypeName());
            }
        }

        private void emitField(FieldNode fieldNode, String typeName) {
            codeBuilder.appendLine("private final %s %s;", typeName, fieldNode.getName());
            codeBuilder.appendLine("private final %s %sClean;", typeName, fieldNode.getName());
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class AllArgsConstructorGenerator {
        private final CodeBuilder codeBuilder = new CodeBuilder();

        AllArgsConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);
            boolean isIdentifiable = false;
            for (ClassNode node : classChain) {
                isIdentifiable |= node.implementsInterface("Identifiable");
            }

            codeBuilder.appendLine("public %s(", NameHelper.getSnapshotTypeName(classNode)).indent().indent();

            if (isIdentifiable) {
                codeBuilder.appendLine("UUID id,");
            }

            for (int c = 0; c < classChain.size(); ++c) {
                List<FieldNode> fields = classChain.get(c).getFields();
                boolean fieldsRemaining = fieldsRemaining(classChain, c);

                for (int f = 0; f < fields.size(); ++f) {
                    FieldNode field = fields.get(f);
                    if (field.getType() instanceof CollectionNode) {
                        CollectionNode node = (CollectionNode)field.getType();
                        if (node.getGenericType() instanceof GeneratedTypeNode) {
                            codeBuilder.append(
                                "%s<%s> %s",
                                node.getCollectionTypeName(),
                                NameHelper.getSnapshotTypeName(node.getGenericType()),
                                field.getName());
                        } else {
                            codeBuilder.append(
                                "%s<%s> %s",
                                node.getCollectionTypeName(), node.getGenericType().getTypeName(), field.getName());
                        }
                    } else if (field.getType() instanceof GeneratedTypeNode) {
                        codeBuilder.append("%s %s", NameHelper.getSnapshotTypeName(field.getType()), field.getName());
                    } else {
                        codeBuilder.append("%s %s", field.getType().getTypeName(), field.getName());
                    }

                    if (f < fields.size() - 1 || fieldsRemaining) {
                        codeBuilder.appendLine(",");
                    }
                }
            }

            codeBuilder.appendLine(") {").unindent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super(").indent();

                if (isIdentifiable) {
                    codeBuilder.appendLine("id,");
                }

                for (int c = 0; c < classChain.size() - 1; ++c) {
                    List<FieldNode> fields = classChain.get(c).getFields();

                    for (int f = 0; f < fields.size(); ++f) {
                        codeBuilder.append(fields.get(f).getName());

                        if (f < fields.size() - 1 || c < classChain.size() - 2) {
                            codeBuilder.appendLine(",");
                        }
                    }
                }

                codeBuilder.appendLine(");").unindent();
            } else if (isIdentifiable) {
                codeBuilder.appendLine("this.id = id;");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                codeBuilder.appendLine("this.%s = %s;", fieldNode.getName(), fieldNode.getName());
                codeBuilder.appendLine(
                    "this.%sClean = %s;", fieldNode.getName(), fieldNode.getDefaultValue().orElse("null"));
            }

            codeBuilder.unindent().appendLine("}");
        }

        private boolean fieldsRemaining(List<ClassNode> classChain, int currentClass) {
            for (int c = currentClass + 1; c < classChain.size(); ++c) {
                if (!classChain.get(c).getFields().isEmpty()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

    private static class ConvertingConstructorGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private final ClassNode classNode;
        private final List<ClassNode> otherClasses;
        private final boolean sourceIsModelClass;

        ConvertingConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses, boolean sourceIsModelClass) {
            this.classNode = classNode;
            this.otherClasses = otherClasses;
            this.sourceIsModelClass = sourceIsModelClass;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder
                .appendLine(
                    "public %s(%s source) {",
                    NameHelper.getSnapshotTypeName(classNode),
                    sourceIsModelClass
                        ? NameHelper.getModelTypeName(classNode)
                        : NameHelper.getSnapshotTypeName(classNode))
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
                String snapshotTypeName = NameHelper.getSnapshotTypeName(typeNode);

                codeBuilder.appendLine("{").indent();
                codeBuilder
                    .appendLine(
                        "TrackingAsyncProperty property = (TrackingAsyncProperty)source.%sProperty();",
                        fieldNode.getName())
                    .appendLine(
                        "%s value = (%s)property.getValue(), cleanValue = (%s)property.getCleanValue();",
                        modelTypeName, modelTypeName, modelTypeName)
                    .appendLine(
                        "this.%s = value != null ? new %s(value) : null;", fieldNode.getName(), snapshotTypeName)
                    .appendLine(
                        "this.%sClean = value == cleanValue ? this.%s : (cleanValue != null ? new %s(cleanValue) : null);",
                        fieldNode.getName(), fieldNode.getName(), snapshotTypeName);
                codeBuilder.unindent().appendLine("}");
            } else {
                codeBuilder
                    .appendLine(
                        "this.%s = source.%s != null ? new %s(source.%s) : null;",
                        fieldNode.getName(),
                        fieldNode.getName(),
                        NameHelper.getSnapshotTypeName(typeNode),
                        fieldNode.getName())
                    .appendLine(
                        "this.%sClean = source.%s == source.%sClean ? this.%s : (source.%sClean != null ? new %s(source.%sClean) : null);",
                        fieldNode.getName(),
                        fieldNode.getName(),
                        fieldNode.getName(),
                        fieldNode.getName(),
                        fieldNode.getName(),
                        NameHelper.getSnapshotTypeName(typeNode),
                        fieldNode.getName());
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (typeNode.getGenericType() instanceof GeneratedTypeNode) {
                if (typeNode.getGenericDerivedTypes().isEmpty()) {
                    codeBuilder.appendLine(
                        "this.%s = CollectionHelper.copy(source.%s(), %s::new);",
                        fieldNode.getName(),
                        NameHelper.getGetterName(fieldNode),
                        NameHelper.getSnapshotTypeName(typeNode.getGenericType()));

                    if (sourceIsModelClass) {
                        codeBuilder
                            .appendLine(
                                "this.%sClean = CollectionHelper.copyDeduplicate(this.%s, source.%sProperty().get(),",
                                fieldNode.getName(), fieldNode.getName(), fieldNode.getName())
                            .indent();

                        if (typeNode.isValueSemantics()) {
                            codeBuilder
                                .appendLine(
                                    "((TrackingAsyncObjectProperty<AsyncObservable%s<%s>>)source.%sProperty()).getCleanValue(), %s::new);",
                                    typeNode.getCollectionTypeName(),
                                    typeNode.getGenericType().getTypeName(),
                                    fieldNode.getName(),
                                    NameHelper.getSnapshotTypeName(typeNode.getGenericType()))
                                .unindent();
                        } else {
                            codeBuilder
                                .appendLine(
                                    "((TrackingAsync%sProperty<%s>)source.%sProperty()).getCleanValue(), %s::new);",
                                    typeNode.getCollectionTypeName(),
                                    typeNode.getGenericType().getTypeName(),
                                    fieldNode.getName(),
                                    NameHelper.getSnapshotTypeName(typeNode.getGenericType()))
                                .unindent();
                        }
                    } else {
                        codeBuilder.appendLine(
                            "this.%sClean = CollectionHelper.copyDeduplicate(this.%s, source.%s, source.%sClean, %s::new);",
                            fieldNode.getName(),
                            fieldNode.getName(),
                            fieldNode.getName(),
                            fieldNode.getName(),
                            NameHelper.getSnapshotTypeName(typeNode.getGenericType()));
                    }
                } else {
                    if (sourceIsModelClass) {
                        String modelTypeName = NameHelper.getModelTypeName(typeNode.getGenericType());
                        String snapshotTypeName = NameHelper.getSnapshotTypeName(typeNode.getGenericType());

                        codeBuilder.appendLine("{").indent();
                        codeBuilder
                            .appendLine("Function<%s, %s> func = item -> {", modelTypeName, snapshotTypeName)
                            .indent();

                        for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                            String derivedModelTypeName = NameHelper.getModelTypeName(derivedTypeName, otherClasses);
                            String derivedSnapshotTypeName =
                                NameHelper.getSnapshotTypeName(derivedTypeName, otherClasses);
                            codeBuilder.appendLine(
                                "if (item instanceof %s) return new %s((%s)item);",
                                derivedModelTypeName, derivedSnapshotTypeName, derivedModelTypeName);
                        }

                        codeBuilder
                            .appendLine("throw new IllegalArgumentException(")
                            .indent()
                            .appendLine(
                                "String.format(\"%s does not support %s values of the derived type %%s.\", "
                                    + "item.getClass().getSimpleName()));",
                                NameHelper.getSnapshotTypeName(classNode),
                                NameHelper.camelCase(NameHelper.getModelTypeName(typeNode.getGenericType())))
                            .unindent()
                            .unindent()
                            .appendLine("};");

                        codeBuilder.appendLine(
                            "TrackingAsync%sProperty<%s> property = (TrackingAsync%sProperty<%s>)source.%sProperty();",
                            typeNode.getCollectionTypeName(),
                            modelTypeName,
                            typeNode.getCollectionTypeName(),
                            modelTypeName,
                            fieldNode.getName());

                        codeBuilder
                            .appendLine(
                                "this.%s = CollectionHelper.copy(property.getValue(), func);", fieldNode.getName())
                            .appendLine(
                                "this.%sClean = CollectionHelper.copyDeduplicate(this.%s, property.getValue(), property.getCleanValue(), func);",
                                fieldNode.getName(), fieldNode.getName());
                        codeBuilder.unindent().appendLine("}");
                    } else {
                        String snapshotTypeName = NameHelper.getSnapshotTypeName(typeNode.getGenericType());
                        codeBuilder.appendLine("{").indent();
                        codeBuilder
                            .appendLine("Function<%s, %s> func = item -> {", snapshotTypeName, snapshotTypeName)
                            .indent();

                        for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                            String derivedSnapshotTypeName =
                                NameHelper.getSnapshotTypeName(derivedTypeName, otherClasses);
                            codeBuilder.appendLine(
                                "if (item instanceof %s) return new %s((%s)item);",
                                derivedSnapshotTypeName, derivedSnapshotTypeName, derivedSnapshotTypeName);
                        }

                        codeBuilder
                            .appendLine("throw new IllegalArgumentException(")
                            .indent()
                            .appendLine(
                                "String.format(\"%s does not support %s values of the derived type %%s.\", "
                                    + "item.getClass().getSimpleName()));",
                                NameHelper.getSnapshotTypeName(classNode),
                                NameHelper.camelCase(NameHelper.getModelTypeName(typeNode.getGenericType())))
                            .unindent()
                            .unindent()
                            .appendLine("};");

                        codeBuilder
                            .appendLine(
                                "this.%s = CollectionHelper.copy(source.%s, func);",
                                fieldNode.getName(), fieldNode.getName())
                            .appendLine(
                                "this.%sClean = CollectionHelper.copyDeduplicate(this.%s, source.%s, source.%sClean, func);",
                                fieldNode.getName(), fieldNode.getName(), fieldNode.getName(), fieldNode.getName());
                        codeBuilder.unindent().appendLine("}");
                    }
                }
            } else {
                codeBuilder.appendLine(
                    "this.%s = CollectionHelper.copy(source.%s());",
                    fieldNode.getName(), NameHelper.getGetterName(fieldNode));

                if (sourceIsModelClass) {
                    if (typeNode.isValueSemantics()) {
                        codeBuilder.appendLine(
                            "this.%sClean = CollectionHelper.copy(((TrackingAsyncObjectProperty<AsyncObservable%s<%s>>)source.%sProperty()).getCleanValue());",
                            fieldNode.getName(),
                            typeNode.getCollectionTypeName(),
                            typeNode.getGenericType().getTypeName(),
                            fieldNode.getName());
                    } else {
                        codeBuilder.appendLine(
                            "this.%sClean = CollectionHelper.copy(((TrackingAsync%sProperty<%s>)source.%sProperty()).getCleanValue());",
                            fieldNode.getName(),
                            typeNode.getCollectionTypeName(),
                            typeNode.getGenericType().getTypeName(),
                            fieldNode.getName());
                    }
                } else {
                    codeBuilder.appendLine(
                        "this.%sClean = CollectionHelper.copy(source.%sClean);",
                        fieldNode.getName(), fieldNode.getName());
                }
            }
        }

        private void visitSimpleField(FieldNode fieldNode) {
            if (sourceIsModelClass) {
                codeBuilder.appendLine("{").indent();
                codeBuilder
                    .appendLine(
                        "TrackingAsyncProperty property = (TrackingAsyncProperty)source.%sProperty();",
                        fieldNode.getName())
                    .appendLine(
                        "this.%s = (%s)property.getValue();", fieldNode.getName(), fieldNode.getType().getTypeName())
                    .appendLine(
                        "this.%sClean = (%s)property.getCleanValue();",
                        fieldNode.getName(), fieldNode.getType().getTypeName());
                codeBuilder.unindent().appendLine("}");
            } else {
                codeBuilder
                    .appendLine("this.%s = source.%s;", fieldNode.getName(), fieldNode.getName())
                    .appendLine("this.%sClean = source.%sClean;", fieldNode.getName(), fieldNode.getName());
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
        private boolean deserializeCleanValues;

        DeserializingConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            this.otherClasses = otherClasses;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder
                .appendLine(
                    "public %s(CompositeDeserializationContext context) {", NameHelper.getSnapshotTypeName(classNode))
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super(context);");
            } else if (classNode.implementsInterface("Identifiable")) {
                codeBuilder.appendLine("this.id = UUID.fromString(context.readString(\"id\"));");
            }

            codeBuilder
                .appendLine("if (context.getOptions() instanceof ProjectSerializationOptions")
                .indent()
                .indent()
                .appendLine("&& ((ProjectSerializationOptions)context.getOptions()).isSerializeTrackingState()) {")
                .unindent();

            deserializeCleanValues = true;
            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("} else {").indent();

            deserializeCleanValues = false;
            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().append("}").unindent().append("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getBoxedTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            if (deserializeCleanValues) {
                visitSimpleFieldWithCleanValue(fieldNode, typeNode.getTypeName());
            } else {
                codeBuilder
                    .appendLine(
                        "this.%s = context.readObject(\"%s\", %s.class, %s);",
                        fieldNode.getName(),
                        fieldNode.getName(),
                        typeNode.getTypeName(),
                        fieldNode.getDefaultValue().orElse("null"))
                    .appendLine("this.%sClean = %s;", fieldNode.getName(), fieldNode.getDefaultValue().orElse("null"));
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            if (deserializeCleanValues) {
                visitSimpleFieldWithCleanValue(fieldNode, typeNode.getTypeName());
            } else {
                codeBuilder
                    .appendLine(
                        "this.%s = context.readEnum(\"%s\", %s.class, %s);",
                        fieldNode.getName(),
                        fieldNode.getName(),
                        typeNode.getTypeName(),
                        fieldNode.getDefaultValue().orElse("null"))
                    .appendLine("this.%sClean = %s;", fieldNode.getName(), fieldNode.getDefaultValue().orElse("null"));
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            String snapshotTypeName = NameHelper.getSnapshotTypeName(typeNode);

            if (deserializeCleanValues) {
                visitSimpleFieldWithCleanValue(fieldNode, snapshotTypeName);
            } else {
                codeBuilder
                    .appendLine(
                        "this.%s = context.readObject(\"%s\", %s.class, %s);",
                        fieldNode.getName(),
                        fieldNode.getName(),
                        snapshotTypeName,
                        fieldNode.getDefaultValue().orElse("null"))
                    .appendLine("this.%sClean = %s;", fieldNode.getName(), fieldNode.getDefaultValue().orElse("null"));
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            String collectionType = typeNode.getCollectionTypeName();
            String typeName =
                typeNode.getGenericType() instanceof GeneratedTypeNode
                    ? NameHelper.getSnapshotTypeName(typeNode.getGenericType())
                    : typeNode.getGenericType().getTypeName();

            if (typeNode.getGenericDerivedTypes().isEmpty()) {
                if (deserializeCleanValues) {
                    codeBuilder.appendLine("{").indent();
                    codeBuilder
                        .appendLine(
                            "Pair%s<%s<%s>> list = new Pair%s<>();",
                            collectionType, collectionType, typeName, collectionType)
                        .appendLine("//noinspection unchecked")
                        .appendLine(
                            "context.read%s(\"%s\", (List)list, %s.class);",
                            collectionType, fieldNode.getName(), typeName)
                        .appendLine(
                            "this.%s = list.get(0); this.%sClean = list.get(1);",
                            fieldNode.getName(), fieldNode.getName());
                    codeBuilder.unindent().appendLine("}");
                } else {
                    codeBuilder
                        .appendLine("this.%s = new Array%s<>();", fieldNode.getName(), collectionType)
                        .appendLine("this.%sClean = new Array%s<>();", fieldNode.getName(), collectionType);
                    codeBuilder.appendLine(
                        "context.read%s(\"%s\", this.%s, %s.class, %s);",
                        collectionType,
                        fieldNode.getName(),
                        fieldNode.getName(),
                        typeName,
                        fieldNode.getDefaultValue().orElse("new Array" + collectionType + "<>()"));
                }
            } else {
                if (deserializeCleanValues) {
                    codeBuilder.appendLine("{").indent();
                    codeBuilder
                        .appendLine(
                            "Pair%s<%s<%s>> list = new Pair%s<>();",
                            collectionType, collectionType, typeName, collectionType)
                        .appendLine("//noinspection unchecked")
                        .append(
                            "context.readPolymorphic%s(\"%s\", (%s)list, %s.class",
                            collectionType, fieldNode.getName(), collectionType, typeName);

                    for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                        codeBuilder.append(", %s.class", NameHelper.getSnapshotTypeName(derivedTypeName, otherClasses));
                    }

                    codeBuilder.appendLine(");");
                    codeBuilder.appendLine(
                        "this.%s = list.get(0); this.%sClean = list.get(1);", fieldNode.getName(), fieldNode.getName());
                    codeBuilder.unindent().appendLine("}");
                } else {
                    codeBuilder
                        .appendLine("this.%s = new Array%s<>();", fieldNode.getName(), collectionType)
                        .appendLine("this.%sClean = new Array%s<>();", fieldNode.getName(), collectionType);
                    codeBuilder.append(
                        "context.readPolymorphic%s(\"%s\", this.%s, %s.class, %s",
                        collectionType,
                        fieldNode.getName(),
                        fieldNode.getName(),
                        typeName,
                        fieldNode.getDefaultValue().orElse("new Array" + collectionType + "<>()"));

                    for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                        codeBuilder.append(", %s.class", NameHelper.getSnapshotTypeName(derivedTypeName, otherClasses));
                    }

                    codeBuilder.appendLine(");");
                }
            }
        }

        private void visitSimpleField(FieldNode fieldNode, String typeName) {
            if (deserializeCleanValues) {
                visitSimpleFieldWithCleanValue(fieldNode, typeName);
            } else {
                codeBuilder
                    .appendLine(
                        "this.%s = context.read%s(\"%s\", %s);",
                        fieldNode.getName(), typeName, fieldNode.getName(), fieldNode.getDefaultValue().orElse("null"))
                    .appendLine("this.%sClean = %s;", fieldNode.getName(), fieldNode.getDefaultValue().orElse("null"));
            }
        }

        private void visitSimpleFieldWithCleanValue(FieldNode fieldNode, String typeName) {
            codeBuilder.appendLine("{").indent();
            codeBuilder
                .appendLine("PairList<%s> list = new PairList<>();", typeName)
                .appendLine("context.readList(\"%s\", list, %s.class);", fieldNode.getName(), typeName)
                .appendLine(
                    "this.%s = list.get(0); this.%sClean = list.get(1);", fieldNode.getName(), fieldNode.getName());
            codeBuilder.unindent().appendLine("}");
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
            emitGetter(fieldNode, NameHelper.getSnapshotTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            String typeName =
                typeNode.getGenericType() instanceof GeneratedTypeNode
                    ? NameHelper.getSnapshotTypeName(typeNode.getGenericType())
                    : typeNode.getGenericType().getTypeName();

            emitGetter(fieldNode, String.format("%s<%s>", typeNode.getCollectionTypeName(), typeName));
        }

        private void emitGetter(FieldNode fieldNode, String typeName) {
            if (Boolean.parseBoolean(fieldNode.getType().findAttributeValue("deprecated", "false"))) {
                codeBuilder.appendLine("@Deprecated");
            }

            codeBuilder
                .appendLine("@Override")
                .appendLine("public %s %s() {", typeName, NameHelper.getGetterName(fieldNode))
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

    private static class SerializeGenerator implements FieldVisitor {
        private final CodeBuilder codeBuilder = new CodeBuilder();
        private boolean serializeCleanValues;

        SerializeGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            serializerGenerator(classNode, classChain, false);

            if (classNode.implementsInterface("BinarySerializable")) {
                serializerGenerator(classNode, classChain, true);
            }
        }

        private void serializerGenerator(ClassNode classNode, List<ClassNode> classChain, boolean isBinary) {
            codeBuilder
                .appendLine("@Override")
                .appendLine(
                    "public void serialize(%s context) {",
                    isBinary ? "BinarySerializationContext" : "CompositeSerializationContext")
                .indent();

            if (classChain.size() > 1) {
                codeBuilder.appendLine("super.serialize(context);");
            } else if (classNode.implementsInterface("Identifiable")) {
                codeBuilder.appendLine("context.writeString(\"id\", this.id.toString());");
            }

            codeBuilder
                .appendLine("if (context.getOptions() instanceof ProjectSerializationOptions")
                .indent()
                .indent()
                .appendLine("&& ((ProjectSerializationOptions)context.getOptions()).isSerializeTrackingState()) {")
                .unindent();

            serializeCleanValues = true;
            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("} else {").indent();

            serializeCleanValues = false;
            for (FieldNode fieldNode : classNode.getFields()) {
                fieldNode.accept(this);
            }

            codeBuilder.unindent().appendLine("}").unindent().appendLine("}");
        }

        @Override
        public void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getBoxedTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, UnknownTypeNode typeNode) {
            if (serializeCleanValues) {
                codeBuilder.appendLine(
                    "context.writeCollection(\"%s\", new PairList(this.%s, this.%sClean), %s.class);",
                    fieldNode.getName(), fieldNode.getName(), fieldNode.getName(), typeNode.getTypeName());
            } else {
                codeBuilder.appendLine(
                    "context.writeObject(\"%s\", this.%s);", fieldNode.getName(), fieldNode.getName());
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            if (serializeCleanValues) {
                codeBuilder.appendLine(
                    "context.writeCollection(\"%s\", new PairList(this.%s, this.%sClean), %s.class);",
                    fieldNode.getName(), fieldNode.getName(), fieldNode.getName(), typeNode.getTypeName());
            } else {
                codeBuilder.appendLine("context.writeEnum(\"%s\", this.%s);", fieldNode.getName(), fieldNode.getName());
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            if (serializeCleanValues) {
                codeBuilder.appendLine(
                    "context.writeCollection(\"%s\", new PairList(this.%s, this.%sClean), %s.class);",
                    fieldNode.getName(), fieldNode.getName(), fieldNode.getName(), typeNode.getTypeName());
            } else {
                codeBuilder.appendLine(
                    "context.writeObject(\"%s\", this.%s);", fieldNode.getName(), fieldNode.getName());
            }
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (typeNode.getGenericType() instanceof GeneratedTypeNode) {
                codeBuilder.appendLine(
                    "context.write%sCollection(\"%s\", %s, %s.class);",
                    typeNode.getGenericDerivedTypes().isEmpty() ? "" : "Polymorphic",
                    fieldNode.getName(),
                    serializeCleanValues
                        ? String.format("new PairList(this.%s, this.%sClean)", fieldNode.getName(), fieldNode.getName())
                        : "this." + fieldNode.getName(),
                    NameHelper.getSnapshotTypeName(typeNode.getGenericType()));
            } else {
                codeBuilder.appendLine(
                    "context.writeCollection(\"%s\", %s, %s.class);",
                    fieldNode.getName(),
                    serializeCleanValues
                        ? String.format("new PairList(this.%s, this.%sClean)", fieldNode.getName(), fieldNode.getName())
                        : "this." + fieldNode.getName(),
                    typeNode.getGenericType().getTypeName());
            }
        }

        private void visitSimpleField(FieldNode fieldNode, String typeName) {
            if (serializeCleanValues) {
                codeBuilder.appendLine(
                    "context.writeCollection(\"%s\", new PairList(this.%s, this.%sClean), %s.class);",
                    fieldNode.getName(), fieldNode.getName(), fieldNode.getName(), typeName);
            } else {
                codeBuilder.appendLine(
                    "context.write%s(\"%s\", this.%s);", typeName, fieldNode.getName(), fieldNode.getName());
            }
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
            String snapshotTypeName = NameHelper.getSnapshotTypeName(classNode);

            codeBuilder.appendLine("@Override");
            codeBuilder.appendLine("public boolean equals(Object obj) {");
            codeBuilder.indent().appendLine("if (this == obj) return true;");
            codeBuilder
                .appendLine("if (!(obj instanceof %s)) return false;", snapshotTypeName)
                .appendLine("%s other = (%s)obj;", snapshotTypeName, snapshotTypeName)
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
            codeBuilder.newLine().append("&& %s == other.%s", fieldNode.getName(), fieldNode.getName());
        }

        private void emitEqualsMethod(FieldNode fieldNode) {
            codeBuilder.newLine().append("&& Objects.equals(%s, other.%s)", fieldNode.getName(), fieldNode.getName());
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
                "result = 31 * result + %s.hashCode(this.%s);", typeNode.getBoxedTypeName(), fieldNode.getName());
        }

        private void emitHashCode(FieldNode fieldNode) {
            codeBuilder.appendLine(
                "result = 31 * result + (this.%s == null ? 0 : this.%s.hashCode());",
                fieldNode.getName(), fieldNode.getName());
        }

        @Override
        public String toString() {
            return codeBuilder.toString();
        }
    }

}
