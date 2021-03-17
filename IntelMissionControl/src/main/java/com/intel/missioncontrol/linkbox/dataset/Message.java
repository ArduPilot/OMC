/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox.dataset;

import com.google.gson.annotations.SerializedName;
import com.intel.missioncontrol.linkbox.LinkBoxAlertLevel;

public class Message {
    @SerializedName("messageText")
    public String MessageText;

    @SerializedName("messageLevel")
    public String MessageLevel;

    public String getMessageText() {
        return MessageText;
    }

    public void setMessageText(String messageText) {
        MessageText = messageText;
    }

    public String getMessageLevel() {
        return MessageLevel;
    }

    public void setMessageLevel(String messageLevel) {
        MessageLevel = messageLevel;
    }

    @Override
    public String toString() {
        return "{ "
            + "\"messageText\" :"
            + "\""
            + MessageText
            + "\""
            + ", "
            + "\"messageLevel\" :"
            + "\""
            + MessageLevel
            + "\""
            + " }";
    }

    public LinkBoxAlertLevel getLinkBoxAlertLevel() {
        return LinkBoxAlertLevel.valueOf(MessageLevel.trim().toUpperCase());
    }
}
