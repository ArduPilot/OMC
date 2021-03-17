/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SrsPrivateSettings;
import com.intel.missioncontrol.settings.SrsSettings;
import eu.mavinci.coordinatetransform.Ellipsoid;
import eu.mavinci.coordinatetransform.PrimeMeridian;
import eu.mavinci.coordinatetransform.UnitOfMeasure;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.gdal.GDALUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.gdal.gdal.gdal;

public class SrsManager implements ISrsManager {

    private final ISettingsManager settingsManager;
    public File gdalDataFolder;

    private int maxId = ID_PRIVATE_MIN;

    private TreeMap<String, MSpatialReference> references = new TreeMap<>();
    private TreeMap<String, TreeSet<MSpatialReference>> referencesSorted = new TreeMap<>();
    private final ObservableMap<String, MSpatialReference> referenceWrapper = FXCollections.observableMap(references);
    private final ObservableMap<String, TreeSet<MSpatialReference>> sortedReferencesWrapper =
        FXCollections.observableMap(referencesSorted);
    private final IWWGlobes globes;

    private MSpatialReference epsg4326WGS84;

    @Inject
    public SrsManager(ISettingsManager settingsManager, IWWGlobes globes) {
        this.globes = globes;
        this.settingsManager = settingsManager;
        if (!initGdal()) return;

        // help gdal to find its data folder
        Debug.getLog().config("Initialize GDAL");
        try {
            String gdalData = System.getProperty("GDAL_DATA");
            if (gdalData == null || gdalData.trim().isEmpty()) {
                Debug.getLog().log(Level.WARNING, "GDAL Data Folder not set");
                return;
            }

            gdal.SetConfigOption("GDAL_DATA", gdalData);

            // magic required to invoke static constructor in GDALUtils
            // it finds gdal/data dir under install dir
            GDALUtils.getErrorMessage();

            Debug.getLog().config("GDAL DATA FOLDER: " + gdal.GetConfigOption("GDAL_DATA"));
        } catch (Throwable e1) {
            Debug.getLog().log(Level.SEVERE, "Could not load gdal natives", e1);
            GDALUtils.getErrorMessage();
        }
        // yes, the following code seems to work... sometimes.. is only checking
        // in one case, not in both
        // gdal.SetConfigOption( "CHECK_WITH_INVERT_PROJ", "TRUE");
        // gdal.SetConfigOption( "THRESHOLD","0.000001");//e1-6==max 2cm
        gdal.SetConfigOption("CPL_LOG_ERRORS", "OFF");
        gdal.SetConfigOption("CPL_LOG_ERRORS", "FALSE");

        // gdal.ErrorReset();

        gdalDataFolder = new File(gdal.GetConfigOption("GDAL_DATA"));
        Debug.getLog().config("GDAL data folder: " + gdalDataFolder);
        if (!gdalDataFolder.exists()) {
            Debug.getLog().log(Level.WARNING, "GDAL Data Folder not found");
            return;
        }

        // taken from Application because needs GDAL
        try {
            PrimeMeridian.assureInit();
        } catch (Exception e1) {
            Debug.getLog().log(Level.WARNING, "Problems loading prime meridian data", e1);
        }

        try {
            UnitOfMeasure.assureInit();
        } catch (Exception e1) {
            Debug.getLog().log(Level.WARNING, "Problems loading units of measure data", e1);
        }

        initFromPrecompiledFile();
        loadFromApp();

        epsg4326WGS84 = references.get(MSpatialReference.WGS84_ID);
        initSrsSettings(settingsManager);
    }

    public Globe getDefaultGlobe() {
        return globes.getDefaultGlobe();
    }

    private void loadReferencesSorted() {
        // check all and sort them into cathegories
        Iterator<Entry<String, MSpatialReference>> it = references.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MSpatialReference> tmp = it.next();

            // dont do this check, since we have a specially prepared list (due to ScanGDAL) -> speedup loading!!
            // try {
            // tmp.getValue().getGDALspatialReference();
            // } catch (Exception e) {
            // Debug.getLog().log(Level.FINE, "Drop CRS " + tmp.getKey(), e);
            // it.remove();
            // continue;
            // }

            String cat = tmp.getValue().getCathegory();
            // System.out.println(tmp.getValue() + " " + tmp.getValue().isGeographic() + " > " + cat);
            TreeSet<MSpatialReference> set = referencesSorted.get(cat);
            if (set == null) {
                set = new TreeSet<MSpatialReference>();
                referencesSorted.put(tmp.getValue().getCathegory(), set);
            }

            set.add(tmp.getValue());
        }

        if (references.size() == 0) {
            Debug.getLog().config("Number loaded CS: " + references.size());
        } else {
            Debug.getLog()
                .config(
                    "Number loaded CS: "
                        + references.size()
                        + "  in ["
                        + references.firstKey()
                        + " ,  "
                        + references.lastKey()
                        + "]");
        }
    }

    private void initSrsSettings(ISettingsManager settingsManager) {
        SrsSettings srsSettings = settingsManager.getSection(SrsSettings.class);
        srsSettings.applicationSrsProperty().set(getSrsByIdOrDefault(srsSettings.getId()));
    }

    private boolean initGdal() {
        Ellipsoid.assureInit();
        Debug.getLog().config("Initialize GDAL");
        try {
            String gdalData = System.getProperty("GDAL_DATA");
            if (gdalData == null || gdalData.trim().isEmpty()) {
                Debug.getLog().log(Level.WARNING, "GDAL Data Folder not set");
                return false;
            }

            gdal.SetConfigOption("GDAL_DATA", gdalData);

            // magic required to invoke constructor in GDALUtils
            // it finds gdal/data dir under install dir
            GDALUtils.getErrorMessage();

            Debug.getLog().config("GDAL DATA FOLDER: " + gdal.GetConfigOption("GDAL_DATA"));
        } catch (Throwable e1) {
            Debug.getLog().log(Level.SEVERE, "Could not load gdal natives", e1);
        }

        // yes, the following code seems to work... sometimes.. is only checking
        // in one case, not in both
        // gdal.SetConfigOption( "CHECK_WITH_INVERT_PROJ", "TRUE");
        // gdal.SetConfigOption( "THRESHOLD","0.000001");//e1-6==max 2cm
        gdal.SetConfigOption("CPL_LOG_ERRORS", "OFF");
        gdal.SetConfigOption("CPL_LOG_ERRORS", "FALSE");

        // gdal.ErrorReset();

        gdalDataFolder = new File(gdal.GetConfigOption("GDAL_DATA"));
        Debug.getLog().config("GDAL data folder: " + gdalDataFolder);
        if (!gdalDataFolder.exists()) {
            Debug.getLog().log(Level.WARNING, "GDAL Data Folder not found");
            return false;
        }

        return true;
    }

    @Override
    public void loadFromApp() {
        references.putAll(settingsManager.getSection(SrsPrivateSettings.class).getAllPrivateSrs(this));
        loadReferencesSorted();
    }

    @Override
    public void storeToApp() {
        settingsManager.getSection(SrsPrivateSettings.class).saveAllSrs(references);
    }

    private void initFromStream(InputStream in) {
        try (InputStreamReader a = new InputStreamReader(in)) {
            initFromReader(a);
        } catch (IOException e) {
            Debug.getLog().log(Level.SEVERE, "problems loading all spatial references", e);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }

    private void initFromReader(Reader a) {
        int lineNo = 0;
        try (BufferedReader br = new BufferedReader(a); ) {
            String line = br.readLine();
            while (line != null && !line.trim().isEmpty()) {
                try {
                    // System.out.println(line);
                    if (line.startsWith("#")) {
                        line = br.readLine();
                        lineNo++;
                        continue;
                    }

                    MSpatialReference ref = new MSpatialReference(line, this, globes.getDefaultGlobe());
                    // System.out.println("ref:"+ref);
                    references.put(ref.id, ref);
                } catch (Exception e) {
                    Debug.getLog().log(Level.WARNING, "problems loading single spatial reference on line:" + lineNo, e);
                }

                line = br.readLine();
                lineNo++;
            }

        } catch (Exception e) {
            Debug.getLog().log(Level.SEVERE, "problems loading all spatial references", e);
        }
    }

    private void initFromPrecompiledFile() {
        initFromStream(ClassLoader.getSystemResourceAsStream("eu/mavinci/other/gdalEPSGknown.dat"));
    }

    public void main(String[] args) throws Exception {
        String wkt =
            FileHelper.readFileAsString(
                new File(
                    "/home/marco/Intel Mission Control/errorDownloads/d51e5b97e4b8d42fe2e0cd16c5796052_1448888782922/2015-11-19_09-13-16/matchings/5/WKT.txt"));

        System.out.println(MSpatialReference.getWktFromGdal(wkt));

        String line = "location	REG_SZ	C:\\Programme und Einstellungen\\GDAL\\";
        line = line.trim();
        Debug.getLog().log(Level.CONFIG, "[reg query] " + line);
        if (line.startsWith("location")) {
            String[] parts = line.split("\t|\\s\\s+");
            for (String s : parts) {
                System.out.println("::" + s + "::");
            }

            System.out.println(Arrays.asList(parts));
            // System.out.println(parts[2].trim());
            //
            // gdalFolder = new File(parts[2].trim());
            // gdalTransform = new File(gdalFolder,"gdaltransform.exe").getAbsolutePath();
            // System.out.println(gdalTransform);
        }
    }

    @Override
    public MSpatialReference getSrsByIdOrDefault(String srsId) {
        if (Strings.isNullOrEmpty(srsId)) {
            srsId = MSpatialReference.WGS84_ID;
        }

        MSpatialReference srs = references.get(srsId);

        if (srs != null) {
            return srs;
        }

        return epsg4326WGS84;
    }

    @Override
    public MSpatialReference getSrsByIdOrNull(String srsId) {
        if (Strings.isNullOrEmpty(srsId)) {
            return null;
        }

        return references.get(srsId);
    }

    @Override
    public MSpatialReference getDefault() {
        return epsg4326WGS84;
    }

    @Override
    public ObservableMap<String, MSpatialReference> getReferences() {
        return referenceWrapper;
    }

    @Override
    public ObservableMap<String, TreeSet<MSpatialReference>> getReferencesSorted() {
        return sortedReferencesWrapper;
    }

    @Override
    public void delete(String id) {
        getReferences().remove(id);
        storeToApp();
        referencesSorted.clear();
        loadReferencesSorted();
    }

    @Override
    public int getMaxId() {
        maxId = ID_PRIVATE_MIN;
        for (Map.Entry<String, MSpatialReference> entry : references.entrySet()) {
            if (!entry.getValue().isPrivate()) {
                continue;
            }

            if (maxId < entry.getValue().getNo()) {
                maxId = entry.getValue().getNo();
            }
        }

        return maxId;
    }

}
