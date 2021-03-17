/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.rs232;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;

public class Rs232ParamsTest {

    private ConfigurationPropertiesStub configurationProperties;
    private Rs232Params params;

    @Before
    public void setUp() throws Exception {
        configurationProperties = new ConfigurationPropertiesStub();
        params = new Rs232Params("conAscTec", configurationProperties);
    }

    @Test
    public void createRs232ParamsWithConfigurationProperties() throws Exception {
        assertThat(params.isAscTec(), is(true));
        //assertThat(params.getBitRate(), is(SerialConfig.BR_921600));
        assertThat(params.getDataBits(), is(Rs232Params.DEFAULT_DATA_BITS));
        assertThat(params.getStopBits(), is(Rs232Params.DEFAULT_STOP_BITS));
        assertThat(params.getParity(), is(Rs232Params.DEFAULT_PARITY));
        assertThat(params.getPort(), is(Rs232Params.getDefaultPort()));
    }

    @Test
    public void propertiesAreStored_whenPortChanged() throws Exception {
        params.setPort("new port");

        assertThat(configurationProperties.wasPortStored, is(true));
        assertThat(configurationProperties.wasBaudRateStored, is(true));
        assertThat(configurationProperties.wasDataBitsStored, is(true));
        assertThat(configurationProperties.wasStopBitsStored, is(true));
        assertThat(configurationProperties.wasParityStored, is(true));
    }

    @Test
    public void propertiesAreNotStored_whenPortChangedToNull() throws Exception {
        params.setPort(null);

        assertThat(configurationProperties.wasPortStored, is(false));
        assertThat(configurationProperties.wasBaudRateStored, is(false));
        assertThat(configurationProperties.wasDataBitsStored, is(false));
        assertThat(configurationProperties.wasStopBitsStored, is(false));
        assertThat(configurationProperties.wasParityStored, is(false));
    }

    @Test
    public void propertiesAreNotStored_whenPortNotChanged() throws Exception {
        params.setPort(Rs232Params.getDefaultPort());

        assertThat(configurationProperties.wasPortStored, is(false));
        assertThat(configurationProperties.wasBaudRateStored, is(false));
        assertThat(configurationProperties.wasDataBitsStored, is(false));
        assertThat(configurationProperties.wasStopBitsStored, is(false));
        assertThat(configurationProperties.wasParityStored, is(false));
    }


    @Test
    public void propertiesAreStored_whenBaudRateChange() throws Exception {
        params.setBitRate(115200);

        assertThat(configurationProperties.wasPortStored, is(true));
        assertThat(configurationProperties.wasBaudRateStored, is(true));
        assertThat(configurationProperties.wasDataBitsStored, is(true));
        assertThat(configurationProperties.wasStopBitsStored, is(true));
        assertThat(configurationProperties.wasParityStored, is(true));
    }


    @Test
    public void propertiesAreNotStored_whenBaudRateNotChange() throws Exception {
        //params.setBitRate(SerialConfig.BR_921600);

        assertThat(configurationProperties.wasPortStored, is(false));
        assertThat(configurationProperties.wasBaudRateStored, is(false));
        assertThat(configurationProperties.wasDataBitsStored, is(false));
        assertThat(configurationProperties.wasStopBitsStored, is(false));
        assertThat(configurationProperties.wasParityStored, is(false));
    }

    @Test
    public void propertiesAreStored_whenDataBitsChange() throws Exception {
        params.setDataBits(16);

        assertThat(configurationProperties.wasPortStored, is(true));
        assertThat(configurationProperties.wasBaudRateStored, is(true));
        assertThat(configurationProperties.wasDataBitsStored, is(true));
        assertThat(configurationProperties.wasStopBitsStored, is(true));
        assertThat(configurationProperties.wasParityStored, is(true));
    }


    @Test
    public void propertiesAreNotStored_whenNotChangeDataBits() throws Exception {
        params.setDataBits(Rs232Params.DEFAULT_DATA_BITS);

        assertThat(configurationProperties.wasPortStored, is(false));
        assertThat(configurationProperties.wasBaudRateStored, is(false));
        assertThat(configurationProperties.wasDataBitsStored, is(false));
        assertThat(configurationProperties.wasStopBitsStored, is(false));
        assertThat(configurationProperties.wasParityStored, is(false));
    }

    @Test
    public void propertiesAreStored_whenStopBitsChange() throws Exception {
        params.setStopBits(1);

        assertThat(configurationProperties.wasPortStored, is(true));
        assertThat(configurationProperties.wasBaudRateStored, is(true));
        assertThat(configurationProperties.wasDataBitsStored, is(true));
        assertThat(configurationProperties.wasStopBitsStored, is(true));
        assertThat(configurationProperties.wasParityStored, is(true));
    }


    @Test
    public void propertiesAreNotStored_whenStopBitsNotChange() throws Exception {
        params.setStopBits(Rs232Params.DEFAULT_STOP_BITS);

        assertThat(configurationProperties.wasPortStored, is(false));
        assertThat(configurationProperties.wasBaudRateStored, is(false));
        assertThat(configurationProperties.wasDataBitsStored, is(false));
        assertThat(configurationProperties.wasStopBitsStored, is(false));
        assertThat(configurationProperties.wasParityStored, is(false));
    }

    @Test
    public void propertiesAreStored_whenParityChanged() throws Exception {
        params.setParity(1);

        assertThat(configurationProperties.wasPortStored, is(true));
        assertThat(configurationProperties.wasBaudRateStored, is(true));
        assertThat(configurationProperties.wasDataBitsStored, is(true));
        assertThat(configurationProperties.wasStopBitsStored, is(true));
        assertThat(configurationProperties.wasParityStored, is(true));
    }


    @Test
    public void propertiesAreNotStored_whenParityNotChanged() throws Exception {
        params.setParity(Rs232Params.DEFAULT_PARITY);

        assertThat(configurationProperties.wasPortStored, is(false));
        assertThat(configurationProperties.wasBaudRateStored, is(false));
        assertThat(configurationProperties.wasDataBitsStored, is(false));
        assertThat(configurationProperties.wasStopBitsStored, is(false));
        assertThat(configurationProperties.wasParityStored, is(false));
    }

    @Test
    public void getListOfPortParameters_ThatCanBeConnectedTo() throws Exception {
        List<Rs232Params> rs232Params = Rs232Params.listDetectedPorts(
            () -> Stream.of("open port 1", "closed port", "open port 2"),
            new ConfigurationPropertiesStub(),
            p -> !"closed port".equals(p.getPort())
        );

        assertThat(rs232Params.size(), is(2));
        assertThat(rs232Params.stream().map(Rs232Params::getPort).collect(toList()), hasItems("open port 1", "open port 2"));
    }

    /*@Test
    public void conversionToSerialConfiguration() throws Exception {
        SerialConfig serialConfig = params.toSerialConfig();

        assertThat(serialConfig.getBitRate(), is(SerialConfig.BR_921600));
        assertThat(serialConfig.getDataBits(), is(3));
        assertThat(serialConfig.getStopBits(), is(Rs232Params.DEFAULT_STOP_BITS));
        assertThat(serialConfig.getParity(), is(Rs232Params.DEFAULT_PARITY));
        assertThat(serialConfig.getPortNameString(), is(Rs232Params.getDefaultPort()));
    }*/

    private static class ConfigurationPropertiesStub implements IConfigurationProperties {

        boolean wasPortStored;
        boolean wasBaudRateStored;
        boolean wasDataBitsStored;
        boolean wasStopBitsStored;
        boolean wasParityStored;

        @Override
        public String getPort(String key, String defaultValue) {
            return defaultValue;
        }

        @Override
        public void changePort(String key, String port) {
            wasPortStored = true;
        }

        @Override
        public int getBaudRate(String key, int defaultValue) {
            return defaultValue;
        }

        @Override
        public void changeBaudRate(String key, int baudRate) {
            wasBaudRateStored = true;
        }

        @Override
        public int getDataBits(String key, int defaultValue) {
            return defaultValue;
        }

        @Override
        public void changeDataBits(String key, int dataBits) {
            wasDataBitsStored = true;
        }

        @Override
        public int getStopBits(String key, int defaultValue) {
            return defaultValue;
        }

        @Override
        public void changeStopBits(String key, int stopBits) {
            wasStopBitsStored = true;
        }

        @Override
        public int getParity(String key, int defaultValue){
            return defaultValue;
        }

        @Override
        public void changeParity(String key, int parity) {
            wasParityStored = true;
        }
    }
}
