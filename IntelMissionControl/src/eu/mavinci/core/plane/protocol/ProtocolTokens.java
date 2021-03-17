/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.protocol;

import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.Port;

public class ProtocolTokens {
    public static final String encoding = "ISO-8859-1";
    public static final String prefixBool = "B";
    public static final String prefixChar = "C";
    public static final String prefixDouble = "D";
    public static final String prefixFloat = "F";
    public static final String prefixInt = "I";
    public static final String prefixString = "S";

    public static final char prefixCodeBool = 'B';
    public static final char prefixCodeChar = 'C';
    public static final char prefixCodeDouble = 'D';
    public static final char prefixCodeFloat = 'F';
    public static final char prefixCodeInt = 'I';
    public static final char prefixCodeString = 'S';

    public static int PROTOCOLL_VERSION = 2;

    public static final String sbegin = "(";

    public static final String send = ")";

    public static final String sendableObjectsPackage = Port.class.getPackage().getName();

    public static final String sepael = "&";

    public static final String separray = "$";

    public static final String seppar = "!";

    public static final String tokenFalse = "0";

    public static final String tokenTrue = "1";

    public static final String mbegin = "#";
    public static final String mend = "\r\n";
    public static final char sep = ';';

    public static final String allSeperators = mbegin + sep + seppar + mend + sbegin + send + separray + sepael;

}
