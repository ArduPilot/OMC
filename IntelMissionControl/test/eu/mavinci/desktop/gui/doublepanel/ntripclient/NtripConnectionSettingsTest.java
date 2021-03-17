/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.core.plane.protocol.Base64;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class NtripConnectionSettingsTest {
    @Test
    public void createNtripConnectionSettings_fromUrl_degenerated() throws Exception {
        NtripConnectionSettings settings;
        NtripConnectionSettings built;

        settings = NtripConnectionSettings.fromUrl(new URL("https://:2101/COBA0"));
        built = NtripConnectionSettings.builder().withHttps().withPort(2101).withStream(new NtripSourceStr(new String[] {"STR", "COBA0"}, "")).build();
        assertThat(built, is(settings));
        assertThat(built.hashCode(), is(settings.hashCode()));

        assertThat(settings.getHost(), is(""));
        assertThat(settings.getPort(), is(2101));
        assertThat(settings.getStreamAsString(), is("COBA0"));
        assertThat(settings.isHttps(), is(true));
        assertThat(settings.getUser(), is(nullValue()));
        assertThat(settings.getPassword(), is(""));

        settings = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net/COBA0"));
        built = NtripConnectionSettings.builder().withHttps().withHost("www.euref-ip.net").withStream(new NtripSourceStr(new String[] {"STR", "COBA0"}, "")).build();
        assertThat(built, is(settings));
        assertThat(built.hashCode(), is(settings.hashCode()));

        assertThat(settings.getHost(), is("www.euref-ip.net"));
        assertThat(settings.getPort(), is(-1));
        assertThat(settings.getStreamAsString(), is("COBA0"));
        assertThat(settings.isHttps(), is(true));
        assertThat(settings.getUser(), is(nullValue()));
        assertThat(settings.getPassword(), is(""));

        settings = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/"));
        built = NtripConnectionSettings.builder().withHttps().withHost("www.euref-ip.net").withPort(2101).build();
        assertThat(built, is(settings));
        assertThat(built.hashCode(), is(settings.hashCode()));

        assertThat(settings.getHost(), is("www.euref-ip.net"));
        assertThat(settings.getPort(), is(2101));
        assertThat(settings.getStreamAsString(), is(""));
        assertThat(settings.isHttps(), is(true));
        assertThat(settings.getUser(), is(nullValue()));
        assertThat(settings.getPassword(), is(""));
    }

    @Test
    public void equals_withStreams() throws Exception {
        assertThat(NtripConnectionSettings.fromUrl(new URL("http://www.euref-ip.net:2101/COBA1")), not(NtripConnectionSettings.fromUrl(new URL("http://www.euref-ip.net:2101/COBA0"))));
    }

    @Test
    public void createNtripConnectionSettings_fromUrl() throws Exception {
        NtripConnectionSettings settings = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/COBA0"));
        NtripConnectionSettings built = NtripConnectionSettings.builder()
            .withHttps()
            .withHost("www.euref-ip.net")
            .withPort(2101)
            .withStream(new NtripSourceStr(new String[] {"STR", "COBA0"}, ""))
            .build();

        assertThat(built, is(settings));
        assertThat(built.hashCode(), is(built.hashCode()));

        assertThat(settings.getHost(), is("www.euref-ip.net"));
        assertThat(settings.getPort(), is(2101));
        assertThat(settings.getStreamAsString(), is("COBA0"));
        assertThat(settings.isHttps(), is(true));
        assertThat(settings.getUser(), is(nullValue()));
        assertThat(settings.getPassword(), is(""));
    }

    @Test
    public void createNtripConnectionSettings_fromUrl_withoutHost() throws Exception {
        NtripConnectionSettings settings = NtripConnectionSettings.fromUrl(new URL("https://:2101/COBA0"));

        NtripConnectionSettings built = NtripConnectionSettings.builder().withHttps().withPort(2101).withStream(new NtripSourceStr(new String[] {"STR", "COBA0"}, "")).build();
        assertThat(built, is(settings));
        assertThat(built.hashCode(), is(settings.hashCode()));

        assertThat(settings.getHost(), is(""));
        assertThat(settings.getPort(), is(2101));
        assertThat(settings.getStreamAsString(), is("COBA0"));
        assertThat(settings.isHttps(), is(true));
        assertThat(settings.getUser(), is(nullValue()));
        assertThat(settings.getPassword(), is(""));
    }

    @Test
    public void createNtripConnectionSettings_fromUrl_withUserInfo() throws Exception {
        NtripConnectionSettings settings = NtripConnectionSettings.fromUrl(new URL("http://username:password@www.euref-ip.net:2101/COBA0"));

        NtripConnectionSettings built = NtripConnectionSettings.builder()
            .withUser("username")
            .withPassword("password")
            .withHost("www.euref-ip.net")
            .withPort(2101)
            .withStream(new NtripSourceStr(new String[] {"STR", "COBA0"}, ""))
            .build();

        assertThat(built, is(settings));
        assertThat(built.hashCode(), is(settings.hashCode()));

        assertThat(settings.getHost(), is("www.euref-ip.net"));
        assertThat(settings.getPort(), is(2101));
        assertThat(settings.getStreamAsString(), is("COBA0"));
        assertThat(settings.isHttps(), is(false));
        assertThat(settings.getUser(), is("username"));
        assertThat(settings.getPassword(), is(Base64.encodeString("password")));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void createNtripConnectionSettings_fromUrl_withUserInfo_withoutPassword() throws Exception {
        NtripConnectionSettings.fromUrl(new URL("http://username@www.euref-ip.net:2101/COBA0"));
    }

    @Test
    public void checkEquals() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(
            new URL("http://username:password@www.euref-ip.net:2101/COBA0")
        );
        NtripConnectionSettings settingsRight = NtripConnectionSettings.fromUrl(
            new URL("http://username:password@www.euref-ip.net:2101/COBA0")
        );

        assertThat(settingsLeft.equals(settingsRight), is(true));
        assertThat(settingsRight.equals(settingsLeft), is(true));

        assertThat(settingsLeft.hashCode(), is(settingsRight.hashCode()));
    }

    @Test
    public void checkEquals_https() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(
            new URL("https://username:password@www.euref-ip.net:2101/COBA0")
        );
        NtripConnectionSettings settingsRight = NtripConnectionSettings.fromUrl(
            new URL("http://username:password@www.euref-ip.net:2101/COBA0")
        );

        assertThat(settingsLeft.equals(settingsRight), is(false));
        assertThat(settingsRight.equals(settingsLeft), is(false));

        assumeThat(settingsLeft.hashCode(), is(not(settingsRight.hashCode())));
    }

    @Test
    public void checkEquals_with_andWithout_userInfo() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(new URL("http://www.euref-ip.net:2101/COBA0"));
        NtripConnectionSettings settingsRight = NtripConnectionSettings.fromUrl(
            new URL("http://username:password@www.euref-ip.net:2101/COBA0")
        );

        assertThat(settingsLeft.equals(settingsRight), is(true));
        assertThat(settingsRight.equals(settingsLeft), is(true));

        assertThat(settingsLeft.hashCode(), is(settingsRight.hashCode()));
    }

    @Test
    public void checkEquals_with_andWithout_stream() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/COBA0"));
        NtripConnectionSettings settingsRight = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/"));

        assertThat(settingsLeft.equals(settingsRight), is(false));
        assertThat(settingsRight.equals(settingsLeft), is(false));
    }

    @Test
    public void checkEquals_with_andWithout_port() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/COBA0"));
        NtripConnectionSettings settingsRight = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net/COBA0"));

        assertThat(settingsLeft.equals(settingsRight), is(false));
        assertThat(settingsRight.equals(settingsLeft), is(false));

        assumeThat(settingsLeft.hashCode(), is(not(settingsRight.hashCode())));
    }

    @Test
    public void checkEquals_with_andWithout_host() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/COBA0"));
        NtripConnectionSettings settingsRight = NtripConnectionSettings.fromUrl(new URL("https://:2101/COBA0"));

        assertThat(settingsLeft.equals(settingsRight), is(false));
        assertThat(settingsRight.equals(settingsLeft), is(false));

        assumeThat(settingsLeft.hashCode(), is(not(settingsRight.hashCode())));
    }

    @Test
    public void checkEquals_with_itself() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/COBA0"));

        assertThat(settingsLeft.equals(settingsLeft), is(true));
    }

    @Test
    public void checkEquals_with_null() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/COBA0"));

        assertThat(settingsLeft.equals(null), is(false));
    }

    @Test
    public void checkEquals_with_object() throws Exception {
        NtripConnectionSettings settingsLeft = NtripConnectionSettings.fromUrl(new URL("https://www.euref-ip.net:2101/COBA0"));

        assertThat(settingsLeft.equals(new Object()), is(false));
    }
}