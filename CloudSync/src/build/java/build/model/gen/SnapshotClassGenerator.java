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

public class SnapshotClassGenerator {

    private final CodeBuilder codeBuilder = new CodeBuilder();

    public SnapshotClassGenerator(
            String packageName, String[] imports, ClassNode classNode, List<ClassNode> otherClasses) {
        codeBuilder.appendLine("package " + packageName + ";");

        for (String imp : imports) {
            codeBuilder.appendLine("import " + imp + ";");
        }

        if (classNode.getInheritedClass().isPresent()) {
            codeBuilder.appendLine(
                "public class %s extends %s implements %s {",
                NameHelper.getSnapshotTypeName(classNode),
                NameHelper.getSnapshotTypeName(classNode.getInheritedClass().get(), otherClasses),
                NameHelper.getInterfaceTypeName(classNode));
        } else {
            codeBuilder.appendLine(
                "public class %s implements %s {",
                NameHelper.getSnapshotTypeName(classNode), NameHelper.getInterfaceTypeName(classNode));
        }

        codeBuilder.indent();
        codeBuilder.appendLines(new FieldDeclarationsGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new AllArgsConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new ConvertingConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new DeserializingConstructorGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new GettersGenerator(classNode, otherClasses).toString());
        codeBuilder.appendLines(new GetObjectDataGenerator(classNode, otherClasses).toString());
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
            if (classNode.getClassChain(otherClasses).size() == 1) {
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

            codeBuilder.appendLine("public %s(", NameHelper.getSnapshotTypeName(classNode));
            codeBuilder.indent().indent().appendLine("UUID id,");

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
                codeBuilder.appendLine("super(");
                codeBuilder.indent().appendLine("id,");

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
            } else {
                codeBuilder.appendLine("this.id = id;");
            }

            for (FieldNode fieldNode : classNode.getFields()) {
                codeBuilder.appendLine("this.%s = %s;", fieldNode.getName(), fieldNode.getName());
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

        ConvertingConstructorGenerator(ClassNode classNode, List<ClassNode> otherClasses) {
            this.classNode = classNode;
            this.otherClasses = otherClasses;
            List<ClassNode> classChain = classNode.getClassChain(otherClasses);

            codeBuilder
                .appendLine(
                    "public %s(%s source) {",
                    NameHelper.getSnapshotTypeName(classNode), NameHelper.getInterfaceTypeName(classNode))
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
                "this.%s = new %s(source.%s());",
                fieldNode.getName(), NameHelper.getSnapshotTypeName(typeNode), NameHelper.getGetterName(fieldNode));
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
                } else {
                    codeBuilder
                        .appendLine(
                            "this.%s = CollectionHelper.copy(source.%s(), item -> {",
                            fieldNode.getName(), NameHelper.getGetterName(fieldNode))
                        .indent();

                    for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                        String interfaceTypeName = NameHelper.getInterfaceTypeName(derivedTypeName, otherClasses);
                        String snapshotTypeName = NameHelper.getSnapshotTypeName(derivedTypeName, otherClasses);
                        codeBuilder.appendLine(
                            "if (item instanceof %s) return new %s((%s)item);",
                            interfaceTypeName, snapshotTypeName, interfaceTypeName);
                    }

                    codeBuilder
                        .appendLine("throw new IllegalArgumentException(")
                        .indent()
                        .appendLine(
                            "String.format(\"%s does not support %s values of the derived type %%s.\", "
                                + "item.getClass().getSimpleName()));",
                            NameHelper.getSnapshotTypeName(classNode),
                            NameHelper.getSnapshotTypeName(typeNode.getGenericType()))
                        .unindent()
                        .unindent()
                        .appendLine("});");
                }
            } else {
                codeBuilder.appendLine(
                    "this.%s = new ArrayList<%s>(source.%s());",
                    fieldNode.getName(), typeNode.getGenericType().getTypeName(), NameHelper.getGetterName(fieldNode));
            }
        }

        private void visitSimpleField(FieldNode fieldNode, TypeNode typeNode) {
            codeBuilder.appendLine("this.%s = source.%s();", fieldNode.getName(), NameHelper.getGetterName(fieldNode));
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
                .appendLine("public %s(DeserializationContext context) {", NameHelper.getSnapshotTypeName(classNode))
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
                "this.%s = context.readObject(\"%s\", %s.class);",
                fieldNode.getName(), fieldNode.getName(), typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            codeBuilder.appendLine(
                "this.%s = context.readEnum(\"%s\", %s.class);",
                fieldNode.getName(), fieldNode.getName(), typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            codeBuilder.appendLine(
                "this.%s = context.readObject(\"%s\", %s.class);",
                fieldNode.getName(), fieldNode.getName(), NameHelper.getSnapshotTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            String typeName =
                typeNode.getGenericType() instanceof GeneratedTypeNode
                    ? NameHelper.getSnapshotTypeName(typeNode.getGenericType())
                    : typeNode.getGenericType().getTypeName();

            codeBuilder.appendLine("this.%s = new ArrayList<>();", fieldNode.getName());

            if (typeNode.getGenericDerivedTypes().isEmpty()) {
                codeBuilder.appendLine(
                    "context.readCollection(\"%s\", this.%s, %s.class);",
                    fieldNode.getName(), fieldNode.getName(), typeName);
            } else {
                codeBuilder.append(
                    "context.readPolymorphicCollection(\"%s\", this.%s, %s.class",
                    fieldNode.getName(), fieldNode.getName(), typeName);

                for (String derivedTypeName : typeNode.getGenericDerivedTypes()) {
                    codeBuilder.append(", %s.class", NameHelper.getSnapshotTypeName(derivedTypeName, otherClasses));
                }

                codeBuilder.appendLine(");");
            }
        }

        private void visitSimpleField(FieldNode fieldNode, String typeName) {
            codeBuilder.appendLine(
                "this.%s = context.read%s(\"%s\");", fieldNode.getName(), typeName, fieldNode.getName());
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
                "context.writeObject(\"%s\", this.%s, %s.class);",
                fieldNode.getName(), fieldNode.getName(), typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, KnownTypeNode typeNode) {
            visitSimpleField(fieldNode, typeNode.getTypeName());
        }

        @Override
        public void visitField(FieldNode fieldNode, EnumTypeNode typeNode) {
            codeBuilder.appendLine("context.writeEnum(\"%s\", this.%s);", fieldNode.getName(), fieldNode.getName());
        }

        @Override
        public void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode) {
            codeBuilder.appendLine(
                "context.writeObject(\"%s\", this.%s, %s.class);",
                fieldNode.getName(), fieldNode.getName(), NameHelper.getSnapshotTypeName(typeNode));
        }

        @Override
        public void visitField(FieldNode fieldNode, CollectionNode typeNode) {
            if (typeNode.getGenericType() instanceof GeneratedTypeNode) {
                codeBuilder.appendLine(
                    "context.write%sCollection(\"%s\", this.%s, %s.class);",
                    typeNode.getGenericDerivedTypes().isEmpty() ? "" : "Polymorphic",
                    fieldNode.getName(),
                    fieldNode.getName(),
                    NameHelper.getSnapshotTypeName(typeNode.getGenericType()));
            } else {
                codeBuilder.appendLine(
                    "context.writeCollection(\"%s\", this.%s, %s.class);",
                    fieldNode.getName(), fieldNode.getName(), typeNode.getGenericType().getTypeName());
            }
        }

        private void visitSimpleField(FieldNode fieldNode, String typeName) {
            codeBuilder.appendLine(
                "context.write%s(\"%s\", this.%s);", typeName, fieldNode.getName(), fieldNode.getName());
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
