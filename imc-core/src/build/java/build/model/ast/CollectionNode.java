/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.ast;

import build.model.gen.FieldVisitor;
import java.util.ArrayList;
import java.util.List;

public class CollectionNode extends TypeNode {

    private final String typeName;
    private final String collectionTypeName;
    private final TypeNode genericType;
    private final List<String> genericDerivedTypes = new ArrayList<>();

    public CollectionNode(String collectionTypeName, TypeNode genericType, List<AttributeNode> attributes) {
        super(attributes);
        this.typeName = collectionTypeName + "<" + genericType.getTypeName() + ">";
        this.collectionTypeName = collectionTypeName;
        this.genericType = genericType;

        for (String type : findAttributeValue("derivedTypes", "").split(" ")) {
            if (type.length() > 0) {
                this.genericDerivedTypes.add(type);
            }
        }
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    public String getCollectionTypeName() {
        return collectionTypeName;
    }

    public TypeNode getGenericType() {
        return genericType;
    }

    public List<String> getGenericDerivedTypes() {
        return genericDerivedTypes;
    }

    public boolean isValueSemantics() {
        return Boolean.parseBoolean(findAttributeValue("valueSemantics", "false"));
    }

    @Override
    public void accept(FieldNode fieldNode, FieldVisitor visitor) {
        visitor.visitField(fieldNode, this);
    }

}
