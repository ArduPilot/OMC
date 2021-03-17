/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import com.intel.missioncontrol.utils.IVersionProvider;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.main.OsTypes;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import org.asyncfx.concurrent.Dispatcher;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class DCRaw {

    private static String dcrawExe;

    public static String getDCRawExe() throws Exception {
        if (dcrawExe == null) {
            IVersionProvider versionProvider = DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class);

            OsTypes system = DependencyInjector.getInstance().getInstanceOf(IVersionProvider.class).getSystem();
            if (versionProvider.isEclipseLaunched()) {
                File installDirParent = versionProvider.getInstallDir().getAbsoluteFile().getParentFile();
                if (installDirParent == null) {
                    dcrawExe = "dcraw";
                } else if (system.isWindows()) {
                    dcrawExe =
                        new File(installDirParent.getParentFile(), "MAVinciDesktopWin\\lib64\\dcraw-9.26-ms-64-bit.exe")
                            .getAbsolutePath();
                } else if (system.isMac()) {
                    dcrawExe =
                        new File(installDirParent.getParentFile(), "MAVinciDesktopMac\\lib\\dcraw").getAbsolutePath();
                } else {
                    dcrawExe = "dcraw";
                }
            } else if (system.isWindows()) {
                dcrawExe = new File(versionProvider.getInstallDir(), "dcraw-9.26-ms-64-bit.exe").getAbsolutePath();
            } else if (system.isMac()) {
                dcrawExe = new File(versionProvider.getInstallDir(), "dcraw").getAbsolutePath();
            } else {
                dcrawExe = "dcraw";
            }
        }

        return dcrawExe;
    }

    public static File callingDCRaw(final File in) throws IOException, Exception {
        Debug.getLog().fine("[dcraw-called]:in=" + in);
        Process process =
            ProcessHelper.exec(
                new String[] {getDCRawExe(), "-c", "-t", "0", "-T", "-4", "-o", "0", in.getAbsolutePath()});
        final File out = new File(MFileFilter.rawFilter.removeExtension(in) + ".tif");
        final File outTmp = new File(MFileFilter.rawFilter.removeExtension(in) + ".tif~" + System.currentTimeMillis());
        process.getOutputStream().close();
        try (InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream()) {
            String line;
            Dispatcher dispatcher = Dispatcher.background();
            dispatcher.run(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            FileHelper.writeStream(stdout, outTmp);
                        } catch (IOException e) {
                            Debug.getLog().log(Level.FINE, "problem writing TIFF file. " + in + " -> " + outTmp, e);
                        }
                    }
                });
            BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stderr));
            while ((line = brCleanUp.readLine()) != null) {
                // System.out.println("[Stderr] " + line);
                Debug.getLog().warning("[dcraw-err]" + line);
            }

            int exitVal = process.waitFor();
            // System.out.println("exit:"+exitVal);
            if (exitVal != 0) {
                throw new Exception("dcraw returned error code: " + exitVal + "  " + in + " -> " + outTmp);
            }
        }

        FileHelper.move(outTmp, out);
        return out;
    }

    public static File callingDCRawThumpnail(final File in) throws IOException, Exception {
        Debug.getLog().fine("[dcraw-called thumb]:in=" + in);
        Process process = ProcessHelper.exec(new String[] {getDCRawExe(), "-c", "-e", "-t", "0", in.getAbsolutePath()});
        final File out = new File(MFileFilter.rawFilter.removeExtension(in) + ".jpg");
        final File outTmp = new File(MFileFilter.rawFilter.removeExtension(in) + ".jpg~" + System.currentTimeMillis());
        process.getOutputStream().close();
        try (InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream()) {
            String line;
            Dispatcher dispatcher = Dispatcher.background();
            dispatcher.run(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            FileHelper.writeStream(stdout, outTmp);
                        } catch (IOException e) {
                            Debug.getLog()
                                .log(Level.FINE, "problem extrecting thumbnail file. " + in + " -> " + outTmp, e);
                        }
                    }
                });
            BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stderr));
            while ((line = brCleanUp.readLine()) != null) {
                // System.out.println("[Stderr] " + line);
                Debug.getLog().warning("[dcraw-err]" + line);
            }

            int exitVal = process.waitFor();
            // System.out.println("exit:"+exitVal);
            if (exitVal != 0) {
                throw new Exception("dcraw returned error code: " + exitVal + "  " + in + " -> " + outTmp);
            }
        }

        FileHelper.move(outTmp, out);
        // boolean ok = outTmp.renameTo(out);
        // System.out.println("outT");
        return out;
    }

    public static BufferedImage callingDCRawToBufferedImage(final File in) throws Exception {
        Debug.getLog().fine("[dcraw-called]:in=" + in);
        Process process =
            ProcessHelper.exec(
                new String[] {getDCRawExe(), "-c", "-t", "0", "-T", "-4", "-o", "0", in.getAbsolutePath()});
        process.getOutputStream().close();

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream()) {
            String line;
            Dispatcher dispatcher = Dispatcher.background();
            dispatcher.run(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            InputStream is = stdout;

                            int bytesRead;
                            byte[] data = new byte[16384];

                            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, bytesRead);
                            }

                            buffer.flush();

                        } catch (Throwable e) {
                            Debug.getLog().log(Level.FINE, "problem writing TIFF image to buffered image. " + in, e);
                        }
                    }
                });
            BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stderr));
            while ((line = brCleanUp.readLine()) != null) {
                // System.out.println("[Stderr] " + line);
                Debug.getLog().warning("[dcraw-err]" + line);
            }

            int exitVal = process.waitFor();

            byte[] bufferArr = buffer.toByteArray();
            // System.out.println("ioLem = " +bufferArr.length);
            ByteArrayInputStream is = new ByteArrayInputStream(bufferArr);
            // System.out.println(bufferArr);
            BufferedImage img = ImageHelper.loadTiffNonWWJ(is);

            // System.out.println("exit:"+exitVal);
            if (exitVal != 0) {
                throw new Exception("dcraw returned error code: " + exitVal + "  " + in);
            }
            // System.out.println("img-return: "+img);
            return img;
        }
    }
}
