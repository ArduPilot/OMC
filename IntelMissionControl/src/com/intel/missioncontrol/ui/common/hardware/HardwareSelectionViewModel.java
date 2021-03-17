/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.common.hardware;

import com.google.inject.Inject;
import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import com.intel.missioncontrol.hardware.ILensDescription;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.ViewModelBase;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.checkerframework.checker.nullness.qual.NonNull;

public class HardwareSelectionViewModel extends ViewModelBase {

    private final ListProperty<PlatformItem> availablePlatformItems =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<IGenericCameraDescription> availableCameras =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ILensDescription> availableLenses =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<PlatformItem> selectedPlatformItem = new SimpleObjectProperty<>();
    private final ObjectProperty<IGenericCameraDescription> selectedCamera = new SimpleObjectProperty<>();
    private final ObjectProperty<ILensDescription> selectedLens = new SimpleObjectProperty<>();

    // could have null value in the beginning
    // hardwareConfiguration is bound to currentMission.currentMatching.hardwareConfiguration or
    // currentMission.currentFlightPlan.hardwareConfiguration depending on the active view
    // method bindHardwareConfiguration is used for that
    private final ObjectProperty<IHardwareConfiguration> hardwareConfiguration = new SimpleObjectProperty<>();

    private final IHardwareConfigurationManager hardwareConfigurationManager;
    private final GeneralSettings generalSettings;

    boolean muteSelectedHardwareListenerDescripton;

    private final INotificationObject.ChangeListener selectedHardwareListenerDescripton =
        (INotificationObject.ChangeListener)
            (event) -> {
                if (muteSelectedHardwareListenerDescripton) {
                    return;
                }

                reinitHardwareConfiguration();
            };

    private final ChangeListener<IHardwareConfiguration> selectedHardwareListener =
        (ChangeListener<IHardwareConfiguration>)
            (observable, oldValue, newValue) -> {
                if (oldValue != null) {
                    oldValue.removeListener(selectedHardwareListenerDescripton);
                }

                if (newValue != null) {
                    newValue.addListener(selectedHardwareListenerDescripton);
                    reinitHardwareConfiguration();
                }
            };
    private final InvalidationListener selectedHardwareInvalidationListener =
        (InvalidationListener)
            (observable) -> {
                if (muteSelectedHardwareListenerDescripton) {
                    return;
                }
                if (hardwareConfiguration.get() != null) {
                    hardwareConfiguration.get().addListener(selectedHardwareListenerDescripton);
                    reinitHardwareConfiguration();
                }
            };

    private final ChangeListener<OperationLevel> operationLevelListener =
        (ChangeListener<OperationLevel>)
            (observable, oldValue, newValue) -> {
                onReload = true;
                refreshPlatformItems(newValue);
                onReload = false;
            };

    private final ChangeListener<PlatformItem> selectedPlatformItemListener;
    private final ChangeListener<ILensDescription> selectedLensListener;
    private final ChangeListener<IGenericCameraDescription> selectedCameraListener;

    private boolean onReload;

    @Inject
    public HardwareSelectionViewModel(
            IHardwareConfigurationManager hardwareConfigurationManager, ISettingsManager settingsManager) {
        this.hardwareConfigurationManager = hardwareConfigurationManager;
        this.generalSettings = settingsManager.getSection(GeneralSettings.class);

        selectedLensListener =
            (observable, oldValue, newValue) -> {
                if (newValue == null || onReload) {
                    return;
                }

                muteSelectedHardwareListenerDescripton = true;
                hardwareConfiguration
                    .get()
                    .getPrimaryPayload(IGenericCameraConfiguration.class)
                    .getLens()
                    .setDescription(newValue);

                muteSelectedHardwareListenerDescripton = false;
            };

        selectedCameraListener =
            (observable, oldValue, newValue) -> {
                if (newValue == null || onReload) {
                    return;
                }

                muteSelectedHardwareListenerDescripton = true;
                hardwareConfiguration
                    .get()
                    .getPrimaryPayload(IGenericCameraConfiguration.class)
                    .setDescription(newValue);
                refreshLensItems();
                muteSelectedHardwareListenerDescripton = false;
            };

        selectedPlatformItemListener =
            (observable, oldValue, newValue) -> {
                if (newValue == null || onReload) {
                    return;
                }

                muteSelectedHardwareListenerDescripton = true;
                this.hardwareConfiguration.get().setPlatformDescription(newValue.getDescription());

                refreshCameraItems();
                selectedCamera.set(chooseCurrentCamera());
                refreshLensItems();
                selectedLens.set(chooseCurrentLens());

                muteSelectedHardwareListenerDescripton = false;
            };
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        selectedPlatformItem.addListener(new WeakChangeListener<>(selectedPlatformItemListener));
        selectedCamera.addListener(new WeakChangeListener<>(selectedCameraListener));
        selectedLens.addListener(new WeakChangeListener<>(selectedLensListener));
        generalSettings.operationLevelProperty().addListener(new WeakChangeListener<>(operationLevelListener));
        refreshPlatformItems(generalSettings.getOperationLevel());
    }

    public void bindHardwareConfiguration(ObjectProperty<IHardwareConfiguration> hardwareConfiguration) {
        this.hardwareConfiguration.unbind();
        this.hardwareConfiguration.bind(hardwareConfiguration);
        this.hardwareConfiguration.addListener(new WeakChangeListener<>(selectedHardwareListener));
        this.hardwareConfiguration.addListener(new WeakInvalidationListener(selectedHardwareInvalidationListener));
        reinitHardwareConfiguration();
    }

    private void reinitHardwareConfiguration() {
        if (hardwareConfiguration.get() == null) {
            return;
        }

        onReload = true;
        try {
            refreshLensItems();
            selectedLens.set(chooseCurrentLens());
            refreshCameraItems();
            selectedCamera.set(chooseCurrentCamera());
            selectPlatformItem(hardwareConfiguration.get().getPlatformDescription());
        } finally {
            onReload = false;
        }
    }

    private ILensDescription chooseCurrentLens() {
        ILensDescription currentLens =
            hardwareConfiguration.get().getPrimaryPayload(IGenericCameraConfiguration.class).getLens().getDescription();
        for (ILensDescription lens : availableLenses) {
            if (lens.equals(currentLens)) {
                return lens;
            }
        }

        if (availableLenses.size() > 0) {
            return availableLenses.get(0);
        }

        return null;
    }

    private IGenericCameraDescription chooseCurrentCamera() {
        IGenericCameraDescription currentCamera =
            hardwareConfiguration.get().getPrimaryPayload(IGenericCameraConfiguration.class).getDescription();
        for (IGenericCameraDescription camera : availableCameras) {
            if (camera.equals(currentCamera)) {
                return camera;
            }
        }

        if (availableCameras.size() > 0) {
            return availableCameras.get(0);
        }

        return null;
    }

    private synchronized void refreshPlatformItems(OperationLevel operationLevel) {
        List<IPlatformDescription> availablePlatforms = Arrays.asList(hardwareConfigurationManager.getPlatforms());

        List<IPlatformDescription> fixedWings =
            availablePlatforms
                .stream()
                .filter(IPlatformDescription::isInFixedWingEditionMode)
                .collect(Collectors.toList());
        List<IPlatformDescription> multicopters =
            availablePlatforms.stream().filter(desc -> !desc.isInFixedWingEditionMode()).collect(Collectors.toList());

        availablePlatformItems.clear();

        if (!fixedWings.isEmpty()) {
            availablePlatformItems.add(new PlatformItem(PlatformItemType.FIXED_WING));
            for (IPlatformDescription desc : fixedWings) {
                availablePlatformItems.add(new PlatformItem(PlatformItemType.FIXED_WING, desc));
            }
        }

        if (!multicopters.isEmpty()) {
            availablePlatformItems.add(new PlatformItem(PlatformItemType.MULTICOPTER));
            for (IPlatformDescription desc : multicopters) {
                availablePlatformItems.add(new PlatformItem(PlatformItemType.MULTICOPTER, desc));
            }
        }

        reinitHardwareConfiguration();
    }

    private void refreshCameraItems() {
        // fix available cameras in case this might have changed
        IGenericCameraDescription[] supportedCameras =
            hardwareConfigurationManager.getCompatibleCameras(hardwareConfiguration.get().getPlatformDescription());

        // update in any case because the instance of selected camera should be contained in the list (not just equal
        // instance)
        availableCameras.setAll(supportedCameras);
    }

    private void refreshLensItems() {
        IGenericCameraConfiguration selectedCameraConfiguration =
            hardwareConfiguration.get().getPrimaryPayload(IGenericCameraConfiguration.class);

        ILensDescription[] supportedLenses =
            hardwareConfigurationManager.getCompatibleLenses(selectedCameraConfiguration.getDescription());

        availableLenses.setAll(supportedLenses);

        selectedLens.setValue(availableLenses.get(0));
    }

    private void selectPlatformItem(@NonNull IPlatformDescription description) {
        for (PlatformItem item : availablePlatformItems) {
            IPlatformDescription itemDesc = item.getDescription();
            if (itemDesc == description) {
                selectedPlatformItem.set(item);
                break;
            }
        }
    }

    public ObservableMap<String, ValidationMessage> getWarnings() {
        // TODO: FANCYPANE - removed warnings
        return FXCollections.observableHashMap();
        // return warningsScope.getAnalysisWarnings();
    }

    public ReadOnlyListProperty<PlatformItem> availablePlatformItemsProperty() {
        return availablePlatformItems;
    }

    public ReadOnlyListProperty<IGenericCameraDescription> availableCamerasProperty() {
        return availableCameras;
    }

    public ReadOnlyListProperty<ILensDescription> availableLensesProperty() {
        return availableLenses;
    }

    public ObjectProperty<PlatformItem> selectedPlatformItemProperty() {
        return selectedPlatformItem;
    }

    public ObjectProperty<IGenericCameraDescription> selectedCameraProperty() {
        return selectedCamera;
    }

    public ObjectProperty<ILensDescription> selectedLensProperty() {
        return selectedLens;
    }

}
