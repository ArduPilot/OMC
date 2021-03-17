/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.common.io.Files;
import com.intel.missioncontrol.ui.ViewModelBase;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

// TODO: It might be used for the Module licenses tab in About menu
public class LicensesSettingsViewModel extends ViewModelBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicensesSettingsViewModel.class);

    private ListProperty<String> licensesList;
    private String selectedLicense;

    public LicensesSettingsViewModel() {
        selectedLicense = "";
        licensesList = new SimpleListProperty<>();
    }

    public ListProperty<String> getLicenses() {
        try {
            List<String> licensesRes =
                FileHelper.scanFilesJar(
                    MFileFilter.txtFilter,
                    "licences",
                    new File(getClass().getResource("/eu/mavinci/").getPath().replaceFirst("/", "")));

            Collections.sort(
                licensesRes,
                (str1, str2) -> {
                    int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
                    if (res == 0) {
                        res = str1.compareTo(str2);
                    }

                    return res;
                });

            for (String s : licensesRes) {
                s = s.substring(0, s.length() - 4); // remove .txt
                int pos = s.lastIndexOf('/');
                String name = s.substring(pos + 1);
                licensesList.add(name);
            }
        } catch (Exception e1) {
            Debug.getLog().log(Level.SEVERE, "Cant load list of licences", e1);
        }

        return licensesList;
    }

    public String getFileResource() throws IOException {
        String pathName =
            getClass()
                .getResource("/eu/mavinci/licences/")
                .getPath()
                .concat(selectedLicense)
                .concat(".txt")
                .replaceFirst("/", "");

        if (selectedLicense != null) {
            try {
                return Files.toString(new File(pathName), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("Error to load a licence file {}", e.getMessage());
            }
        }

        return "";
    }

    public void setSelectedLicense(String selectedLicense) {
        this.selectedLicense = selectedLicense;
    }

}
