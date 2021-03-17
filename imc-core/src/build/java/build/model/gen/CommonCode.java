/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.gen;

import build.model.ast.ClassNode;
import build.model.ast.TypeNode;
import java.util.ArrayList;
import java.util.List;

class CommonCode {

    static void extendsInterfaces(
            CodeBuilder codeBuilder,
            String currentTypeName,
            List<ClassNode> classChain,
            String... additionalInterfaces) {
        implementInterfacesInternal(codeBuilder, currentTypeName, classChain, "extends", true, additionalInterfaces);
    }

    static void implementsInterfaces(
            CodeBuilder codeBuilder,
            String currentTypeName,
            List<ClassNode> classChain,
            String... additionalInterfaces) {
        implementInterfacesInternal(
            codeBuilder, currentTypeName, classChain, "implements", false, additionalInterfaces);
    }

    private static void implementInterfacesInternal(
            CodeBuilder codeBuilder,
            String currentTypeName,
            List<ClassNode> classChain,
            String extendOrImplement,
            boolean implementByDefault,
            String... additionalInterfaces) {
        ClassNode classNode = classChain.get(classChain.size() - 1);
        List<String> implementedInterfaces = new ArrayList<>(List.of(additionalInterfaces));
        for (TypeNode typeNode : classNode.getImplementedInterfaces()) {
            String implementedOn = typeNode.findAttributeValue("implementedOn", null);
            if (implementedOn == null && implementByDefault) {
                implementedInterfaces.add(typeNode.getTypeName());
            } else if (implementedOn != null && implementedOn.equals(currentTypeName)) {
                implementedInterfaces.add(typeNode.getTypeName());
            }
        }

        if (!implementedInterfaces.isEmpty()) {
            codeBuilder.append(" ").append(extendOrImplement).append(" ").append(implementedInterfaces.get(0));

            for (int i = 1; i < implementedInterfaces.size(); ++i) {
                codeBuilder.append(", ").append(implementedInterfaces.get(i));
            }
        }

        codeBuilder.appendLine(" {");
    }

}
