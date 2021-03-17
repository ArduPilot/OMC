package net.dongliu.vcdiff.vc;

import java.util.Arrays;

/**
 * the InstructionMap is somethings do reverse work of CodeTable, it get index by instruction.
 *
 * @author dongliu
 */
public class InstructionMap {

    /**
     * Used to find the single instruction's idx.
     */
    private final SingleInstructionMap singleInstructionMap;
    /**
     * Used to find the idx of a combined instruction.
     */
    private final CombinedInstructionMap combinedInstructionMap;

    public static final InstructionMap DEFAULT = new InstructionMap(CodeTable.Default, AddressCache.defaultLastMode());

    public InstructionMap(CodeTable codeTable, short maxMode) {
        this.singleInstructionMap = new SingleInstructionMap(Instruction.TYPE_LAST_INSTRUCTION + maxMode + 1,
                findInstMaxSize(codeTable.entries, 0));
        this.combinedInstructionMap = new CombinedInstructionMap(Instruction.TYPE_LAST_INSTRUCTION + maxMode + 1,
                findInstMaxSize(codeTable.entries, 1));
        // fill up singleInstructionMap
        for (short opcode = 0; opcode < CodeTable.CodeTableSize; ++opcode) {
            if (codeTable.entries[opcode][1].getIst() == Instruction.TYPE_NO_OP) {
                // If there is more than one opcode for the same inst, mode, and size,
                // then the lowest-numbered opcode will always be used by the encoder.
                singleInstructionMap.add(codeTable.entries[opcode][0], opcode);
            } else if (codeTable.entries[opcode][0].getIst() == Instruction.TYPE_NO_OP) {
                // An unusual case where inst1 == NO_OP and inst2 == ADD, RUN, or COPY.
                // This is valid under the standard, but unlikely to be used.
                singleInstructionMap.add(codeTable.entries[opcode][1], opcode);
            }
        }

        // Second pass to fill up combinedInstructionMap (depends on first pass)
        for (short opcode = 0; opcode < CodeTable.CodeTableSize; ++opcode) {
            if ((codeTable.entries[opcode][0].getIst() != Instruction.TYPE_NO_OP)
                    && (codeTable.entries[opcode][1].getIst() != Instruction.TYPE_NO_OP)) {
                // Double instruction. Find the corresponding single instruction opcode
                short singleOpcode = lookupSingleOpcode(codeTable.entries[opcode][0]);
                if (singleOpcode == -1) {
                    continue; // No single opcode found
                }
                combinedInstructionMap.add(singleOpcode, codeTable.entries[opcode][1], opcode);
            }
        }
    }

    /**
     * findInstMaxSize.
     *
     * @param idx
     *            can be 0 or 1
     * @return the max value of instruction's size field
     */
    private short findInstMaxSize(Instruction[][] instructions, int idx) {
        short maxSize = 0;
        for (int i = 0; i < CodeTable.CodeTableSize; ++i) {
            if (instructions[i][idx].getSize() > maxSize) {
                maxSize = instructions[i][idx].getSize();
            }
        }
        return maxSize;
    }

    /**
     * find a single instruction's idx
     *
     * @return -1 if not found
     */
    public short lookupSingleOpcode(Instruction inst) {
        return singleInstructionMap.lookup(inst);
    }

    /**
     * find a combined instruction entry's index, by the last instruction's idx and this instruction.
     *
     * @return the instruction's idx, -1 if not found
     */
    public short lookupCombinedOpcode(short lastOpcode, Instruction inst) {
        return combinedInstructionMap.lookup(lastOpcode, inst);
    }

    private static class SingleInstructionMap {
        private int numInstructionTypeModes;

        // The maximum value of a size1 element in code_table_data
        //
        private int maxSize;

        /**
         * the first index is inst + mode, the second index is size.
         */
        short[][] firstOpcodes;

        public SingleInstructionMap(int numInstsAndModes, int maxSize) {
            this.numInstructionTypeModes = numInstsAndModes;
            this.maxSize = maxSize;
            firstOpcodes = new short[numInstructionTypeModes][];
            for (int i = 0; i < numInstructionTypeModes; ++i) {
                // There must be at least (maxSize + 1) elements in firstOpcodes
                // because the element first_opcodes[maxSize] will be referenced.
                firstOpcodes[i] = new short[this.maxSize + 1];
                Arrays.fill(firstOpcodes[i], (short) -1);
            }
        }

        void add(Instruction inst, short opcode) {
            short opcodeSlot = firstOpcodes[inst.getIst() + inst.getMode()][inst.getSize()];
            if (opcodeSlot == -1) {
                firstOpcodes[inst.getIst() + inst.getMode()][inst.getSize()] = opcode;
            }
        }

        short lookup(Instruction inst) {
            int instMode = (inst.getIst() == Instruction.TYPE_COPY) ? (inst.getIst() + inst.getMode()) : inst.getIst();
            if (inst.getSize() > maxSize) {
                return -1;
            }
            // lookup specific-sized opcode
            return firstOpcodes[instMode][inst.getSize()];
        }
    }

    private static class CombinedInstructionMap {
        int numInstructionTypeModes;
        // The maximum value of a size2 element in code_table_data
        int maxSize;

        /**
         * the first index lastOpcode, the second index is inst + mode, the third index is size.
         */
        short[][][] secondOpcodes = new short[CodeTable.CodeTableSize][][];

        private CombinedInstructionMap(int numInstructionsAndModes, int maxSize) {
            this.numInstructionTypeModes = numInstructionsAndModes;
            this.maxSize = maxSize;
        }

        private void add(short firstOpcode, Instruction inst, short secondOpcode) {
            if (secondOpcodes[firstOpcode] == null) {
                secondOpcodes[firstOpcode] = new short[numInstructionTypeModes][];
            }
            short[][] instModeArray = secondOpcodes[firstOpcode];
            int instMode = inst.getIst() + inst.getMode();
            if (instModeArray[instMode] == null) {
                // There must be at least (maxSize + 1) elements in size_array
                // because the element size_array[maxSize] will be referenced.
                instModeArray[instMode] = new short[maxSize + 1];
                Arrays.fill(instModeArray[instMode], (short) -1);
            }
            short[] sizeArray = instModeArray[instMode];

            if (sizeArray[inst.getSize()] == -1) {
                sizeArray[inst.getSize()] = secondOpcode;
            }
        }

        short lookup(short firstOpcode, Instruction inst) {
            if (inst.getSize() > maxSize) {
                return -1;
            }
            short[][] instModeArray = secondOpcodes[firstOpcode];
            if (instModeArray == null) {
                return -1;
            }
            int instMode = (inst.getIst() == Instruction.TYPE_COPY) ? (inst.getIst() + inst.getMode()) : inst.getIst();
            short[] sizeArray = instModeArray[instMode];
            if (sizeArray == null) {
                return -1;
            }
            return sizeArray[inst.getSize()];
        }
    }
}
