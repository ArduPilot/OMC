/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import eu.mavinci.core.helper.StringHelper;

public abstract class CDump extends CAFlightplanstatement implements IFlightplanStatement {

    protected String body = DEFAULT_BODY;

    public static final int MAX_BODY_LENGHT = 256;
    public static final String DEFAULT_BODY = "";

    public static final String logLineFirstElement = CPhotoLogLine.LINE_LOG_PREFIX + "DUMP";
    public static final String prefixLogLine = logLineFirstElement + ";";
    public static final String prefixFPhash = "FP-Hash:";

    public static String removeHashDumpAndCommentsFromXML(String fpXml) {
        return StringHelper.replaceAllCaseInsensitive(
                fpXml,
                "[\\s]*<"
                    + FMLReader.Tokens.DUMP
                    + ">"
                    + CDump.prefixFPhash
                    + "[0-9a-f]{32}</"
                    + FMLReader.Tokens.DUMP
                    + ">",
                "")
            .replaceAll("[\\s]*<!--[\\s\\S]*?-->", "");
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        if (body.length() > MAX_BODY_LENGHT) {
            body = body.substring(0, MAX_BODY_LENGHT);
        }

        if (body != null && !this.body.equals(body)) {
            this.body = body;
            informChangeListener();
        }
    }

    protected CDump(String body) {
        setBody(body);
    }

    protected CDump(String body, IFlightplanContainer parent) {
        super(parent);
        setBody(body);
    }

    public String toString() {
        return "CDump"; // TODO more useful name
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CDump) {
            CDump fp = (CDump)o;
            return body.equals(fp.body);
        }

        return false;
    }

}
