package net.dongliu.vcdiff.vc;

import java.io.IOException;
import java.util.Arrays;
import net.dongliu.vcdiff.io.FixedByteArrayStream;
import net.dongliu.vcdiff.io.RandomAccessStream;
import net.dongliu.vcdiff.utils.IOUtils;

/**
 * Cache used for encoding/decoding addresses. Used to efficiently encode the addresses of COPY instructions
 *
 * @author dongliu
 */
public class AddressCache {

    private static final int defaultNearSize = 4;
    private static final int defaultSameSize = 3;

    private int nearSize;

    private int sameSize;

    /**
     * A "near" cache is an array with "s_near" slots, each containing an address used for encoding addresses nearby to previously encoded
     * addresses
     */
    private int[] near;

    private int nextNearSlot;

    /**
     * The same cache maintains a hash table of recent addresses used for repeated encoding of the exact same address
     */
    private int[] same;

    private RandomAccessStream addressStream;

    public AddressCache() {
        this(defaultNearSize, defaultSameSize);
    }

    public AddressCache(int nearSize, int sameSize) {
        this.nearSize = nearSize;
        this.sameSize = sameSize;
        near = new int[nearSize];
        same = new int[sameSize * 256];
    }

    public void reset(byte[] addresses) {
        nextNearSlot = 0;
        Arrays.fill(near, 0);
        Arrays.fill(same, 0);

        addressStream = new FixedByteArrayStream(addresses, true);
    }

    /**
     * @param here
     *            the current location in the target data
     * @param mode
     * @return
     * @throws IOException
     */
    public int decodeAddress(int here, short mode) throws IOException {
        int address;

        if (mode == 0) {
            // The address was encoded by itself as an integer
            address = IOUtils.readVarIntBE(addressStream);
        } else if (mode == 1) {
            // The address was encoded as the integer value "here - addr"
            address = here - IOUtils.readVarIntBE(addressStream);
        } else if (mode <= nearSize + 1) {
            // Near modes: The "near modes" are in the range [2,nearSize+1]
            // The address was encoded as the integer value "addr - near[m-2]"
            address = near[mode - 2] + IOUtils.readVarIntBE(addressStream);
        } else if (mode <= nearSize + sameSize + 1) {
            // Same modes: are in the range [nearSize+2,nearSize+sameSize+1].
            // The address was encoded as a single byte b such that "addr == same[(mode - (s_near+2))*256 + b]".
            int m = mode - (nearSize + 2);
            address = same[(m * 256) + IOUtils.readByte(addressStream)];
        } else {
            // should never reach here.
            throw new RuntimeException("Should never reach here");
        }

        update(address);
        return address;
    }

    /**
     * update caches each time a COPY instruction is processed by the encoder or decoder.
     *
     * @param address
     */
    private void update(int address) {
        if (nearSize > 0) {
            near[nextNearSlot] = address;
            nextNearSlot = (nextNearSlot + 1) % nearSize;
        }
        if (sameSize > 0) {
            same[address % (sameSize * 256)] = address;
        }
    }

    public static short defaultLastMode() {
        return Vcdiff.VCD_FIRST_NEAR_MODE + defaultNearSize + defaultSameSize - 1;
    }

    /**
     * encode address
     *
     * @param address
     * @param hereAddress
     * @param encodedAddress
     *            the encode address. because java cannot pass reference, we use a wrapper
     * @return the mode
     */
    public short encodeAddress(int address, int hereAddress, int[] encodedAddress) {
        assert address >= 0;
        assert address < hereAddress;
        // Try using the SAME cache. This method, if available, always
        // results in the smallest encoding and takes priority over other modes.
        if (sameSize > 0) {
            int sameCachePos = address % (sameSize * 256);
            if (same[sameCachePos] == address) {
                // This is the only mode for which an single byte will be written
                // to the address stream instead of a variable-length integer.
                update(address);
                encodedAddress[0] = sameCachePos % 256;
                return (short) (firstSameMode() + (sameCachePos / 256)); // SAME mode
            }
        }

        // Try SELF mode
        short bestMode = Vcdiff.VCD_SELF_MODE;
        int bestEncodedAddress = address;

        // Try HERE mode
        int hereEncodedAddress = hereAddress - address;
        if (hereEncodedAddress < bestEncodedAddress) {
            bestMode = Vcdiff.VCD_HERE_MODE;
            bestEncodedAddress = hereEncodedAddress;
        }

        // Try using the NEAR cache
        for (int i = 0; i < nearSize; ++i) {
            int nearEncodedAddress = address - near[i];
            if ((nearEncodedAddress >= 0) && (nearEncodedAddress < bestEncodedAddress)) {
                bestMode = (short) (firstNearMode() + i);
                bestEncodedAddress = nearEncodedAddress;
            }
        }

        update(address);
        encodedAddress[0] = bestEncodedAddress;
        return bestMode;
    }

    private short firstNearMode() {
        return Vcdiff.VCD_FIRST_NEAR_MODE;
    }

    private short firstSameMode() {
        return (short) (Vcdiff.VCD_FIRST_NEAR_MODE + nearSize);
    }

    public boolean writeAddressAsVarIntForMode(short mode) {
        return !IsSameMode(mode);
    }

    public boolean IsSameMode(short mode) {
        return (mode >= firstSameMode()) && (mode <= LastMode());
    }

    public short LastMode() {
        return (short) (firstSameMode() + sameSize - 1);
    }
}
