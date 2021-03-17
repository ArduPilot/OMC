/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.gen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue", "unused"})
class CodeBuilder {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private final StringBuilder stringBuilder = new StringBuilder();
    private int indentLevel;
    private boolean indentNextLine;

    CodeBuilder append(Object value) {
        if (indentNextLine) {
            indentNextLine = false;
            stringBuilder.append("    ".repeat(indentLevel));
        }

        stringBuilder.append(value);
        return this;
    }

    CodeBuilder append(String format, Object... values) {
        append(String.format(format, values));
        return this;
    }

    CodeBuilder appendLine(Object value) {
        return append(value).newLine();
    }

    CodeBuilder appendLine(String format, Object... values) {
        append(String.format(format, values));
        return newLine();
    }

    CodeBuilder appendLines(String lines) {
        try (BufferedReader reader = new BufferedReader(new StringReader(lines))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appendLine(line);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return this;
    }

    CodeBuilder appendIf(boolean condition, Object value) {
        if (condition) {
            append(value);
        }

        return this;
    }

    CodeBuilder appendLineIf(boolean condition, Object value) {
        if (condition) {
            appendLine(value);
        }

        return this;
    }

    CodeBuilder appendLineIf(boolean condition, String format, Object... values) {
        if (condition) {
            appendLine(format, values);
        }

        return this;
    }

    CodeBuilder append(CodeBuilder codeBuilder) {
        for (String line : codeBuilder.toString().split(NEW_LINE)) {
            appendLine(line);
        }

        return this;
    }

    CodeBuilder newLine() {
        indentNextLine = true;
        stringBuilder.append("\r\n");
        return this;
    }

    CodeBuilder indent() {
        if (++indentLevel == 1) {
            indentNextLine = true;
        }

        return this;
    }

    CodeBuilder unindent() {
        --indentLevel;
        return this;
    }

    CodeBuilder deleteLast(int count) {
        if (stringBuilder.charAt(stringBuilder.length() - 2) == '\r'
                && stringBuilder.charAt(stringBuilder.length() - 1) == '\n') {
            stringBuilder.delete(stringBuilder.length() - count - 2, stringBuilder.length());
            stringBuilder.append("\r\n");
        } else {
            stringBuilder.delete(stringBuilder.length() - count, stringBuilder.length());
        }

        return this;
    }

    boolean endsWith(String str) {
        if (str.length() > stringBuilder.length()) {
            return false;
        }

        return str.equals(stringBuilder.substring(stringBuilder.length() - str.length()));
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

}
