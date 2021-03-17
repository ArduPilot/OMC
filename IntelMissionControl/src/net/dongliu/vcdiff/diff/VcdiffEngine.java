package net.dongliu.vcdiff.diff;

import java.io.IOException;
import java.io.OutputStream;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import net.dongliu.vcdiff.vc.CodeTableWriter;

/**
 * generate vcdiff
 *
 * @author dongliu
 */
public class VcdiffEngine {

    private Pointer sourcePointer;

    private int sourceSize;

    private BlockHash blockHash;

    private static final int MINIMUM_MATCH_SIZE = 32;

    public VcdiffEngine(Pointer pointer, int size) {
        this.sourcePointer = pointer;
        this.sourceSize = size;
        this.blockHash = null;
    }

    public void init() throws VcdiffEncodeException {
        this.blockHash = BlockHash.createDictionaryHash(this.sourcePointer, sourceSize);
    }

    /**
     * do vcdiff encode
     *
     * @param targetData
     *            the target file data
     * @param targetSize
     *            targetSize
     * @param lookForTargetMatches
     *            if use copy from target self
     * @param diff
     *            the diff file stream
     * @param coder
     * @throws IOException
     */
    public void encode(byte[] targetData, int targetSize, boolean lookForTargetMatches, OutputStream diff, CodeTableWriter coder)
            throws IOException, VcdiffEncodeException {
        // Do nothing for empty target
        if (targetSize == 0) {
            return;
        }

        // Special case for really small input
        if (targetSize < BlockHash.K_BLOCK_SIZE) {
            addUnmatchedRemainder(new Pointer(targetData), targetSize, coder);
            finishEncoding(targetSize, diff, coder);
            return;
        }

        RollingHash hasher = new RollingHash(BlockHash.K_BLOCK_SIZE);
        BlockHash targetHash = null;
        if (lookForTargetMatches) {
            // Check matches against previously encoded target data
            // in this same target window, as well as against the sourcePointer
            targetHash = BlockHash.createTargetHash(new Pointer(targetData), targetSize, sourceSize);
        }
        int startOfLastBlock = targetSize - BlockHash.K_BLOCK_SIZE;
        // Offset of next bytes in string to ADD if NOT copied (i.e., not found in
        // sourcePointer)
        Pointer nextEncode = new Pointer(targetData);
        // candidate_pos points to the start of the kBlockSize-byte block that may
        // begin a match with the sourcePointer or previously encoded target data.
        Pointer candidatePos = new Pointer(targetData);
        int hashValue = hasher.hash(candidatePos);
        while (true) {
            int bytesEncoded = encodeCopyForBestMatch(hashValue, candidatePos, nextEncode, targetSize - nextEncode.offset(), targetHash,
                    lookForTargetMatches, coder);

            if (bytesEncoded > 0) {
                // match found
                nextEncode = nextEncode.slice(bytesEncoded); // Advance past COPYed data
                candidatePos = nextEncode.copy();
                if (candidatePos.offset() > startOfLastBlock) {
                    break;
                }
                // candidate_pos has jumped ahead by bytes_encoded bytes, so UpdateHash
                // can't be used to calculate the hash value at its new position.
                hashValue = hasher.hash(candidatePos);
                if (lookForTargetMatches) {
                    // Update the target hash for the ADDed and COPYed data
                    targetHash.addAllBlocksThroughIndex(nextEncode.offset());
                }
            } else {
                // No match, or match is too small to be worth a COPY instruction.
                // advance one byte, and compare.
                if ((candidatePos.offset() + 1) > startOfLastBlock) {
                    break;
                }
                if (lookForTargetMatches) {
                    targetHash.addOneIndexHash(candidatePos.offset(), hashValue);
                }
                hashValue = hasher.updateHash(hashValue, candidatePos.get(0), candidatePos.get(BlockHash.K_BLOCK_SIZE));
                candidatePos.up();
            }
        }
        addUnmatchedRemainder(nextEncode.copy(), targetSize - nextEncode.offset(), coder);
        finishEncoding(targetSize, diff, coder);
    }

    /**
     * This helper function tries to find an appropriate match within hashed_dictionary for the block starting at the current target
     * position. If target_hash is not NULL, this function will also look for a match within the previously encoded target data.
     *
     * @return the number of bytes processed by both instructions. 0 If no appropriate match is found
     */
    private int encodeCopyForBestMatch(int hashValue, Pointer targetCandidate, Pointer unencodedTarget, int unencodedTargetSize,
            BlockHash targetLockHash, boolean lookForTargetMatches, CodeTableWriter coder) throws IOException, VcdiffEncodeException {
        BlockHash.Match bestMatch = new BlockHash.Match();

        blockHash.findBestMatch(hashValue, targetCandidate, unencodedTarget, unencodedTargetSize, bestMatch);
        // If target matching is enabled, then see if there is a better match
        // within the target data that has been encoded so far.
        if (lookForTargetMatches) {
            targetLockHash.findBestMatch(hashValue, targetCandidate, unencodedTarget, unencodedTargetSize, bestMatch);
        }
        if (bestMatch.getSize() < MINIMUM_MATCH_SIZE) {
            // not found match
            return 0;
        }
        if (bestMatch.getTargetOffset() > 0) {
            // Create an ADD instruction to encode all target bytes from the end of the last COPY match,
            // up to the beginning of this COPY match.
            coder.add(unencodedTarget, bestMatch.getTargetOffset());
        }
        coder.copy(bestMatch.getSourceOffset(), bestMatch.getSize());
        return bestMatch.getTargetOffset() + bestMatch.getSize();
    }

    /**
     * creates an ADD instruction to encode all target bytes from the end of the last COPY match, if any, through the end of the target
     * data.
     */
    private void addUnmatchedRemainder(Pointer unencodedTargetStart, int unencodedTargetSize, CodeTableWriter coder)
            throws IOException, VcdiffEncodeException {
        if (unencodedTargetSize > 0) {
            coder.add(unencodedTargetStart, unencodedTargetSize);
        }
    }

    private void finishEncoding(int targetSize, OutputStream diff, CodeTableWriter coder) throws IOException, VcdiffEncodeException {
        if (targetSize != coder.targetLength()) {
            throw new VcdiffEncodeException(
                    "target size " + targetSize + " does not match number of bytes processed:" + coder.targetLength());
        }
        coder.output(diff);
    }

    public int getSourceSize() {
        return sourceSize;
    }
}
