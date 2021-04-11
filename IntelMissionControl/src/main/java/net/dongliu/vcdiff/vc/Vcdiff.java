package net.dongliu.vcdiff.vc;

/**
 * @author dongliu
 */
public class Vcdiff {
    public static final byte[] MAGIC_HEADER = new byte[] { (byte) ('V' | 0x80), (byte) ('C' | 0x80), (byte) ('D' | 0x80), };

    public static final byte VCD_SOURCE = 0x01;

    public static final byte VCD_TARGET = 0x02;
    // If this flag is set, the delta window includes an Adler32 checksum
    // of the target window data. Not part of the RFC draft standard.
    public static final byte VCD_CHECKSUM = 0x04;

    // vcdiff modes
    public static final short VCD_SELF_MODE = 0;
    public static final short VCD_HERE_MODE = 1;
    public static final short VCD_FIRST_NEAR_MODE = 2;
    public static final short VCD_MAX_MODES = 256;
}
