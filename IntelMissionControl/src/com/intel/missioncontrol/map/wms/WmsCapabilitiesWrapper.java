/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.intel.missioncontrol.map.wms;

import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.settings.WmsServersSettings;
import eu.mavinci.desktop.helper.FileHelper;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around WorldWind's WmsCapabilities. Retrieves wms capabilities, writes/reads them to/from the
 * capabilitiesFile. Manages capabilities file back up.
 */
public class WmsCapabilitiesWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(WmsCapabilitiesWrapper.class);
    public static final String KEY = "eu.mavinci.desktop.gui.doublepanel.mapmanager.wms.MapLayerWMS";
    private static final String HYPERSPECTRAL_KEY = "%com.intel.missioncontrol.map.worldwind.wms.HyperspectralLayer";
    private static final String WMS_KEY = "%com.intel.missioncontrol.map.worldwind.wms.WmsLayer";

    private final File capabilitiesFile;
    private final File capabilitiesFileBackup;
    private final String serverURI;

    // wms server name
    private final AsyncObjectProperty<LayerName> serverName = new SimpleAsyncObjectProperty<>(this);

    // raster sublayers
    private final AsyncListProperty<WmsMap> wmsMaps =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WmsMap>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    public WmsCapabilitiesWrapper(IPathProvider pathProvider, String serverURI, LayerName defaultName) {
        this.serverURI = serverURI;
        serverName.set(defaultName);

        capabilitiesFile =
            new File(
                pathProvider.getCacheDirectory().toString()
                    + File.separatorChar
                    + "wmsCapabilities"
                    + File.separatorChar
                    + FileHelper.urlToFileName(serverURI)
                    + ".xml");
        capabilitiesFileBackup = new File(capabilitiesFile.getAbsolutePath() + "~");

        if (!capabilitiesFile.exists()) {
            capabilitiesFile.getParentFile().mkdirs();
        }

        try {
            if (!capabilitiesFileBackup.exists()) {
                capabilitiesFileBackup.createNewFile();
            }

            FileHelper.copyFile(capabilitiesFile, capabilitiesFileBackup);
        } catch (IOException e) {
            LOGGER.error(
                "Cannot copy WMS capabilities file to a backup file "
                    + capabilitiesFile
                    + " : "
                    + capabilitiesFileBackup);
        }
    }

    /**
     * tries to load wms server capabilities from the internet and to override the capabilities file
     *
     * @throws Exception
     */
    private void retrieveCapabilities() throws Exception {

        // marco: this code is long an ugly, and copyied from the WW Class:

        // Capabilities
        if (this.serverURI == null) {
            String message = Logging.getMessage("nullValue.URIIsNull");
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }

        // Request the capabilities document from the server.
        CapabilitiesRequest req = new CapabilitiesRequest(new URI(this.serverURI), "WMS");
        URL capsURL = req.getUri().toURL();

        URLRetriever retriever =
            URLRetriever.createRetriever(
                capsURL,
                new RetrievalPostProcessor() {
                    public ByteBuffer run(Retriever retriever) {
                        return retriever.getBuffer();
                    }
                });

        if (retriever == null) {
            String message = Logging.getMessage("generic.UnrecognizedProtocol");
            LOGGER.info(message);
            throw new WWRuntimeException(message);
        }

        retriever.call();

        if (!retriever.getState().equals(URLRetriever.RETRIEVER_STATE_SUCCESSFUL)) {
            String message = Logging.getMessage("generic.RetrievalFailed", serverURI.toString());
            LOGGER.info(message);
            throw new WWRuntimeException(message);
        }

        if (retriever.getBuffer() == null || retriever.getBuffer().limit() == 0) {
            String message = Logging.getMessage("generic.RetrievalReturnedNoContent", serverURI.toString());
            LOGGER.info(message);
            throw new WWRuntimeException(message);
        }

        if (retriever.getContentType().equalsIgnoreCase("application/vnd.ogc.se_xml")) {
            String exceptionMessage = WWXML.extractOGCServiceException(retriever.getBuffer());
            String message =
                Logging.getMessage(
                    "OGC.ServiceException",
                    serverURI.toString() + ": " + exceptionMessage != null ? exceptionMessage : "");
            LOGGER.info(message);
            throw new WWRuntimeException(message);
        }

        // Parse the DOM as a capabilities document.
        // is = WWIO.getInputStreamFromByteBuffer(retriever.getBuffer());
        String xml = WWIO.byteBufferToString(retriever.getBuffer(), "iso-8859-1");
        String serverURL = serverURI.toString();
        int pos = serverURL.indexOf('@');
        if (pos >= 0) {
            int pos1 = serverURL.indexOf("//");
            int pos2 = serverURL.indexOf('/', pos1 + 2);
            String domainWithPW = serverURL.substring(0, pos2);
            String userPw = serverURL.substring(pos1 + 2, pos);
            String domainWithoutPW = domainWithPW.replaceFirst(Pattern.quote(userPw + '@'), "");
            // System.out.println("domainWithPW" + domainWithPW);
            // System.out.println("domainWithoutPW" + domainWithoutPW);

            // System.out.println("before" + xml);
            xml = xml.replaceAll(Pattern.quote(domainWithoutPW), Matcher.quoteReplacement(domainWithPW));
            // System.out.println("after" + xml);
        }

        if (xml.indexOf("MAVinci") > 0) {
            // Fixing WMS-Proxy WMS server, since it is not really WMS compatibel
            xml = xml.replaceAll(Pattern.quote("version=\"1.3.0\""), Matcher.quoteReplacement("version=\"1.1.1\""));
            // System.out.println("---->:"+xml);
        }
        // System.out.println("----");
        // System.out.println(xml);

        // System.out.println("write file to: " + capabilitiesFile);
        try (Writer out = new OutputStreamWriter(new FileOutputStream(capabilitiesFile), "iso-8859-1")) {
            out.write(xml);
            out.close();
        }
        // WWIO.writeTextFile(xml, capabilitiesFile);

        if (!FileHelper.canRead(capabilitiesFile, null)) {
            String message = Logging.getMessage("WMS server capabilities file cannot be read " + capabilitiesFile);
            LOGGER.info(message);
            throw new WWRuntimeException(message);
        }
    }

    public void loadCapabilities() throws Exception {
        try (InputStream is = new FileInputStream(capabilitiesFile)) {
            WMSCapabilities caps = new WMSCapabilities(is);
            caps.parse();

            if (serverURI.startsWith("http://services.sentinel-hub.com")) {
                serverName.set(new LayerName(HYPERSPECTRAL_KEY));
            } else if (caps.getServiceInformation() == null) {
                serverName.set(new LayerName(WMS_KEY, serverURI));
                LOGGER.info("WMS Server service Informations are NULL:" + serverURI);
            } else {
                if (caps.getServiceInformation().getServiceTitle() != null) {
                    serverName.set(new LayerName(WMS_KEY, caps.getServiceInformation().getServiceTitle()));
                }
            }

            // Gather up all the named layers and make a world wind layer for each.
            final List<WMSLayerCapabilities> namedLayerCaps = caps.getNamedLayers();

            if (namedLayerCaps == null) {
                throw new Exception("WMS Server has no fitting layers " + serverURI);
            }

            refreshLayerInfoList(caps, namedLayerCaps);

            if (this.wmsMaps.size() == 0) {
                throw new Exception("WMS Server contains no layers: " + serverURI);
            }
        }
    }

    private void refreshLayerInfoList(WMSCapabilities caps, List<WMSLayerCapabilities> namedLayerCaps) {
        List<WmsMap> temp = new ArrayList<>();

        for (WMSLayerCapabilities lc : namedLayerCaps) {
            Set<WMSLayerStyle> styles = lc.getStyles();
            if (styles == null || styles.size() == 0) {
                WmsMap layerInfo = WmsMap.createMapInfo(caps, lc, null);
                if (!temp.contains(layerInfo)) {
                    temp.add(layerInfo);
                }
            } else {
                for (WMSLayerStyle style : styles) {
                    WmsMap layerInfo = WmsMap.createMapInfo(caps, lc, style);
                    if (!temp.contains(layerInfo)) {
                        temp.add(layerInfo);
                    }
                }
            }
        }
        this.wmsMaps.setAll(temp);
    }

    private void restoreBackupCache() throws IOException {
        FileHelper.copyFile(capabilitiesFileBackup, capabilitiesFile);
    }

    public void retrieveAndLoadCapabilities() throws Exception {
        try {
            // trying to retrieve new capabilities
            retrieveCapabilities();
        } finally {
            try {
                // in any case if retrieving succeeded or failed trying to update capabilities from the file
                loadCapabilities();
            } catch (Exception e) {
                // in case of failure: best effort trying to restore capabilities from the backup file and load them
                restoreBackupCache();
                loadCapabilities();
            }
        }
    }

    public AsyncListProperty<WmsMap> wmsMapsProperty() {
        return wmsMaps;
    }

    public void deleteFiles() {
        try {
            capabilitiesFile.delete();
            capabilitiesFileBackup.delete();
        } catch (Exception e) {
            LOGGER.warn("Could not delete capability files of : " + this, e);
        }
    }

    public AsyncObjectProperty<LayerName> serverNameProperty() {
        return serverName;
    }
}
