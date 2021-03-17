/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.gen;

import build.model.ast.CollectionNode;
import build.model.ast.EnumTypeNode;
import build.model.ast.FieldNode;
import build.model.ast.GeneratedTypeNode;
import build.model.ast.KnownTypeNode;
import build.model.ast.PrimitiveTypeNode;
import build.model.ast.UnknownTypeNode;

public interface FieldVisitor {

    void visitField(FieldNode fieldNode, PrimitiveTypeNode typeNode);

    void visitField(FieldNode fieldNode, UnknownTypeNode typeNode);

    void visitField(FieldNode fieldNode, KnownTypeNode typeNode);

    void visitField(FieldNode fieldNode, EnumTypeNode typeNode);

    void visitField(FieldNode fieldNode, GeneratedTypeNode typeNode);

    void visitField(FieldNode fieldNode, CollectionNode typeNode);

}
