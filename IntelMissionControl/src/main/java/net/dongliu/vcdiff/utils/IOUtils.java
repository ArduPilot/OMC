package net.dongliu.vcdiff.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.dongliu.vcdiff.io.FixedByteArrayStream;
import net.dongliu.vcdiff.io.RandomAccessStream;

/**
 * IOUtils form vcdiff.
 *
 * @author dongliu
 */
public class IOUtils {

    /**
     * read N bytes from input stream. throw exception when not enough data in is.
     *
     * @param is
     * @param size
     * @throws IOException
     */
    public static byte[] readBytes(InputStream is, int size) throws IOException {
        byte[] data = new byte[size];
        int offset = 0;
        while (offset < size) {
            int readSize = is.read(data, offset, size - offset);
            if (readSize < 0) {
                // end of is
                throw new IndexOutOfBoundsException("Not enough data in inputStream.");
            }
            offset += readSize;
        }
        return data;
    }

    /**
     * @param randomAccessStream
     * @param size
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(RandomAccessStream randomAccessStream, int size) throws IOException {
        byte[] data = new byte[size];
        int offset = 0;
        while (offset < size) {
            int readSize = randomAccessStream.read(data, offset, size - offset);
            if (readSize < 0) {
                // end of is
                throw new IndexOutOfBoundsException("Not enough data in inputStream, require:" + (size - offset));
            }
            offset += readSize;
        }
        return data;
    }

    /**
     * 从stream中获得一个指定大小为length，从当前pos处开始的stream. 副作用：ss的position会增加length.
     *
     * @param randomAccessStream
     * @return
     * @throws IOException
     */
    public static RandomAccessStream slice(RandomAccessStream randomAccessStream, int length, boolean shareData) throws IOException {
        if (shareData) {
            return randomAccessStream.slice(length);
        } else {
            byte[] bytes = readBytes(randomAccessStream, length);
            return new FixedByteArrayStream(bytes, true);
        }
    }

    /**
     * copy bytes
     *
     * @param sourceStream
     * @param targetDataStream
     * @param size
     * @throws IOException
     */
    public static void copy(RandomAccessStream sourceStream, RandomAccessStream targetDataStream, int size) throws IOException {
        byte[] bytes = readBytes(sourceStream, size);
        targetDataStream.write(bytes, 0, bytes.length);
    }

    /**
     * close quietly.
     *
     * @param closeable
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignore) {
        }
    }

    /**
     * read one byte from input stream.
     *
     * @return
     * @throws IOException
     */
    public static int readByte(RandomAccessStream randomAccessStream) throws IOException {
        int b = randomAccessStream.read();
        if (b == -1) {
            // end of is
            throw new IndexOutOfBoundsException("Not enough data in inputStream.");
        }
        return b;
    }

    /**
     * read 7 bit encoded int.by big endian.
     *
     * @return
     * @throws IOException
     */
    public static int readVarIntBE(RandomAccessStream randomAccessStream) throws IOException {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            int b = randomAccessStream.read();
            if (b == -1) {
                throw new IndexOutOfBoundsException("Not enough data in inputStream.");
            }
            ret = (ret << 7) | (b & 0x7f);
            // end of int encoded.
            if ((b & 0x80) == 0) {
                return ret;
            }
        }
        // Still haven't seen a byte with the high bit unset? Dodgy data.
        throw new IOException("Invalid 7-bit encoded integer in stream.");
    }

    /**
     * read 7 bit encoded int.by big endian.
     *
     * @return
     * @throws IOException
     */
    public static int readVarIntBE(InputStream is) throws IOException {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            int b = is.read();
            if (b == -1) {
                throw new IndexOutOfBoundsException("Not enough data in inputStream.");
            }
            ret = (ret << 7) | (b & 0x7f);
            // end of int encoded.
            if ((b & 0x80) == 0) {
                return ret;
            }
        }
        // Still haven't seen a byte with the high bit unset? Dodgy data.
        throw new IOException("Invalid 7-bit encoded integer in stream.");
    }

    public static int varIntLen(int i) {
        boolean flag = false;
        int shift = 4 * 7;
        for (int idx = 4; idx >= 0; idx--, shift = shift - 7) {
            int j = i >>> shift;
            byte b = (byte) (j & 0x7f);
            if (b != 0) {
                return idx + 1;
            }
        }
        return 1;
    }

    /**
     * write int as var-len int
     */
    public static void writeVarIntBE(int i, OutputStream out) throws IOException {
        boolean flag = false;
        int shift = 4 * 7;
        for (int idx = 4; idx >= 0; idx--, shift = shift - 7) {
            int j = i >>> shift;
            int b = j & 0x7f;
            if (b != 0) {
                if (idx != 0) {
                    b = b ^ 0x80;
                }
                out.write(b);
                flag = true;
            } else if (flag) {
                if (idx != 0) {
                    b = 0x80;
                }
                out.write(b);
            }
        }
        if (!flag) {
            // zero
            out.write(0);
        }
    }

    // for checksum encode
    public static int varLongLength(long checksum_) {
        // TODO: to be implemented
        throw new UnsupportedOperationException();
    }

    // for checksum encode
    public static void writeVarLongBE(OutputStream out, long checksum_) {
        // TODO: to be implemented
        throw new UnsupportedOperationException();
    }

    public static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        out.flush();
        return out.toByteArray();
    }
}
