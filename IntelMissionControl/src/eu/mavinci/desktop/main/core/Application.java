/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.core;

import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.helper.SvgUtils;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.PathSettings;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.desktop.main.debug.IProfilingManager;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.management.CAirport;
import eu.mavinci.desktop.main.debug.Debug;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.util.Duration;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import thebuzzmedia.exiftool.ExifTool;

@Deprecated(forRemoval = true)
public class Application {

    private static IPathProvider pathProvider;

    public static final URI CHANGELOG_URI =
        URI.create("http://www.mavinci.de/download/Intel-Mission-Control_release-notes.pdf");

    public static final String KEY = "Application";

    private static boolean isClosingRequest = false;
    private static boolean isClosing = false;

    public static boolean isClosing() {
        return isClosing;
    }

    private static WeakListenerList<IAppListener> appListeners;

    public static boolean closeAppRequest() {
        if (isClosing) {
            return true;
        }

        isClosing = true;
        boolean allowClosing = true;
        for (IAppListener listener : appListeners.reversed()) {
            if (listener != null) {
                allowClosing = listener.appRequestClosing();
            }

            if (!allowClosing) {
                break;
            }
        }

        isClosing = false;
        return allowClosing;
    }

    public static boolean closeApp() {
        if (isClosing) {
            return true;
        }

        Debug.getLog().config("Application: closeApp");
        if (closeAppRequest()) {
            closeAppForce();
            return true;
        } else {
            return false;
        }
    }

    public static void closeAppForce() {
        isClosing = true;
        if (isClosingRequest) {
            return;
        }

        Debug.getLog().config("Application: closeAppForce");
        try {
            try {
                if (ExifTool.instance.isRunning()) {
                    Debug.getLog().log(Level.INFO, "exif tool running, try to close");
                    ExifTool.instance.close();
                } else {
                    Debug.getLog().log(Level.INFO, "exif tool not running");
                }
            } catch (Exception e) {
                Debug.getLog().log(Level.WARNING, "problems shutdown exif tool", e);
            }

            isClosingRequest = true;

            appListeners.reversed().forEach((IAppListener::appIsClosing));

            System.out.flush();

        } finally {
            // final logging
            DependencyInjector.getInstance().getInstanceOf(IProfilingManager.class).guiClosingDone();
            // flushing loggers
            LogManager.shutdown();
        }
    }

    public static synchronized void fireGuiReadyLoaded() {
        if (isGuiReadyLoaded) return;
        isGuiReadyLoaded = true;
        for (IAppListener listener : appListeners.reversed()) {
            if (listener != null) {
                listener.guiReadyLoaded();
            }
        }

        Dispatcher.schedule(
            Application::checkFreeDiskSpace, Duration.millis(0), Duration.millis(DISC_SPACE_CHECK_CYCLE));
    }

    public static boolean checkFreeDiskSpace() {
        boolean enoughSpace = true;
        fileRoots.clear();
        fileRoots.addAll(Arrays.asList(File.listRoots()));

        enoughSpace &= checkDiskSpace(pathProvider.getSettingsDirectory().toFile(), "AppSettings");
        //enoughSpace &= checkDiskSpace(pathProvider.getProjectsDirectory().toFile(), "ProjectBase");
        enoughSpace &= checkDiskSpace(pathProvider.getUserHomeDirectory().toFile(), "userHome");
        enoughSpace &= checkDiskSpace(DependencyInjector.getInstance()
                .getInstanceOf(ISettingsManager.class)
                .getSection(PathSettings.class)
                .projectFolderProperty().get().toFile(), "ProjectData");
        return enoughSpace;
    }

    public static final long minFreeDiskSpace = 1 * 1024 * 1024 * 1024L;

    static TreeSet<File> fileRoots = new TreeSet<>();

    public static boolean checkDiskSpace(File f, String name) {
        try {
            f = f.getCanonicalFile();
        } catch (IOException e) {
            f = f.getAbsoluteFile();
        }

        for (File r : fileRoots) {
            if (f.getAbsolutePath().startsWith(r.getAbsolutePath())) {
                fileRoots.remove(r);
                long free = f.getFreeSpace();
                Debug.getLog().finest("freeSpace for=" + name + " Dir=" + f + " drive=" + r + " free=" + free + "B");
                if (free < minFreeDiskSpace) {
                    Debug.getLog()
                        .severe(
                            "Not enough free disk space on "
                                + name
                                + " drive: "
                                + r
                                + " "
                                + StringHelper.bytesToIngName(free, -3, false));
                    return false;
                }

                break;
            }
        }

        return true;
    }

    private static boolean isGuiReadyLoaded;

    public static boolean isGuiReadyLoaded() {
        return isGuiReadyLoaded;
    }

    public static void preInit(IPathProvider pathProvider) {
        Application.pathProvider = pathProvider;

        appListeners = new WeakListenerList<>("appListeners");

        Debug.getLog().info("Environment=" + System.getenv());
    }

    public static final long DISC_SPACE_CHECK_CYCLE = 60000L;

    public static void addApplicationListener(IAppListener listener) {
        if (appListeners != null) {
            appListeners.add(listener);
        }
    }

    public static void removeApplicationListener(IAppListener listener) {
        if (appListeners != null) {
            appListeners.remove(listener);
        }
    }

    private static Map<String, BufferedImage> bufferedImageMap = new HashMap<>();
    private static Map<BufferedImage, BufferedImage> bufferedImageBrightingMap = new HashMap<>();
    private static Map<String, ImageIcon> imageIconMap = new HashMap<>();

    public static BufferedImage getBufferedImageFromResource(String resourcePath) {
        return getBufferedImageFromResource(resourcePath, -1);
    }

    public static BufferedImage getBufferedImageFromResource(String resourcePath, int reqestedWidthHeight) {
        return getBufferedImageFromResource(resourcePath, reqestedWidthHeight, reqestedWidthHeight);
    }

    public static BufferedImage getBufferedImageFromResource(
            String resourcePath, int requestedWidth, int requestedHeight) {
        if (bufferedImageMap.containsKey(resourcePath)) {
            return bufferedImageMap.get(resourcePath);
        } else {
            Image img = getImageIconFromResource(resourcePath, requestedWidth, requestedHeight).getImage();

            int imageWidth = img.getWidth(null); // es muß keinen ImageObserver geben
            int imageHeight = img.getHeight(null);
            if (imageWidth < 0 && imageHeight < 0) {
                imageWidth = requestedWidth;
                imageHeight = requestedHeight;
            }

            BufferedImage bufImg = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
            // einfachster Fall : BufferedImage.TYPE_INT_RGB
            Graphics2D g = bufImg.createGraphics();
            g.drawImage(img, requestedWidth, requestedHeight, null);
            g.dispose();
            bufferedImageMap.put(resourcePath, bufImg);
            return bufImg;
        }
    }

    public static ImageIcon getImageIconFromResource(String resourcePath) {
        return getImageIconFromResource(resourcePath, -1, -1);
    }

    public static ImageIcon getImageIconFromResource(String resourcePath, int requestedWidth, int requestedHeight) {
        if (imageIconMap.containsKey(resourcePath)) {
            return imageIconMap.get(resourcePath);
        } else {
            ImageIcon img;

            try {
                if (resourcePath.endsWith(".svg") || resourcePath.endsWith(".svgref")) {
                    img = new ImageIcon(SvgUtils.getImageFromFile(resourcePath));
                } else {
                    img = new ImageIcon(ClassLoader.getSystemResource(resourcePath));
                }

                if (requestedWidth > 0) {
                    Image image = img.getImage(); // transform it
                    // scale it the smooth way
                    Image newimg =
                        image.getScaledInstance(requestedWidth, requestedHeight, java.awt.Image.SCALE_SMOOTH);
                    img = new ImageIcon(newimg); // transform it back
                } else if (requestedWidth == -2) {
                    // dont scale anything!
                } /*else if (CLanguage.getUIScale() != 1) {
                      Image image = img.getImage(); // transform it
                      Image newimg =
                          image.getScaledInstance(
                              SwingHelper.scaleIntMin1(img.getIconWidth()),
                              SwingHelper.scaleIntMin1(img.getIconHeight()),
                              java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
                      img = new ImageIcon(newimg); // transform it back
                  }*/
            } catch (NullPointerException e) {
                if (resourcePath.isEmpty() || resourcePath.contains("unknown.png")) {
                    Debug.getLog().log(Level.CONFIG, "Cannot find default icon unknown.png");
                    return null;
                } else {
                    // if we can not load the Image, load default image
                    Debug.getLog().log(Level.CONFIG, "Cannot find " + resourcePath + " loading unknown.png instead");
                    int pos = resourcePath.lastIndexOf("/");
                    String resurceOfUnknownInSameSize;
                    if (pos >= 0) {
                        resurceOfUnknownInSameSize = resourcePath.substring(0, pos).concat("/unknown.png");
                    } else {
                        resurceOfUnknownInSameSize = "eu/mavinci/icons/16x16/unknown.png";
                    }

                    img = getImageIconFromResource(resurceOfUnknownInSameSize, requestedWidth, requestedHeight);
                }
            }

            imageIconMap.put(resourcePath, img);
            return img;
        }
    }

    static final float brightness = 1.8f;
    static final float[] scaleFactors = {brightness, brightness, brightness, 1};

    public static BufferedImage getBufferedImageBrighter(BufferedImage image) {
        if (bufferedImageBrightingMap.containsKey(image)) {
            return bufferedImageBrightingMap.get(image);
        } else {
            // System.out.println("new BufImg Brighting");
            BufferedImage brighterImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

            RescaleOp rescaleOp = new RescaleOp(scaleFactors, new float[4], null);

            Graphics2D g = brighterImage.createGraphics();
            g.drawImage(image, rescaleOp, 0, 0);
            g.dispose();

            bufferedImageBrightingMap.put(image, brighterImage);
            return brighterImage;
        }
    }

}
