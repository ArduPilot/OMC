package net.dongliu.vcdiff.diff;

/**
 * @author dongliu
 */
public class Pointer {
    private int offset;
    private byte[] data;

    public Pointer(byte[] data) {
        this.data = data;
        this.offset = 0;
    }

    public Pointer(Pointer pointer, int offset) {
        this.data = pointer.data;
        this.offset = offset;
    }

    public Pointer(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    public byte get(int pos) {
        return data[offset + pos];
    }

    public static boolean compare(Pointer pointer1, Pointer pointer2, int size) {
        for (int i = 0; i < size; i++) {
            if (pointer1.data[pointer1.offset + i] != pointer2.data[pointer2.offset + i]) {
                return false;
            }
        }
        return true;
    }

    public Pointer slice(int offset) {
        return new Pointer(data, this.offset + offset);
    }

    public Pointer copy() {
        return new Pointer(data, this.offset);
    }

    public void down() {
        offset--;
    }

    public void up() {
        offset++;
    }

    public int offset() {
        return this.offset;
    }

    public void offset(int offset) {
        this.offset = offset();
    }

    public byte[] getData() {
        return this.data;
    }
}
