/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.helper.SystemInformation;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.notifications.Toast;
import com.intel.missioncontrol.ui.notifications.ToastType;
import com.intel.missioncontrol.utils.IVersionProvider;
import com.intel.missioncontrol.utils.VersionProvider;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import eu.mavinci.core.flightplan.CDump;
import eu.mavinci.core.helper.CFileHelper;
import eu.mavinci.core.helper.Pair;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.desktop.gui.widgets.MProgressMonitor;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.desktop.main.debug.UserNotificationHubSwing;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.io.FilenameUtils;
import org.asyncfx.concurrent.Dispatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FileHelper extends CFileHelper {

    public static final String KEY = "eu.mavinci.desktop.helper.FileHelper";
    public static final Integer LAST_SENT_NATIVE = 2;
    public static final Integer LAST_SENT_URL = 1;
    public static Integer LAST_SENT = 0;

    private static void addFiles(ArrayList<File> files, File[] toAdd) {
        if (toAdd == null || toAdd.length == 0) {
            return;
        }

        files.addAll(Arrays.asList(toAdd));
    }

    public static ArrayList<File> getSystemDrives() {
        ArrayList<File> roots = new ArrayList<File>();
        if (SystemInformation.isWindows()) {
            addFiles(roots, File.listRoots());
        } else {
            roots.add(new File("/"));
            addFiles(roots, new File("/mnt").listFiles(MFileFilter.folderFilter));
            addFiles(roots, new File("/media").listFiles(MFileFilter.folderFilter));
            addFiles(roots, new File("/mount").listFiles(MFileFilter.folderFilter));
            addFiles(
                roots, new File("/var/run/media", System.getProperty("user.name")).listFiles(MFileFilter.folderFilter));
        }

        return roots;
    }

    /**
     * this version of rename is able to move even above filesystem borders, and folders into its subfolders
     *
     * @param sourceLocation
     * @param targetLocation
     * @throws IOException
     */
    public static void move(File sourceLocation, File targetLocation) throws IOException {
        // System.out.println("moving " + sourceLocation + " to:" +
        // targetLocation);
        if (!sourceLocation.renameTo(targetLocation)) {
            // System.out.println("advanced stuff");
            if (sourceLocation.isDirectory()) {
                String[] children = sourceLocation.list();
                if (!targetLocation.exists()) {
                    targetLocation.mkdir();
                }

                if (children != null) {
                    for (int i = 0; i < children.length; i++) {
                        move(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
                        String[] sourcelocationList = sourceLocation.list();
                        if (sourcelocationList != null && sourcelocationList.length == 0) {
                            sourceLocation.delete();
                        }
                    }
                }
            } else {
                if (targetLocation.exists()) {
                    targetLocation.delete();
                }

                if (!sourceLocation.renameTo(targetLocation)) {
                    copyFile(sourceLocation, targetLocation);
                    sourceLocation.delete();
                }
            }
        }
    }

    /**
     * Extracting one folder in JAR to one disk folder, but keep local changes as long as their is no remote change, by
     * monitoring last extracting in one subfolder
     *
     * <p>list if null, just ignore it. otherwise extract only filenames contained here!
     */
    public static boolean copyJarFolderToDiskInclAsking(
            ILanguageHelper languageHelper,
            ILicenceManager licenceManager,
            String jarPath,
            File destFolder,
            List<String> list) {
        try {
            Debug.getLog().log(Level.FINE, "Extract external config folder into appSettings directory:" + destFolder);

            File defaults = new File(destFolder, "factory_defaults");
            defaults.mkdirs();

            File tmp = new File(defaults, "tmp");
            FileHelper.deleteDir(languageHelper, tmp, true);
            tmp.mkdirs();

            FileHelper.scanFilesJarAndWriteToDisk(MFileFilter.getAllFilterNonSVNnonTmp(licenceManager), jarPath, tmp);
            File[] jarDefaultFiles = tmp.listFiles();
            if (jarDefaultFiles == null) {
                return false;
            }

            for (File jarDefaultFile : jarDefaultFiles) {
                if (list != null) {
                    // System.out.println("\nf:"+f.getName());
                    boolean found = false;
                    for (String s : list) {
                        // System.out.println("s:"+s);
                        String filter = s;
                        String filterNot = "*";
                        if (jarPath.equals("com/intel/missioncontrol/descriptions/cameras/")) {
                            filter = s.replace(" 8+", "");
                        }

                        if (jarPath.equals("com/intel/missioncontrol/descriptions/lenses/")) {
                            if (s.equals("Falcon 8+")) {
                                filter = "";
                                filterNot = "DJI";
                            }
                        }

                        if (jarPath.equals("com/intel/missioncontrol/templates/")) {
                            filter = s.replace("GrayHawk", "Gray Hawk");
                            if ((licenceManager.getMaxOperationLevel().compareTo(OperationLevel.TECHNICIAN) >= 0
                                        || licenceManager.isGrayHawkEditionProperty().get())
                                    && jarDefaultFile.getName().startsWith("_")
                                    && jarDefaultFile.getName().contains(" - " + filter)) {
                                found = true;
                                break;
                            }

                            if (jarDefaultFile.getName().contains(" - " + filter)
                                    && !jarDefaultFile.getName().startsWith("_")) {
                                found = true;
                                break;
                            }
                        } else {
                            if ((licenceManager.getMaxOperationLevel().compareTo(OperationLevel.TECHNICIAN) >= 0
                                        || licenceManager.isGrayHawkEditionProperty().get())
                                    && jarDefaultFile.getName().startsWith("_" + filter)) {
                                found = true;
                                break;
                            }

                            if (jarDefaultFile.getName().startsWith(filter)
                                    && !jarDefaultFile.getName().startsWith("_")
                                    && !jarDefaultFile.getName().startsWith(filterNot)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    // System.out.println("found:"+found);
                    if (!found) {
                        jarDefaultFile.delete();
                        continue;
                    }
                }

                File currentDefaultFile = new File(defaults, jarDefaultFile.getName());
                File currentUserFile = new File(destFolder, jarDefaultFile.getName());

                if (!currentUserFile.exists()) {
                    FileHelper.copyFile(jarDefaultFile, currentUserFile);
                    FileHelper.move(jarDefaultFile, currentDefaultFile);
                } else {
                    if (FileHelper.equalContent(jarDefaultFile, currentDefaultFile)) {
                        // only overwrite content on disk if defaults in installer have changed, otherwise keep the disk
                        // modifications
                        continue;
                    }

                    FileHelper.copyFile(jarDefaultFile, currentDefaultFile);
                    File backup = new File(destFolder, "old_" + currentUserFile.getName() + "~");
                    FileHelper.copyFile(currentUserFile, FileHelper.getNextFreeFilename(backup));
                    FileHelper.copyFile(jarDefaultFile, currentUserFile);
                }
            }

            FileHelper.deleteDir(languageHelper, tmp, true);
            return true;
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Extracting default settings folder failed ", e);
            return false;
        }
    }

    public static boolean copyFile(File sourceFile, File destFile, boolean askOverwrite, boolean leaveIfEqualContent)
            throws IOException {
        // System.out.println("copy (askOverwrite="+askOverwrite+"): " + sourceFile + " -> " + destFile + "
        // equals="+equals(sourceFile,destFile));
        if (!sourceFile.exists()) {
            throw new FileNotFoundException(sourceFile.getAbsolutePath());
        }

        try {
            if (equals(sourceFile, destFile)) {
                return true;
            }

            if (leaveIfEqualContent) {
                if (equalContent(sourceFile, destFile)) {
                    return true;
                }
            }

            copyFile(sourceFile, destFile, askOverwrite);

        } catch (Exception e) {
            throw new IOException("problem copy " + sourceFile + " -> " + destFile, e);
        }

        return true;
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        return copyFile(sourceFile, destFile, false);
    }

    public static boolean copyFile(File sourceFile, File destFile, boolean askOverwrite) throws IOException {
        // System.out.println("copy (askOverwrite="+askOverwrite+"): " + sourceFile + " -> " + destFile + "
        // equals="+equals(sourceFile,destFile));
        if (!sourceFile.exists()) {
            throw new FileNotFoundException(sourceFile.getAbsolutePath());
        }

        try {
            if (equals(sourceFile, destFile)) {
                return true;
            }

            File destFileTmp =
                getNextFreeFilename(new File(destFile.getAbsolutePath() + "~" + System.currentTimeMillis()));

            if (!destFile.exists()) {
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }

                destFileTmp.createNewFile();
            } else if (askOverwrite) {
                if (!askForOverwrite(destFile, null)) {
                    return false;
                }
            }

            // FileChannel source = null;
            // FileChannel destination = null;
            // try {
            // source = new FileInputStream(sourceFile).getChannel();
            // destination = new FileOutputStream(destFile).getChannel();
            // long count = 0;
            // long size = sourceFile.length();
            // while (count < size) {
            // //count += source.transferTo(count, size - count, destination);
            // count += destination.transferFrom(source, count, size - count);
            // }
            Files.copy(
                sourceFile.toPath(),
                destFileTmp.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);
            long time = sourceFile.lastModified();
            if (time > 0) {
                // some falcon logfiles have a invalid timestamp (negative) which would cause an exception if we try to
                // restore it
                destFileTmp.setLastModified(time);
            }

            destFileTmp.setExecutable(sourceFile.canExecute());
            if (!destFile.equals(destFileTmp)) {
                // otherwise other SW might try to parse the data while its still copying
                if (!destFileTmp.renameTo(destFile)) {
                    destFile.delete();
                    destFileTmp.renameTo(destFile);
                }
            }
            // } finally {
            // try {
            // source.close();
            // } catch (Throwable t) {
            // }
            // try {
            // destination.close();
            // } catch (Throwable t) {
            // }
            // }
        } catch (Exception e) {
            throw new IOException("problem copy " + sourceFile + " -> " + destFile, e);
        }

        return true;
    }

    public interface ICopyCallback {
        void copyFailed(Throwable e);

        void copyFinised();

        void copyCancelt();
    }

    /** copy directory without progressbar on current thread */
    public static void copyDirectorySynchron(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            String[] children = sourceLocation.list();
            if (children == null) {
                return;
            }

            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            long time = sourceLocation.lastModified();
            if (time > 0) {
                // some falcon logfiles have a invalid timestamp (negative) which would cause an exception if we try to
                // restore it
                targetLocation.setLastModified(time);
            }

            targetLocation.setExecutable(sourceLocation.canExecute());

            for (String child : children) {
                copyDirectorySynchron(new File(sourceLocation, child), new File(targetLocation, child));
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    public static void copyDirectory(
            final File sourceLocation, final File targetLocation, final ICopyCallback callback) {
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            new Runnable() {

                @Override
                public void run() {
                    final ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
                    MProgressMonitor mon =
                        new MProgressMonitor(
                            null,
                            languageHelper.getString(KEY + ".copy.msg"),
                            languageHelper.getString(KEY + ".copy.note.init", sourceLocation.getPath()),
                            -1,
                            100);
                    Vector<File> files = scanFiles(null, sourceLocation, -1);
                    try {
                        long size = 0;
                        for (File f : files) {
                            if (f.isDirectory()) {
                                continue;
                            }

                            size += f.length();
                        }

                        mon.setMaximum((int)(size / 100));
                        final boolean ret;
                        if (size >= targetLocation.getFreeSpace()) {
                            ret = false;

                            StaticInjector.getInstance(IApplicationContext.class)
                                .addToast(
                                    Toast.of(ToastType.ALERT)
                                        .setText(
                                            languageHelper.getString(
                                                KEY + ".copy.notEngouhDiskSpace.msg",
                                                sourceLocation,
                                                targetLocation,
                                                StringHelper.bytesToIngName(size, -3, false),
                                                StringHelper.bytesToIngName(targetLocation.getFreeSpace(), -3, false)))
                                        .setShowIcon(true)
                                        .create());
                        } else {
                            ret = copyDirectory(sourceLocation, targetLocation, mon, size);
                        }

                        Dispatcher platform = Dispatcher.platform();
                        platform.run(
                            new Runnable() {

                                @Override
                                public void run() {
                                    if (ret) {
                                        callback.copyFinised();
                                    } else {
                                        callback.copyCancelt();
                                    }
                                }
                            });
                    } catch (final Throwable e) {
                        Dispatcher platform = Dispatcher.platform();
                        platform.run(
                            new Runnable() {

                                @Override
                                public void run() {
                                    callback.copyFailed(e);
                                }
                            });
                    } finally {
                        mon.close();
                    }
                }
            });
    }

    private static boolean copyDirectory(File sourceLocation, File targetLocation, MProgressMonitor mon, long total)
            throws IOException {
        if (mon.isCanceled()) {
            return false;
        }

        ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
        mon.setProgressNote(
            languageHelper.getString(
                KEY + ".copy.note",
                StringHelper.bytesToIngName(mon.tmpLon, -3, true),
                StringHelper.bytesToIngName(total, -3, true),
                targetLocation.getName()),
            (int)(mon.tmpLon / 100));

        if (sourceLocation.isDirectory()) {
            String[] children = sourceLocation.list();
            if (children == null) {
                return false;
            }

            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            for (String child : children) {
                if (!copyDirectory(new File(sourceLocation, child), new File(targetLocation, child), mon, total)) {
                    return false;
                }
            }

            return true;
        } else {
            copyFile(sourceLocation, targetLocation);
            mon.tmpLon += sourceLocation.length();
            return true;
        }
    }

    public static void openFile(File f) throws IOException {
        if (!f.exists()) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }

        if (!f.isFile() && !f.isDirectory()) {
            throw new IOException("File to open is not a file or folder : " + f.getAbsolutePath());
        }

        openFileOrURL(f.getAbsolutePath());
    }

    public static void openFileOrURL(String path) throws IOException {
        if (SystemInformation.isWindows()) {
            ProcessHelper.exec(new String[] {"rundll32", "url.dll,FileProtocolHandler", path});
        } else if (SystemInformation.isLinux()) {
            ProcessHelper.exec(new String[] {"xdg-open", path});
        } else if (SystemInformation.isMac()) {
            ProcessHelper.exec(new String[] {"open", path});
        } else {
            throw new RuntimeException("Unsupported operating system.");
        }
    }

    public static void sendEmailNativeClient(String toAddress, String ccAddress, String subject, String body)
            throws Exception {
        if (body.length() > 1000) {
            body = body.substring(0, 997) + "...";
        }

        String escapedSubject = URLEncoder.encode(subject, "UTF-8").replace("+", "%20");
        String escapedBody = URLEncoder.encode(body, "UTF-8").replace("+", "%20");
        String uri;

        toAddress = toAddress.replace(",", ";");
        if (ccAddress != null) ccAddress = ccAddress.replace(",", ";");
        if (UpdateURL.DEV_EMAIL_MAVINCI.equalsIgnoreCase(toAddress)) {
            uri =
                String.format(
                    "mailto:%s?cc=%s&subject=%s&body=%s",
                    toAddress,
                    URLEncoder.encode(ccAddress != null ? ccAddress : "", "UTF-8"),
                    escapedSubject,
                    escapedBody);
        } else {
            uri =
                String.format(
                    "mailto:%s?cc=%s&bcc=%s&subject=%s&body=%s",
                    toAddress,
                    URLEncoder.encode(ccAddress != null ? ccAddress : "", "UTF-8"),
                    URLEncoder.encode(UpdateURL.ENTERPRISE_EMAIL, "UTF-8"),
                    escapedSubject,
                    escapedBody);
        }

        openFileOrURL(uri);
        LAST_SENT = LAST_SENT_NATIVE;
    }

    public static void sendEmailNativeClient(String toAddress, String subject, String body) throws Exception {
        sendEmailNativeClient(toAddress, null, subject, body);
    }

    public static boolean sendEMail(String toAddress, String subject, String body) {
        return sendEMail(toAddress, null, subject, body);
    }

    public static boolean sendEMail(String toAddress, String ccAddress, String subject, String body) {
        if (!UpdateURL.sendEMail(toAddress, ccAddress, subject, body, Level.FINEST)) {
            try {
                sendEmailNativeClient(toAddress, ccAddress, subject, body);
                LAST_SENT = LAST_SENT_NATIVE;
                return true;
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "could not send eMail with native eMail client", e);
                return false;
            }
        }

        LAST_SENT = LAST_SENT_URL;
        return true;
    }

    public static void sendEmailNativeClient(String toAddress, String subject, String body, File attachment)
            throws Exception {
        body +=
            "\n\nattachment: "
                + attachment.getAbsolutePath()
                + "\nIf the attachment is not available in the email, add it manually";
        if (body.length() > 1000) {
            body = body.substring(0, 997) + "...";
        }

        openFileOrURL(
            "mailto:"
                + toAddress
                + "?subject="
                + URLEncoder.encode(subject, "UTF-8").replace("+", "%20")
                + "&body="
                + URLEncoder.encode(body, "UTF-8").replace("+", "%20")
                + "&attachment="
                + URLEncoder.encode(attachment.getAbsolutePath(), "UTF-8").replace("+", "%20"));
        LAST_SENT = LAST_SENT_NATIVE;
    }

    public static boolean sendEMail(String toAddress, String subject, String body, File attachment) {
        try {
            sendEmailNativeClient(toAddress, subject, body, attachment);
            LAST_SENT = LAST_SENT_NATIVE;
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "could not send eMail with native eMail client", e);
            return false;
        }

        return true;
    }

    private static String[] deniedPaths =
        new String[] {
            "drive:A:", "drive:B:", "drive:C:", "drive:X:", "drive:Y:", "drive:Z:", "A:", "B:", "C:", "X:", "Y:", "Z:"
        };

    private static boolean couldBeSdCard(File f) {
        if (!f.isDirectory()) return false;
        String name = f.getName();
        for (String s : deniedPaths) {
            if (name.startsWith(s)) return false;
        }

        return true;
    }

    public static File tryToFindInitialLogSdCardFolder(File drive) {
        // try to detect classical falcon SD card
        File asctecFolder = new File(drive, "ASCTEC");
        File[] allFilesInAsctecFolder = asctecFolder.listFiles();

        if (allFilesInAsctecFolder != null) {
            for (File ff : allFilesInAsctecFolder) {
                if (MFileFilter.ascTecLogFolder.acceptTrinityLog(ff)) {
                    // found!
                    return asctecFolder;
                }
            }
        }
        // try to detect classical falcon SD card
        File intelFolder = new File(drive, "INTEL");
        File[] allFilesInIntelFolder = intelFolder.listFiles();

        if (allFilesInIntelFolder != null) {
            for (File ff : allFilesInIntelFolder) {
                if (MFileFilter.ascTecLogFolder.acceptTrinityLog(ff)) {
                    // found!
                    return intelFolder;
                }
            }
        }

        // try to detect JSON SD-card (new falcon payloads)
        File[] allFilesInDrive = drive.listFiles();
        if (allFilesInDrive != null) {
            LinkedList<File> allFilesInDriveList = new LinkedList<>();
            Arrays.asList(allFilesInDrive).forEach(allFilesInDriveList::add);
            Collections.sort(allFilesInDriveList);
            Collections.reverse(allFilesInDriveList);
            for (File driveFolder : allFilesInDriveList) {
                File[] driveFolderContent = driveFolder.listFiles();

                if (driveFolderContent != null) {
                    for (File maybeLogfile : driveFolderContent) {
                        if (MFileFilter.photoJsonFilter.accept(maybeLogfile.getName())) {
                            // found!
                            return drive;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static File tryToFindInitialLogSdCard() {
        for (File drive : FileHelper.getSystemDrives()) {
            if (!drive.exists() || !couldBeSdCard(drive)) {
                continue;
            }

            File folder = tryToFindInitialLogSdCardFolder(drive);
            if (folder != null) {
                return folder;
            }
        }

        return null;
    }

    public static File tryToFindInitialImagesSdCard() {
        for (File f : FileHelper.getSystemDrives()) {
            if (!couldBeSdCard(f)) {
                continue;
            }

            File dcim = new File(f, "DCIM");
            boolean detected = false;
            if (dcim.exists()) {
                f = dcim;
                detected = true;
            }

            dcim = new File(f, "dcim");
            if (!detected && dcim.exists()) {
                f = dcim;
                detected = true;
            }

            if (detected) {
                File subFolder = new File(f, "100MSDCF");
                if (subFolder.exists()) {
                    f = subFolder;
                }
            }

            if (!detected) {
                // search for eg.g redEdge 0001SET folders
                int i = 0;
                File fOrg = f;
                while (true) {
                    dcim = new File(fOrg, String.format("%04d", i) + "SET");
                    if (dcim.exists()) {
                        f = dcim;
                        detected = true;
                    } else {
                        break;
                    }

                    i++;
                }
            }

            if (detected) return f;
        }

        return null;
    }

    public static Vector<File> getSDcardJPEGs(File f, FileFilterUniversal filter) {
        File dcim = new File(f, "DCIM");
        boolean detected = false;
        if (dcim.exists()) {
            f = dcim;
            detected = true;
        }

        dcim = new File(f, "dcim");
        if (!detected && dcim.exists()) {
            f = dcim;
            detected = true;
        }

        if (!detected) {
            // search for eg.g redEdge 0001SET folders
            int i = 0;
            File fOrg = f;
            while (true) {
                dcim = new File(fOrg, String.format("%04d", i) + "SET");
                if (dcim.exists()) {
                    f = dcim;
                    detected = true;
                } else {
                    break;
                }

                i++;
            }
        }
        // System.out.println("f:"+f);
        return FileHelper.scanFiles(filter, f, 2);
    }

    public static Vector<File> getRikolaTiffs(File f) {
        return FileHelper.scanFiles(MFileFilter.rikolaTiffs, f, 2);
    }

    /**
     * Searches a filder recursive for File's (or Folders) which match a FileFilter.
     *
     * @param f
     * @param baseFolder
     * @param maxDepth the maximal number of folders to enter. If <0, it fill scan infinitely
     * @return
     */
    public static Vector<File> scanFiles(FileFilterUniversal f, File baseFolder, int maxDepth) {
        Vector<File> result = new Vector<File>();
        scanFiles(f, baseFolder, maxDepth, result);
        return result;
    }

    private static void scanFiles(FileFilterUniversal f, File baseFolder, int maxDepth, Vector<File> result) {
        if (!baseFolder.exists()) {
            Debug.getLog()
                .log(
                    Level.FINE,
                    "Problems on dir listing",
                    new RuntimeException("base folder/file for file search not found: " + baseFolder));
            return;
        }

        if (f == null || f.accept(baseFolder)) {
            result.add(baseFolder);
        }

        if (maxDepth == 0) {
            return;
        }

        if (!baseFolder.isDirectory()) {
            return;
        }

        File[] list = baseFolder.listFiles(f);
        if (list == null) {
            return;
        }

        for (File tmp : list) {
            scanFiles(f, tmp, maxDepth - 1, result);
        }
    }

    private static VersionProvider versionProvider = new VersionProvider();

    /**
     * scans the containing jar file or source code location (if launched from eclipse) for stuff what matches the
     * Filter f, and which is starting with the suffix fromDir. All this files are copyed recursively to the harddist in
     * the folder targetDir in the same structure they where below the folder fromDir. files will be overwritten without
     * asking. but no other files will be touched
     *
     * @param f
     * @param fromDir
     * @param targetDir
     * @return the list of files written to disk
     * @throws IOException
     */
    public static Vector<File> scanFilesJarAndWriteToDisk(FileFilterUniversal f, String fromDir, File targetDir) {
        try {
            File jarF = versionProvider.getCodeSourceFile();
            Ensure.notNull(jarF, "jarF");
            // File jarF = new
            // File("/home/marco/mavinci/drohne-src/branches/2.4/MAVinciDesktopLinux64/MAVinciDesktopLinux64.jar");//
            // new
            // File(jar.getFile());
            fromDir = fromDir.replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"));

            Vector<File> result = new Vector<File>();
            if (versionProvider.isEclipseLaunched()) {
                Vector<File> files =
                    FileHelper.scanFiles(
                        f,
                        new File(
                            jarF, fromDir.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator))),
                        -1);
                // System.out.println("scan result:"+files);
                int prefixLen =
                    jarF.getAbsolutePath()
                            .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"))
                            .length()
                        + 1; // +1
                // makes
                // also
                // this
                // filenames
                // occour
                // without
                // leading
                // "/"
                // System.out.println("prefixLen:"+prefixLen);
                for (File file : files) {
                    if (file.getName().equals(".svn")) {
                        continue;
                    }

                    String orgName =
                        file.getAbsolutePath().replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"));
                    // System.out.println("orgName:"+orgName + " " + orgName.length());
                    if (orgName.length() < prefixLen) {
                        continue;
                    }

                    orgName = orgName.substring(prefixLen);
                    // System.out.println("orgName:"+orgName);
                    if (!orgName.startsWith(fromDir)) {
                        continue;
                    }

                    File target =
                        new File(targetDir.getAbsolutePath() + File.separator + orgName.substring(fromDir.length()));
                    if (file.isDirectory()) {
                        target.mkdirs();
                    } else {
                        copyFile(file, target);
                    }

                    result.add(target);
                }
            } else {
                try (JarFile jarFile = new JarFile(jarF)) { // auto close
                    final Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        final JarEntry entry = entries.nextElement();
                        String orgName = entry.getName();
                        // System.out.println("entry:"+orgName);
                        if (!f.accept(new File(orgName))) {
                            continue;
                        }
                        // System.out.println("acepted by filter");
                        orgName = orgName.replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"));
                        if (!orgName.startsWith(fromDir)) {
                            continue;
                        }
                        // System.out.println("orgName:"+orgName);
                        // System.out.println("acepted by prefix");

                        File target =
                            new File(
                                targetDir.getAbsolutePath() + File.separator + orgName.substring(fromDir.length()));
                        if (entry.isDirectory()) {
                            target.mkdirs();
                        } else {
                            target.getParentFile().mkdirs();
                            InputStream is = jarFile.getInputStream(entry); // get the input stream
                            writeStream(is, target);
                        }
                        // System.out.println("written!");
                        result.add(target);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Extracting default settings folder failed ", e);
            return null;
        }
    }

    public static Vector<String> scanFilesJar(FileFilterUniversal f, String fromDir, File jarApplicationFile)
            throws Exception {
        IVersionProvider versionProvider = StaticInjector.getInstance(IVersionProvider.class);

        File jarF = (jarApplicationFile != null) ? jarApplicationFile : versionProvider.getCodeSourceFile();
        Ensure.notNull(jarF, "jarF");
        // File jarF = new
        // File("/home/marco/mavinci/drohne-src/branches/2.4/MAVinciDesktopLinux64/MAVinciDesktopLinux64.jar");// new
        // File(jar.getFile());
        fromDir = fromDir.replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"));

        Vector<String> result = new Vector<String>();
        if (versionProvider.isEclipseLaunched()) {
            System.out.println(jarF + "  " + fromDir);
            Vector<File> files =
                FileHelper.scanFiles(
                    f,
                    new File(jarF, fromDir.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator))),
                    -1);
            System.out.println("scan result:" + files);
            int prefixLen =
                jarF.getAbsolutePath().replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/")).length()
                    + 1; // +1
            // makes
            // also
            // this
            // filenames
            // occour
            // without
            // leading
            // "/"
            // System.out.println("prefixLen:"+prefixLen);
            for (File file : files) {
                if (file.getName().equals(".svn")) {
                    continue;
                }

                String orgName =
                    file.getAbsolutePath().replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"));
                // System.out.println("orgName:"+orgName + " " + orgName.length());
                if (orgName.length() < prefixLen) {
                    continue;
                }

                orgName = orgName.substring(prefixLen);
                // System.out.println("orgName:"+orgName);
                if (!orgName.startsWith(fromDir)) {
                    continue;
                }

                if (jarApplicationFile != null) {
                    if (!file.isDirectory()) {
                        result.add(orgName);
                    }
                } else {
                    result.add(orgName);
                }
            }
        } else {
            try (JarFile jarFile = new JarFile(jarF)) { // auto close
                final Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();
                    String orgName = entry.getName();
                    // System.out.println("entry:"+orgName);
                    if (!f.accept(new File(orgName))) {
                        continue;
                    }
                    // System.out.println("acepted by filter");
                    orgName = orgName.replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"));
                    if (!orgName.startsWith(fromDir)) {
                        continue;
                    }
                    // System.out.println("orgName:"+orgName);
                    // System.out.println("acepted by prefix");
                    result.add(orgName);
                }
            }
        }

        return result;
    }

    public static Vector<String> scanFilesJar(FileFilterUniversal f, String fromDir) throws Exception {
        return scanFilesJar(f, fromDir, null);
    }

    /**
     * computes a md5 hash of a files content but ignoring all xml comment fields
     *
     * @param file
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String getHashXML(File file) throws IOException {
        String content = readFileAsString(file);
        return StringHelper.getHashXML(content);
    }

    public static String getHashXMLwithoutHashDumpAndComments(File file) throws IOException {
        String content = readFileAsString(file);
        String contentAfterRemove = CDump.removeHashDumpAndCommentsFromXML(content);
        return StringHelper.getHashXML(contentAfterRemove);
    }

    public static String readFileAsString(File file) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
        }

        return new String(fileData.toString().getBytes(), "UTF-8");
    }

    public static void mkdirs(File[] folders) throws IOException {
        for (File file : folders) {
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new IOException("Could not create folder: " + file);
                }
            }
        }
    }

    public static File validateFileName(final File file, final MFileFilter filter) {
        if (filter.accept(file)) {
            return file;
        }
        // remove wrong file extension if any
        String fileName = file.getName();
        final int index = fileName.lastIndexOf(".");
        if (index > 0) {
            fileName = fileName.substring(0, index);
        }

        final String extension = filter.getExtension();
        final String newFileName = fileName + "." + extension;

        final File newFile = new File(file.getParent(), newFileName);

        return newFile;
    }

    public static boolean deleteDir(ILanguageHelper languageHelper, File dir, boolean silent) {
        if (dir == null || !dir.exists()) {
            return true;
        }

        boolean succeed = true;

        MProgressMonitor mon = null;
        if (!silent) {
            mon =
                new MProgressMonitor(
                    null,
                    languageHelper.getString(KEY + ".deleteDir.title", dir.getAbsolutePath()),
                    languageHelper.getString(KEY + ".deleteDir.initNote"),
                    0,
                    100);
        }

        Vector<File> files = listFiles(dir);
        // System.out.println("delDir"+dir);
        // System.out.println(files.toString());
        if (mon != null) {
            mon.setMaximum(files.size());
        }

        String regex = Pattern.quote(dir.getAbsolutePath() + File.separator);
        for (int i = 0; i != files.size(); i++) {
            File f = files.get(i);
            String rel = f.getAbsolutePath().replaceFirst(regex, "");
            if (mon != null) {
                mon.setProgressNote(languageHelper.getString(KEY + ".deleteDir.prog", rel, i, files.size()), i);

                if (mon.isCanceled()) {
                    mon.close();
                    return false;
                }
            }

            if (!f.delete()) {
                // System.out.println("problemFile = " + f);
                succeed = false;
            }
        }

        if (mon != null) {
            mon.close();
        }
        // System.out.println("succ=" + succeed);
        return succeed;
    }

    public static Vector<File> listFiles(File dir) {
        Vector<File> f = new Vector<File>();
        listFiles(dir, f);
        return f;
    }

    protected static void listFiles(File dir, Vector<File> list) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            Ensure.notNull(children, "children");
            for (int i = 0; i < children.length; i++) {
                listFiles(new File(dir, children[i]), list);
            }
        }

        list.add(dir);
    }

    /**
     * /** For given file, if it allready exist it asks if user realy whant to overwrite it
     *
     * @param file
     * @param parent
     * @return true if file can be written
     */
    public static boolean askForOverwrite(File file, Component parent) {
        if (!file.exists()) {
            return true;
        }

        ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
        return JOptionPane.showConfirmDialog(
                parent,
                UserNotificationHubSwing.wrapText(
                    languageHelper.getString(KEY + ".replace.msg", file.getAbsolutePath())),
                languageHelper.getString(KEY + ".replace.title"),
                JOptionPane.YES_NO_OPTION)
            == JOptionPane.YES_OPTION;
    }

    public static boolean askForDelete(File file, Component parent) {
        if (!file.exists()) {
            return true;
        }

        ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);
        boolean del =
            JOptionPane.showConfirmDialog(
                    parent,
                    UserNotificationHubSwing.wrapText(
                        languageHelper.getString(KEY + ".delete.msg", file.getAbsolutePath())),
                    languageHelper.getString(KEY + ".delete.title"),
                    JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
        if (del) {
            file.delete();
        }

        return del;
    }

    public static boolean canRead(File file, Component parent) {
        if (file == null) {
            Debug.getLog().log(Debug.WARNING, "File to load is NULL");
            return false;
        }

        if (!file.exists()) {
            Debug.getLog().log(Debug.WARNING, "File doesn't exist: " + file);
            return false;
        }

        if (!file.canRead()) {
            Debug.getLog().log(Debug.WARNING, "File is not readable: " + file);
            return false;
        }

        if (file.length() == 0) {
            Debug.getLog().log(Debug.WARNING, "File is empty (and corrupted): " + file);
            return false;
        }

        return true;
    }

    public static String getRelativePath(String targetPath, String basePath, String pathSeparator) {
        boolean toggleSep = false;
        // check if relativation makes sens at all
        String[] base = basePath.split(Pattern.quote(pathSeparator));
        String[] target = targetPath.split(Pattern.quote(pathSeparator));
        int i = 0;
        if (base[0].length() == 0 && target[0].length() == 0) {
            i = 1;
        }

        if (!base[i].equals(target[i])) {
            return targetPath;
        }

        // the base path is a suffix of the target, its obviously a folder which is fine automatically
        // if not check if the base has some trailing pathSeperator and if not, assume this is a file not a folder..
        // so the file name has to be removed from tha base path
        if (!targetPath.startsWith(basePath) && !basePath.endsWith(pathSeparator)) {
            int pos = basePath.lastIndexOf(pathSeparator);
            if (pos > 0) {
                basePath = basePath.substring(0, pos);
            }
        }

        if (!pathSeparator.equals(File.separator)) {
            toggleSep = true;
            targetPath = targetPath.replaceAll(Pattern.quote(pathSeparator), Matcher.quoteReplacement(File.separator));
            basePath = basePath.replaceAll(Pattern.quote(pathSeparator), Matcher.quoteReplacement(File.separator));
        }

        Path targetPathP = Paths.get(targetPath);
        Path basePathP = Paths.get(basePath);
        Path relativePathP = basePathP.relativize(targetPathP);
        String out = relativePathP.toString();
        if (toggleSep) {
            out = out.replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement(pathSeparator));
        }

        return out;
    }

    public static String getRelativePath(String targetPath, String basePath) {
        return getRelativePath(targetPath, basePath, File.separator);
    }

    public static String getRelativePath(File targetPath, File basePath, String pathSeparator) {
        return getRelativePath(targetPath.getAbsolutePath(), basePath.getAbsolutePath(), File.separator)
            .replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement(pathSeparator));
    }

    /**
     * this functions makes a absolute file from a base file and a relative path string this is NOT using any
     * File.getCanoncialPath() stuff, since this is unrolling symlinks in linux paths!
     *
     * @param basePath
     * @param path
     * @return
     * @throws IOException
     */
    public static File unmakeRelativePath(File basePath, String path) throws IOException {
        // System.out.println(basePath.getAbsolutePath()+ path);
        if (path.startsWith(".")) {
            if (path.startsWith("..")) {
                path = basePath.getAbsolutePath() + File.separator + path;
            } else {
                // if (path.substring(1).startsWith("/") || path.substring(1).startsWith("\\")){
                // path = basePath.getAbsolutePath() + path.substring(1);
                // } else {
                path = basePath.getAbsolutePath() + File.separator + path.substring(1);
                // }
            }
        } else if (!Paths.get(path).isAbsolute()) {
            // perhaps it is a relative path without leading '.'
            path = basePath.getAbsolutePath() + File.separator + path;
        }
        // System.out.println(path);
        path = MFile.adaptToCurSystem(path);
        // System.out.println(path);

        path = (new File(path)).getAbsolutePath();
        // System.out.println("path->"+path);
        String[] elements = path.split(Pattern.quote(File.separator));
        Stack<String> out = new Stack<String>();
        for (String tok : elements) {
            // System.out.println(tok);
            if (tok.equals(".") || tok.equals("$")) {
                continue;
            }

            if (tok.equals("..")) {
                if (out.empty()) {
                    throw new IOException("this path contains too many ..");
                }

                out.pop();
            } else {
                out.push(tok);
            }
        }
        // System.out.println("stack:"+out);
        String newPath = "";
        int i = 0;
        for (String tok : out) {
            if (i <= 1 || !tok.isEmpty()) {
                newPath += File.separator;
            }
            // if (!first && !tok.isEmpty()) newPath += File.separator; //this causes harm on windows, since \\ is a
            // valid reference on the
            // beginning to a shared drive!!
            i++;
            newPath += tok;
        }
        // System.out.println("newFile:"+newPath);
        return (new File(newPath)).getAbsoluteFile();
    }

    public static final String pathSeperatorExport = "\\";

    /**
     * Returns a relative path representation
     *
     * @param targetPath where we are interested in it's relative path
     * @param basePath the place where the path should be relative to
     * @return
     */
    public static String getRelativePath(File targetPath, File basePath) {
        return getRelativePath(targetPath.getAbsolutePath(), basePath.getAbsolutePath());
    }

    /**
     * if toTruncated is something inside baseFolder, remove it's suffix, otherwise return relative path. adapts the
     * seperator to unix style, to make the result plattform independent, since some cody needs unique results
     *
     * @param baseFolder
     * @param toTruncated
     * @return
     */
    public static String makeRelativePathSysIndep(File baseFolder, File toTruncated) {
        String base = baseFolder.getAbsolutePath();
        if (!base.endsWith(File.separator)) {
            base += File.separator; // otherwise getRelative wont work
        }

        String trunc = toTruncated.getAbsolutePath();
        String diff = trunc;
        if (trunc.startsWith(base)) {
            diff = "." + File.separator + diff.substring(base.length());
        } else {
            diff = getRelativePath(trunc, base);
        }

        diff = MFile.adaptToUnix(diff);
        return diff;
    }

    public static String normalizePathToBaseFolder(File baseFolder, File file) {
        if (isChild(file, baseFolder)) {
            return "." + File.separator + cutBaseFolderPath(baseFolder, file);
        }

        return MFile.adaptToUnix(file.getAbsolutePath());
    }

    private static boolean isChild(File file, File baseFolder) {
        return file.getAbsolutePath().startsWith(baseFolder.getAbsolutePath());
    }

    private static String cutBaseFolderPath(File baseFolder, File file) {
        String baseFolderPath = baseFolder.getAbsolutePath();
        int charsToCut =
            baseFolderPath.endsWith(File.separator) ? baseFolderPath.length() : baseFolderPath.length() + 1;
        return file.getAbsolutePath().substring(charsToCut);
    }

    public static File getNextFreeFilename(File f) {
        if (!f.exists()) {
            return f;
        }

        Pair<String, String> prefixSuffix = splitExtension(f);
        Pair<String, String> prefixSuffix2 = splitExtension(prefixSuffix.first, "_");
        int i = 1;
        String prefix = prefixSuffix2.first;
        if (!prefixSuffix2.second.equals("")) {
            try {
                i = Integer.valueOf(prefixSuffix2.second.substring(1));
            } catch (NumberFormatException e) {
                prefix += prefixSuffix2.second;
            }
        }

        File fNew = null;
        while (true) {
            fNew = new File(prefix + "_" + i + prefixSuffix.second);
            if (!fNew.exists()) {
                return fNew;
            }

            i++;
        }
    }

    public static Pair<String, String> splitExtension(File f) {
        return splitExtension(f.getAbsolutePath(), ".");
    }

    public static Pair<String, String> splitExtension(String f, String splitter) {
        String prefix = f;
        int pos = prefix.lastIndexOf(splitter);
        if (pos < 0) {
            return new Pair<String, String>(prefix, "");
        }

        String suffix = prefix.substring(pos);
        prefix = prefix.substring(0, pos);
        return new Pair<String, String>(prefix, suffix);
    }
    /*
     * inject a string into a filename before the extension
     */
    public static File injectIntoFileName(File f, String inject) {
        Pair<String, String> prefixSuffix = splitExtension(f);
        return new File(prefixSuffix.first + inject + prefixSuffix.second);
    }

    private static @Nullable File myDocumentsFolder = null;
    private static @Nullable File downloadsFolder = null;

    /**
     * This function returns the real user home directory, which is different to the folder in the system.properties
     * "user.home" since windows user expect it to be equal to the "My Documents folder". On linux their are no such
     * differences
     *
     * @return
     */
    public static File getMyDocumentsFolder() {
        if (myDocumentsFolder == null) {
            if (SystemInformation.isWindows()) {
                PointerByReference ptrRef = new PointerByReference();
                Shell32.INSTANCE.SHGetKnownFolderPath(
                    Guid.GUID.fromString("FDD39AD0-238F-46AF-ADB4-6C85480369C7"),
                    0,
                    new WinNT.HANDLE(Pointer.NULL),
                    ptrRef);
                Pointer ptr = ptrRef.getValue();
                String path = ptr.getWideString(0);
                Ole32.INSTANCE.CoTaskMemFree(ptr);
                myDocumentsFolder = new File(path);
            } else {
                JFileChooser fr = new JFileChooser();
                FileSystemView fw = fr.getFileSystemView();
                myDocumentsFolder = fw.getDefaultDirectory();
            }
        }

        return myDocumentsFolder;
    }

    public static File getDownloadsFolder() {
        if (downloadsFolder == null) {
            if (SystemInformation.isWindows()) {
                PointerByReference ptrRef = new PointerByReference();
                Shell32.INSTANCE.SHGetKnownFolderPath(
                    Guid.GUID.fromString("374DE290-123F-4565-9164-39C4925E467B"),
                    0,
                    new WinNT.HANDLE(Pointer.NULL),
                    ptrRef);
                Pointer ptr = ptrRef.getValue();
                String path = ptr.getWideString(0);
                Ole32.INSTANCE.CoTaskMemFree(ptr);
                downloadsFolder = new File(path);
            } else {
                downloadsFolder = new File(System.getProperty("user.home") + File.separator + "Downloads");
            }
        }

        return downloadsFolder;
    }

    public static File getAppDataFolder() {
        // TODO FIXME, how to do this on mac/linux
        if (SystemInformation.isWindows()) {
            return new File(System.getenv("APPDATA"));
        } else {
            return new File(System.getProperty("user.home"));
        }
    }

    public static void zipSingleFile(File fileInner, File zipName) throws IOException {
        File tmp = FileHelper.zipSingleFile(fileInner);
        tmp.renameTo(zipName);
        // fileInner.delete();
    }

    public static File zipSingleFile(File file) throws IOException {
        // Create a buffer for reading the files
        byte[] buf = new byte[2048];
        File outFile = new File(file.getAbsolutePath() + ".zip");
        // Create the ZIP file, compress the file
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
            FileInputStream in = new FileInputStream(file)) {
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(file.getName()));

            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            // Complete the entry
            out.closeEntry();
            return outFile;
        }
    }

    public static long getCRC32Checksum(File file) throws IOException {
        FileInputStream fs = new FileInputStream(file);
        try {
            CheckedInputStream check = new CheckedInputStream(fs, new CRC32());
            BufferedInputStream in = new BufferedInputStream(check);
            while (in.read() != -1) {
                // Read file in completely
            }

            return check.getChecksum().getValue();
        } finally {
            try {
                fs.close();
            } catch (Exception e) {
            }
        }
    }

    public static void zipManyFiles(File outFile, Vector<File> files) throws IOException {
        // Create a buffer for reading the files
        byte[] buf = new byte[2048];
        // Create the ZIP file
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile))) {
            // agisoft photoscan requires zip files of STORED type
            out.setMethod(ZipOutputStream.STORED);
            for (File file : files) {
                // Compress the file
                try (FileInputStream in = new FileInputStream(file)) {

                    // Add ZIP entry to output stream.
                    ZipEntry ze = new ZipEntry(file.getName());
                    // agisoft photoscan requires zip files of STORED type
                    ze.setSize(file.length());
                    ze.setCrc(getCRC32Checksum(file));
                    try {
                        out.putNextEntry(ze);
                    } catch (ZipException e) {
                        // duplicate entry
                        Debug.getLog().log(Level.INFO, e.getMessage(), e);
                        continue;
                    }
                    // Transfer bytes from the file to the ZIP file
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    out.closeEntry();
                }
            }
        }
    }

    public static File unzipSingleFile(File file, boolean askOverwrite) throws IOException {
        // Create a buffer for reading the files
        try (ZipFile zf = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> e = zf.entries();
            if (!e.hasMoreElements()) {
                throw new IOException("no entry in zip file");
            }

            ZipEntry entry = e.nextElement();
            if (e.hasMoreElements()) {
                return null; // only unzip if a single file is inside
            }

            if (entry.isDirectory()) {
                return null; // only unzip if a single file is inside
            }

            File fOut = new File(file.getParentFile(), entry.getName());
            try (InputStream in = zf.getInputStream(entry);
                FileOutputStream out = new FileOutputStream(fOut)) {
                if (!askOverwrite || askForOverwrite(fOut, null)) {;
                }

                byte[] buf = new byte[2048];

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

            return fOut;
        }
    }

    public static long getFreeDiskspace(File file) {
        while (file != null && file.getUsableSpace() <= 0) {
            file = file.getParentFile();
        }

        if (file == null) {
            return 0;
        }

        return file.getUsableSpace();
    }

    public static void decompressGzipFile(File gzipFile, File newFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(gzipFile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(newFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }
    }

    public static void compressGzipFile(File file, File gzipFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(gzipFile);
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, len);
            }
        }
    }

    public static class GetFotosResult {
        public Vector<File> fotos;
        public File picFolder;
    }

    public static GetFotosResult fetchFotos(File picFolder, FileFilterUniversal filter) {
        if (!picFolder.isDirectory()) {
            return null;
        }

        final Vector<File> files = FileHelper.getSDcardJPEGs(picFolder, filter);

        if (files == null || files.size() == 0) {
            return null;
        }

        GetFotosResult res = new GetFotosResult();
        res.fotos = files;
        res.picFolder = picFolder;

        return res;
    }

    public static GetFotosResult fetchFotos(List<File> imageFolder) {
        if (imageFolder == null || imageFolder.size() == 0) {
            return null;
        }

        if (imageFolder.size() == 1) {
            if (imageFolder.get(0).isFile()) {
                String extension = "." + FilenameUtils.getExtension(imageFolder.get(0).getName());
                FileFilterUniversal filter =
                    new FileFilterUniversal() {
                        @Override
                        public boolean accept(File f) {
                            return MFileFilter.jpegFilter.getWithoutFolders().accept(f.getName());
                        }

                        @Override
                        public String getDescription() {
                            return "";
                        }
                    };
                return fetchFotos(imageFolder.get(0).getParentFile(), filter);
            } else {
                return fetchFotos(imageFolder.get(0), MFileFilter.jpegFilter.getWithoutFolders());
            }
        } else { // size is > 1
            // handle only selected files

            GetFotosResult res = new GetFotosResult();
            res.fotos = new Vector<>(imageFolder);
            res.picFolder = imageFolder.get(0).getParentFile();
            return res;
        }
    }

    public static void scanSubFoldersForFiles(
            File folder, List<File> result, int maxDepth, Function<File, Boolean> filter) {
        if (folder != null) {
            if (filter.apply(folder)) {
                result.add(folder);
                return;
            }

            if (maxDepth != 0) {
                File[] files = folder.listFiles();
                if (files == null) {
                    return;
                }

                maxDepth--;
                for (File d : files) {
                    scanSubFoldersForFiles(d, result, maxDepth, filter);
                }
            }
        }
    }

    public static long countDiskSize(File folder) {
        if (folder != null) {
            if (folder.isFile()) {
                return folder.length();
            } else if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files == null) {
                    return 0;
                }

                long tmp = 0;
                for (File d : files) {
                    tmp += countDiskSize(d);
                }

                return tmp;
            }
        }

        return 0;
    }

}
