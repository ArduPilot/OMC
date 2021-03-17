/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.protocol;

/** Former Base64 interface refactored to use java.util.Base64 */
public class Base64 {

    public static int DEFAULT = 0;

    public static String encodeString(String in) {
        String out = null;

        out = new String(java.util.Base64.getEncoder().encode(in.getBytes()));

        return out;
    }

    public static String encodeString(byte[] in) {
        String out = null;

        out = new String(java.util.Base64.getEncoder().encode(in));

        return out;
    }

    public static String decodeString(String in) {
        String out = new String(java.util.Base64.getDecoder().decode(in));

        return out;
    }

    public static byte[] decode(String in, int dEFAULT2) {
        byte[] out = java.util.Base64.getDecoder().decode(in);
        return out;
    }

}
