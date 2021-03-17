/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.ast;

import build.model.gen.FieldVisitor;
import java.util.Collections;
import java.util.List;

public abstract class TypeNode {

    private final List<AttributeNode> attributes;

    TypeNode(List<AttributeNode> attributes) {
        this.attributes = attributes != null ? attributes : Collections.emptyList();
    }

    public abstract String getTypeName();

    public String getDefaultValue() {
        return findAttributeValue("default", null);
    }

    public List<AttributeNode> getAttributes() {
        return attributes;
    }

    public abstract void accept(FieldNode fieldNode, FieldVisitor visitor);

    public AttributeNode findAttribute(String name) {
        if (attributes == null) {
            return null;
        }

        for (AttributeNode attribute : getAttributes()) {
            if (attribute.getName().equals(name)) {
                return attribute;
            }
        }

        return null;
    }

    public String findAttributeValue(String name) {
        for (AttributeNode attribute : getAttributes()) {
            if (attribute.getName().equals(name)) {
                return attribute.getValue();
            }
        }

        throw new RuntimeException("Attribute '" + name + "' not found.");
    }

    public String findAttributeValue(String name, String fallbackValue) {
        for (AttributeNode attribute : getAttributes()) {
            if (attribute.getName().equals(name)) {
                return attribute.getValue();
            }
        }

        return fallbackValue;
    }

    public boolean findAttributeFlag(String name) {
        for (AttributeNode attribute : getAttributes()) {
            if (attribute.getName().equals(name)) {
                return Boolean.parseBoolean(attribute.getValue());
            }
        }

        return false;
    }

}
