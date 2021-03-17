/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A label that can contain formatted text. In contrast to {@link TextFlow}, RichTextLabel exposes {@link
 * RichTextLabel#textProperty()} to facilitate data binding. Formatting is controlled with inline tags <b></b> (bold)
 * and <i></i> (italic). If this label is used to show user-generated text (for example: user names), the default
 * formatting tags should not be used. Instead, wrap the user-generated text in safe tags using {@link
 * RichTextLabel#bold(String)} or {@link RichTextLabel#italic(String)}. Safe tags contain non-printable characters that
 * cannot be entered by users via standard JavaFX controls. If the text contains a safe tag, default tags will be
 * ignored for formatting.
 */
@DefaultProperty("text")
public class RichTextLabel extends TextFlow {

    /** Wraps the string in safe bold tags and returns the wrapped string. */
    public static String bold(String text) {
        return BEGIN_BOLD + text + END_BOLD;
    }

    /** Wraps the string in safe italic tags and returns the wrapped string. */
    public static String italic(String text) {
        return BEGIN_ITALIC + text + END_ITALIC;
    }

    private static final String BEGIN_BOLD_SAFE = "\\u0000<b>";
    private static final String END_BOLD_SAFE = "\\u0000</b>";
    private static final String BEGIN_ITALIC_SAFE = "\\u0000<i>";
    private static final String END_ITALIC_SAFE = "\\u0000</i>";
    private static final String BEGIN_BOLD = "<b>";
    private static final String END_BOLD = "</b>";
    private static final String BEGIN_ITALIC = "<i>";
    private static final String END_ITALIC = "</i>";

    private static final String[] DELIMITERS = new String[] {BEGIN_BOLD, END_BOLD, BEGIN_ITALIC, END_ITALIC};

    private static final String[] SAFE_DELIMITERS =
        new String[] {BEGIN_BOLD_SAFE, END_BOLD_SAFE, BEGIN_ITALIC_SAFE, END_ITALIC_SAFE};

    private final StringProperty text =
        new SimpleStringProperty(this, "text", "") {
            @Override
            protected void invalidated() {
                textUpdated(get());
            }
        };

    public RichTextLabel() {
        this.getStyleClass().setAll("label");
    }

    public RichTextLabel(String text) {
        this.getStyleClass().setAll("label");
        textUpdated(text);
    }

    public StringProperty textProperty() {
        return text;
    }

    public final void setText(String text) {
        this.text.setValue(text);
    }

    public final String getText() {
        return text.getValue();
    }

    private void textUpdated(String text) {
        if (text == null) {
            getChildren().clear();
            return;
        }

        String[] tokens = split(text);
        List<Text> texts = new ArrayList<>();
        Text currentText = new Text();
        for (String token : tokens) {
            switch (token) {
            case BEGIN_BOLD:
                currentText.setStyle(currentText.getStyle().concat(";-fx-font-weight: bold"));
                break;
            case BEGIN_ITALIC:
                currentText.setStyle(currentText.getStyle().concat(";-fx-font-style: italic"));
                break;
            case END_BOLD:
            case END_ITALIC:
                break;
            default:
                currentText.setText(token);
                texts.add(currentText);
                currentText = new Text();
            }
        }

        getChildren().setAll(texts);
    }

    private boolean usesSafeDelimiters(String text) {
        for (String delimiter : SAFE_DELIMITERS) {
            int idx = text.indexOf(delimiter);
            if (idx >= 0) {
                return true;
            }
        }

        return false;
    }

    private String[] split(String text) {
        String[] delimiters = usesSafeDelimiters(text) ? SAFE_DELIMITERS : DELIMITERS;
        List<String> tokens = new ArrayList<>();
        while (!text.isEmpty()) {
            int idx = Integer.MAX_VALUE;
            int delimLength = 0;
            for (String delimiter : delimiters) {
                int i = text.indexOf(delimiter);
                if (i >= 0 && i < idx) {
                    idx = i;
                    delimLength = delimiter.length();
                }
            }

            if (idx < Integer.MAX_VALUE) {
                tokens.add(text.substring(0, idx));
                tokens.add(text.substring(idx, idx + delimLength));
                text = text.substring(idx + delimLength);
            } else {
                tokens.add(text);
                break;
            }
        }

        return tokens.toArray(new String[0]);
    }

}
