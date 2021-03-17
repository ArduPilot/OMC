/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

public class codegen {

    @SuppressWarnings("unchecked")
    private static final CombinatorialTemplate[] TEMPLATES =
        new CombinatorialTemplate[] {
            new CombinatorialTemplate(
                new String[] {
                    "org/asyncfx/beans/property/ReadOnlyAsync_T_Wrapper.java",
                    "org/asyncfx/beans/property/ReadOnlyAsync_T_Property.java",
                    "org/asyncfx/beans/property/ReadOnlyAsync_T_PropertyProxy.java",
                    "org/asyncfx/beans/property/ReadOnly_T_PropertyProxy.java",
                    "org/asyncfx/beans/property/Async_T_Property.java",
                    "org/asyncfx/beans/property/Async_T_PropertyBaseImpl.java",
                    "org/asyncfx/beans/property/Async_T_PropertyProxy.java",
                    "org/asyncfx/beans/property/_T_PropertyProxy.java",
                    "org/asyncfx/beans/property/SimpleAsync_T_Property.java",
                    "org/asyncfx/beans/property/UIAsync_T_Property.java"
                },
                new Map[] {
                    map().put("primType", "boolean").put("boxedType", "Boolean").put("numberType", "Boolean").build(),
                    map().put("primType", "int").put("boxedType", "Integer").put("numberType", "Number").build(),
                    map().put("primType", "long").put("boxedType", "Long").put("numberType", "Number").build(),
                    map().put("primType", "float").put("boxedType", "Float").put("numberType", "Number").build(),
                    map().put("primType", "double").put("boxedType", "Double").put("numberType", "Number").build(),
                    map().put("primType", "String").put("boxedType", "String").put("numberType", "String").build(),
                    map().put("primType", "T")
                        .put("boxedType", "Object")
                        .put("numberType", "T")
                        .put("genericType", "<T>")
                        .put("additionalImplements", ", AsyncSubObservableValue<T>")
                        .build()
                }),
            new CombinatorialTemplate(
                "org/asyncfx/beans/binding/Async_T_Binding.java",
                new Map[] {
                    map().put("primType", "boolean").put("boxedType", "Boolean").put("numberType", "Boolean").build(),
                    map().put("primType", "int").put("boxedType", "Integer").put("numberType", "Number").build(),
                    map().put("primType", "long").put("boxedType", "Long").put("numberType", "Number").build(),
                    map().put("primType", "float").put("boxedType", "Float").put("numberType", "Number").build(),
                    map().put("primType", "double").put("boxedType", "Double").put("numberType", "Number").build(),
                    map().put("primType", "String").put("boxedType", "String").put("numberType", "String").build(),
                    map().put("primType", "T")
                        .put("boxedType", "Object")
                        .put("numberType", "T")
                        .put("genericType", "<T>")
                        .build()
                }),
            new CombinatorialTemplate("org/asyncfx/beans/binding/CriticalBindings.java", new HashMap<>())
        };

    private static class CombinatorialTemplate {
        final String[] fileNames;
        final Map<String, String>[] variables;

        @SuppressWarnings("unchecked")
        CombinatorialTemplate(String fileName, Map<String, String> context) {
            this.fileNames = new String[] {fileName};
            this.variables = new Map[] {context};
        }

        @SuppressWarnings("unchecked")
        CombinatorialTemplate(String[] fileNames, Map<String, String> context) {
            this.fileNames = fileNames;
            this.variables = new Map[] {context};
        }

        CombinatorialTemplate(String fileName, Map<String, String>[] variables) {
            this.fileNames = new String[] {fileName};
            this.variables = variables;
        }

        CombinatorialTemplate(String[] fileNames, Map<String, String>[] variables) {
            this.fileNames = fileNames;
            this.variables = variables;
        }
    }

    private static ImmutableMap.Builder<String, String> map() {
        return ImmutableMap.builder();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        velocityEngine.setProperty("file.resource.loader.path", "");
        velocityEngine.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
        velocityEngine.init();

        File baseDir =
            Paths.get(codegen.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toFile()
                .getParentFile()
                .getParentFile()
                .getParentFile();

        File templateDir = new File(baseDir, "/src/main/templates");
        File generatedDir = new File(baseDir, "/target/generated-sources");

        for (CombinatorialTemplate template : TEMPLATES) {
            for (String fileName : template.fileNames) {
                Template t = velocityEngine.getTemplate(new File(templateDir, fileName).getPath());

                for (Map<String, String> variables : template.variables) {
                    VelocityContext velocityContext = new VelocityContext();
                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                        velocityContext.put(entry.getKey(), entry.getValue());
                    }

                    String outputFileName = fileName.replace("_T_", variables.getOrDefault("boxedType", ""));
                    File outputFile = new File(generatedDir, outputFileName);
                    outputFile.getParentFile().mkdirs();

                    try (PrintWriter printWriter = new PrintWriter(outputFile)) {
                        t.merge(velocityContext, printWriter);
                    }
                }
            }
        }
    }

}
