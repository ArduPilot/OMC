package org.droidplanner.services.android.impl.utils;

import android.content.Context;
import android.os.Bundle;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class NetworkUtils {
    /**
     * Is internet connection available. This method also returns true for the SITL build type
     * @param context
     * @return Internet connection availability.
     */
    public static boolean isNetworkAvailable(Context context) {
        return true;
    }

    public static String getCurrentWifiLink(Context context) {
        return "";
    }


    public static boolean isSoloNetwork(String ssid) {
        return false;
    }


    /** doesn't do anything on desktop */
    public static void bindSocketToNetwork(Bundle extras, DatagramSocket socket) throws IOException {

    }

    /** doesn't do anything on desktop */
    public static void bindSocketToNetwork(Bundle extras, Socket socket) throws IOException {

    }


}
