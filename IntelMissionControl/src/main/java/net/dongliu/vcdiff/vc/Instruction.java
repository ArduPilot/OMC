package net.dongliu.vcdiff.vc;

/**
 * vcdiff op instruction.
 *
 * @author dongliu
 */
public class Instruction {

    public static final byte TYPE_NO_OP = 0;
    public static final byte TYPE_ADD = 1;
    public static final byte TYPE_RUN = 2;
    public static final byte TYPE_COPY = 3;
    public static final byte TYPE_LAST_INSTRUCTION = TYPE_COPY;

    /**
     * type([0,3]) can be hold in one signed byte.
     */
    private byte ist;
    /**
     * size may be negative with byte, if used, convert it to short
     */
    private short size;
    /**
     * only used for copy instruction
     * <p/>
     * for default code table, the mode can be hold in one signed byte. however, if use a specify code table with large same_size and
     * near_size, the mode may need one unsigned char to hold, so we use short here
     */
    private short mode;

    public Instruction(byte type, short size, short mode) {
        this.ist = type;
        this.size = size;
        this.mode = mode;
    }

    public byte getIst() {
        return ist;
    }

    public short getSize() {
        return size;
    }

    public short getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instruction that = (Instruction) o;

        if (ist != that.ist) {
            return false;
        }
        if (mode != that.mode) {
            return false;
        }
        if (size != that.size) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) ist;
        result = 31 * result + (int) size;
        result = 31 * result + (int) mode;
        return result;
    }

    @Override
    public String toString() {
        return "Instruction{" + "ist=" + ist + ", size=" + size + ", mode=" + mode + '}';
    }
}
