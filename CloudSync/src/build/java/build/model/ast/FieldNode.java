/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.ast;

import build.model.gen.FieldVisitor;
import java.util.Optional;

public class FieldNode {

    private final String name;
    private final boolean readOnly;
    private final TypeNode type;
    private final String defaultValue;

    public FieldNode(String name, TypeNode type) {
        this.name = name;
        this.readOnly = "true".equals(type.findAttributeValue("readonly", "false"));
        this.type = type;
        this.defaultValue = type.findAttributeValue("default", null);
    }

    public String getName() {
        return name;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public TypeNode getType() {
        return type;
    }

    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public void accept(FieldVisitor visitor) {
        type.accept(this, visitor);
    }

}
