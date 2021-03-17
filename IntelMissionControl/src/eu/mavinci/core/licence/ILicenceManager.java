/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.licence;

import com.intel.missioncontrol.settings.OperationLevel;
import java.io.File;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface ILicenceManager {
    void registerLicence(File file) throws Exception;

    ReadOnlyObjectProperty<Licence> activeLicenceProperty();

    Licence getActiveLicence();

    ReadOnlyObjectProperty<OperationLevel> maxOperationLevelProperty();

    OperationLevel getMaxOperationLevel();

    ReadOnlyObjectProperty<AllowedUser> allowedUserProperty();

    AllowedUser getAllowedUser();

    void resetToDefaultLicence();

    String getExportHeader();

    String getExportHeaderCore();

    boolean isCompatibleService(String licenceReleaseVersion);

    ReadOnlyBooleanProperty isDJIEditionProperty();

    ReadOnlyBooleanProperty isFalconEditionProperty();
}
