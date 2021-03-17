/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import co.unruly.matchers.OptionalMatchers;
import gov.nasa.worldwind.geom.LatLon;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class NtripSourceTableEntryTest {

    @Test
    public void parseEmptyEntry() throws Exception {
        assertThat(NtripSourceTableEntry.parse(""), OptionalMatchers.empty());
    }

    @Test
    public void parseEntryWithoutSeparator() throws Exception {
        assertThat(NtripSourceTableEntry.parse("string-without-separator"), OptionalMatchers.empty());
    }

    @Test
    public void parseCasEntry() throws Exception {
        String line = "CAS;192.106.234.17;2101;NTRIPcaster/2.0.21;ASI - eGeos;0;ITA;40.65;16.07;0.0.0.0;0;http://euref-ip.asi.it";
        NtripSourceCas cas = new NtripSourceCas(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(cas));

        assertThat(cas.getHost(), is("192.106.234.17"));
        assertThat(cas.getPort(), is(2101));
        assertThat(cas.getIdentifier(), is("NTRIPcaster/2.0.21"));
        assertThat(cas.getOperator(), is("ASI - eGeos"));
        assertThat(cas.isNeedNmea(), is(false));
        assertThat(cas.getCountry(), is("ITA"));
        assertThat(cas.getLatLon(), is(LatLon.fromDegrees(40.65, 16.07)));
        assertThat(cas.getFallbackHost(), is("0.0.0.0"));
        assertThat(cas.getFallbackPort(), is(0));
    }

    @Test(expected = NumberFormatException.class)
    public void errorParseCasPort() throws Exception {
        String line = "CAS;192.106.234.17;WRONG_PORT;NTRIPcaster/2.0.21;ASI - eGeos;0;ITA;40.65;16.07;0.0.0.0;0;http://euref-ip.asi.it";
        NtripSourceTableEntry.parse(line);
//        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.empty());
    }

    @Test
    public void errorParseCasLatLon() throws Exception {
        String line = "CAS;192.106.234.17;2101;NTRIPcaster/2.0.21;ASI - eGeos;0;ITA;WRONG_LAT;WRONG_LON;0.0.0.0;0;http://euref-ip.asi.it";
        NtripSourceCas cas = new NtripSourceCas(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(cas));

        assertThat(cas.getHost(), is("192.106.234.17"));
        assertThat(cas.getPort(), is(2101));
        assertThat(cas.getIdentifier(), is("NTRIPcaster/2.0.21"));
        assertThat(cas.getOperator(), is("ASI - eGeos"));
        assertThat(cas.isNeedNmea(), is(false));
        assertThat(cas.getCountry(), is("ITA"));
        assertThat(cas.getLatLon(), is(nullValue()));
        assertThat(cas.getFallbackHost(), is(nullValue()));
        assertThat(cas.getFallbackPort(), is(80));
    }

    @Test
    public void errorParseCasFallbackPort() throws Exception {
        String line = "CAS;192.106.234.17;2101;NTRIPcaster/2.0.21;ASI - eGeos;0;ITA;40.65;16.07;0.0.0.0;FALLBACK-PORT;http://euref-ip.asi.it";
        NtripSourceCas cas = new NtripSourceCas(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(cas));

        assertThat(cas.getHost(), is("192.106.234.17"));
        assertThat(cas.getPort(), is(2101));
        assertThat(cas.getIdentifier(), is("NTRIPcaster/2.0.21"));
        assertThat(cas.getOperator(), is("ASI - eGeos"));
        assertThat(cas.isNeedNmea(), is(false));
        assertThat(cas.getCountry(), is("ITA"));
        assertThat(cas.getLatLon(), is(LatLon.fromDegrees(40.65, 16.07)));
        assertThat(cas.getFallbackHost(), is("0.0.0.0"));
        assertThat(cas.getFallbackPort(), is(80));
    }

    @Test
    public void parseNetEntry() throws Exception {
        String line = "NET;EUREF;EUREF;B;N;https://igs.bkg.bund.de/root_ftp/NTRIP/streams/streamlist_euref-ip.htm;https://igs.bkg.bund.de:443/root_ftp/EUREF/station/rnxskl/;http://register.rtcm-ntrip.org;none";
        NtripSourceNet net = new NtripSourceNet(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(net));

        assertThat(net.getIdentifier(), is("EUREF"));
        assertThat(net.getOperator(), is("EUREF"));
        assertThat(net.getAuthType(), is(AuthType.BASIC));
        assertThat(net.isFee(), is(false));
        assertThat(net.getUrlNetInfo(), is("https://igs.bkg.bund.de/root_ftp/NTRIP/streams/streamlist_euref-ip.htm"));
        assertThat(net.getUrlStreamInfo(), is("https://igs.bkg.bund.de:443/root_ftp/EUREF/station/rnxskl/"));
        assertThat(net.getUrlRegistration(), is("http://register.rtcm-ntrip.org"));
    }

    @Test
    public void parseNetWithoutAuth() throws Exception {
        String line = "NET;EUREF;EUREF;NO_AUTH;N;https://igs.bkg.bund.de/root_ftp/NTRIP/streams/streamlist_euref-ip.htm;https://igs.bkg.bund.de:443/root_ftp/EUREF/station/rnxskl/;http://register.rtcm-ntrip.org;none";
        NtripSourceNet net = new NtripSourceNet(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(net));

        assertThat(net.getIdentifier(), is("EUREF"));
        assertThat(net.getOperator(), is("EUREF"));
        assertThat(net.getAuthType(), is(nullValue()));
        assertThat(net.isFee(), is(false));
        assertThat(net.getUrlNetInfo(), is("https://igs.bkg.bund.de/root_ftp/NTRIP/streams/streamlist_euref-ip.htm"));
        assertThat(net.getUrlStreamInfo(), is("https://igs.bkg.bund.de:443/root_ftp/EUREF/station/rnxskl/"));
        assertThat(net.getUrlRegistration(), is("http://register.rtcm-ntrip.org"));
    }

    @Test
    public void parseStrEntry() throws Exception {
        String line = "STR;ACOR0;Coruna;RTCM 3.1;1004(1),1006(30),1008(30),1019(120),1033(30);2;GPS;EUREF;ESP;43.36;351.60;0;0;LEICA GRX1200PRO;none;B;N;3000;ergnss-ip.ign.es:2101/ACOR0(1)";
        NtripSourceStr str = new NtripSourceStr(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(str));

        assertThat(str.getMountPoint(), is("ACOR0"));
        assertThat(str.getIdentifier(), is("Coruna"));
        assertThat(str.getFormat(), is("RTCM 3.1"));
        assertThat(str.getFormatDetails(), is("1004(1),1006(30),1008(30),1019(120),1033(30)"));
        assertThat(str.getCarrier(), is(2));
        assertThat(str.getNavSystem(), is("GPS"));
        assertThat(str.getNetwork(), is("EUREF"));
        assertThat(str.getCountry(), is("ESP"));
        assertThat(str.getLatLon(), is(LatLon.fromDegrees(43.36, 351.60)));
        assertThat(str.isNeedNmea(), is(false));
        assertThat(str.isSolutionNetworkBased(), is(false));
        assertThat(str.getGenerator(), is("LEICA GRX1200PRO"));
        assertThat(str.getCompressionEncryption(), is("none"));
        assertThat(str.getAuthType(), is(AuthType.BASIC));
        assertThat(str.isFee(), is(false));
        assertThat(str.getBitsPerSeconds(), is(3000));
    }

    @Test
    public void parseStrWithWrongCarrier() throws Exception {
        String line = "STR;ACOR0;Coruna;RTCM 3.1;1004(1),1006(30),1008(30),1019(120),1033(30);WRONG_CARRIER;GPS;EUREF;ESP;43.36;351.60;0;0;LEICA GRX1200PRO;none;B;N;3000;ergnss-ip.ign.es:2101/ACOR0(1)";
        NtripSourceStr str = new NtripSourceStr(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(str));

        assertThat(str.getMountPoint(), is("ACOR0"));
        assertThat(str.getIdentifier(), is("Coruna"));
        assertThat(str.getFormat(), is("RTCM 3.1"));
        assertThat(str.getFormatDetails(), is("1004(1),1006(30),1008(30),1019(120),1033(30)"));
        assertThat(str.getCarrier(), is(0));
        assertThat(str.getNavSystem(), is(nullValue()));
        assertThat(str.getNetwork(), is(nullValue()));
        assertThat(str.getCountry(), is(nullValue()));
        assertThat(str.getLatLon(), is(nullValue()));
        assertThat(str.isNeedNmea(), is(false));
        assertThat(str.isSolutionNetworkBased(), is(false));
        assertThat(str.getGenerator(), is(nullValue()));
        assertThat(str.getCompressionEncryption(), is(nullValue()));
        assertThat(str.getAuthType(), is(nullValue()));
        assertThat(str.isFee(), is(false));
        assertThat(str.getBitsPerSeconds(), is(0));
    }

    @Test
    public void parseStrWithWrongLatLon() throws Exception {
        String line = "STR;ACOR0;Coruna;RTCM 3.1;1004(1),1006(30),1008(30),1019(120),1033(30);2;GPS;EUREF;ESP;WRONG_LAT;WRONG_LON;0;0;LEICA GRX1200PRO;none;B;N;3000;ergnss-ip.ign.es:2101/ACOR0(1)";
        NtripSourceStr str = new NtripSourceStr(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(str));

        assertThat(str.getMountPoint(), is("ACOR0"));
        assertThat(str.getIdentifier(), is("Coruna"));
        assertThat(str.getFormat(), is("RTCM 3.1"));
        assertThat(str.getFormatDetails(), is("1004(1),1006(30),1008(30),1019(120),1033(30)"));
        assertThat(str.getCarrier(), is(2));
        assertThat(str.getNavSystem(), is("GPS"));
        assertThat(str.getNetwork(), is("EUREF"));
        assertThat(str.getCountry(), is("ESP"));
        assertThat(str.getLatLon(), is(nullValue()));
        assertThat(str.isNeedNmea(), is(false));
        assertThat(str.isSolutionNetworkBased(), is(false));
        assertThat(str.getGenerator(), is(nullValue()));
        assertThat(str.getCompressionEncryption(), is(nullValue()));
        assertThat(str.getAuthType(), is(nullValue()));
        assertThat(str.isFee(), is(false));
        assertThat(str.getBitsPerSeconds(), is(0));
    }

    @Test
    public void parseStrWithoutAuthType() throws Exception {
        String line = "STR;ACOR0;Coruna;RTCM 3.1;1004(1),1006(30),1008(30),1019(120),1033(30);2;GPS;EUREF;ESP;43.36;351.60;0;0;LEICA GRX1200PRO;none;NO_AUTH_TYPE;N;3000;ergnss-ip.ign.es:2101/ACOR0(1)";
        NtripSourceStr str = new NtripSourceStr(line.split(";"), line);
        assertThat(NtripSourceTableEntry.parse(line), OptionalMatchers.contains(str));

        assertThat(str.getMountPoint(), is("ACOR0"));
        assertThat(str.getIdentifier(), is("Coruna"));
        assertThat(str.getFormat(), is("RTCM 3.1"));
        assertThat(str.getFormatDetails(), is("1004(1),1006(30),1008(30),1019(120),1033(30)"));
        assertThat(str.getCarrier(), is(2));
        assertThat(str.getNavSystem(), is("GPS"));
        assertThat(str.getNetwork(), is("EUREF"));
        assertThat(str.getCountry(), is("ESP"));
        assertThat(str.getLatLon(), is(LatLon.fromDegrees(43.36, 351.60)));
        assertThat(str.isNeedNmea(), is(false));
        assertThat(str.isSolutionNetworkBased(), is(false));
        assertThat(str.getGenerator(), is("LEICA GRX1200PRO"));
        assertThat(str.getCompressionEncryption(), is("none"));
        assertThat(str.getAuthType(), is(nullValue()));
        assertThat(str.isFee(), is(false));
        assertThat(str.getBitsPerSeconds(), is(3000));
    }

    @Test
    public void parseStrWithoutBitRate() throws Exception {
        String line = "STR;ACOR0;Coruna;RTCM 3.1;1004(1),1006(30),1008(30),1019(120),1033(30);2;GPS;EUREF;ESP;43.36;351.60;0;0;LEICA GRX1200PRO;none;B;N;NO_BIT_RATE;ergnss-ip.ign.es:2101/ACOR0(1)";
        NtripSourceStr str = new NtripSourceStr(line.split(";"), line);
        Optional<NtripSourceTableEntry> parse = NtripSourceTableEntry.parse(line);
        assertThat(parse, OptionalMatchers.contains(str));

        assertThat(str.getMountPoint(), is("ACOR0"));
        assertThat(str.getIdentifier(), is("Coruna"));
        assertThat(str.getFormat(), is("RTCM 3.1"));
        assertThat(str.getFormatDetails(), is("1004(1),1006(30),1008(30),1019(120),1033(30)"));
        assertThat(str.getCarrier(), is(2));
        assertThat(str.getNavSystem(), is("GPS"));
        assertThat(str.getNetwork(), is("EUREF"));
        assertThat(str.getCountry(), is("ESP"));
        assertThat(str.getLatLon(), is(LatLon.fromDegrees(43.36, 351.60)));
        assertThat(str.isNeedNmea(), is(false));
        assertThat(str.isSolutionNetworkBased(), is(false));
        assertThat(str.getGenerator(), is("LEICA GRX1200PRO"));
        assertThat(str.getCompressionEncryption(), is("none"));
        assertThat(str.getAuthType(), is(AuthType.BASIC));
        assertThat(str.isFee(), is(false));
        assertThat(str.getBitsPerSeconds(), is(0));
    }

    @Test
    public void toStringTest() throws Exception {
        String line = "STR;ACOR0;Coruna;RTCM 3.1;1004(1),1006(30),1008(30),1019(120),1033(30);2;GPS;EUREF;ESP;43.36;351.60;0;0;LEICA GRX1200PRO;none;B;N;NO_BIT_RATE;ergnss-ip.ign.es:2101/ACOR0(1)";
        NtripSourceStr str = new NtripSourceStr(line.split(";"), line);

        assertThat(str.toString(), is("STR,"+str.getMountPoint()+","+str.getFormat()));

        String netLine = "NET;EUREF;EUREF;NO_AUTH;N;https://igs.bkg.bund.de/root_ftp/NTRIP/streams/streamlist_euref-ip.htm;https://igs.bkg.bund.de:443/root_ftp/EUREF/station/rnxskl/;http://register.rtcm-ntrip.org;none";
        NtripSourceNet net = new NtripSourceNet(netLine.split(";"), netLine);

        assertThat(net.toString(), is("NET,"+net.getIdentifier()));

        String casLine = "CAS;192.106.234.17;2101;NTRIPcaster/2.0.21;ASI - eGeos;0;ITA;40.65;16.07;0.0.0.0;0;http://euref-ip.asi.it";
        NtripSourceCas cas = new NtripSourceCas(casLine.split(";"), casLine);

        assertThat(cas.toString(), is("CAS,"+cas.getHost()+","+cas.getPort()));
    }

    @Test
    public void parseAuthType() throws Exception {
        assertThat(AuthType.parse("B"), is(AuthType.BASIC));
        assertThat(AuthType.parse("N"), is(AuthType.NONE));
        assertThat(AuthType.parse("D"), is(AuthType.DIGEST));
    }
}