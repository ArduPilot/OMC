package net.dongliu.vcdiff.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Wraps a random access file.
 *
 * @author dongliu
 */
public class FileStream implements RandomAccessStream {

    private final boolean readOnly;
    private final RandomAccessFile raf;

    /**
     * Constructs a new RandomAccessFileSeekableSource.
     *
     * @param file
     * @throws FileNotFoundException
     */
    public FileStream(RandomAccessFile file) throws FileNotFoundException {
        this(file, false);
    }

    public FileStream(RandomAccessFile file, boolean readOnly) throws FileNotFoundException {
        if (file == null) {
            throw new NullPointerException();
        }
        this.raf = file;
        this.readOnly = readOnly;
    }

    public void seek(int pos) throws IOException {
        raf.seek(pos);
    }

    public int pos() throws IOException {
        return (int) raf.getFilePointer();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return raf.read(b, off, len);
    }

    @Override
    public int length() throws IOException {
        return (int) raf.length();
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }
        this.raf.write(data, offset, length);
    }

    @Override
    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    @Override
    public void write(byte b) throws IOException {
        if (readOnly) {
            throw new UnsupportedOperationException();
        }
        this.raf.write(b);
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public int read() throws IOException {
        return this.raf.read();
    }

    @Override
    public RandomAccessStream slice(int length) throws IOException {
        // use byteBuffer to slice.
        // this strategy is SPECIALLY for jVcdiff use.
        FileChannel fc = this.raf.getChannel();
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, this.raf.getFilePointer(), length);
        this.raf.seek(this.raf.getFilePointer() + length);
        return new FixedByteArrayStream(buffer);
    }

}
