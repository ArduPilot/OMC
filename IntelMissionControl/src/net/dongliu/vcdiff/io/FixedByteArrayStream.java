package net.dongliu.vcdiff.io;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Wraps a byte array / byte buffer as a source, the buffer's size is fixed.
 *
 * @author dongliu
 */
public class FixedByteArrayStream implements RandomAccessStream {

    private ByteBuffer buffer;

    private boolean readOnly;

    /**
     * Constructs a new ByteArraySeekableSource.
     */
    public FixedByteArrayStream(byte[] source) {
        this(source, false);
    }

    /**
     * Constructs a new ByteArraySeekableSource.
     */
    public FixedByteArrayStream(byte[] source, boolean readOnly) {
        this.buffer = ByteBuffer.wrap(source);
        this.readOnly = readOnly;
    }

    public FixedByteArrayStream(ByteBuffer bytebuffer) {
        this.buffer = bytebuffer;
        this.readOnly = bytebuffer.isReadOnly();
    }

    @Override
    public void seek(int pos) throws IOException {
        if (pos < 0 || pos > this.buffer.limit()) {
            throw new IOException("Not a seekable pos, larger than length or less than zero.");
        }
        this.buffer.position(pos);
    }

    @Override
    public void close() throws IOException {
        // do nothing.
        this.buffer = null;
    }

    @Override
    public int pos() throws IOException {
        return this.buffer.position();
    }

    @Override
    public int read(byte[] data, int offset, int length) {

        if (!this.buffer.hasRemaining()) {
            return -1;
        }

        int byteRead;
        if (length > this.buffer.remaining()) {
            byteRead = this.buffer.remaining();
        } else {
            byteRead = length;
        }

        this.buffer.get(data, offset, byteRead);
        return byteRead;
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }
        this.buffer.put(data, offset, length);
    }

    @Override
    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    @Override
    public void write(byte b) {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }
        this.buffer.put(b);
    }

    @Override
    public int length() throws IOException {
        return this.buffer.limit();
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public int read() {
        try {
            return this.buffer.get() & 0xff;
        } catch (BufferUnderflowException e) {
            return -1;
        }
    }

    @Override
    public RandomAccessStream slice(int offset) {
        if (offset > this.buffer.remaining()) {
            throw new BufferUnderflowException();
        }
        int limit = this.buffer.limit();
        this.buffer.limit(this.buffer.position() + offset);
        ByteBuffer newBuffer = this.buffer.slice();
        this.buffer.limit(limit);
        this.buffer.position(this.buffer.position() + offset);
        return new ByteArrayStream(newBuffer);
    }
}