/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intel.missioncontrol.airtraffic.dto.AirAvionicsResponse;
import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import com.intel.missioncontrol.airtraffic.dto.AittrafficObjectProperties;
import com.intel.missioncontrol.airtraffic.dto.Geometry;
import com.intel.missioncontrol.helper.Expect;
import eu.mavinci.core.plane.protocol.Base64;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unirest.HttpResponse;
import unirest.JsonNode;
import unirest.Unirest;

public class AirAvionicsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirAvionicsClient.class);

    /**
     * This is the initial token from airavionics for debug and testing purposes.
     */
    private static String defaultToken =
            "{\n"
                    + "  \"type\": \"service_account\",\n"
                    + "  \"project_id\": \"traffic-monitor-254907\",\n"
                    + "  \"private_key_id\": \"17bcb2fee489226672825ac561dcf3dcf26adaf3\",\n"
                    + "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDCoS5iAzbg6qRN\\n/5uL4reU670MIPE6MMpRRu4M/88z3xwFycQ/e2/SveJ5xuX75xZ+te0nWjrN7bf5\\nYfr+Tejx45UqMveeIBmpNR2voRaJrA2vqYrIYKdWhMElTJDW9DpPVQVdWXpdb4Y8\\nWEyp3Abez9329LE1CRZZb2SeBHDxUrJVVcKf9Pqm6uzJICST01quF1PYLZaF0/V9\\nPPCR4kvCVdcR2dyDrgBe/FbF+PsAqjRtrRqkTpS4HQYf73oG5oFYsePK+HU0SKxl\\n1lvVFgHl3jXLjogSrXCZjNCcjWGevPLu9vudBw8ddDl0ljb5O/0knMxHRbaXYz16\\nWRPZITWxAgMBAAECggEAAJ4Pa4LRIJIPUfVpseVItkdTq1rqrP0mxgsmWgmCxDF0\\n7de3TskxHMZay6PoWPNMGINWGBu2RC7jm+w9N/Hj91uy7LRqYZiwOQpRqmolndZF\\n56zSxwKJX5WK+ns3NpQFoA4NT9GRlZkaJ1gMT28i0tUZSUKsp0tufPRzFD/44nkX\\n1J49QNO0EhcZ1WOTpeY0iKa1y0EcXLcqNhuNuQvPzf9D0Smded7ggLS3pd6bkkfb\\n+IS9tgJIbKrBvARGShNRYfJ7IbHu2OYL6ZNH3FT5A84HCwy4xVmRYfZ9gefQqBos\\nZIAKVeMMpkK0loCq/IaYzT9gDyUpO/Du/L+zwTqzwQKBgQD6OCfC/N5Ws7WdVXl1\\nSUzdE9aiJQvs48BDFfBGWzMqchbZ2IGMQv9C9hmCpLhbBdH9ic5L95CFgEggj458\\n5sNAxlzaa2y4rBSXb7mfDUhGpbM8PuGwoN2gvoPJYD9Pjv3JlnfuA3OYpne3H3aT\\nVxiLp1Zd1MO4p19X6cdXsRRUYQKBgQDHIEHxqy4GdIcQ+TVjvzR3YhjhN/o8YIac\\nIEYEB79w5vLkRqX5W5gQiwqVJ+Xv7q9Wuwsk1bEx+s+QAgcZmNBx0uxlHS3LWDEq\\n9VyFb77NSynhHkfIzXRn9QzLBypNVhdhS4NPBOVjThpm4lhHBuLsshctWVppn23l\\nyOjQXrFjUQKBgHeWQxqj9DeH6DLr2QK/J6c5hgk0n4B2UTs+zvkxOUD+CgHPwgqh\\nMpJ0QDj6NMn7/CKqrBgOl9nWN6KoObdkKslYhKQ3V1zjkWvXD+X+WR66rvEKNJ9Q\\nItxicWXbRCbn9k9URLlSmPrKBMlWljtREwAb1+E8gNZN1/mSV52HmmnBAoGBAJtw\\ns+mKHgCOAE2grCu0O6mh2xgckWuGUi4YB2/n+GqS7jKCb9fKx1bKozUyEw2L/jWv\\nBxD2+z6vsk2RN6ekobro0v5Pm8rril4lcNb9Fy28qfGAqLq4n1DzeswWSNBcDx41\\nS0ho4wP1GSkNMaooVwY8M1mEMc7X1HsWrZ7S/JgxAoGBAMWKjWUjWfqJ9J9tuvEg\\nM+vseBa5LnChiuf0ZPFdMuTCC8JrzzisIBCjTvHPiKknrVx4iFlfRTzLdNuawN9y\\njQCbCDlXKfXf4gt8mzb2bHiFYyGGw8gIbgKyNUu2IDHjcDXUuQfMR0DFug194l4V\\ntYENjthUt9WIHEBeooEINnes\\n-----END PRIVATE KEY-----\\n\",\n"
                    + "  \"client_email\": \"intel-mission-control@traffic-monitor-254907.iam.gserviceaccount.com\",\n"
                    + "  \"client_id\": \"105660411294249919498\",\n"
                    + "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n"
                    + "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n"
                    + "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n"
                    + "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/intel-mission-control%40traffic-monitor-254907.iam.gserviceaccount.com\"\n"
                    + "}\n";

    private Date tokenRefreshTime = new Date();
    private String jwtToken;
    private boolean useDefaultToken = false;
    private String privateK;
    private String email;
    private String endpoint;

    public AirAvionicsClient() {
        this.jwtToken = "";
        this.useDefaultToken = true;
    }

    public AirAvionicsClient(String privateK, String email, String endpoint) {
        this.jwtToken = "";
        this.privateK = privateK;
        this.email = email;
        this.endpoint = endpoint;
        this.useDefaultToken = false;
    }

    private static PrivateKey loadPrivateKey(String key)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        // The private key should be in dem format, the following removes
        // the tags so that we can load it into java

        StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(key));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }

        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN RSA PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END RSA PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

        // This may have to be activated to initialize the internal keystore
        // com.sun.org.apache.xml.internal.security.Init.init();

        byte[] clearKey = Base64.decode(pkcs8Pem, 1);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clearKey);
        KeyFactory fact = null;
        fact = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = null;
        privateKey = fact.generatePrivate(keySpec);

        Arrays.fill(clearKey, (byte) 0);

        return privateKey;
    }

    private static Gson gsonInstance() {
        return GsonHolder.INSTANCE;
    }

    public void refreshTokenIfNeeded() {
        if (this.tokenRefreshTime.before(new Date())) {
            if (useDefaultToken) {
                refreshDefaultToken();
            } else {
                refreshToken(this.privateK, this.email, this.endpoint);
            }
        }
    }

    public String refreshDefaultToken() {
        var privateK =
                "-----BEGIN RSA PRIVATE KEY-----\n"
                        + "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDCoS5iAzbg6qRN\n"
                        + "/5uL4reU670MIPE6MMpRRu4M/88z3xwFycQ/e2/SveJ5xuX75xZ+te0nWjrN7bf5\n"
                        + "Yfr+Tejx45UqMveeIBmpNR2voRaJrA2vqYrIYKdWhMElTJDW9DpPVQVdWXpdb4Y8\n"
                        + "WEyp3Abez9329LE1CRZZb2SeBHDxUrJVVcKf9Pqm6uzJICST01quF1PYLZaF0/V9\n"
                        + "PPCR4kvCVdcR2dyDrgBe/FbF+PsAqjRtrRqkTpS4HQYf73oG5oFYsePK+HU0SKxl\n"
                        + "1lvVFgHl3jXLjogSrXCZjNCcjWGevPLu9vudBw8ddDl0ljb5O/0knMxHRbaXYz16\n"
                        + "WRPZITWxAgMBAAECggEAAJ4Pa4LRIJIPUfVpseVItkdTq1rqrP0mxgsmWgmCxDF0\n"
                        + "7de3TskxHMZay6PoWPNMGINWGBu2RC7jm+w9N/Hj91uy7LRqYZiwOQpRqmolndZF\n"
                        + "56zSxwKJX5WK+ns3NpQFoA4NT9GRlZkaJ1gMT28i0tUZSUKsp0tufPRzFD/44nkX\n"
                        + "1J49QNO0EhcZ1WOTpeY0iKa1y0EcXLcqNhuNuQvPzf9D0Smded7ggLS3pd6bkkfb\n"
                        + "+IS9tgJIbKrBvARGShNRYfJ7IbHu2OYL6ZNH3FT5A84HCwy4xVmRYfZ9gefQqBos\n"
                        + "ZIAKVeMMpkK0loCq/IaYzT9gDyUpO/Du/L+zwTqzwQKBgQD6OCfC/N5Ws7WdVXl1\n"
                        + "SUzdE9aiJQvs48BDFfBGWzMqchbZ2IGMQv9C9hmCpLhbBdH9ic5L95CFgEggj458\n"
                        + "5sNAxlzaa2y4rBSXb7mfDUhGpbM8PuGwoN2gvoPJYD9Pjv3JlnfuA3OYpne3H3aT\n"
                        + "VxiLp1Zd1MO4p19X6cdXsRRUYQKBgQDHIEHxqy4GdIcQ+TVjvzR3YhjhN/o8YIac\n"
                        + "IEYEB79w5vLkRqX5W5gQiwqVJ+Xv7q9Wuwsk1bEx+s+QAgcZmNBx0uxlHS3LWDEq\n"
                        + "9VyFb77NSynhHkfIzXRn9QzLBypNVhdhS4NPBOVjThpm4lhHBuLsshctWVppn23l\n"
                        + "yOjQXrFjUQKBgHeWQxqj9DeH6DLr2QK/J6c5hgk0n4B2UTs+zvkxOUD+CgHPwgqh\n"
                        + "MpJ0QDj6NMn7/CKqrBgOl9nWN6KoObdkKslYhKQ3V1zjkWvXD+X+WR66rvEKNJ9Q\n"
                        + "ItxicWXbRCbn9k9URLlSmPrKBMlWljtREwAb1+E8gNZN1/mSV52HmmnBAoGBAJtw\n"
                        + "s+mKHgCOAE2grCu0O6mh2xgckWuGUi4YB2/n+GqS7jKCb9fKx1bKozUyEw2L/jWv\n"
                        + "BxD2+z6vsk2RN6ekobro0v5Pm8rril4lcNb9Fy28qfGAqLq4n1DzeswWSNBcDx41\n"
                        + "S0ho4wP1GSkNMaooVwY8M1mEMc7X1HsWrZ7S/JgxAoGBAMWKjWUjWfqJ9J9tuvEg\n"
                        + "M+vseBa5LnChiuf0ZPFdMuTCC8JrzzisIBCjTvHPiKknrVx4iFlfRTzLdNuawN9y\n"
                        + "jQCbCDlXKfXf4gt8mzb2bHiFYyGGw8gIbgKyNUu2IDHjcDXUuQfMR0DFug194l4V\n"
                        + "tYENjthUt9WIHEBeooEINnes\n"
                        + "-----END RSA PRIVATE KEY-----";
        return refreshToken(
                privateK,
                "intel-mission-control@traffic-monitor-254907.iam.gserviceaccount.com",
                "endpoints-esp-dqgoqs6jea-ew.a.run.app");
    }

    private String refreshToken(String privateK, String email, String endpoint) {
        PrivateKey key = null;
        try {
            key = loadPrivateKey(privateK);
        } catch (Exception e) {
            LOGGER.error("can't load key", e);
        }

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date nowPlusAuthTime = new Date(nowMillis + 30000);
        var c =
                Jwts.builder()
                        .setIssuer(email)
                        .setSubject(email)
                        .setAudience(endpoint)
                        .setIssuedAt(now)
                        .setExpiration(nowPlusAuthTime)
                        .signWith(key)
                        .compact();
        this.jwtToken = c;
        this.tokenRefreshTime = nowPlusAuthTime;
        return c;
    }

    public String getTraffic(double latitude, double longitude, double radius, double time) {
        return getTraffic(
                "https://endpoints-esp-dqgoqs6jea-ew.a.run.app/api/v1/traffic/around", latitude, longitude, radius, time);
    }

    public String getTraffic(String endPointAddress, double latitude, double longitude, double radius, double maxAge) {
        Expect.isTrue(latitude <= 90.0 && latitude >= -90.0, "latitude must be in [-90,90]");
        Expect.isTrue(longitude <= 180 && longitude >= -180, "longitude must be in [-180,180]");
        Expect.isTrue(radius >= 0, "radius must be larger than zero");

        refreshTokenIfNeeded();
        // status check and return
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Authorization", "Bearer " + this.jwtToken);

        Map<String, Object> fields = new HashMap<>();
        var posString = "" + latitude + "," + longitude;
        fields.put("pos", posString);
        fields.put("dist", radius);
        fields.put("maxAge", maxAge);
        HttpResponse<JsonNode> jsonResponse =
                Unirest.get(endPointAddress).headers(headers).queryString(fields).asJson();
        // System.out.println(jsonResponse.getBody());
        return jsonResponse.getBody().toString();
    }

    /**
     * @param lon
     * @param lat
     * @param baroAltitude     Barometric altitude in meters above 1013.25hPa
     * @param courseOverGround True course over ground in degree between 0 and 360
     * @param idType           0, 1, 2, 3, 4 for unknown, icao_addr, non_icao, flarm, infrastructure
     * @param identifier       Unique identifier dependent on 'idType', e.g. six-digit hex ICAO address if 'idType' is 1
     * @param onGround         Whether target is on ground or not. Should be used if target is on ground.
     * @param speedOverGround  Speed over ground in m/s
     * @param timestamp        Timestamp as ISO8610 strings in UTC or empty string for now()
     * @param type             The aircraft's type or category, 0 if not set.
     * @param verticalSpeed    Vertical speed in m/s.
     * @param wgs84Altitude    GNSS altitude in meters above WGS84.
     * @return
     */
    public String publishUavPosition(double lon,
                                     double lat,
                                     int baroAltitude,
                                     int courseOverGround,
                                     int idType,
                                     String identifier,
                                     boolean onGround,
                                     int speedOverGround,
                                     String timestamp,
                                     int type,
                                     double verticalSpeed,
                                     int wgs84Altitude) {
        return publishUavPosition("https://endpoints-esp-dqgoqs6jea-ew.a.run.app/api/v1/traffic/receive", lon, lat, baroAltitude, courseOverGround, idType, identifier, onGround, speedOverGround, timestamp, type, verticalSpeed, wgs84Altitude);
    }

    private boolean isValidISO8601TimeStamp(String dateStr) {
        var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public String publishUavPosition(String target,
                                     double lon,
                                     double lat,
                                     int baroAltitude,
                                     int courseOverGround,
                                     int idType,
                                     String identifier,
                                     boolean onGround,
                                     int speedOverGround,
                                     String timestamp,
                                     int type,
                                     double verticalSpeed,
                                     int wgs84Altitude) {
        Expect.isTrue(lat <= 90.0 && lat >= -90.0, "latitude must be in [-90,90]");
        Expect.isTrue(lon <= 180 && lon >= -180, "longitude must be in [-180,180]");
        Expect.isTrue(courseOverGround <= 360 && courseOverGround >= 0, "courseOverGround must be in [0,360]");
        Expect.isTrue(idType == 0 || idType == 1 || idType == 2 || idType == 3 || idType == 4, "idType must be 0,1,2,3,4");
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (timestamp.isEmpty()) {
            Date date = new Date(System.currentTimeMillis());

            timestamp = sdf.format(date);
        }
        Expect.isTrue(isValidISO8601TimeStamp(timestamp), "timeStamp needs to be empty or valid ISO8601 timestamp");


        refreshTokenIfNeeded();
        // status check and return
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json");
        headers.put("Authorization", "Bearer " + this.jwtToken);

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        String nowString = timestamp = sdf.format(now);

        var properties = new AittrafficObjectProperties(baroAltitude, wgs84Altitude,
                null, null,
                type, identifier,
                now, onGround,
                type, courseOverGround,
                speedOverGround, verticalSpeed);
        var geom = new Geometry(List.of(lat, lon), "Point");
        var ato = new AirtrafficObject(geom, null, "Feature", properties);
        var aar = new AirAvionicsResponse(List.of(ato), "FeatureCollection");


        var gson = gsonInstance();

        var payload = gson.toJson(aar);

        var reqs = putRequestWithHeaderAndBody(target, this.jwtToken, payload);

        return reqs.toString();


    }

    public Response putRequestWithHeaderAndBody(String url, String token, String jsonBody) {

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBody);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .put(body) //PUT
                .addHeader("Authorization", token)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            LOGGER.error("Error while publishing uav position", e);
        }
        return response;
    }

    private static class GsonHolder {
        private static Gson INSTANCE = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").create();
    }

}
