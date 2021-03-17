/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.ast;

import build.model.gen.FieldVisitor;
import java.util.Collections;
import java.util.List;

public class KnownTypeNode extends TypeNode {

    private final String typeName;

    public KnownTypeNode(String typeName) {
        super(Collections.emptyList());
        this.typeName = typeName;
    }

    public KnownTypeNode(String typeName, List<AttributeNode> attributes) {
        super(attributes);
        this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public void accept(FieldNode fieldNode, FieldVisitor visitor) {
        visitor.visitField(fieldNode, this);
    }

}
