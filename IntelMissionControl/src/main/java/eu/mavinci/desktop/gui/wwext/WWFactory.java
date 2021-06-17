/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.settings.ExpertSettings;
import com.jogamp.opengl.Threading;
import eu.mavinci.core.update.UpdateURL;
import eu.mavinci.desktop.helper.FileFilterUniversal;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.DataConfigurationFilter;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import javax.xml.xpath.XPath;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;
import org.asyncfx.concurrent.Dispatcher;
import org.w3c.dom.Document;

/**
 * Providing the Application with Instances of WorldWind Objects These Objects are trying to share as much resources as
 * possible....
 *
 * @author caller, Peter Schauß
 */
public class WWFactory {

    public static final String KEY_AV_Description = "eu.mavinci.description";

    protected static WaveFrontLoader2 waveFrontLoader;

    /**
     * Configure WWJ dependency injection this has to happen before WWJ is "starting up" by creating any object by the
     * internal dependency injection
     */
    public static void configWW(ExpertSettings expertSettings) {
        Threading.disableSingleThreading(); // speedup OpenGL on Multi Cores

        int mapPoolSizeDefaultValue = 10;
        int mapFetchQueueSizeDefaultValue = 200;

        String mapFetchPoolSize = expertSettings.getMapsFetchPoolSize(mapPoolSizeDefaultValue);
        Debug.getLog().fine("Maps Fetch Pool Size =" + mapFetchPoolSize);

        String mapFetchQueueSize = expertSettings.getMapsFetchQueueSize(mapFetchQueueSizeDefaultValue);
        Debug.getLog().fine("Maps Fetch Queue Size =" + mapFetchQueueSize);

        try {
            Configuration.setValue(AVKey.TASK_POOL_SIZE, Integer.parseInt(mapFetchPoolSize));
            Configuration.setValue(AVKey.TASK_QUEUE_SIZE, Integer.parseInt(mapFetchQueueSize));
        } catch (NumberFormatException exp) {
            exp.printStackTrace();
            Configuration.setValue(AVKey.TASK_POOL_SIZE, mapPoolSizeDefaultValue);
            Configuration.setValue(AVKey.TASK_QUEUE_SIZE, mapFetchQueueSizeDefaultValue);
        }

        int retrieverPoolSizeDefaultValue = 20;
        int retrieverQueueSizeDefaultValue = 200;

        String retrieverPoolSize = expertSettings.getRetrieverPoolSize(retrieverPoolSizeDefaultValue);
        Debug.getLog().fine("Retriever Pool Size =" + mapFetchPoolSize);

        String retrieverQueueSize = expertSettings.getRetrieverQueueSize(retrieverQueueSizeDefaultValue);
        Debug.getLog().fine("Retriever Queue Size =" + mapFetchQueueSize);

        try {
            Configuration.setValue(AVKey.RETRIEVAL_POOL_SIZE, Integer.parseInt(retrieverPoolSize));
            Configuration.setValue(AVKey.RETRIEVAL_QUEUE_SIZE, Integer.parseInt(retrieverQueueSize));
        } catch (NumberFormatException exp) {
            exp.printStackTrace();
            Configuration.setValue(AVKey.RETRIEVAL_POOL_SIZE, retrieverPoolSizeDefaultValue);
            Configuration.setValue(AVKey.RETRIEVAL_QUEUE_SIZE, retrieverQueueSizeDefaultValue);
        }

        Debug.getLog().fine("WWJ-Version=" + gov.nasa.worldwind.Version.getVersion());
        StringBuffer sb = new StringBuffer();
        sb.append("\nJOGL Values\n");
        String pkgName = "com.jogamp.opengl";
        try {
            WWFactory.class.getClassLoader().loadClass(pkgName + ".GL");

            Package p = Package.getPackage(pkgName);
            if (p == null) {
                sb.append("WARNING: Package.getPackage(" + pkgName + ") is null\n");
            } else {
                sb.append(p + "\n");
                sb.append("Specification Title = " + p.getSpecificationTitle() + "\n");
                sb.append("Specification Vendor = " + p.getSpecificationVendor() + "\n");
                sb.append("Specification Version = " + p.getSpecificationVersion() + "\n");
                sb.append("Implementation Vendor = " + p.getImplementationVendor() + "\n");
                sb.append("Implementation Version = " + p.getImplementationVersion() + "\n");
            }
        } catch (ClassNotFoundException e) {
            sb.append("Unable to load " + pkgName + "\n");
        }

        Debug.getLog().fine(sb.toString());

        waveFrontLoader = new WaveFrontLoader2();
        modelCacheMap = new HashMap<>();
    }

    public static WaveFrontLoader2 getWaveFrontLoader() {
        return waveFrontLoader;
    }

    private static Map<String, Model> modelCacheMap;

    /**
     * load airplane model get a new 3d model... Attention: This function block your thread for a while!! caching of the
     * model is not useful because we need a new model for every wordwind window
     *
     * @return
     */
    protected static Model getModel(String src) {
        if (modelCacheMap.containsKey(src)) {
            return modelCacheMap.get(src);
        }

        Debug.getLog().log(Level.FINE, "Now try to load " + src + " model");

        Model model = null;
        try {
            URL url = ClassLoader.getSystemResource(src);
            if (url == null) {
                Debug.getLog().log(Level.SEVERE, "cannot find " + src + " model in " + src);
                throw new ModelLoadException();
            }

            model = getWaveFrontLoader().load(url.toString());

            if (model == null) {
                throw new ModelLoadException();
            }
        } catch (ModelLoadException e) {
            Debug.getLog().log(Level.SEVERE, "cannot load " + src + " model", e);
        }

        Debug.getLog().log(Level.FINE, "Ready creating " + src + " model");

        modelCacheMap.put(src, model);
        return model;
    }

    public static Model getAndroidModel() {
        final String src = "eu/mavinci/modells3d/android.obj";
        return getModel(src);
    }

    public static Model getAirplaneModelRed() {
        final String src = "eu/mavinci/modells3d/miniplane_red.obj";
        return getModel(src);
    }

    public static Model getAirplaneModelOrange() {
        final String src = "eu/mavinci/modells3d/miniplane_orange.obj";
        return getModel(src);
    }

    public static Model getAirplaneModelGreen() {
        final String src = "eu/mavinci/modells3d/miniplane_green.obj";
        return getModel(src);
    }

    public static Model getAirplaneModelFalcon8() {
        final String src = "eu/mavinci/modells3d/falcon8.obj";
        return getModel(src);
    }

    public static Model getAirplaneModelGrayHawk() {
        //final String src = "eu/mavinci/modells3d/GH_V5_no_texture.obj";
        final String src = "eu/mavinci/modells3d/falcon8.obj";
        return getModel(src);
    }

    public static Model getAirplaneModeGenericDrone() {
        final String src = "eu/mavinci/modells3d/generic-drone.obj";
        return getModel(src);
    }

    public static int maxTextureSize = 1024 * 4;

    public static final String KEY = "wmsFactory";

    public static boolean dropDataCache(AVList avList, int maximumAgeInSeconds, String onlineTestURL) {
        if (avList == null) {
            return false;
        }

        if (maximumAgeInSeconds < 0) {
            if (avList instanceof Layer) {
                Layer layer = (Layer)avList;
                layer.setNetworkRetrievalEnabled(false);
            } else if (avList instanceof ElevationModel) {
                ElevationModel elev = (ElevationModel)avList;
                elev.setNetworkRetrievalEnabled(false);
            }
        }

        AVList constructionParams = (AVList)avList.getValue(AVKey.CONSTRUCTION_PARAMETERS);
        if (constructionParams == null) {
            return false;
        }

        String dataCacheName = constructionParams.getStringValue(AVKey.DATA_CACHE_NAME);
        if (dataCacheName == null) {
            return false;
        }

        return dropDataCache(dataCacheName, maximumAgeInSeconds, onlineTestURL);
    }

    public static boolean dropDataCache(AVList avList) {
        return dropDataCache(avList, -1, null);
    }

    public static boolean dropDataCache(String cacheName) {
        return dropDataCache(cacheName, -1, null);
    }

    public static boolean dropDataCache(
            final String cacheName, final int maximumAgeInSeconds, final String onlineTestURL) {
        if (cacheName == null) {
            return false;
        }
        // clear WWJ cache
        FileStore fileStore = WorldWind.getDataFileStore();

        XPath path = WWXML.makeXPath();
        boolean found = false;
        for (File f : fileStore.getLocations()) {
            if (!f.exists()) {
                continue;
            }

            String[] names = WWIO.listDescendantFilenames(f, new DataConfigurationFilter(), false);
            if (names == null || names.length == 0) {
                continue;
            }

            for (String filename : names) {
                Document doc = null;
                final File dataConfigFile = new File(f, filename);
                try {
                    doc = WWXML.openDocument(dataConfigFile);
                    if (doc == null) {
                        continue;
                    }

                    doc = DataConfigurationUtils.convertToStandardDataConfigDocument(doc);
                } catch (WWRuntimeException e) {
                    continue;
                }

                if (doc == null) {
                    continue;
                }

                if (cacheName.equals(WWXML.getText(doc.getDocumentElement(), "DataCacheName", path))) {
                    doDropCache(cacheName, maximumAgeInSeconds, dataConfigFile.getParentFile(), onlineTestURL);
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            for (File f : fileStore.getLocations()) {
                if (!f.exists()) {
                    continue;
                }

                File dataFolder = new File(f + File.separator + cacheName);
                if (dataFolder.exists()) {
                    doDropCache(cacheName, maximumAgeInSeconds, dataFolder, onlineTestURL);
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    private static void doDropCache(
            String cacheName, int maximumAgeInSeconds, File cacheBaseFolder, String onlineTestURL) {
        // found
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.run(
            new Runnable() {

                @Override
                public void run() {
                    ILanguageHelper languageHelper = StaticInjector.getInstance(ILanguageHelper.class);

                    if (maximumAgeInSeconds < 0) {
                        Debug.getLog()
                            .info(languageHelper.getString(KEY + ".willDropCache", cacheBaseFolder.getAbsolutePath()));
                        FileHelper.deleteDir(languageHelper, cacheBaseFolder, true);
                        Debug.getLog()
                            .info(
                                languageHelper.getString(
                                    KEY + ".dropCacheSuccessed", cacheBaseFolder.getAbsolutePath()));
                    } else {
                        Debug.getLog().info("Begin dropping Expired Cache for " + cacheName);
                        boolean isOffline = WorldWind.isOfflineMode();
                        if (!isOffline && onlineTestURL != null) {
                            try {
                                String data = UpdateURL.getURLData(onlineTestURL);
                                if (data == null) {
                                    isOffline = true;
                                }
                            } catch (Exception e) {
                                isOffline = true;
                            }
                        }

                        Debug.getLog()
                            .info("Dropping Expired Cache for " + cacheName + " has tested isOffline=" + isOffline);
                        Vector<File> files =
                            FileHelper.scanFiles(
                                new FileFilterUniversal() {
                                    @Override
                                    public String getDescription() {
                                        return null;
                                    }

                                    @Override
                                    public boolean accept(File f) {
                                        return !f.isDirectory() && !f.getName().toLowerCase().endsWith(".xml");
                                    }
                                },
                                cacheBaseFolder,
                                -1);
                        Debug.getLog()
                            .info(
                                "Dropping Expired Cache for "
                                    + cacheName
                                    + " has found "
                                    + files.size()
                                    + " for maybe deleting.");
                        int count = 0;
                        for (File f : files) {
                            if (!isOffline
                                    && System.currentTimeMillis() - f.lastModified() > maximumAgeInSeconds * 1000L) {
                                count++;
                                f.delete();
                            }
                        }

                        Debug.getLog()
                            .info("Dropping Expired Cache for " + cacheName + " has deleted " + count + " files.");
                    }
                }
            });
    }
}
