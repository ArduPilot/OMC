/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        return buf.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }

    @Override
    public synchronized void reset() throws IOException {
        buf.reset();
    }

    @Override
    public int available() throws IOException {
        return buf.remaining();
    }

    @Override
    public synchronized void mark(int readlimit) {
        buf.mark();
    }

    @Override
    public long skip(long n) throws IOException {
        int remaining = available();
        if (remaining < n) {
            n = remaining;
        }

        buf.position(buf.position() + (int)n);
        return n;
    }

    @Override
    public boolean markSupported() {
        return true;
    }
}
