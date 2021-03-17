/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.translation;

import eu.mavinci.desktop.main.debug.Debug;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;

public class GoogleTranslator {

    private static final String charset = StandardCharsets.UTF_8.name();
    private static final String url = "https://www.googleapis.com/language/translate/v2";

    // Google Translate API key --> store encrypted ?
    private static final String key = "AIzaSyBdpjfutWhA19V8Olf3Qb1kzqiDE2wnQN8";

    public static String createQuery(String requestStr, String lan1, String lan2) {
        String query = null;
        requestStr = requestStr.replace("_", "");
        try {
            query =
                url
                    + "?"
                    + String.format(
                        "key=%s&source=%s&target=%s&q=%s",
                        URLEncoder.encode(key, charset),
                        URLEncoder.encode(lan1, charset),
                        URLEncoder.encode(lan2, charset),
                        URLEncoder.encode(requestStr, charset));
        } catch (UnsupportedEncodingException e) {
            // no exception with default utf-8
        }

        return query;
    }

    public static String getTranslation(String query, boolean upper) {
        String responseBody = "";
        try {
            URLConnection connection = new URL(query).openConnection();
            InputStream response = connection.getInputStream();
            try (Scanner scanner = new Scanner(response)) {
                responseBody = scanner.useDelimiter("\\A").next();
            }

            response.close();
        } catch (IOException e) {
            // I/O exception
            Debug.getLog().log(Level.WARNING, "Google Translate connectionDialog error", e);
        }

        String translations = getTranslatedText(responseBody);

        translations = translations.replace("&quot;", "\"");

        String transformed;
        if (upper) {
            transformed = Character.toUpperCase(translations.charAt(0)) + translations.substring(1);
        } else {
            transformed = Character.toLowerCase(translations.charAt(0)) + translations.substring(1);
        }

        return transformed;
    }

    public static String getTranslation(String requestStr, String lan1, String lan2) {
        return getTranslation(createQuery(requestStr, lan1, lan2), Character.isUpperCase(requestStr.charAt(0)));
    }

    private static String getTranslatedText(String response) {
        String result = response.substring(response.indexOf("\"translatedText\": \"") + 19, response.indexOf("\"\n"));

        return result;
    }

}
