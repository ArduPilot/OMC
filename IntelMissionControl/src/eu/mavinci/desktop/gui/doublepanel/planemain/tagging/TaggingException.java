/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.planemain.tagging;

/** @author Vladimir Iordanov */
public class TaggingException extends Exception {

    protected final String shortMessage;

    public TaggingException(String shortMessage, String message) {
        super(message);
        this.shortMessage = shortMessage;
    }

    public TaggingException(String shortMessage, String message, Throwable cause) {
        super(message, cause);
        this.shortMessage = shortMessage;
    }

    public String getShortMessage() {
        return shortMessage;
    }
}
