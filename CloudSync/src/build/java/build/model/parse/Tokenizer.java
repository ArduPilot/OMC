/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package build.model.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Splits a line of text into a token stream. */
public class Tokenizer {

    public static final String NO_TOKEN = "";

    private static final Pattern SPLITTER =
        Pattern.compile(
            "(\"(?:\\\\\"|[^\"])+\"|[+-]?\\d*\\.?\\d+[LlFfDd]?|\\w+|:|<|>|\\[|]|\\(|\\)|\\{|}|\\+|-|\\*|/|==|=|\\.|#)");

    private static final List<String> SYMBOLS =
        List.of("+", "-", "*", "/", "=", "==", "(", ")", "<", ">", "[", "]", "{", "}", ":", ".");

    private final List<String> tokens = new ArrayList<>();
    private int currentToken;

    public Tokenizer(String text) {
        Matcher matcher = SPLITTER.matcher(text);
        while (matcher.find() && matcher.groupCount() > 0) {
            tokens.add(matcher.group(0));
        }
    }

    public String get() {
        if (currentToken < tokens.size()) {
            return tokens.get(currentToken);
        }

        return NO_TOKEN;
    }

    public String peek() {
        if ((currentToken + 1) < tokens.size()) {
            return tokens.get(currentToken + 1);
        }

        return NO_TOKEN;
    }

    public String getAndConsume() {
        String token = get();
        consume();
        return token;
    }

    public String getIdentifier(int lineNumber) {
        String token = get();
        if (!isIdentifier(token)) {
            throw getException(lineNumber, "Expected identifier, but found '" + token + "'.");
        }

        return token;
    }

    public String getIdentifierAndConsume(int lineNumber) {
        String token = get();
        if (!isIdentifier(token)) {
            throw getException(lineNumber, "Expected identifier, but found '" + token + "'.");
        }

        consume();
        return token;
    }

    public void consume() {
        if (currentToken < tokens.size()) {
            currentToken++;
        } else {
            throw new RuntimeException("No tokens to consume.");
        }
    }

    public void consumeSymbol(String expectedSymbol, int lineNumber) {
        String token = get();
        if (!token.equals(expectedSymbol)) {
            throw getException(
                lineNumber,
                "Expected '" + expectedSymbol + "', but found '" + (token.isEmpty() ? "<null>" : token) + "'.");
        }

        consume();
    }

    public String consumeAndGetIdentifier(int lineNumber) {
        consume();
        String token = get();
        if (!isIdentifier(token)) {
            throw getException(lineNumber, "'" + token + "' is not a valid identifier.");
        }

        return token;
    }

    public String consumeAndGetSymbol(int lineNumber) {
        consume();
        String token = get();
        if (!isSymbol(token)) {
            throw getException(lineNumber, "'" + token + "' is not a valid symbol.");
        }

        return token;
    }

    public static boolean isIdentifier(String token) {
        if (token.isEmpty()) {
            return false;
        }

        if (!Character.isLetter(token.charAt(0))) {
            return false;
        }

        for (int i = 1; i < token.length(); ++i) {
            if (!Character.isLetterOrDigit(token.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isSymbol(String token) {
        return SYMBOLS.contains(token);
    }

    private RuntimeException getException(int lineNumber, String message) {
        return new RuntimeException("Line " + lineNumber + ": " + message);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Tokenizer:\n");

        for (int i = 0; i < tokens.size(); ++i) {
            stringBuilder.append(currentToken == i ? "--> " : "    ").append(tokens.get(i)).append("\n");
        }

        if (currentToken >= tokens.size()) {
            stringBuilder.append("--> <end>");
        }

        return stringBuilder.toString();
    }

}
