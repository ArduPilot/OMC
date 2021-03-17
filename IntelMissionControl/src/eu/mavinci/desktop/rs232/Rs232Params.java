/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.rs232;

import static java.util.stream.Collectors.toList;

import com.intel.missioncontrol.settings.Rs232Settings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.helper.IPropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rs232Params implements IPropertyChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rs232Params.class);

    static Rs232Settings rs232Settings = DependencyInjector.getInstance().getInstanceOf(Rs232Settings.class);

    public static final IConfigurationProperties DEFAULT_CONFIGURATION_PROPERTIES =
        new IConfigurationProperties() {
            @Override
            public void changePort(String key, String port) {
                rs232Settings.getPortProperty(key).setValue(port);
            }

            @Override
            public String getPort(String key, String defaultValue) {
                return rs232Settings.getPortProperty(key, defaultValue).get();
            }

            @Override
            public int getBaudRate(String key, int defaultValue) {
                return rs232Settings.getBitRate(key, defaultValue);
            }

            @Override
            public void changeBaudRate(String key, int baudRate) {
                rs232Settings.getBitRateProperty(key).setValue(baudRate);
            }

            @Override
            public int getDataBits(String key, int defaultValue) {
                return rs232Settings.getDataBits(key, defaultValue);
            }

            @Override
            public void changeDataBits(String key, int dataBits) {
                rs232Settings.getDataBitsProperty(key).setValue(dataBits);
            }

            @Override
            public int getStopBits(String key, int defaultValue) {
                return rs232Settings.getStopBits(key, defaultValue);
            }

            @Override
            public void changeStopBits(String key, int stopBits) {
                rs232Settings.getStopBitsProperty(key).setValue(stopBits);
            }

            @Override
            public int getParity(String key, int defaultValue) {
                return rs232Settings.getParity(key, defaultValue);
            }

            @Override
            public void changeParity(String key, int parity) {
                rs232Settings.getParityProperty(key).setValue(parity);
            }
        };

    public static final PortsSource DEFAULT_PORTS_SOURCE =
        () ->
                Arrays.stream(new String[]{} )
        ;

    public enum StopBit {
        BITS1(0),
        BITS2(1);

        private final int value;

        StopBit(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            /**
             * Need to increase the value because of incorrect value of constants {@link Serialio.SerialConfig.ST_1BITS}
             * && {@link Serialio.SerialConfig.ST_2BITS}
             */
            return String.valueOf(value + 1);
        }

        public static StopBit getDefault() {
            return BITS1;
        }

        public static StopBit valueOf(int value) {
            return Arrays.stream(values())
                .filter(stopBit -> stopBit.value != value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Didn't find bit rate for value " + value));
        }

        @Override
        public String toString() {
            return getLabel();
        }
    }

    public enum Parity {
        NONE(0),
        EVEN(1),
        ODD(2),
        MARK(3),
        SPACE(4);

        private final int value;

        Parity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return name().toLowerCase();
        }

        public static Parity getDefault() {
            return NONE;
        }

        public static Parity valueOf(int value) {
            return Arrays.stream(values())
                .filter(parity -> parity.value != value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Didn't find parity for value " + value));
        }

        @Override
        public String toString() {
            return getLabel();
        }
    }

    public enum DataBits {
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8);

        private final int value;

        DataBits(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return String.valueOf(value);
        }

        public static DataBits getDefault() {
            return EIGHT;
        }

        public static DataBits valueOf(int value) {
            return Arrays.stream(values())
                .filter(parity -> parity.value != value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Didn't find data bits for value " + value));
        }

        @Override
        public String toString() {
            return getLabel();
        }
    }

    private static String DEFAULT_PORT;

    public static String getDefaultPort() {
        if (DEFAULT_PORT == null) {
            // dont use dependency injection here to access IVersionProvider, since it would break startup eventually
            DEFAULT_PORT = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 ? "COM1" : "/dev/ttyUSB0";
        }

        return DEFAULT_PORT;
    }

    public static final int DEFAULT_BIT_RATE = 460800;
    public static final int DEFAULT_DATA_BITS = DataBits.getDefault().getValue();
    public static final int DEFAULT_STOP_BITS = StopBit.getDefault().getValue();
    public static final int DEFAULT_PARITY = Parity.getDefault().getValue();

    public static final int MAX_BIT_RATE = 1000 * 1000;
    public static final int MIN_BIT_RATE = 1200;
    public static final Integer[] DATA_BITS =
        Arrays.stream(DataBits.values()).map(DataBits::getValue).toArray(Integer[]::new);
    public static final Integer[] STOP_BITS =
        Arrays.stream(StopBit.values()).map(StopBit::getValue).toArray(Integer[]::new);
    public static final Integer[] PARITY = Arrays.stream(Parity.values()).map(Parity::getValue).toArray(Integer[]::new);

    public static final Rs232Params conExtRTKbase = new Rs232Params("conExtRTKbase", DEFAULT_CONFIGURATION_PROPERTIES);
    public static final Rs232Params conOutputRTKcorr =
        new Rs232Params("conOutputRTKcorr", DEFAULT_CONFIGURATION_PROPERTIES);
    public static final Rs232Params conFlarm = new Rs232Params("conFlarm", DEFAULT_CONFIGURATION_PROPERTIES);
    public static final Rs232Params conAntennaTracker =
        new Rs232Params("conAntennaTracker", DEFAULT_CONFIGURATION_PROPERTIES);
    public static final Rs232Params conNMEA = new Rs232Params("conNMEA", DEFAULT_CONFIGURATION_PROPERTIES);
    public static final Rs232Params conAscTec = new Rs232Params("conAscTec", DEFAULT_CONFIGURATION_PROPERTIES);

    private final IConfigurationProperties configurationProperties;
    private final String key;

    private String port;
    private int bitRate;
    private int dataBits;
    private int stopBits;
    private int parity;
    private boolean isAscTec;
    private short planePin;

    public static List<Rs232Params> listDetectedPorts(
            PortsSource portsSource,
            IConfigurationProperties configurationProperties,
            Predicate<Rs232Params> isAvailable) {
        return portsSource
            .listPorts()
            .map(
                comPort -> {
                    Rs232Params params = new Rs232Params("conAscTec", configurationProperties);
                    params.port = comPort;
                    return params;
                })
            .filter(isAvailable)
            .collect(toList());
    }

    /*SerialConfig toSerialConfig() {
        SerialConfig serCfg = new SerialConfig();
        serCfg.setBitRate(getBitRate());
        serCfg.setDataBits(convertDataBitsToSerial());
        serCfg.setStopBits(getStopBits());
        serCfg.setParity(getParity());
        serCfg.setPortName(port);
        return serCfg;
    } */

    private int convertDataBitsToSerial() {
        return getDataBits() - 5;
    }

    public MSerialPort openPort() throws IOException {
        /*SerialPortLocal serialPort = new SerialPortLocal();
        serialPort.setConfig(toSerialConfig());
        try {
            serialPort.open();
        } catch (IOException e) {
            LOGGER.warn("Could not connect to serial port " + this, e);
            serialPort.close();
            throw e;
        }*/

        return new MSerialPort(null, this);
    }

    Rs232Params(String key, IConfigurationProperties configurationProperties) {
        this.key = key;
        this.configurationProperties = configurationProperties;
        // specific defaults for different HW param types
        isAscTec = "conAscTec".equals(key);
        if ("conFlarm".equals(key)) {
            bitRate = 19200;
        } else if ("conAntennaTracker".equals(key)) {
            bitRate = 19200;
        } else if (isAscTec) {
            bitRate = 921600;
        }

        load();
    }

    private void load() {
        port = configurationProperties.getPort(key, getDefaultPort());
        // bitRate = configurationProperties.getBaudRate(key, SerialConfig.BR_460800);
        dataBits = configurationProperties.getDataBits(key, DEFAULT_DATA_BITS);
        stopBits = configurationProperties.getStopBits(key, DEFAULT_STOP_BITS);
        parity = configurationProperties.getParity(key, DEFAULT_PARITY);
    }

    private void store() {
        configurationProperties.changePort(key, port);
        configurationProperties.changeBaudRate(key, bitRate);
        configurationProperties.changeDataBits(key, dataBits);
        configurationProperties.changeStopBits(key, stopBits);
        configurationProperties.changeParity(key, parity);
    }

    @Override
    public void propertyChanged(String key, String newValue) {
        load();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        if (port == null || port.equals(this.port)) {
            return;
        }

        this.port = port;
        store();
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        if (this.bitRate == bitRate) {
            return;
        }

        this.bitRate = bitRate;
        store();
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        if (this.dataBits == dataBits) {
            return;
        }

        this.dataBits = dataBits;
        store();
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        if (this.stopBits == stopBits) {
            return;
        }

        this.stopBits = stopBits;
        store();
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        if (this.parity == parity) {
            return;
        }

        this.parity = parity;
        store();
    }

    @Override
    public String toString() {
        return "SerialPort[port="
            + port
            + ",baud="
            + bitRate
            + ",data="
            + dataBits
            + ",stop="
            + stopBits
            + ",parity="
            + parity
            + "]";
    }

    public boolean isAscTec() {
        return isAscTec;
    }

    public void setIsAscTec(Boolean item) {
        isAscTec = item;
    }

    public short getPlanePin() {
        return planePin;
    }

    public void setPlanePin(short planePin) {
        this.planePin = planePin;
    }

    public static Rs232Params of(String key, String port, int bitRate, int dataBits, int stopBits, int parity) {
        Rs232Params rs232Params = new Rs232Params(key, DEFAULT_CONFIGURATION_PROPERTIES);

        rs232Params.setPort(port);
        rs232Params.setBitRate(bitRate);
        rs232Params.setDataBits(dataBits);
        rs232Params.setStopBits(stopBits);
        rs232Params.setParity(parity);

        return rs232Params;
    }
}
