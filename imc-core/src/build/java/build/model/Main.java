/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model;

import build.model.ast.ClassNode;
import build.model.gen.InterfaceClassGenerator;
import build.model.gen.ModelClassGenerator;
import build.model.gen.NameHelper;
import build.model.gen.SnapshotClassGenerator;
import build.model.parse.Parser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final String[] IMPORTS =
        new String[] {
            "com.intel.missioncontrol.geometry.*",
            "com.intel.missioncontrol.geospatial.*",
            "com.intel.missioncontrol.project.*",
            "com.intel.missioncontrol.project.property.*",
            "com.intel.missioncontrol.project.hardware.*",
            "com.intel.missioncontrol.serialization.*",
            "java.time.Instant",
            "java.time.OffsetDateTime",
            "java.util.ArrayList",
            "java.util.List",
            "java.util.Set",
            "java.util.Objects",
            "java.util.UUID",
            "java.util.function.Function",
            "javafx.beans.InvalidationListener",
            "org.asyncfx.beans.property.*",
            "org.asyncfx.collections.*"
        };

    public static void main(String[] args) throws IOException, URISyntaxException {
        Path baseDir =
            Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParent()
                .getParent()
                .getParent();

        Path templatesDir = baseDir.resolve("src/main/templates");
        Path generatedDir = baseDir.resolve("build/generated/sources");

        List<Path> templateFiles =
            Files.walk(templatesDir).filter(f -> Files.isRegularFile(f)).collect(Collectors.toList());

        List<String> templateNames =
            templateFiles.stream().map(path -> path.toFile().getName().split("\\.")[0]).collect(Collectors.toList());

        List<ClassNode> classes = new ArrayList<>();
        for (int i = 0; i < templateFiles.size(); ++i) {
            classes.add(new Parser(templateNames.get(i), templateNames).parse(Files.readString(templateFiles.get(i))));
        }

        for (Path templateFile : templateFiles) {
            Path targetDir = generatedDir.resolve(templatesDir.relativize(templateFile).getParent());
            Files.createDirectories(targetDir);
            Files.walk(targetDir).filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);
        }

        for (int i = 0; i < templateFiles.size(); ++i) {
            Path targetDir = generatedDir.resolve(templatesDir.relativize(templateFiles.get(i)).getParent());

            ClassNode classNode = classes.get(i);
            Path filePath = targetDir.resolve(NameHelper.getInterfaceFileName(classNode));
            try (BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.toFile())))) {
                writer.write(
                    new InterfaceClassGenerator(
                            getPackageName(templatesDir, templateFiles.get(i)), IMPORTS, classNode, classes)
                        .toString());
            }

            filePath = targetDir.resolve(NameHelper.getSnapshotFileName(classNode));
            try (BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.toFile())))) {
                writer.write(
                    new SnapshotClassGenerator(
                            getPackageName(templatesDir, templateFiles.get(i)), IMPORTS, classNode, classes)
                        .toString());
            }

            if (classNode.isImmutable()) {
                continue;
            }

            filePath = targetDir.resolve(NameHelper.getModelFileName(classNode));
            try (BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath.toFile())))) {
                writer.write(
                    new ModelClassGenerator(
                            getPackageName(templatesDir, templateFiles.get(i)), IMPORTS, classNode, classes)
                        .toString());
            }
        }
    }

    private static String getPackageName(Path templatesDir, Path templateFile) {
        Path packageDir = templatesDir.relativize(templateFile).getParent();
        StringBuilder packageName = new StringBuilder();

        for (int i = 0; i < packageDir.getNameCount(); ++i) {
            if (i > 0) {
                packageName.append('.');
            }

            packageName.append(packageDir.getName(i));
        }

        return packageName.toString();
    }

}
