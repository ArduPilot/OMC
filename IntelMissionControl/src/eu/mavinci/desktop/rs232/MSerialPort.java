/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.rs232;

import com.intel.missioncontrol.concurrent.Dispatcher;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import jssc.SerialPortList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class MSerialPort {

    private static String[] portsCache = null;

    public static String[] getPorts(boolean forceReload) throws IOException {
        if (portsCache == null || forceReload) portsCache = SerialPortList.getPortNames();
        return portsCache;
    }

    boolean open = true;
    Rs232Params params;
    static Object serialPort = null;

    long nextClose;
    boolean isWriting = false;

    public static final int SAMPLING_INTERVAL_MS = 10;

    WeakListenerList<ISerialPortEvent> listeners =
        new WeakListenerList<ISerialPortEvent>("serial port listener" + this.hashCode());

    boolean hasListenersBeenInformed = false;

    public MSerialPort(Object port, final Rs232Params params) {
        serialPort = port;
        this.params = params;
    }

    public void writeBytes(byte[] buffer, long timeOut) throws IOException {
        try {
            // Debug.getLog().info("write "+new String(buffer,"ISO-8859-1"));
            nextClose = System.currentTimeMillis() + timeOut;
            isWriting = true;
            //serialPort.putData(buffer);
        } finally {
            isWriting = false;
        }
    }

    public void closePort() throws IOException {
        open = false;
        //serialPort.close();
    }

    public boolean isOpened() {
        return false;//serialPort.isOpen();
    }

    byte[] tmp = new byte[1024];

    public byte[] readBytes() throws IOException {
        int len;
        len = 0;//serialPort.getData(tmp);
        hasListenersBeenInformed = false;
        if (len == 0) {
            return null;
        }

        byte[] tmp2 = new byte[len];
        for (int i = 0; i < len; i++) {
            tmp2[i] = tmp[i];
        }
        // System.out.println("read: " +len + " "+ new String(tmp2,"ISO-8859-1"));
        return tmp2;
    }

    boolean isCheckingThreadStarted = false;

    synchronized void startCheckingThreadIfNessesary() {
        if (isCheckingThreadStarted) {
            return;
        }

        isCheckingThreadStarted = true;
        Dispatcher.post(
                new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("checking thread started");
                        // was ist bei neu connecting, wird das object recycelt? wenn ja, geht das so nicht, da dann der
                        // thread weg ist FIXME
                        while (open) {
                            try {
                                Thread.sleep(SAMPLING_INTERVAL_MS);
                            } catch (InterruptedException e) {
                            }
                            // this method always returns false
                            // if (serialPort.readSinceRxBufCheck() == false) continue;
                            if (hasListenersBeenInformed) {
                                continue;
                            }

                            hasListenersBeenInformed = true;

                            for (ISerialPortEvent listener : listeners) {
                                listener.serialPortNewData();
                            }
                        }
                    }

                });
    }

    public BufferedReader getBufferedReader() {
        BufferedReader buf =
            new BufferedReader(
                new Reader() {

                    @Override
                    public int read(char[] cbuf, int off, int len) throws IOException {
                        byte[] bbuf = new byte[len];
                        int ret = 0;//serialPort.getData(bbuf, off, len);
                        for (int i = off; i < len && i < ret + off; i++) {
                            cbuf[i] = (char)bbuf[i];
                        }

                        return ret;
                    }

                    @Override
                    public void close() throws IOException {
                        MSerialPort.this.closePort();
                    }
                });
        return buf;
    }

    public void addListener(ISerialPortEvent l) {
        startCheckingThreadIfNessesary();
        listeners.add(l);
    }

    public void addListenerAtBegin(ISerialPortEvent l) {
        startCheckingThreadIfNessesary();
        listeners.addAtBegin(l);
    }

    public void removeListener(ISerialPortEvent l) {
        listeners.remove(l);
    }

    public void writeString(String string) throws IOException {
        writeBytes(string.getBytes("ISO-8859-1"), TIMEOUT);
    }

    public static final int TIMEOUT = 5000;

    public Rs232Params getParams() {
        return params;
    }

}
