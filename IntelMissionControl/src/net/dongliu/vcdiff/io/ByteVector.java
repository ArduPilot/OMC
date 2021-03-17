package net.dongliu.vcdiff.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import net.dongliu.vcdiff.diff.Pointer;

/**
 * this may be instead by ByteArrayStream
 *
 * @author dongliu
 */
public class ByteVector extends OutputStream {
    private byte[] data;
    private int size;
    private static final int INIT_SIZE = 32;

    private static final float expandFactor = 2.0f;

    public ByteVector() {
        this.data = new byte[INIT_SIZE];
    }

    public int size() {
        return size;
    }

    public ByteVector push(byte b) {
        if (size + 1 > data.length) {
            expand(size + 1);
        }
        data[size++] = b;
        return this;
    }

    public ByteVector push(byte[] bytes) {
        push(bytes, 0, bytes.length);
        return this;
    }

    public ByteVector push(byte[] bytes, int offset, int len) {
        if (size + len > data.length) {
            expand(size + len);
        }
        System.arraycopy(bytes, offset, data, size, len);
        size += len;
        return this;
    }

    private void expand(int targetSize) {
        int newSize = data.length;
        while (newSize <= targetSize) {
            newSize = (int) (newSize * expandFactor);
        }
        byte[] newData = new byte[newSize];
        System.arraycopy(data, 0, newData, 0, size);
        this.data = newData;
    }

    public byte get(int i) {
        if (i >= size) {
            throw new BufferOverflowException();
        }
        return this.data[i];
    }

    public short getUnsigned(int i) {
        if (i >= size) {
            throw new BufferOverflowException();
        }
        return (short) (this.data[i] & 0xFF);
    }

    public ByteVector set(int i, byte b) {
        if (i >= size) {
            throw new BufferOverflowException();
        }
        this.data[i] = b;
        return this;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[size];
        System.arraycopy(data, 0, bytes, 0, size);
        return bytes;
    }

    public ByteVector set(int i, short s) {
        set(i, (byte) s);
        return this;
    }

    public ByteVector push(Pointer data, int size) {
        push(data.getData(), data.offset(), size);
        return this;
    }

    public boolean empty() {
        return this.size == 0;
    }

    public byte[] data() {
        return this.data;
    }

    public ByteVector clear() {
        this.size = 0;
        return this;
    }

    // methods for implement output stream
    @Override
    public void write(int b) throws IOException {
        push((byte) b);
    }

    @Override
    public void write(byte bytes[]) throws IOException {
        push(bytes);
    }

    @Override
    public void close() throws IOException {
        clear();
    }
}