package net.dongliu.vcdiff.utils;

/**
 *
 * @author dongliu
 */
public class Misc {

    /**
     * convert byte as unsigned value to short
     *
     * @param b
     *            the byte
     * @return short value ,always positive
     */
    public static short b(byte b) {
        return (short) (b & 0xFF);
    }

    public static boolean ArrayEqual(byte[] a, byte[] b, int size) {
        for (int i = 0; i < size; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
