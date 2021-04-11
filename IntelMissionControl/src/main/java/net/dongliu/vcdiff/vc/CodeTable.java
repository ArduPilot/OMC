package net.dongliu.vcdiff.vc;

import net.dongliu.vcdiff.utils.Misc;

/**
 * vcdiff instruction table.
 *
 * @author dongliu
 */
public class CodeTable {

    /**
     * Default code table specified in RFC 3284.
     */
    public static final CodeTable Default = BuildDefaultCodeTable();
    public static final short CodeTableSize = 256;

    /**
     * code table entries.
     */
    Instruction[][] entries;

    public CodeTable(byte[] bytes) {
        entries = initCodeTableEntries();
        for (int i = 0; i < CodeTableSize; i++) {
            entries[i][0] = new Instruction(bytes[i], Misc.b(bytes[i + 512]), Misc.b(bytes[i + 1024]));
            entries[i][1] = new Instruction(bytes[i + 256], Misc.b(bytes[i + 768]), Misc.b(bytes[i + 1280]));
        }
    }

    private CodeTable(Instruction[][] entries) {
        this.entries = entries;
    }

    /**
     * Builds the default code table specified in RFC 3284. Vcdiff itself defines a "default code table" in which s_near is 4 ands_same is
     * 3.
     * 
     * <pre>
     *   ----------------------------------------------------------------------
     *
     *        The default rfc3284 instruction table:
     *            (see RFC for the explanation)
     *
     *           TYPE      SIZE     MODE    TYPE     SIZE     MODE     INDEX
     *   --------------------------------------------------------------------
     *       1.  Run         0        0     Noop       0        0        0
     *       2.  Add    0, [1,17]     0     Noop       0        0      [1,18]
     *       3.  Copy   0, [4,18]     0     Noop       0        0     [19,34]
     *       4.  Copy   0, [4,18]     1     Noop       0        0     [35,50]
     *       5.  Copy   0, [4,18]     2     Noop       0        0     [51,66]
     *       6.  Copy   0, [4,18]     3     Noop       0        0     [67,82]
     *       7.  Copy   0, [4,18]     4     Noop       0        0     [83,98]
     *       8.  Copy   0, [4,18]     5     Noop       0        0     [99,114]
     *       9.  Copy   0, [4,18]     6     Noop       0        0    [115,130]
     *      10.  Copy   0, [4,18]     7     Noop       0        0    [131,146]
     *      11.  Copy   0, [4,18]     8     Noop       0        0    [147,162]
     *      12.  Add       [1,4]      0     Copy     [4,6]      0    [163,174]
     *      13.  Add       [1,4]      0     Copy     [4,6]      1    [175,186]
     *      14.  Add       [1,4]      0     Copy     [4,6]      2    [187,198]
     *      15.  Add       [1,4]      0     Copy     [4,6]      3    [199,210]
     *      16.  Add       [1,4]      0     Copy     [4,6]      4    [211,222]
     *      17.  Add       [1,4]      0     Copy     [4,6]      5    [223,234]
     *      18.  Add       [1,4]      0     Copy       4        6    [235,238]
     *      19.  Add       [1,4]      0     Copy       4        7    [239,242]
     *      20.  Add       [1,4]      0     Copy       4        8    [243,246]
     *      21.  Copy        4      [0,8]   Add        1        0    [247,255]
     *   --------------------------------------------------------------------
     * </pre>
     *
     * @return
     */
    private static CodeTable BuildDefaultCodeTable() {
        // Defaults are NoOps with size and mode 0.
        Instruction[][] entries = initCodeTableEntries();

        for (int i = 0; i < entries.length; i++) {
            entries[i] = new Instruction[2];
        }

        // Entry 0. RUN instruction
        entries[0][0] = new Instruction(Instruction.TYPE_RUN, (short) 0, (short) 0);
        entries[0][1] = new Instruction(Instruction.TYPE_NO_OP, (short) 0, (short) 0);

        // Entries 1-18. 18 single ADD instructions
        for (short i = 1; i <= 18; i++) {
            entries[i][0] = new Instruction(Instruction.TYPE_ADD, (short) (i - 1), (short) 0);
            entries[i][1] = new Instruction(Instruction.TYPE_NO_OP, (short) 0, (short) 0);
        }

        int index = 19;

        // Entries 19-162. single COPY instructions
        for (short mode = 0; mode < 9; mode++) {
            entries[index][0] = new Instruction(Instruction.TYPE_COPY, (short) 0, mode);
            entries[index++][1] = new Instruction(Instruction.TYPE_NO_OP, (short) 0, (short) 0);
            for (short size = 4; size <= 18; size++) {
                entries[index][0] = new Instruction(Instruction.TYPE_COPY, size, mode);
                entries[index++][1] = new Instruction(Instruction.TYPE_NO_OP, (short) 0, (short) 0);
            }
        }

        // Entries 163-234
        for (short mode = 0; mode <= 5; mode++) {
            for (short addSize = 1; addSize <= 4; addSize++) {
                for (short copySize = 4; copySize <= 6; copySize++) {
                    entries[index][0] = new Instruction(Instruction.TYPE_ADD, addSize, (short) 0);
                    entries[index++][1] = new Instruction(Instruction.TYPE_COPY, copySize, mode);
                }
            }
        }

        // Entries 235-246
        for (short mode = 6; mode <= 8; mode++) {
            for (short addSize = 1; addSize <= 4; addSize++) {
                entries[index][0] = new Instruction(Instruction.TYPE_ADD, addSize, (short) 0);
                entries[index++][1] = new Instruction(Instruction.TYPE_COPY, (short) 4, mode);
            }
        }

        // Entries 247-255
        for (short mode = 0; mode <= 8; mode++) {
            entries[index][0] = new Instruction(Instruction.TYPE_COPY, (short) 4, mode);
            entries[index++][1] = new Instruction(Instruction.TYPE_ADD, (short) 1, (short) 0);
        }

        return new CodeTable(entries);
    }

    private static Instruction[][] initCodeTableEntries() {
        Instruction[][] entries = new Instruction[CodeTableSize][2];

        for (int i = 0; i < entries.length; i++) {
            entries[i] = new Instruction[2];
        }
        return entries;
    }

    public byte[] getBytes() {
        byte[] ret = new byte[1536];
        for (int i = 0; i < CodeTableSize; i++) {
            ret[i] = entries[i][0].getIst();
            ret[i + 256] = entries[i][1].getIst();
            ret[i + 512] = (byte) entries[i][0].getSize();
            ret[i + 768] = (byte) entries[i][1].getSize();
            ret[i + 1024] = (byte) entries[i][0].getMode();
            ret[i + 1280] = (byte) entries[i][1].getMode();
        }
        return ret;
    }

    /**
     * get instruction by opcode, and idx(0, 1).
     */
    public Instruction get(int opcode, int idx) {
        return entries[opcode][idx];
    }

}
