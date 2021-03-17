/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.ntripclient;

import eu.mavinci.desktop.bluetooth.BTService;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;

public class ConnectionObjects implements Closeable {
    private BufferedReader reader;
    private InputStream is;
    // private OutputStream os;
    private Socket socket;
    private PrintWriter pw;
    StreamConnection btConn;

    long trafficOut;
    long trafficIn;

    public long getTrafficIn() {
        return trafficIn;
    }

    public long getTrafficOut() {
        return trafficOut;
    }

    public boolean expectHeader() {
        return socket == null;
    }

    public ConnectionObjects(HttpURLConnection httpConnection) throws IOException {
        this(
            httpConnection.getInputStream(),
            null);
    }

    public ConnectionObjects(Socket socket) throws IOException {
        this(socket.getInputStream(), socket.getOutputStream());
        this.socket = socket;
    }

    public ConnectionObjects(InputStream is, OutputStream os) {
        this.is = is;
        // this.os = os;
        if (os != null) {
            this.pw = new PrintWriter(os);
        }
    }

    public ConnectionObjects(BTService btService) throws IOException {
        btConn = (StreamConnection)Connector.open(btService.getUrl());
        this.is = btConn.openDataInputStream();
        this.pw = new PrintWriter(btConn.openDataOutputStream());
    }

    private BufferedReader getReader() {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(is));
            System.out.println("reader created");
        }

        return reader;
    }

    public String readLine() throws IOException {
        String line = getReader().readLine();
        if (line != null) {
            trafficIn += line.length() + 2;
        }

        return line;
    }

    @Override
    public void close() throws IOException {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
        }

        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
        }

        try {
            if (pw != null) {
                pw.close();
            }
        } catch (Exception e) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
        }

        try {
            if (btConn != null) {
                btConn.close();
            }
        } catch (Exception e) {
        }
    }

    public void print(String toSend) {
        if (pw == null) {
            return;
        }

        pw.print(toSend);
        trafficOut += toSend.length();
    }

    public void flush() {
        if (pw == null) {
            return;
        }

        pw.flush();
    }

    public int read(byte[] b) throws IOException {
        //        if (is.available() <= 0) {
        //            return 0;
        //        }
        int used = is.read(b);
        if (used > 0) {
            trafficIn += used;
        }

        return used;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        //        if (is.available() <= 0) {
        //            return 0;
        //        }
        int used = is.read(b, off, len);
        if (used > 0) {
            trafficIn += used;
        }

        return used;
    }
}
