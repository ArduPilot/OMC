package net.dongliu.vcdiff;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.io.ByteArrayStream;
import net.dongliu.vcdiff.io.FileStream;
import net.dongliu.vcdiff.io.FixedByteArrayStream;
import net.dongliu.vcdiff.io.RandomAccessStream;
import net.dongliu.vcdiff.utils.IOUtils;
import net.dongliu.vcdiff.utils.Misc;
import net.dongliu.vcdiff.vc.AddressCache;
import net.dongliu.vcdiff.vc.CodeTable;
import net.dongliu.vcdiff.vc.Instruction;
import net.dongliu.vcdiff.vc.Vcdiff;

/**
 * vcdiff decode.
 *
 * @author dongliu
 */
public class VcdiffDecoder {

    private RandomAccessStream sourceStream;

    private InputStream patchStream;

    private RandomAccessStream targetStream;

    /**
     * code table
     */
    private CodeTable codeTable = CodeTable.Default;

    private AddressCache cache = new AddressCache(4, 3);

    public VcdiffDecoder(RandomAccessStream sourceStream, InputStream patchStream, RandomAccessStream targetStream) {
        this.sourceStream = sourceStream;
        this.patchStream = patchStream;
        this.targetStream = targetStream;
    }

    /**
     * Convenient static method for caller.Apply vcdiff decode file to originFile.
     *
     * @param sourceFile
     *            the old file.
     * @param patchFile
     *            the decode file.
     * @param targetFile
     *            the decode result file.
     * @throws IOException
     * @throws net.dongliu.vcdiff.exception.VcdiffDecodeException
     */
    public static void decode(File sourceFile, File patchFile, File targetFile) throws IOException, VcdiffDecodeException {
        try (RandomAccessStream sourceStream = new FileStream(new RandomAccessFile(sourceFile, "r"), true);
                InputStream patchStream = new FileInputStream(patchFile);
                RandomAccessStream targetStream = new FileStream(new RandomAccessFile(targetFile, "rw"))){
            decode(sourceStream, patchStream, targetStream);
        } 
    }

    /**
     * Convenient static method for caller.Apply vcdiff decode file to originFile.
     *
     * @param sourceStream
     *            the inputStream of source file.
     * @param patchStream
     *            the decode file stream, should be seekable.
     * @param targetStream
     *            the output stream of output file.
     * @throws IOException
     * @throws net.dongliu.vcdiff.exception.VcdiffDecodeException
     */
    public static void decode(RandomAccessStream sourceStream, InputStream patchStream, RandomAccessStream targetStream)
            throws IOException, VcdiffDecodeException {
        VcdiffDecoder decoder = new VcdiffDecoder(sourceStream, patchStream, targetStream);
        decoder.decode();
    }

    /**
     * do vcdiff decode.
     *
     * @throws IOException
     * @throws net.dongliu.vcdiff.exception.VcdiffDecodeException
     */
    public void decode() throws IOException, VcdiffDecodeException {
        readHeader();
        while (decodeWindow()) {
            ;
        }
    }

    private void readHeader() throws IOException, VcdiffDecodeException {
        byte[] magic = IOUtils.readBytes(patchStream, 4);
        // magic num.
        if (!Misc.ArrayEqual(magic, Vcdiff.MAGIC_HEADER, 3)) {
            // not vcdiff file.
            throw new VcdiffDecodeException("The file is not valid vcdiff file.");
        }
        if (magic[3] != 0) {
            // version num.for standard vcdiff file, is always 0.
            throw new UnsupportedOperationException("Unsupported vcdiff version.");
        }
        byte headerIndicator = (byte) patchStream.read();
        if ((headerIndicator & 1) != 0) {
            // secondary compress.
            throw new UnsupportedOperationException("Patch file using secondary compressors not supported.");
        }

        boolean customCodeTable = ((headerIndicator & 2) != 0);
        boolean applicationHeader = ((headerIndicator & 4) != 0);

        if ((headerIndicator & 0xf8) != 0) {
            // other bits should be zero.
            throw new VcdiffDecodeException("Invalid header indicator - bits 3-7 not all zero.");
        }

        // if has custom code table.
        if (customCodeTable) {
            // load custom code table
            readCodeTable();
        }

        // Ignore the application header if we have one.
        if (applicationHeader) {
            int appHeaderLength = IOUtils.readVarIntBE(patchStream);
            // skip bytes.
            IOUtils.readBytes(patchStream, appHeaderLength);
        }

    }

    /**
     * load custom code table.
     *
     * @throws net.dongliu.vcdiff.exception.VcdiffDecodeException
     * @throws IOException
     */
    private void readCodeTable() throws IOException, VcdiffDecodeException {
        int compressedTableLen = IOUtils.readVarIntBE(patchStream) - 2;
        int nearSize = patchStream.read();
        int sameSize = patchStream.read();
        byte[] compressedTableData = IOUtils.readBytes(patchStream, compressedTableLen);

        byte[] defaultTableData = CodeTable.Default.getBytes();

        RandomAccessStream tableOriginal = new FixedByteArrayStream(defaultTableData, true);
        InputStream tableDelta = new ByteArrayInputStream(compressedTableData);
        byte[] decompressedTableData = new byte[1536];
        RandomAccessStream tableOutput = new ByteArrayStream(decompressedTableData);
        VcdiffDecoder.decode(tableOriginal, tableDelta, tableOutput);
        if (tableOutput.pos() != 1536) {
            throw new VcdiffDecodeException("Compressed code table was incorrect size");
        }

        codeTable = new CodeTable(decompressedTableData);
        cache = new AddressCache(nearSize, sameSize);
    }

    private boolean decodeWindow() throws IOException, VcdiffDecodeException {

        int windowIndicator = patchStream.read();
        // finished.
        if (windowIndicator == -1) {
            return false;
        }

        RandomAccessStream sourceStream;

        int tempTargetStreamPos = -1;

        // xdelta3 uses an undocumented extra bit which indicates that there are
        // an extra 4 bytes at the end of the encoding for the window
        boolean hasAdler32Checksum = ((windowIndicator & 4) == 4);

        // Get rid of the checksum bit for the rest
        windowIndicator &= 0xfb;

        // Work out what the source data is, and detect invalid window indicators
        switch (windowIndicator) {
        // No source data used in this window
        case 0:
            sourceStream = null;
            break;
        // Source data comes from the original stream
        case 1:
            if (this.sourceStream == null) {
                throw new VcdiffDecodeException("Source stream required.");
            }
            sourceStream = this.sourceStream;
            break;
        // Source data comes from the target stream
        case 2:
            sourceStream = targetStream;
            tempTargetStreamPos = targetStream.pos();
            break;
        case 3:
        default:
            throw new VcdiffDecodeException("Invalid window indicator.");
        }

        // Read the source data, if any
        RandomAccessStream sourceData = null;
        int sourceLen = 0;
        // xdelta 有时生成的diff，sourceLen会大于实际可用的大小.
        int realSourceLen = 0;
        if (sourceStream != null) {
            sourceLen = IOUtils.readVarIntBE(patchStream);
            int sourcePos = IOUtils.readVarIntBE(patchStream);

            sourceStream.seek(sourcePos);

            realSourceLen = sourceLen;

            if (sourceLen + sourcePos > sourceStream.length()) {
                realSourceLen = sourceStream.length() - sourcePos;
            }

            sourceData = IOUtils.slice(sourceStream, realSourceLen, false);

            // restore the position the source stream if appropriate
            if (tempTargetStreamPos != -1) {
                targetStream.seek(tempTargetStreamPos);
            }
        }
        // sourceStream = null;

        // Length of the delta encoding
        IOUtils.readVarIntBE(patchStream);

        // Length of the target window.the actual size of the target window after decompression
        int targetLen = IOUtils.readVarIntBE(patchStream);

        // Delta_Indicator.
        int deltaIndicator = patchStream.read();
        if (deltaIndicator != 0) {
            throw new UnsupportedOperationException("Compressed delta sections not supported.");
        }

        byte[] targetData = new byte[targetLen];
        RandomAccessStream targetDataStream = new ByteArrayStream(targetData);

        // Length of data for ADDs and RUNs
        int addRunDataLen = IOUtils.readVarIntBE(patchStream);
        // Length of instructions and sizes
        int instructionsLen = IOUtils.readVarIntBE(patchStream);
        // Length of addresses for COPYs
        int addressesLen = IOUtils.readVarIntBE(patchStream);

        // If we've been given a checksum, we have to read it and we might as well
        int checksumInFile = 0;
        if (hasAdler32Checksum) {
            byte[] checksumBytes = IOUtils.readBytes(patchStream, 4);
            checksumInFile = (checksumBytes[0] << 24) | (checksumBytes[1] << 16) | (checksumBytes[2] << 8) | checksumBytes[3];
        }

        // Data section for ADDs and RUNs
        byte[] addRunData = IOUtils.readBytes(patchStream, addRunDataLen);
        int addRunDataIndex = 0;
        // Instructions and sizes section
        byte[] instructions = IOUtils.readBytes(patchStream, instructionsLen);
        // Addresses section for COPYs
        byte[] addresses = IOUtils.readBytes(patchStream, addressesLen);

        RandomAccessStream instructionStream = new FixedByteArrayStream(instructions, true);

        cache.reset(addresses);

        while (true) {
            int instructionIndex = instructionStream.read();
            if (instructionIndex == -1) {
                break;
            }

            for (int i = 0; i < 2; i++) {
                Instruction instruction = codeTable.get(instructionIndex, i);
                int size = instruction.getSize();
                // separated encoded size
                if (size == 0 && instruction.getIst() != Instruction.TYPE_NO_OP) {
                    size = IOUtils.readVarIntBE(instructionStream);
                }
                switch (instruction.getIst()) {
                case Instruction.TYPE_NO_OP:
                    break;
                case Instruction.TYPE_ADD:
                    targetDataStream.write(addRunData, addRunDataIndex, size);
                    addRunDataIndex += size;
                    break;
                case Instruction.TYPE_COPY:
                    int addr = cache.decodeAddress(targetDataStream.pos() + sourceLen, instruction.getMode());
                    if (sourceData != null && addr < realSourceLen) {
                        sourceData.seek(addr);
                        IOUtils.copy(sourceData, targetDataStream, size);
                    } else {
                        // Data is in target data, Get rid of the offset
                        addr -= sourceLen;
                        // Can we just ignore overlap issues?
                        if (addr + size < targetDataStream.pos()) {
                            targetDataStream.write(targetData, addr, size);
                        } else {
                            for (int j = 0; j < size; j++) {
                                targetDataStream.write(targetData[addr++]);
                            }
                        }
                    }
                    break;
                case Instruction.TYPE_RUN:
                    byte data = addRunData[addRunDataIndex++];
                    for (int j = 0; j < size; j++) {
                        targetDataStream.write(data);
                    }
                    break;
                default:
                    throw new VcdiffDecodeException("Invalid instruction type found.");
                }
            }
        }
        IOUtils.closeQuietly(targetDataStream);
        IOUtils.closeQuietly(sourceData);
        targetStream.write(targetData, 0, targetLen);

        if (hasAdler32Checksum) {
            // check sum
            // skip
            check(checksumInFile, targetData);
        }
        return true;
    }

    private void check(int checksumInFile, byte[] targetData) {
        // TODO: adler32 check.
    }
}
