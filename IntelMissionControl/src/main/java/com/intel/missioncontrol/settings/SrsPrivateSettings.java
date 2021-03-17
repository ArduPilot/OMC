/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.google.inject.Inject;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import eu.mavinci.desktop.helper.gdal.SrsManager;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import org.slf4j.LoggerFactory;

@Deprecated(since = "Switch to async properties")
@SettingsMetadata(section = "srsPrivateSettings")
public class SrsPrivateSettings implements ISettings {

    private final MapProperty<String, SrsPrivateSetting> srsAllPrivateSettings =
        new SimpleMapProperty<>(FXCollections.observableHashMap());

    public SrsPrivateSettings() {
        srsAllPrivateSettings.addListener(
            ((observable, oldValue, newValue) -> {
                if (newValue == null) {}
            }));
    }

    public void add(MSpatialReference srs) {
        srsAllPrivateSettings.put(srs.id, new SrsPrivateSetting(srs));
    }

    class SrsPrivateSetting {

        private final transient ObjectProperty<MSpatialReference> privateSrs = new SimpleObjectProperty<>();
        private final StringProperty id = new SimpleStringProperty();
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty wkt = new SimpleStringProperty();
        private final StringProperty origin = new SimpleStringProperty();

        @Inject
        public SrsPrivateSetting(MSpatialReference srs) {
            privateSrs.addListener(
                ((observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.id.equals(getId())) {
                        id.setValue(newValue.id);
                        name.setValue(newValue.name);
                        wkt.setValue(newValue.wkt);
                        origin.setValue(newValue.getOrigin().toString());
                        System.out.println(privateSrs.toString());
                    }
                }));
            setSrs(srs);
        }

        public MSpatialReference getSrsPrivate(SrsManager srsManager) {
            if (privateSrs == null) {
                MSpatialReference srs = null;
                try {
                    srs =
                        new MSpatialReference(
                            this.id.get(), this.name.get(), this.wkt.get(), srsManager, srsManager.getDefaultGlobe());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return srs;
            }

            return privateSrs.get();
        }

        public StringProperty idProperty() {
            return id;
        }

        public String getId() {
            return id.get();
        }

        public void setSrs(MSpatialReference srs) {
            privateSrs.setValue(srs);
        }
    }

    public void saveAllSrs(TreeMap<String, MSpatialReference> srsReferences) {
        srsAllPrivateSettings.clear();
        for (MSpatialReference srs : srsReferences.values()) {
            if (!srs.isPrivate()) {
                continue;
            }

            srsAllPrivateSettings.put(srs.id, new SrsPrivateSetting(srs));
        }
    }

    public TreeMap<String, MSpatialReference> getAllPrivateSrs(SrsManager srsManager) {
        TreeMap<String, MSpatialReference> references = new TreeMap<>();

        int err = 0;
        if (srsAllPrivateSettings == null || srsAllPrivateSettings.entrySet() == null) {
            return references;
        }

        Iterator<Map.Entry<String, SrsPrivateSetting>> it = srsAllPrivateSettings.entrySet().iterator();
        while (it.hasNext()) {
            MSpatialReference srs;
            SrsPrivateSetting srsPrivateSetting = it.next().getValue();
            if (srsPrivateSetting != null) {
                srs = srsPrivateSetting.getSrsPrivate(srsManager);

                try {
                    srs.check();
                    references.put(srs.id, srs);
                } catch (Exception e) {
                    err++;
                    LoggerFactory.getLogger(SrsPrivateSetting.class)
                        .warn("could not load SRS from settings, WKT: " + srs.wkt, e);
                }
            }
        }

        LoggerFactory.getLogger(SrsPrivateSetting.class)
            .info(
                "loaded " + (srsAllPrivateSettings.getSize() - err) + " SRS from settings, " + err + " with warnings");

        return references;
    }

}
