/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.FiniteQue;
import eu.mavinci.core.helper.StringHelper;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtcmParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtcmParser.class);

    private static final byte PACKAGE_TYPE_BYTE = (byte)0xD3;
    private static final int NUMBER_OF_CHECKSUM_BYTES = 3;
    private static final int NUMBER_OF_PACKAGE_TYPE_BYTES = 1;
    private static final int NUMBER_OF_PACKAGE_LENGTH_BYTES = 2;
    private static final int MIN_PACKAGE_LENGTH = 6;
    private static final int STATION_ARP_TYPE = 1005;
    private static final int STATION_ARP_AND_ANTENNA_HEIGHT = 1006;
    private static final int TEXT_STRING = 1029;
    private static final int ANTENNA_DESCRIPTION_AND_SETUP = 1008;
    private static final int ANTENNA_DESCRIPTOR = 1007;
    private static final int ANTENNA_ID_OFFSET = 7;
    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();

    public static class StatisticEntry {

        private final Clock clock;
        int packageCounter;
        FiniteQue<Long> que = new FiniteQue<>(20);

        public StatisticEntry(Clock clock) {
            this.clock = clock;
        }

        public synchronized int getReceivedPackagesNumber() {
            return packageCounter;
        }

        public synchronized void onPackageReceived() {
            packageCounter++;
            que.add(clock.millis());
        }

        public synchronized void reset() {
            que.clear();
            packageCounter = 0;
        }

        /** avg. time in sec between two packages */
        public synchronized double estimateRate() {
            if (que.size() < 2) {
                return Double.POSITIVE_INFINITY;
            }

            long delta = que.getFirst() - que.get(1);
            long recent = Math.max(que.getFirst(), clock.millis() - delta);
            return (recent - que.getLast()) / (que.size() - 1) / 1000.0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StatisticEntry that = (StatisticEntry)o;
            return packageCounter == that.packageCounter && Objects.equals(que, that.que);
        }

        @Override
        public int hashCode() {
            return Objects.hash(packageCounter, que);
        }

        public String toString() {
            if (que.isEmpty()) {
                return "---";
            }

            if (packageCounter <= 1) {
                return Integer.toString(packageCounter) + " @ ? sec";
            }

            return Integer.toString(packageCounter) + " @ " + StringHelper.round(estimateRate(), 1, true) + " sec";
        }
    }

    public interface IStatisticEntryProvider {
        StatisticEntry createEntryFor(int packageType);
    }

    public static class StatisticEntryProvider implements IStatisticEntryProvider {
        private final Clock clock = Clock.systemDefaultZone();

        @Override
        public StatisticEntry createEntryFor(int packageType) {
            return new StatisticEntry(clock);
        }
    }

    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.ntripclient.RtcmParser";
    private final IStatisticEntryProvider statisticEntryProvider;

    public RtcmParser(IStatisticEntryProvider statisticEntryProvider) {
        this.statisticEntryProvider = statisticEntryProvider;
        statistics.put(1004, statisticEntryProvider.createEntryFor(1004));
        statistics.put(1012, statisticEntryProvider.createEntryFor(1012));
        statistics.put(1005, statisticEntryProvider.createEntryFor(1005));
        statistics.put(1006, statisticEntryProvider.createEntryFor(1006));
        statistics.put(ANTENNA_DESCRIPTOR, statisticEntryProvider.createEntryFor(ANTENNA_DESCRIPTOR));
        statistics.put(
            ANTENNA_DESCRIPTION_AND_SETUP, statisticEntryProvider.createEntryFor(ANTENNA_DESCRIPTION_AND_SETUP));
    }

    public long sinceLastReceive() {
        return System.currentTimeMillis() - lastReceive;
    }

    public static final int inBufferSize = 5000;

    byte[] buff = new byte[inBufferSize];
    private int buffLevel = 0;

    public void resetAll() {
        resetReconnect();
        for (StatisticEntry stat : statistics.values()) {
            stat.reset();
        }
    }

    public void flushBuffer() {
        if (buffLevel <= 0) {
            return;
        }

        skipBytes(buff, 0, buffLevel);
        buffLevel = 0;
    }

    public void resetReconnect() {
        skipBytes(buff, 0, buffLevel);
        buffLevel = 0;
        totalBytes = 0;
        skippedBytes = 0;
        lastReceive = -1;
        seenRTCM = false;
    }

    long lastReceive = -1;

    public boolean addToBuffer(byte[] inBuffer, int offset, int len) {
        if (len <= 0) {
            return false;
        }

        lastReceive = System.currentTimeMillis();
        totalBytes += len;
        if (buffLevel + len > buff.length) {
            buffLevel = 0;
        }

        prepareBufferForProcessing(inBuffer, offset, len);
        return processInBuffer();
    }

    private void prepareBufferForProcessing(byte[] inBuffer, int offset, int length) {
        System.arraycopy(inBuffer, offset, buff, buffLevel, length);
        buffLevel += length;
    }

    boolean seenRTCM = false;

    long totalBytes = 0;
    long skippedBytes = 0;

    public long getBytesSkipped() {
        return skippedBytes;
    }

    public long getBytesTotal() {
        return totalBytes;
    }

    private boolean processInBuffer() {
        // search RTCM header
        int start = 0;
        while (start < buffLevel) {
            if (start + MIN_PACKAGE_LENGTH >= buff.length) { // System.out.println("OVERFLOW 1");
                skipBytes(buff, 0, buffLevel);
                buffLevel = 0; // buffer overflow!
                break; // package len minimal 6 byte
            }

            if (buff[start] == PACKAGE_TYPE_BYTE) {
                final int packageLength = packageLength(start);
                final int fullPackageLength = fullPackageLength(packageLength);
                if (start + fullPackageLength > buff.length) { // System.out.println("OVERFLOW 2");
                    skipBytes(buff, 0, buffLevel);
                    buffLevel = 0; // buffer overflow!
                    break; // package len minimal 6 byte
                }

                if (start + fullPackageLength > buffLevel) {
                    if (start == 0) {
                        break; // shortcut
                    }

                    skippedBytes += start;

                    // skipp unusable data
                    shiftBufferLeft(start, buffLevel - start);
                    break;
                }

                final byte[] computedCheckSum = computeCheckSum(start);
                final byte[] packageCheckSum = getCheckSumBytes(start);
                if (checkPackage(computedCheckSum, packageCheckSum)) {
                    skippedBytes += start;

                    byte[] msg = copyPackageContent(start);
                    if (!seenRTCM) {
                        seenFirstRTCM();
                        seenRTCM = true;
                    }

                    try {
                        processPackage(msg);
                    } catch (IOException e) {
                        LOGGER.warn("Problem to further processing rtk package", e);
                    }

                    shiftBufferLeft(start + fullPackageLength, buffLevel - (start + fullPackageLength));
                } else {
                    skippedBytes += start + 1;

                    // skipp unusable data
                    shiftBufferLeft(start + 1, buffLevel - (start + 1));
                }
            }

            start++;
        }

        return seenRTCM;
    }

    private void shiftBufferLeft(int offset, int length) {
        System.arraycopy(buff, offset, buff, 0, length);
        buffLevel = length;
    }

    private byte[] copyPackageContent(int start) {
        int packageLen = packageLength(start);
        byte[] packageContent = new byte[1 + fullPackageLength(packageLen)];
        System.arraycopy(buff, start, packageContent, 0, packageContent.length);
        return packageContent;
    }

    private int packageLength(int start) {
        return (buff[start + 1] & 0x03) << 8 | (buff[start + 2] & 0xFF);
    }

    private byte[] computeCheckSum(int start) {
        int packageLen = packageLength(start);
        CRC24 crc = new CRC24();
        for (int j = 0; j < packageLengthWithoutChecksum(packageLen); j++) {
            crc.update(buff[start + j] & 0xFF);
        }

        int c = crc.getValue();

        byte b0 = (byte)((c >> 16) & 0xFF);
        byte b1 = (byte)((c >> 8) & 0xFF);
        byte b2 = (byte)((c & 0xFF));
        return new byte[] {b0, b1, b2};
    }

    private byte[] getCheckSumBytes(int start) {
        int packageLen = packageLength(start);
        byte[] checkSumBytes = new byte[NUMBER_OF_CHECKSUM_BYTES];
        System.arraycopy(buff, start + packageLen + NUMBER_OF_CHECKSUM_BYTES, checkSumBytes, 0, checkSumBytes.length);
        return checkSumBytes;
    }

    private boolean checkPackage(byte[] computedCheckSum, byte[] packageCheckSum) {
        return Arrays.equals(computedCheckSum, packageCheckSum);
    }

    private int packageLengthWithoutChecksum(int packageLength) {
        return NUMBER_OF_PACKAGE_TYPE_BYTES + NUMBER_OF_PACKAGE_LENGTH_BYTES + packageLength;
    }

    private int fullPackageLength(int packageLength) {
        return packageLengthWithoutChecksum(packageLength) + NUMBER_OF_CHECKSUM_BYTES;
    }

    protected void seenFirstRTCM() {}

    private Map<Integer, StatisticEntry> statistics = new ConcurrentSkipListMap<>();

    public Map<Integer, StatisticEntry> getStatistics() {
        return statistics;
    }

    public static final int LENGTH_HEADER = 3;
    public static final int LENGTH_FOOTER = 3;

    public int lastStationID = -1;
    public volatile Position lastStationPos;
    public String antennaID;
    public String antennaSerialNo;
    public int antennaSetupID = -1;

    public int baseStationFilter = -1;
    public boolean baseStationFilterAll = true;
    public Vector<Integer> foundBaseStations = new Vector<Integer>();

    private void processPackage(final byte[] rtkPackage) throws IOException {
        final int packageType = (rtkPackage[3] & 0xFF) << 4 | ((rtkPackage[4] & 0xFF) >> 4);
        final int stationID = (rtkPackage[4] & 0x0F) << 8 | ((rtkPackage[5] & 0xFF));

        if (!foundBaseStations.contains(stationID)) {
            foundBaseStations.add(stationID);
        }

        if (baseStationFilter >= 0 && baseStationFilter != stationID) {
            return;
        }

        lastStationID = stationID;
        if (packageType == TEXT_STRING) {
            byte[] strB = new byte[rtkPackage.length - LENGTH_HEADER - 9 - LENGTH_FOOTER];
            for (int i = 0; i != strB.length; i++) {
                strB[i] = rtkPackage[i + LENGTH_HEADER + 9];
            }

            String str = new String(strB, "UTF-8");
            LOGGER.debug("RTK package type <1029> content: '{}'", str);
            sendRTCMmessageToUser(str);
        }

        if (packageType == STATION_ARP_TYPE || packageType == STATION_ARP_AND_ANTENNA_HEIGHT) {
            // bit 0 starts in byte rtkPackage[3]
            final long negMask = 0xFFFC0000; // for synthesizing 2-complement

            // ECEF-X bit 34 (+38)
            long ecefX =
                ((long)rtkPackage[7] & 0x3F) << 32
                    | ((long)rtkPackage[8] & 0xFF) << 24
                    | ((long)rtkPackage[9] & 0xFF) << 16
                    | ((long)rtkPackage[10] & 0xFF) << 8
                    | ((long)rtkPackage[11] & 0xFF);
            if ((rtkPackage[7] & 0x20) != 0) {
                // System.out.println("neg X");
                ecefX |= negMask;
            }

            // ECEF-Y bit 74 (+38)
            long ecefY =
                ((long)rtkPackage[12] & 0x3F) << 32
                    | ((long)rtkPackage[13] & 0xFF) << 24
                    | ((long)rtkPackage[14] & 0xFF) << 16
                    | ((long)rtkPackage[15] & 0xFF) << 8
                    | ((long)rtkPackage[16] & 0xFF);
            if ((rtkPackage[12] & 0x20) != 0) {
                // System.out.println("neg Y");
                ecefY |= negMask;
            }

            // ECEF-Z bit 114 (+38)
            long ecefZ =
                ((long)rtkPackage[17] & 0x3F) << 32
                    | ((long)rtkPackage[18] & 0xFF) << 24
                    | ((long)rtkPackage[19] & 0xFF) << 16
                    | ((long)rtkPackage[20] & 0xFF) << 8
                    | ((long)rtkPackage[21] & 0xFF);
            if ((rtkPackage[17] & 0x20) != 0) {
                // System.out.println("neg Z");
                ecefY |= negMask;
            }
            // System.out.println("ECEF: \t" + ecefX + " \t" + ecefY + " \t" + ecefZ);
            Vec4 ecef = new Vec4(ecefY, ecefZ, ecefX);
            ecef = ecef.multiply3(0.0001); // change from unit 10^-4m m to meter
            // System.out.println("ECEF: Vec m" + ecef);

            lastStationPos = globe.computePositionFromPoint(ecef);
        }

        if (packageType == ANTENNA_DESCRIPTOR) {
            int antennaIdLength = rtkPackage[6] & 0xFF;
            antennaID = new String(rtkPackage, ANTENNA_ID_OFFSET, antennaIdLength, "ISO8859-1");
            LOGGER.debug("RTK Antenna ID: [{}]", antennaID);
        }

        if (packageType == ANTENNA_DESCRIPTION_AND_SETUP) {
            int antennaIdLength = rtkPackage[6] & 0xFF;
            antennaID = new String(rtkPackage, ANTENNA_ID_OFFSET, antennaIdLength, "ISO8859-1");
            antennaSetupID = rtkPackage[6 + antennaIdLength + 1] & 0xFF;
            int antennaSerialNoLength = rtkPackage[6 + antennaIdLength + 2] & 0xFF;
            antennaSerialNo = new String(rtkPackage, 6 + antennaIdLength + 2 + 1, antennaSerialNoLength, "ISO8859-1");
            LOGGER.debug(
                "RTK Antenna ID: [{}] antennaSetup: [{}] antennaSerialNo: [{}]", antennaID, antennaID, antennaSerialNo);
        }

        updatePackageStatistics(packageType);

        sendRTCMmessage(rtkPackage, packageType);
    }

    public void updatePackageStatistics(int packageType) {
        statistics.computeIfAbsent(packageType, statisticEntryProvider::createEntryFor).onPackageReceived();
    }

    protected void sendRTCMmessage(byte[] msg, final int rtype) throws IOException {}

    public void sendRTCMmessageToUser(String message) {
        DependencyInjector.getInstance()
            .getInstanceOf(IApplicationContext.class)
            .addToast(
                Toast.of(ToastType.INFO)
                    .setText(
                        DependencyInjector.getInstance()
                                .getInstanceOf(ILanguageHelper.class)
                                .getString(KEY + ".RTCMmessage.title")
                            + ": "
                            + message)
                    .create());
    }

    public void skipBytes(byte[] bytes, int start, int len) {
        if (len > 0) {
            skippedBytes += len;
            String skipped = new String(bytes, start, len);
            LOGGER.info("skipped this text from RTK input port: {}", skipped);
        }
    }
}
