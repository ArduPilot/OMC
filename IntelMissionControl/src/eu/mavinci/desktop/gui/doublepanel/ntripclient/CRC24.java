package eu.mavinci.desktop.gui.doublepanel.ntripclient;

/**
 * https://github.com/bcgit/bc-java/blob/master/pg/src/main/java/org/bouncycastle/bcpg/CRC24.java
 *
 * <p>Copyright (c) 2000-2015 The Legion of the Bouncy Castle Inc. (http://www.bouncycastle.org)
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class CRC24 {

    private int crc; // in the original code, this was initialized by 0x0b704ce

    public CRC24() {
        reset();
    }

    public void update(int b) {
        crc ^= (b) << (16);
        for (int i = 0; i < 8; i++) {
            crc <<= 1;
            if ((crc & 0x1000000) != 0) {
                crc ^= 0x01864cfb;
            }
        }
    }

    public int getValue() {
        return crc;
    }

    public void reset() {
        crc = 0;
    }
}
