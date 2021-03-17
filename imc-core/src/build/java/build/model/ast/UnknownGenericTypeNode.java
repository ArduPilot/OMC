/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.ast;

import java.util.List;

public class UnknownGenericTypeNode extends UnknownTypeNode {

    private final String outerTypeName;
    private final TypeNode genericType;

    public UnknownGenericTypeNode(String outerTypeName, TypeNode innerType, List<AttributeNode> attributes) {
        super(outerTypeName + "<" + innerType.getTypeName() + ">", attributes);
        this.outerTypeName = outerTypeName;
        this.genericType = innerType;
    }

    public String getOuterTypeName() {
        return outerTypeName;
    }

    public TypeNode getGenericType() {
        return genericType;
    }

}
