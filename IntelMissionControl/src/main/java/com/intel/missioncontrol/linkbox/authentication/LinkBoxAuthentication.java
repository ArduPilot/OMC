/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.linkbox.authentication;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import de.saxsys.mvvmfx.utils.validation.Severity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.json.JSONException;
import org.json.JSONObject;

public class LinkBoxAuthentication {

    private final String METHOD_POST = "POST";
    private final String METHOD_DELETE = "DELETE";
    private final AsyncBooleanProperty authorized = new SimpleAsyncBooleanProperty(this);
    private String deviceName = getIMCdeviceName();
    private String mavLinkKeyImc = "1234567890";
    private String mavLinkKeyDrone = "";
    private ResolvableValidationMessage authenticationResolvable;
    private TrustManager[] trustAllCerts =
        new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            }
        };

    public String getMavLinkKeyImc() {
        return mavLinkKeyImc;
    }

    public void setMavLinkKeyImc(String mavLinkKeyImc) {
        this.mavLinkKeyImc = mavLinkKeyImc;
    }

    public String getMavLinkKeyDrone() {
        return mavLinkKeyDrone;
    }

    public void setAuthorized(boolean status) {
        authorized.set(status);
    }

    public ReadOnlyAsyncBooleanProperty authorizedProperty() {
        return authorized;
    }

    public boolean requestLinkBoxAuthentication() {
        HttpURLConnection connection = null;
        try {
            connection = sendRequest(METHOD_POST);
            if (connection != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    parseResponse(readResponse(reader));
                } else {
                    authorized.setValue(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            authorized.setValue(false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return true;
    }

    private String readResponse(BufferedReader reader) {
        StringBuilder stringBuffer = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuffer.toString();
    }

    private String getAuthRequestBody() {
        return "{ \"deviceName\":\"" + deviceName + "\"," + "\"imcMavLinkKey\" :\"" + mavLinkKeyImc + "\" }";
    }

    private String getAuthRequestBody(String mavLinkKeyImc) {
        return "{ \"deviceName\":\"" + deviceName + "\"," + "\"imcMavLinkKey\" :\"" + mavLinkKeyImc + "\" }";
    }

    private void parseResponse(String foo) {
        try {
            System.out.println(foo);
            JSONObject response = new JSONObject(foo);
            String status = response.getString("status");
            if (status != null && status.equalsIgnoreCase("OK")) {
                authorized.setValue(true);
            } else {
                authorized.setValue(false);
            }

            String mavlinkKeyBox = response.getString("boxMavLinkKey");
            String deauthToken = response.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean requestLinkBoxCancellation() {
        HttpURLConnection connection = null;
        try {
            connection = sendRequest(METHOD_DELETE);
            if (connection != null) {
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    authorized.setValue(false);
                } else {
                    authorized.setValue(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return true;
    }

    private HttpURLConnection sendRequest(String method) {
        HttpURLConnection connection = null;
        try {
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            String uriString = "https://launchbox.internal/api/v1/Auth/client";
            if (method.equals(METHOD_DELETE)) {
                uriString += deviceName;
            }

            URL url = new URL(uriString);

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Authorization");
            connection.setDoOutput(true);
            connection.setConnectTimeout(Duration.ofMinutes(1).toSecondsPart());
            setMavLinkKeyImc(RandomStringUtils.randomAlphanumeric(10));
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = getAuthRequestBody().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    private String getIMCdeviceName() {
        InetAddress ip;
        StringBuilder sb = new StringBuilder();
        try {
            ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();

            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public ResolvableValidationMessage getAuthenticationResolvable(ILanguageHelper languageHelper, String linkBoxName) {
        if (authenticationResolvable == null) {
            authenticationResolvable =
                new ResolvableValidationMessage(
                    null,
                    Severity.WARNING,
                    ValidationMessageCategory.BLOCKING,
                    languageHelper.getString(LinkBoxAuthentication.class, "uavLockedMessage", linkBoxName),
                    new AuthorizationResolveAction(languageHelper, this::requestLinkBoxAuthentication));
        }

        return authenticationResolvable;
    }
}
