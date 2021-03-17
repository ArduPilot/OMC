/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.protocol;

import com.intel.missioncontrol.helper.Ensure;

public class MavinciTokenizer {

    private static final boolean isDelimArr[] = new boolean[65536];

    static {
        isDelimArr[ProtocolTokens.seppar.codePointAt(0)] = true;
        isDelimArr[ProtocolTokens.sbegin.codePointAt(0)] = true;
        isDelimArr[ProtocolTokens.send.codePointAt(0)] = true;
        isDelimArr[ProtocolTokens.separray.codePointAt(0)] = true;
        isDelimArr[ProtocolTokens.sepael.codePointAt(0)] = true;
    }

    private int currentPosition;
    private int maxPosition;
    private String str;

    private int lastTokenLen;

    public MavinciTokenizer(String str) {
        this.str = str;
        currentPosition = 0;
        maxPosition = str.length();
        lastTokenLen = 0;
    }

    public String nextToken() throws Exception {
        if (currentPosition >= maxPosition) {
            lastTokenLen = 0;
            return null;
        } else {
            int position = currentPosition;
            while (position < maxPosition) {
                if (isDelimArr[str.charAt(position)]) {
                    break;
                }

                position++;
            }

            if (currentPosition == position) {
                position++;
            }

            String token = str.substring(currentPosition, position);
            currentPosition = position;
            lastTokenLen = token.length();
            return token;
        }
    }

    public void undoReadCurrentToken() throws Exception {
        if (lastTokenLen == 0) {
            throw new Exception("Unable to undo twice!");
        }

        currentPosition -= lastTokenLen;
        lastTokenLen = 0;
    }

    public boolean hasMoreTokens() {
        return currentPosition < maxPosition;
    }

    public void readSpecialToken(char token) throws Exception {
        lastTokenLen = 1;
        if (currentPosition >= maxPosition || str.charAt(currentPosition) != token) {
            // currentPosition++;
            throw new Exception(
                (currentPosition)
                    + " "
                    + maxPosition
                    + "Read unexpected Token from Stream: \'"
                    + str.charAt(currentPosition)
                    + "\'+ expected \'"
                    + token
                    + "\'");
        }

        currentPosition++;
    }

    public void readSpecialToken(String token) throws Exception {
        String nextToken = nextToken();
        Ensure.notNull(nextToken, "nextToken");
        if (!nextToken.equals(token)) {
            throw new Exception("Read unexpected Token from Stream");
        }
    }

    public void skipCharacters(int charCount) throws Exception {
        currentPosition += charCount;
        lastTokenLen = charCount;
        if (currentPosition >= maxPosition) {
            throw new Exception("unable to skip somutch chars");
        }
    }

}
