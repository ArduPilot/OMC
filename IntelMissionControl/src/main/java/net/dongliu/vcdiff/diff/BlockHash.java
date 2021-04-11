package net.dongliu.vcdiff.diff;

import java.util.Arrays;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

/**
 * A generic hash table which will be used to keep track of byte runs of size K_BLOCK_SIZE in both the incrementally processed target data
 * and the preprocessed source dictionary.
 *
 * @author dongliu
 */
public class BlockHash {
    // Block size; must be a power of two.
    public static final int K_BLOCK_SIZE = 16;

    private Pointer sourceData;

    private int sourceSize;

    /**
     * the hash table
     */
    private int[] hashTable;

    /**
     * hash chain for conflict solve
     */
    private int[] nextBlockTable;

    /**
     * the last element of hash chain
     */
    private int[] lastBlockTable;

    private int hashTableMask;

    private int startingOffset;

    private int lastBlockAdded;

    /**
     * max block num examined when conflicted
     */
    private static final int MAX_PROBES = 16;

    private static final int MAX_MATCHES_TO_CHECK = (K_BLOCK_SIZE >= 32) ? 32 : (32 * (32 / K_BLOCK_SIZE));

    public BlockHash(Pointer sourceData, int sourceSize, int startingOffset) {
        this.sourceData = sourceData;
        this.sourceSize = sourceSize;
        this.hashTableMask = 0;
        this.startingOffset = startingOffset;
        this.lastBlockAdded = -1;
    }

    public void init(boolean populateHashTable) throws VcdiffEncodeException {
        int tableSize = calcTableSize(this.sourceSize);
        // Since table_size is a power of 2, (table_size - 1) is a bit mask
        // containing all the bits below table_size.
        hashTableMask = tableSize - 1;
        this.hashTable = new int[tableSize];
        Arrays.fill(hashTable, -1);
        nextBlockTable = new int[numberOfBlocks()];
        Arrays.fill(nextBlockTable, -1);
        lastBlockTable = new int[numberOfBlocks()];
        Arrays.fill(lastBlockTable, -1);
        if (populateHashTable) {
            addAllBlocks();
        }
    }

    private int numberOfBlocks() {
        return sourceSize / K_BLOCK_SIZE;
    }

    public static BlockHash createDictionaryHash(Pointer dictionaryData, int dictionarySize) throws VcdiffEncodeException {
        BlockHash newDictionaryHash = new BlockHash(dictionaryData, dictionarySize, 0);
        newDictionaryHash.init(true);
        return newDictionaryHash;
    }

    public static BlockHash createTargetHash(Pointer targetData, int targetSize, int dictionarySize) throws VcdiffEncodeException {
        BlockHash newTargetHash = new BlockHash(targetData, targetSize, dictionarySize);
        newTargetHash.init(false);
        return newTargetHash;
    }

    private int calcTableSize(int sourceSize) throws VcdiffEncodeException {
        // Over allocate the hash table by making it the same size (in bytes) as the source data.
        int intSize = 4;
        int minSize = (sourceSize / intSize) + 1;
        int tableSize = 1;
        // Find the smallest power of 2 that is >= min_size, and assign
        // that value to table_size.
        while (tableSize < minSize) {
            tableSize <<= 1;
            // Guard against an infinite loop
            if (tableSize <= 0) {
                throw new VcdiffEncodeException("too large data size:" + sourceSize);
            }
        }
        return tableSize;
    }

    /**
     * If the hash value is already available from the rolling hash, call this function to save time.
     *
     * @param hashValue
     *            the hash value
     */
    public void addBlock(int hashValue) throws VcdiffEncodeException {
        // The initial value of last_block_added_ is -1.
        int blockNumber = lastBlockAdded + 1;
        int totalBlocks = sourceSize / K_BLOCK_SIZE; // round down
        if (blockNumber >= totalBlocks) {
            throw new VcdiffEncodeException("larger or equal than total block num:" + blockNumber);
        }
        if (nextBlockTable[blockNumber] != -1) {
            throw new VcdiffEncodeException("next block should be -1, but:" + nextBlockTable[blockNumber]);
        }
        int hashTableIndex = getHashTableIndex(hashValue);
        int firstMatchingBlock = hashTable[hashTableIndex];
        if (firstMatchingBlock < 0) {
            // This is the first entry with this hash value
            hashTable[hashTableIndex] = blockNumber;
            lastBlockTable[blockNumber] = blockNumber;
        } else {
            // Add this entry at the end of the chain of matching blocks
            int lastMatchingBlock = lastBlockTable[firstMatchingBlock];
            if (nextBlockTable[lastMatchingBlock] != -1) {
                throw new VcdiffEncodeException("next block should be -1, but:" + nextBlockTable[lastMatchingBlock]);
            }
            nextBlockTable[lastMatchingBlock] = blockNumber;
            lastBlockTable[firstMatchingBlock] = blockNumber;
        }
        lastBlockAdded = blockNumber;
    }

    private int getHashTableIndex(int hashValue) {
        return hashValue & hashTableMask;
    }

    public void addAllBlocks() throws VcdiffEncodeException {
        addAllBlocksThroughIndex(sourceSize);
    }

    /**
     * add all blocks till endIndex.
     *
     * @param endIndex
     *            the end index
     *
     * @throws VcdiffEncodeException
     */
    public void addAllBlocksThroughIndex(int endIndex) throws VcdiffEncodeException {
        if (endIndex > sourceSize) {
            throw new ArrayIndexOutOfBoundsException("exceed data size:" + endIndex);
        }

        int lastIndexAdded = lastBlockAdded * K_BLOCK_SIZE;
        if (endIndex <= lastIndexAdded) {
            throw new VcdiffEncodeException("must be larger than last added, which is:" + lastIndexAdded);
        }
        int endLimit = endIndex;

        // Don't allow reading any indices at or past source_size_.
        int lastLegalHashIndex = sourceSize - K_BLOCK_SIZE;
        if (endLimit > lastLegalHashIndex) {
            endLimit = lastLegalHashIndex + 1;
        }

        int begin = nextIndexToAdd();
        RollingHash rollingHash = new RollingHash(K_BLOCK_SIZE);
        while (begin < endLimit) {
            addBlock(rollingHash.hash(sourceData.slice(begin)));
            begin += K_BLOCK_SIZE;
        }
    }

    private int nextIndexToAdd() {
        return (lastBlockAdded + 1) * K_BLOCK_SIZE;
    }

    private boolean blockContentsMatch(Pointer block1, Pointer block2) {
        return Pointer.compare(block1, block2, K_BLOCK_SIZE);
    }

    private int skipNonMatchingBlocks(int blockNumber, Pointer block) {
        int probes = 0;
        while ((blockNumber >= 0) && !blockContentsMatch(block, sourceData.slice(blockNumber * K_BLOCK_SIZE))) {
            if (++probes > MAX_PROBES) {
                return -1; // Avoid too much chaining
            }
            blockNumber = nextBlockTable[blockNumber];
        }
        return blockNumber;
    }

    private int firstMatchingBlock(int hashValue, Pointer block) {
        int hash = getHashTableIndex(hashValue);
        return skipNonMatchingBlocks(hashTable[hash], block);
    }

    private int nextMatchingBlock(int blockNumber, Pointer block) throws VcdiffEncodeException {
        if (blockNumber >= numberOfBlocks()) {
            throw new VcdiffEncodeException("block number larger than block counts:" + blockNumber);
        }
        return skipNonMatchingBlocks(nextBlockTable[blockNumber], block);
    }

    /**
     * Returns the number of bytes to the left of source_match_start that match the corresponding bytes to the left of target_match_start.
     */
    private int matchingBytesToLeft(Pointer sourceMatchStart, Pointer targetMatchStart, int maxBytes) {
        Pointer sourcePtr = sourceMatchStart.copy();
        Pointer targetPtr = targetMatchStart.copy();
        int bytesFound = 0;
        while (bytesFound < maxBytes) {
            sourcePtr.down();
            targetPtr.down();
            if (sourcePtr.get(0) != targetPtr.get(0)) {
                break;
            }
            ++bytesFound;
        }
        return bytesFound;
    }

    /**
     * Returns the number of bytes starting at source_match_end that match the corresponding bytes starting at target_match_end.
     */
    private int matchingBytesToRight(Pointer sourceMatchStart, Pointer targetMatchStart, int maxBytes) {
        Pointer sourcePtr = sourceMatchStart.copy();
        Pointer targetPtr = targetMatchStart.copy();
        int bytesFound = 0;
        while ((bytesFound < maxBytes) && (sourcePtr.get(0) == targetPtr.get(0))) {
            ++bytesFound;
            sourcePtr.up();
            targetPtr.up();
        }
        return bytesFound;
    }

    public void findBestMatch(int hashValue, Pointer targetCandidate, Pointer target, int targetSize, Match bestMatch)
            throws VcdiffEncodeException {
        int matchCounter = 0;
        for (int blockNumber = firstMatchingBlock(hashValue, targetCandidate); (blockNumber >= 0)
                && ++matchCounter < MAX_MATCHES_TO_CHECK; blockNumber = nextMatchingBlock(blockNumber, targetCandidate)) {
            int sourceMatchOffset = blockNumber * K_BLOCK_SIZE;
            int sourceMatchEnd = sourceMatchOffset + K_BLOCK_SIZE;

            int targetMatchOffset = targetCandidate.offset() - target.offset();
            int targetMatchEnd = targetMatchOffset + K_BLOCK_SIZE;

            int matchSize = K_BLOCK_SIZE;
            // Extend match start towards beginning of unencoded data
            int limitBytesToLeft = Math.min(sourceMatchOffset, targetMatchOffset);
            int matchingBytesToLeft = matchingBytesToLeft(sourceData.slice(sourceMatchOffset), target.slice(targetMatchOffset),
                    limitBytesToLeft);
            sourceMatchOffset -= matchingBytesToLeft;
            targetMatchOffset -= matchingBytesToLeft;
            matchSize += matchingBytesToLeft;
            // Extend match end towards end of unencoded data
            int sourceBytesToRight = sourceSize - sourceMatchEnd;
            int targetBytesToRight = targetSize - targetMatchEnd;
            int limitBytesToRight = Math.min(sourceBytesToRight, targetBytesToRight);
            matchSize += matchingBytesToRight(sourceData.slice(sourceMatchEnd), target.slice(targetMatchEnd), limitBytesToRight);
            // Update in/out parameter if the best match found was better
            // than any match already stored in *best_match.
            bestMatch.replaceIfBetterMatch(matchSize, sourceMatchOffset + startingOffset, targetMatchOffset);
        }
    }

    public void addOneIndexHash(int index, int hashValue) throws VcdiffEncodeException {
        if (index == nextIndexToAdd()) {
            addBlock(hashValue);
        }
    }

    /**
     * a binary data match
     */
    public static class Match {
        private int size;
        /**
         * the offset from source beginning
         */
        private int sourceOffset;
        /**
         * the offset from target beginning. if valid, targetOffset = sourceOffset - source.len
         */
        private int targetOffset;

        public Match() {
            size = 0;
            sourceOffset = targetOffset = -1;
        }

        private void replaceIfBetterMatch(int size, int sourceOffset, int targetOffset) {
            if (size > this.size) {
                this.size = size;
                this.sourceOffset = sourceOffset;
                this.targetOffset = targetOffset;
            }
        }

        public int getSize() {
            return size;
        }

        public int getSourceOffset() {
            return sourceOffset;
        }

        public int getTargetOffset() {
            return targetOffset;
        }

    }
}
