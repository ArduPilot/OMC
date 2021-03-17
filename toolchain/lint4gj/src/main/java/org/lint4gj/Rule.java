/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.lint4gj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalIsPresent"})
class Rule implements Comparable<Rule> {

    private static class InvalidRule extends Linter {}

    private static final String MAINTAINER = "maintainer";
    private static final String SUPPRESS = "suppress";
    private static final List<String> ALL_LINTER_NAMES =
        Arrays.stream(Linter.getLinters()).map(Class::getSimpleName).collect(Collectors.toList());

    private final String basePath;
    private final String pattern;
    private final String originalPattern;
    private final Optional<Maintainer[]> maintainers;
    private final Optional<Set<Class<? extends Linter>>> suppressions;

    boolean encountered;

    private Rule(
            String basePath,
            String pattern,
            String originalPattern,
            Maintainer[] maintainers,
            Set<Class<? extends Linter>> suppressions) {
        this.basePath = basePath;
        this.pattern = pattern;
        this.originalPattern = originalPattern;
        this.maintainers = Optional.ofNullable(maintainers);
        this.suppressions = Optional.ofNullable(suppressions);
    }

    public String getBasePath() {
        return basePath;
    }

    public String getPattern() {
        return pattern;
    }

    public String getOriginalPattern() {
        return originalPattern;
    }

    public Optional<Maintainer[]> getMaintainers() {
        return maintainers;
    }

    public Optional<Set<Class<? extends Linter>>> getSuppressions() {
        return suppressions;
    }

    @Override
    public int compareTo(Rule other) {
        return pattern.compareTo(other.pattern);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(originalPattern);

        if (maintainers.isPresent()) {
            builder.append(":maintainer=")
                .append(Arrays.stream(maintainers.get()).map(Object::toString).collect(Collectors.joining(",")));
        }

        if (suppressions.isPresent()) {
            builder.append(":suppress=")
                .append(suppressions.get().stream().map(Class::getSimpleName).collect(Collectors.joining(",")));
        }

        return builder.toString();
    }

    public List<String> getMatchingFiles(String[] files) {
        if (files == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String file : files) {
            if (matchesFile(file)) {
                result.add(file);
            }
        }

        return result;
    }

    public boolean matchesFile(String file) {
        file = file.toLowerCase();
        int idx = file.indexOf(pattern.endsWith("*") ? pattern.substring(0, pattern.length() - 1) : pattern);
        if (idx > 0) {
            file = file.substring(idx);
        }

        if (pattern.equals(file)) {
            return true;
        }

        idx = Utils.indexOfDifference(file, pattern);
        return idx >= 0 && pattern.substring(idx).equals("*") && pattern.length() == (idx + 1);
    }

    // Returns a new rule that corresponds to the current rule, but adds maintainers and suppressions if the current
    // rule doesn't have its own maintainers or suppressions.
    static Rule amend(Rule rule, Maintainer[] maintainers, Set<Class<? extends Linter>> suppressions) {
        if (rule == null) {
            return null;
        }

        return new Rule(
            rule.basePath,
            rule.pattern,
            rule.originalPattern,
            rule.maintainers.orElse(maintainers),
            rule.suppressions.orElse(suppressions));
    }

    static Set<Rule> readRules(File file, ErrorHandler errorHandler) throws IOException {
        if (file.exists()) {
            File dir = file.getParentFile();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                Set<Rule> rules = new TreeSet<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    Rule rule = parseRule(dir, line, errorHandler);
                    if (rule != null) {
                        rules.add(rule);
                    }
                }

                return rules;
            }
        }

        return Collections.emptySet();
    }

    private static Rule parseRule(File dir, String text, ErrorHandler errorHandler) {
        String pattern;
        String maintainer = null;
        String suppressions = null;
        String[] parts = text.split(":");

        if (parts.length == 1) {
            pattern = new File("/" + text).getPath().replace('\\', '/');
        } else if (parts.length == 2) {
            pattern = new File("/" + parts[0]).getPath().replace('\\', '/');
            maintainer = parseParameter(parts[1], MAINTAINER);
            suppressions = parseParameter(parts[1], SUPPRESS);
        } else {
            pattern = new File("/" + parts[0]).getPath().replace('\\', '/');
            maintainer = parseParameter(parts[1], MAINTAINER);

            if (maintainer == null) {
                maintainer = parseParameter(parts[2], MAINTAINER);
                suppressions = parseParameter(parts[1], SUPPRESS);
            } else {
                suppressions = parseParameter(parts[2], SUPPRESS);
            }
        }

        if (maintainer == null && suppressions == null) {
            errorHandler.handle(
                new InvalidRule(),
                "Invalid rule "
                    + text
                    + ": Specify at least one of these arguments: maintainer=<name0>,<name1> suppress=<linter0>,<linter1>");
            return null;
        }

        Set<Class<? extends Linter>> suppressedLinters = null;
        if (suppressions != null) {
            List<String> linterStrings =
                List.of(suppressions.split(",")).stream().map(String::trim).collect(Collectors.toList());

            for (String unknownLinter :
                linterStrings.stream().filter(l -> !ALL_LINTER_NAMES.contains(l)).collect(Collectors.toList())) {
                errorHandler.handle(
                    new InvalidRule(), "Invalid rule " + text + ": Unknown linter " + unknownLinter + ".");
            }

            suppressedLinters =
                Arrays.stream(Linter.getLinters())
                    .filter(l -> linterStrings.contains(l.getSimpleName()))
                    .collect(Collectors.toSet());
        }

        return new Rule(
            dir.getPath(),
            pattern.toLowerCase(),
            pattern,
            maintainer != null
                ? Arrays.stream(maintainer.split(",")).map(Maintainer::parse).toArray(Maintainer[]::new)
                : null,
            suppressedLinters);
    }

    private static String parseParameter(String text, String key) {
        text = text.trim();

        if (text.toLowerCase().startsWith(key)) {
            int start = key.length();

            do {
                if (text.charAt(start) == '=') {
                    ++start;
                    break;
                }

                if (Character.isWhitespace(text.charAt(start))) {
                    ++start;
                } else {
                    return null;
                }
            } while (start < text.length());

            String res = text.substring(start).trim();
            return res.isEmpty() ? null : res;
        }

        return null;
    }

}
