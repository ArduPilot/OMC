/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis.options;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.common.BindingUtils;
import eu.mavinci.core.helper.StringHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;

public class AnalysisOptionsStatisticsViewModel extends ViewModelBase {

    private static final double ORTHO_RATIO_WARNING = 0.99;

    private static final String KB = "KB";
    private static final String MB = "MB";
    private static final String GB = "GB";
    private static final String TB = "TB";

    private static final String SIZE_FORMAT = "%.2f %s";

    private static final long KILOBYTE = 1024L;
    private static final long MEGABYTE = KILOBYTE * KILOBYTE;
    private static final long GIGABYTE = MEGABYTE * KILOBYTE;
    private static final long TERABYTE = GIGABYTE * KILOBYTE;

    private static final String RESOLUTION_FORMAT = "%dx%d px (%.1f MP)";

    private static final String KEY_PSEUDO_ORTHO_ITEM =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsStatisticsView.pseudoOrthoItem";

    private static final String KEY_TRUE_ORTHO_ITEM =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsStatisticsView.trueOrthoItem";

    private static final String KEY_PROGRESS_DESCRIPTION =
        "com.intel.missioncontrol.ui.analysis.AnalysisOptionsStatisticsView.lblProgressDescription";

    private final IntegerProperty filteredItemsCount = new SimpleIntegerProperty(0);
    private final IntegerProperty picturesCount = new SimpleIntegerProperty(0);

    private final LongProperty filteredPicturesSizeBytes = new SimpleLongProperty(0L);
    private final StringProperty filteredPicturesSizeDescription = new SimpleStringProperty("");
    private final StringProperty filteredPictureType = new SimpleStringProperty("");

    private final LongProperty rtkFixCount = new SimpleLongProperty(0L);
    private final LongProperty rtkFloatCount = new SimpleLongProperty(0L);
    private final LongProperty diffGpsFixCount = new SimpleLongProperty(0L);
    private final LongProperty gpsFixCount = new SimpleLongProperty(0L);

    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty progressDescription = new SimpleStringProperty("");

    private final ListProperty<CoverageItem> coverageItems =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    private final StringProperty imageResolutionDescription = new SimpleStringProperty("");

    private final ObjectProperty<IHardwareConfiguration> hardwareConfiguration = new SimpleObjectProperty<>();

    private final ChangeListener<? super Number> progressListener =
        (observable, oldValue, newValue) -> updateProgress();

    private final BooleanProperty rtkStatisticVisible = new SimpleBooleanProperty();

    private final ILanguageHelper languageHelper;
    private final ISettingsManager settingsManager;
    private final ObjectProperty<Matching> currentMatching = new SimpleObjectProperty<>();

    private CoverageItem trueOrthoItem;
    private CoverageItem pseudoOrthoItem;
    private ChangeListener<IHardwareConfiguration> hardwareConfigurationChangeListener;

    @Inject
    public AnalysisOptionsStatisticsViewModel(
            ILanguageHelper languageHelper, ISettingsManager settingsManager, IApplicationContext applicationContext) {
        this.languageHelper = languageHelper;
        this.settingsManager = settingsManager;
        rtkStatisticVisible.bind(
            settingsManager.getSection(GeneralSettings.class).operationLevelProperty().isEqualTo(OperationLevel.DEBUG));

        initCoverageItems();
        initSystemOfMeasurement();

        filteredItemsCount.addListener(progressListener);
        picturesCount.addListener(progressListener);

        hardwareConfigurationChangeListener = (observable, oldValue, newValue) -> updateImageResolutionDescription();
        hardwareConfiguration.addListener(new WeakChangeListener<>(hardwareConfigurationChangeListener));
        filteredPicturesSizeBytes.addListener((observable, oldValue, newValue) -> updatePicturesSizeDescription());

        currentMatching.addListener((observable, oldValue, newValue) -> initMatching(newValue));
        currentMatching.bind(
            PropertyPath.from(applicationContext.currentMissionProperty())
                .selectReadOnlyObject(Mission::currentMatchingProperty));
    }

    private void initSystemOfMeasurement() {
        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        generalSettings
            .systemOfMeasurementProperty()
            .addListener((observable, oldValue, newValue) -> updateCoverageItems());
    }

    private void initMatching(Matching matching) {
        if (matching == null) {
            resetMatchingProperties();
            return;
        }

        BindingUtils.rebind(filteredItemsCount, Matching::filteredItemsCountProperty, matching);
        BindingUtils.rebind(picturesCount, Matching::picturesCountProperty, matching);

        BindingUtils.rebind(filteredPicturesSizeBytes, Matching::filteredPicturesSizeBytesProperty, matching);
        BindingUtils.rebind(filteredPictureType, Matching::filteredPictureTypeProperty, matching);

        BindingUtils.rebind(rtkFixCount, Matching::rtkFixCountProperty, matching);
        BindingUtils.rebind(rtkFloatCount, Matching::rtkFloatCountProperty, matching);
        BindingUtils.rebind(diffGpsFixCount, Matching::diffGpsFixCountProperty, matching);
        BindingUtils.rebind(gpsFixCount, Matching::gpsFixCountProperty, matching);

        BindingUtils.rebind(hardwareConfiguration, Matching::hardwareConfigurationProperty, matching);

        bindCoverageItems(matching);
    }

    private void resetMatchingProperties() {
        filteredItemsCount.unbind();
        picturesCount.unbind();

        filteredPicturesSizeBytes.unbind();
        filteredPictureType.unbind();

        rtkFixCount.unbind();
        rtkFloatCount.unbind();
        diffGpsFixCount.unbind();
        gpsFixCount.unbind();

        hardwareConfiguration.unbind();

        filteredItemsCount.set(0);
        picturesCount.set(0);
        filteredPicturesSizeBytes.set(0L);
        filteredPictureType.set("");

        rtkFixCount.set(0L);
        rtkFloatCount.set(0L);
        diffGpsFixCount.set(0L);
        gpsFixCount.set(0L);

        imageResolutionDescription.set("");
    }

    private void updateProgress() {
        int picturesTotalCount = picturesCount.get();
        int picturesFilteredCount = filteredItemsCount.get();

        double progressValue =
            (((picturesTotalCount > 0) && (picturesFilteredCount > 0))
                ? ((double)picturesFilteredCount / (double)picturesTotalCount)
                : (0.0));

        Dispatcher.postToUI(
            () -> {
                progress.set(progressValue);
                progressDescription.set(
                    languageHelper.getString(KEY_PROGRESS_DESCRIPTION, picturesFilteredCount, picturesTotalCount));
            });
    }

    private void initCoverageItems() {
        coverageItems.clear();

        trueOrthoItem = new CoverageItem(languageHelper.getString(KEY_TRUE_ORTHO_ITEM));
        coverageItems.add(trueOrthoItem);

        pseudoOrthoItem = new CoverageItem(languageHelper.getString(KEY_PSEUDO_ORTHO_ITEM));
        coverageItems.add(pseudoOrthoItem);
    }

    private void bindCoverageItems(Matching matching) {
        BindingUtils.rebind(trueOrthoItem.orthoRatioProperty(), Matching::trueOrthoRatioProperty, matching);
        BindingUtils.rebind(pseudoOrthoItem.orthoRatioProperty(), Matching::pseudoOrthoRatioProperty, matching);

        BindingUtils.rebind(trueOrthoItem.areaProperty(), Matching::trueOrthoAreaProperty, matching);
        BindingUtils.rebind(pseudoOrthoItem.areaProperty(), Matching::pseudoOrthoAreaProperty, matching);
    }

    private void updateCoverageItems() {
        trueOrthoItem.updateDescription();
        pseudoOrthoItem.updateDescription();
    }

    private void updateImageResolutionDescription() {
        IGenericCameraDescription cameraDescription =
            hardwareConfiguration.get().getPrimaryPayload(IGenericCameraConfiguration.class).getDescription();
        String resolutionDescription =
            ((cameraDescription == null) ? ("") : (buildImageResolutionDescription(cameraDescription)));

        Dispatcher.postToUI(
            () -> {
                imageResolutionDescription.set(resolutionDescription);
            });
    }

    private String buildImageResolutionDescription(IGenericCameraDescription cameraDescription) {
        long resolution = (long)cameraDescription.getCcdResX() * (long)cameraDescription.getCcdResY();
        double resolutionMp = (double)resolution / 1000000.0;

        return String.format(
            RESOLUTION_FORMAT, cameraDescription.getCcdResX(), cameraDescription.getCcdResY(), resolutionMp);
    }

    private void updatePicturesSizeDescription() {
        long sizeBytes = filteredPicturesSizeBytes.get();
        String sizeDescription = buildSizeDescription(sizeBytes);

        Dispatcher.postToUI(
            () -> {
                filteredPicturesSizeDescription.set(sizeDescription);
            });
    }

    private String buildSizeDescription(long sizeBytes) {
        if (sizeBytes <= 0L) {
            return "0 KB";
        }

        if (sizeBytes < MEGABYTE) {
            return String.format(SIZE_FORMAT, (double)sizeBytes / KILOBYTE, KB);
        }

        if (sizeBytes < GIGABYTE) {
            return String.format(SIZE_FORMAT, (double)sizeBytes / MEGABYTE, MB);
        }

        if (sizeBytes < TERABYTE) {
            return String.format(SIZE_FORMAT, (double)sizeBytes / GIGABYTE, GB);
        }

        return String.format(SIZE_FORMAT, (double)sizeBytes / TERABYTE, TB);
    }

    public IntegerProperty filteredItemsCountProperty() {
        return filteredItemsCount;
    }

    public IntegerProperty picturesCountProperty() {
        return picturesCount;
    }

    public LongProperty filteredPicturesSizeBytesProperty() {
        return filteredPicturesSizeBytes;
    }

    public Property<String> filteredPicturesSizeDescriptionProperty() {
        return filteredPicturesSizeDescription;
    }

    public Property<String> filteredPictureTypeProperty() {
        return filteredPictureType;
    }

    public LongProperty rtkFixCountProperty() {
        return rtkFixCount;
    }

    public LongProperty rtkFloatCountProperty() {
        return rtkFloatCount;
    }

    public LongProperty diffGpsFixCountProperty() {
        return diffGpsFixCount;
    }

    public LongProperty gpsFixCountProperty() {
        return gpsFixCount;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public StringProperty progressDescriptionProperty() {
        return progressDescription;
    }

    public ListProperty<CoverageItem> coverageItemsProperty() {
        return coverageItems;
    }

    public StringProperty imageResolutionDescriptionProperty() {
        return imageResolutionDescription;
    }

    public BooleanProperty rtkStatisticVisibleProperty() {
        return rtkStatisticVisible;
    }

    public static class CoverageItem {

        private final StringProperty name = new SimpleStringProperty("");
        private final DoubleProperty area = new SimpleDoubleProperty(0.0);
        private final StringProperty areaDescription = new SimpleStringProperty("0");
        private final DoubleProperty orthoRatio = new SimpleDoubleProperty(0.0);
        private final BooleanProperty warning = new SimpleBooleanProperty(false);

        public CoverageItem(String name) {
            this.name.set(name);
            this.area.addListener((observable, oldValue, newValue) -> updateDescription(newValue));

            this.orthoRatio.addListener(
                (observable, oldValue, newValue) -> {
                    warning.set(
                        (newValue == null)
                            || ((newValue.doubleValue() < ORTHO_RATIO_WARNING)) && newValue.doubleValue() >= 0);
                });
        }

        public void updateDescription() {
            updateDescription(area.getValue());
        }

        private void updateDescription(Number value) {
            double areaValue = extractPositiveDoubleValue(value);
            String areaDescription = StringHelper.areaToIngName(areaValue, -3, false);

            Dispatcher.postToUI(
                () -> {
                    this.areaDescription.set(areaDescription);
                });
        }

        private static double extractPositiveDoubleValue(Number value) {
            if (value == null) {
                return 0.0;
            }

            return Math.max(value.doubleValue(), 0.0);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public DoubleProperty areaProperty() {
            return area;
        }

        public StringProperty areaDescriptionProperty() {
            return areaDescription;
        }

        public DoubleProperty orthoRatioProperty() {
            return orthoRatio;
        }

        public BooleanProperty warningProperty() {
            return warning;
        }

    }

}
