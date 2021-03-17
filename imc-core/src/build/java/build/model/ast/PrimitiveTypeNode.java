/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.ast;

import build.model.gen.FieldVisitor;
import java.util.List;

public class PrimitiveTypeNode extends TypeNode {

    private final String typeName;
    private final String boxedTypeName;

    public PrimitiveTypeNode(String typeName, String boxedTypeName, List<AttributeNode> attributes) {
        super(attributes);
        this.typeName = typeName;
        this.boxedTypeName = boxedTypeName;
    }

    @Override
    public String getDefaultValue() {
        String defaultValue = super.getDefaultValue();
        if (defaultValue != null) {
            return defaultValue;
        }

        switch (typeName) {
        case "byte":
        case "short":
        case "int":
        case "long":
        case "float":
        case "double":
            return "0";
        case "boolean":
            return "false";
        default:
            return null;
        }
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    public String getBoxedTypeName() {
        return boxedTypeName;
    }

    @Override
    public void accept(FieldNode fieldNode, FieldVisitor visitor) {
        visitor.visitField(fieldNode, this);
    }

}
