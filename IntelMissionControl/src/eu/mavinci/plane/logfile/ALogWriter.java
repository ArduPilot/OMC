/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.logfile;

import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.core.plane.protocol.ProtocolTokens;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class ALogWriter
        implements IAirplaneListenerGuiClose, Closeable, AutoCloseable {

    private boolean m_zip;
    protected boolean m_do_log = false;
    private BufferedWriter m_writer = null;
    protected AirplaneFlightphase m_flightphase;
    protected IAirplane m_plane = null;
    protected String suffix;
    protected FileOutputStream fileOutStream = null;

    public ALogWriter(IAirplane plane) {
        m_plane = plane;
        m_plane.addListener(this);
        plane.baseFolderProperty().addListener(((observable, oldValue, newValue) -> cutLogFile()));
    }

    @Override
    public void close() throws IOException {
        try {
            m_writer.flush();
        } catch (Exception e) {
            // nothing
        }

        try {
            m_writer.close();
        } catch (Exception e) {
            // nothing
        }

        m_writer = null;
        try {
            fileOutStream.close();
        } catch (IOException e) {
            // nothing
        }

        fileOutStream = null;
    }

    public void cutLogFile() {
        if (!m_do_log) {
            return;
        }

        Debug.getLog().log(Level.CONFIG, "Cutting file", new Exception());

        disableLogging(); // this also closes files
        logToFile(getNextFilename()); // creates new files with new timestamps
        enableLogging();
    }

    protected String getNextFilename() {
        String name = m_plane.getLogFolder().getAbsolutePath();
        name += "/";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = df.format(Calendar.getInstance().getTime());
        name += timestamp;
        try {
            name += "_" + m_plane.getAirplaneCache().getFileName();
        } catch (AirplaneCacheEmptyException e) {
        }

        name += "." + suffix;
        if (m_zip) {
            name += ".zip";
        }

        return name;
    }

    protected File logFile;
    protected String file_name;

    public void logToFile(String file_name) {
        if (isLoggingEnabled()) {
            return;
            // if (isLoggingEnabled()) disableLogging();
        }

        if (file_name.endsWith(".zip")) {
            m_zip = true;
        }

        logFile = new File(file_name);
        this.file_name = file_name;
        isEmpty = true;
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        assert (logFile.canWrite());
    }

    protected String getHeader() {
        return null;
    }

    protected void openWriterForLogging() {
        if (m_writer != null) {
            return;
        }

        if (logFile == null) {
            m_zip = true; // zip by default
            logToFile(getNextFilename()); // creates new files with new timestamps
        }

        OutputStream out;
        // Debug.printStackTrace("OPEN logging"+file_name);
        try {
            this.fileOutStream = new FileOutputStream(logFile);
            if (m_zip) {
                out = new ZipOutputStream(this.fileOutStream);
                ((ZipOutputStream)out)
                    .putNextEntry(
                        new ZipEntry(file_name.substring(file_name.lastIndexOf("/"), file_name.indexOf(".zip"))));
            } else {
                out = this.fileOutStream;
            }

            m_writer = new BufferedWriter(new OutputStreamWriter(out, ProtocolTokens.encoding));
            m_writer.write(DependencyInjector.getInstance().getInstanceOf(ILicenceManager.class).getExportHeader());
            String header = getHeader();
            if (header != null) {
                write(header);
            }

            isEmpty = true;
        } catch (FileNotFoundException e) {
            Debug.getLog().log(Level.WARNING, "Cannot open file for writing: " + file_name);
        } catch (IOException e) {
            Debug.getLog().log(Level.WARNING, "Zip error: " + file_name);
        }
    }

    boolean isEmpty = true;

    protected void write(String str) throws IOException {
        if (m_plane.getAirplaneConnector() instanceof ALogReader) {
            return; // dont log replays
        }

        if (m_writer == null) {
            return; // throw new IOException("no output defined");
        }

        isEmpty = false;
        m_writer.write(str);
        m_writer.write("\r\n");
        m_writer.flush();
    }

    // public void recv_flightPhase(Integer fp){
    // AirplaneFlightphase previous_flightphase = m_flightphase;
    // try {
    // m_flightphase = m_plane.getAirplaneCache().getFlightPhase();
    // } catch (AirplaneCacheEmptyException e) {
    // Debug.getLog().log(Level.SEVERE, "could not determine flightphase",e);
    // return;
    // }
    // if (previous_flightphase == null) return;
    //
    // if (m_flightphase.isFlightphaseOnGround() ==0 && previous_flightphase.isFlightphaseOnGround()!=0)
    // cutLogFile();
    // }

    String prevName = null;

    // @Override
    // public void recv_nameChange(String name) {
    // if (name.equals(prevName)) return;
    // prevName = name;
    // cutLogFile();
    // }

    protected abstract void requestAll();

    protected boolean shouldLog() {
        return (m_plane.isWriteable() && m_plane.getPlanePort() != null) || m_do_log; // never disable logging!
    }

    public void autoChangeEnability() {
        boolean shouldLog = shouldLog();
        if (m_do_log && !shouldLog) {
            disableLogging();
        } else if (!m_do_log && shouldLog) {
            enableLogging();
        }
    }

    public void enableLogging(boolean requestAll) {
        openWriterForLogging();
        if (m_do_log) {
            return;
        }

        m_do_log = true;
        if (requestAll) {
            requestAll();
        }

        if (this instanceof LogWriterFLG) {
            m_plane.fireLoggingChangedFLG(true);
        } else if (this instanceof LogWriterVLG) {
            m_plane.fireLoggingChangedTCP(true);
        } else {
            Debug.getLog().log(Level.WARNING, "Unknown Logger to be started");
        }
    }

    public void enableLogging() {
        enableLogging((m_plane.isWriteable() && m_plane.getPlanePort() != null));
    }

    public void disableLogging() {
        // Debug.printStackTrace("disableLogging");
        if (!m_do_log) {
            return;
        }

        m_do_log = false;
        if (this instanceof LogWriterFLG) {
            m_plane.fireLoggingChangedFLG(false);
        } else if (this instanceof LogWriterVLG) {
            m_plane.fireLoggingChangedTCP(false);
        } else {
            Debug.getLog().log(Level.WARNING, "Unknown Logger to be stopped");
        }

        finishAndCloseFile();
    }

    public boolean isLoggingEnabled() {
        return m_do_log;
    }

    private void finishAndCloseFile() {
        m_do_log = false;
        if (m_writer == null) {
            return;
        }

        try {
            close();
        } catch (IOException e) {
            Debug.getLog().log(Level.FINER, "problems disabling logging", e);
        }

        if (isEmpty) {
            // delete log file
            logFile.delete();
        }
    }

    @Override
    public void guiClose() {
        disableLogging();
    }

    @Override
    public boolean guiCloseRequest() {
        return true;
    }

    @Override
    public void storeToSessionNow() {}


}
