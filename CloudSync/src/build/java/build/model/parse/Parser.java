/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.parse;

import build.model.ast.AttributeNode;
import build.model.ast.ClassNode;
import build.model.ast.CollectionNode;
import build.model.ast.EnumTypeNode;
import build.model.ast.FieldNode;
import build.model.ast.GeneratedTypeNode;
import build.model.ast.KnownTypeNode;
import build.model.ast.PrimitiveTypeNode;
import build.model.ast.TypeNode;
import build.model.ast.UnknownTypeNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/** Parses a token stream into an abstract syntax tree. */
public class Parser {

    private static final List<String> KNOWN_TYPES = List.of("String", "OffsetDateTime", "Vec2", "Vec3", "Vec4");

    private final String className;
    private final List<String> generatedClassNames;

    public Parser(String className, List<String> generatedClassNames) {
        this.className = className;
        this.generatedClassNames = generatedClassNames;
    }

    public ClassNode parse(String text) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            int lineNumber = 1;
            String inheritedClass = null;
            boolean abstractBase = false;
            List<FieldNode> fields = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                Tokenizer tokenizer = new Tokenizer(line);

                if (!tokenizer.get().equals(Tokenizer.NO_TOKEN) && !tokenizer.get().startsWith("#")) {
                    if (tokenizer.get().equals("inherits") && !tokenizer.peek().equals(":")) {
                        tokenizer.consume();

                        if (tokenizer.get().equals("abstract")) {
                            tokenizer.consume();
                            abstractBase = true;
                        }

                        inheritedClass = tokenizer.getIdentifier(lineNumber);
                    } else {
                        fields.add(parseField(tokenizer, lineNumber));
                    }
                }

                ++lineNumber;
            }

            return new ClassNode(className, inheritedClass, abstractBase, fields);
        }
    }

    private FieldNode parseField(Tokenizer tokenizer, int lineNumber) {
        String fieldName = tokenizer.getIdentifierAndConsume(lineNumber);
        tokenizer.consumeSymbol(":", lineNumber);
        TypeNode typeNode = parseType(tokenizer, lineNumber);
        return new FieldNode(fieldName, typeNode);
    }

    private TypeNode parseType(Tokenizer tokenizer, int lineNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        while (tokenizer.get().equals(".") || Tokenizer.isIdentifier(tokenizer.get())) {
            stringBuilder.append(tokenizer.get());
            tokenizer.consume();
        }

        String outerType = stringBuilder.toString();
        TypeNode innerType = null;
        List<AttributeNode> attributes = null;

        if (tokenizer.get().equals("<")) {
            tokenizer.consume();
            innerType = parseType(tokenizer, lineNumber);
            tokenizer.consumeSymbol(">", lineNumber);
        }

        if (tokenizer.get().equals("(")) {
            tokenizer.consume();
            attributes = parseAttributeList(tokenizer, lineNumber);
            tokenizer.consumeSymbol(")", lineNumber);
        }

        if (innerType != null) {
            switch (outerType) {
            case "List":
            case "Set":
                return new CollectionNode(outerType, innerType, attributes);

            case "Enum":
                return new EnumTypeNode(innerType.getTypeName(), attributes);

            default:
                return new UnknownTypeNode(outerType + "<" + innerType + ">", attributes);
            }
        }

        if (KNOWN_TYPES.contains(outerType)) {
            return new KnownTypeNode(outerType);
        } else if (generatedClassNames.contains(outerType)) {
            return new GeneratedTypeNode(outerType);
        }

        switch (outerType) {
        case "boolean":
            return new PrimitiveTypeNode(outerType, "Boolean", attributes);
        case "byte":
            return new PrimitiveTypeNode(outerType, "Byte", attributes);
        case "short":
            return new PrimitiveTypeNode(outerType, "Short", attributes);
        case "int":
            return new PrimitiveTypeNode(outerType, "Integer", attributes);
        case "long":
            return new PrimitiveTypeNode(outerType, "Long", attributes);
        case "float":
            return new PrimitiveTypeNode(outerType, "Float", attributes);
        case "double":
            return new PrimitiveTypeNode(outerType, "Double", attributes);
        default:
            return new UnknownTypeNode(outerType, attributes);
        }
    }

    private List<AttributeNode> parseAttributeList(Tokenizer tokenizer, int lineNumber) {
        List<AttributeNode> list = new ArrayList<>();

        while (Tokenizer.isIdentifier(tokenizer.get())) {
            String key = tokenizer.getIdentifierAndConsume(lineNumber);
            tokenizer.consumeSymbol(":", lineNumber);
            String value = parseExpression(tokenizer, lineNumber);
            list.add(new AttributeNode(key, value));
        }

        return list;
    }

    private String parseExpression(Tokenizer tokenizer, int lineNumber) {
        StringBuilder stringBuilder = new StringBuilder();
        int angle = 0, round = 0, curly = 0, square = 0;
        boolean lastTokenWasIdentifier = false;

        while (true) {
            switch (tokenizer.get()) {
            case Tokenizer.NO_TOKEN:
                return stringBuilder.toString().trim();

            case ",":
                if (angle == 0 && round == 0 && curly == 0 && square == 0) {
                    tokenizer.consume();
                    return stringBuilder.toString().trim();
                }

                stringBuilder.append(tokenizer.getAndConsume());
                break;

            case "(":
                ++round;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            case ")":
                if (round == 0) {
                    return stringBuilder.toString().trim();
                }

                --round;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            case "<":
                ++angle;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            case ">":
                --angle;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            case "{":
                ++curly;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            case "}":
                --curly;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            case "[":
                ++square;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            case "]":
                --square;
                stringBuilder.append(tokenizer.getAndConsume());
                lastTokenWasIdentifier = false;
                break;

            default:
                if (Tokenizer.isIdentifier(tokenizer.get())) {
                    if (lastTokenWasIdentifier) {
                        stringBuilder.append(' ');
                    }

                    lastTokenWasIdentifier = true;
                } else {
                    lastTokenWasIdentifier = false;
                }

                stringBuilder.append(tokenizer.get());
                tokenizer.consume();
            }
        }
    }

}
