package net.dongliu.vcdiff.io;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Wraps a byte array / byte buffer as a source / target.
 *
 * @author dongliu
 */
public class ByteArrayStream implements RandomAccessStream {

    private ByteBuffer buffer;

    private int maxSize;

    private static final int INIT_SIZE = 64;

    public ByteArrayStream() {
        this(INIT_SIZE);
    }

    public ByteArrayStream(int initSize) {
        this(ByteBuffer.allocate(initSize));
    }

    /**
     * Constructs a new ByteArraySeekableSource.
     */
    public ByteArrayStream(byte[] source) {
        this(ByteBuffer.wrap(source));
    }

    public ByteArrayStream(ByteBuffer bytebuffer) {
        this.buffer = bytebuffer;
        updateMaxSize();
    }

    @Override
    public void seek(int pos) throws IOException {
        if (pos < 0) {
            throw new IOException("Not a seekable pos, less than zero.");
        }
        ensureCapacity(pos);
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
        ensureCapacity(this.buffer.position() + length);
        this.buffer.put(data, offset, length);
        updateMaxSize();
    }

    @Override
    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    @Override
    public void write(byte b) {
        ensureCapacity(this.buffer.position() + 1);
        this.buffer.put(b);
        updateMaxSize();
    }

    @Override
    public int length() throws IOException {
        return this.maxSize;
    }

    @Override
    public boolean isReadOnly() {
        return false;
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

    private void ensureCapacity(int size) {
        if (this.buffer.limit() < size) {
            int newCapacity = this.buffer.limit() << 1;
            if (newCapacity < size) {
                newCapacity = size;
            }

            ByteBuffer newBuffer;
            if (this.buffer.isDirect()) {
                newBuffer = ByteBuffer.allocateDirect(newCapacity);
            } else {
                newBuffer = ByteBuffer.allocate(newCapacity);
            }

            int position = this.buffer.position();
            this.buffer.rewind();
            newBuffer.put(this.buffer);
            newBuffer.position(position);
            newBuffer.limit(newCapacity);
            this.buffer = newBuffer;
        }
    }

    private void updateMaxSize() {
        if (this.buffer.position() > maxSize) {
            maxSize = this.buffer.position();
        }
    }

    /**
     * copy internal data into byte array
     *
     * @return byte array
     */
    public byte[] toBytes() {
        this.buffer.position(0);
        byte[] data = new byte[this.maxSize];
        this.buffer.limit(this.maxSize);
        this.buffer.get(data);
        return data;
    }
}
