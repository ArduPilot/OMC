/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug;

public class UserNotificationHubSwing {

    private static final int DEFAULT_TEXT_LENGTH = 80;

    public static String wrapText(String passedStr) {
        return wrapText(passedStr, DEFAULT_TEXT_LENGTH);
    }

    // https://community.oracle.com/thread/2091436?start=0&tstart=0
    public static String wrapText(String passedStr, final int LINE_LENGTH) {

        /**
         * ******************************************************************* This method solves the problem of
         * JOptionPanes not automatically * wrapping long lines of text. Set the LINE_LENGTH constant to * the length
         * you want each line to be. A newline character will be * inserted at the first whitespace after each character
         * located at * the LINE_LENGTH postition. * * Known problem: * - Traditional double-whitespace that appears
         * between sentences * can sometimes result in lines starting with whitespace, causing * the left edge of the
         * text to look jagged. This can be "fixed" * by only hitting the spacebar once between sentences. *
         * *******************************************************************
         */

        // Constant to indicate the length of each line.
        // final short LINE_LENGTH = 80;

        // Variable to indicate the next carriage-return position.
        int nextCR = LINE_LENGTH;

        // Create a StringBuffer copy of the string to wrap (passedStr).
        StringBuffer newStr = new StringBuffer(passedStr);

        // Read entire string from beginning to end.
        for (int i = 0; i < newStr.length(); i++) {
            // Push back nextCR another LINE_LENGTH for all newlines found.
            if (newStr.charAt(i) == '\n' || newStr.charAt(i) == '\r') {
                nextCR = i + LINE_LENGTH;
            }

            // Insert carriage-return at whitespace on or after nextCR.
            if ((newStr.charAt(i) == ' ' || newStr.charAt(i) == '/' || newStr.charAt(i) == '\\') && i >= nextCR) {
                newStr.insert((i + 1), "\n");
                nextCR += LINE_LENGTH;
            }
        }

        return newStr.toString();
    }
}
