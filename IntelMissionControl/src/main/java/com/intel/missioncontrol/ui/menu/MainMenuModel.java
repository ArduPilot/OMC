/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.menu;

import com.intel.missioncontrol.helper.ILanguageHelper;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.net.URL;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Contains the model for the main menu, consisting of its structure and identifiers for its menu items.
 */
public class MainMenuModel {

    public static MenuModel create(ILanguageHelper languageHelper) {
        return MenuModel.build(
                MenuModel.menu(
                        Project.MENU_CAPTION,
                        languageHelper.toFriendlyName(Project.MENU_CAPTION),
                        MenuModel.group(
                                MenuModel.item(
                                        Project.NEW,
                                        languageHelper.toFriendlyName(Project.NEW),
                                        languageHelper.toFriendlyName(ProjectText.NEW),
                                        Project.NEW.getIcon()),
                                MenuModel.item(
                                        Project.OPEN,
                                        languageHelper.toFriendlyName(Project.OPEN),
                                        languageHelper.toFriendlyName(ProjectText.OPEN),
                                        Project.OPEN.getIcon()),
                                MenuModel.item(
                                        Project.CLOSE,
                                        languageHelper.toFriendlyName(Project.CLOSE),
                                        languageHelper.toFriendlyName(ProjectText.CLOSE))),
                        MenuModel.group(
                                MenuModel.item(
                                        Project.CLONE,
                                        languageHelper.toFriendlyName(Project.CLONE),
                                        languageHelper.toFriendlyName(ProjectText.CLONE)),
                                MenuModel.item(
                                        Project.RENAME,
                                        languageHelper.toFriendlyName(Project.RENAME),
                                        languageHelper.toFriendlyName(ProjectText.RENAME),
                                        Project.RENAME.getIcon()),
                                MenuModel.item(
                                        Project.SHOW,
                                        languageHelper.toFriendlyName(Project.SHOW),
                                        languageHelper.toFriendlyName(ProjectText.SHOW))),
                        MenuModel.group(
                                MenuModel.item(
                                        Project.EXIT,
                                        languageHelper.toFriendlyName(Project.EXIT),
                                        languageHelper.toFriendlyName(ProjectText.EXIT),
                                        new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)))),
                MenuModel.menu(
                        FlightPlan.MENU_CAPTION,
                        languageHelper.toFriendlyName(FlightPlan.MENU_CAPTION),
                        MenuModel.group(
                                MenuModel.item(
                                        FlightPlan.NEW,
                                        languageHelper.toFriendlyName(FlightPlan.NEW),
                                        languageHelper.toFriendlyName(FlightPlanText.NEW),
                                        FlightPlan.NEW.getIcon()),
                                MenuModel.item(
                                        FlightPlan.OPEN,
                                        languageHelper.toFriendlyName(FlightPlan.OPEN),
                                        languageHelper.toFriendlyName(FlightPlanText.OPEN),
                                        FlightPlan.OPEN.getIcon(),
                                        new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)),
                                MenuModel.item(
                                        FlightPlan.CLOSE,
                                        languageHelper.toFriendlyName(FlightPlan.CLOSE),
                                        languageHelper.toFriendlyName(FlightPlanText.CLOSE))),
                        MenuModel.group(
                                MenuModel.item(
                                        FlightPlan.SAVE,
                                        languageHelper.toFriendlyName(FlightPlan.SAVE),
                                        languageHelper.toFriendlyName(FlightPlanText.SAVE),
                                        FlightPlan.SAVE.getIcon(),
                                        new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)),
                                MenuModel.item(
                                        FlightPlan.SAVE_AS,
                                        languageHelper.toFriendlyName(FlightPlan.SAVE_AS),
                                        languageHelper.toFriendlyName(FlightPlanText.SAVE_AS),
                                        new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)),
                                MenuModel.item(
                                        FlightPlan.SAVE_AND_FlY,
                                        languageHelper.toFriendlyName(FlightPlan.SAVE_AND_FlY),
                                        languageHelper.toFriendlyName(FlightPlanText.SAVE_AND_FlY)),
                                MenuModel.item(
                                        FlightPlan.SAVE_AS_TEMPLATE,
                                        languageHelper.toFriendlyName(FlightPlan.SAVE_AS_TEMPLATE),
                                        languageHelper.toFriendlyName(FlightPlanText.SAVE_AS_TEMPLATE))),

                        MenuModel.group(
                                MenuModel.item(
                                        FlightPlan.CLONE,
                                        languageHelper.toFriendlyName(FlightPlan.CLONE),
                                        languageHelper.toFriendlyName(FlightPlanText.CLONE)),
                                MenuModel.item(
                                        FlightPlan.RENAME,
                                        languageHelper.toFriendlyName(FlightPlan.RENAME),
                                        languageHelper.toFriendlyName(FlightPlanText.RENAME),
                                        FlightPlan.RENAME.getIcon()),
                                MenuModel.item(
                                        FlightPlan.EXPORT,
                                        languageHelper.toFriendlyName(FlightPlan.EXPORT),
                                        languageHelper.toFriendlyName(FlightPlanText.EXPORT),
                                        FlightPlan.EXPORT.getIcon(),
                                        new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN))),
                        MenuModel.group(
                                MenuModel.item(
                                        FlightPlan.REVERT_CHANGES,
                                        languageHelper.toFriendlyName(FlightPlan.REVERT_CHANGES),
                                        languageHelper.toFriendlyName(FlightPlanText.REVERT_CHANGES))),
                        MenuModel.group(
                                MenuModel.item(
                                        FlightPlan.AIRMAP_LAANC,
                                        languageHelper.toFriendlyName(FlightPlan.AIRMAP_LAANC),
                                        languageHelper.toFriendlyName(FlightPlanText.AIRMAP_LAANC),
                                        FlightPlan.AIRMAP_LAANC.getIcon(),
                                        new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN)),
                                MenuModel.item(
                                        FlightPlan.UPDATE_PARENT_TEMPLATE,
                                        languageHelper.toFriendlyName(FlightPlan.UPDATE_PARENT_TEMPLATE),
                                        languageHelper.toFriendlyName(FlightPlanText.UPDATE_PARENT_TEMPLATE)),
                                MenuModel.item(
                                        FlightPlan.RECALCULATE,
                                        languageHelper.toFriendlyName(FlightPlan.RECALCULATE),
                                        languageHelper.toFriendlyName(FlightPlanText.RECALCULATE),
                                        new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN))),
                        MenuModel.item(
                                FlightPlan.SHOW,
                                languageHelper.toFriendlyName(FlightPlan.SHOW),
                                languageHelper.toFriendlyName(FlightPlanText.SHOW))
                ),
                MenuModel.menu(
                        Dataset.MENU_CAPTION,
                        languageHelper.toFriendlyName(Dataset.MENU_CAPTION),
                        MenuModel.group(
                                MenuModel.item(
                                        Dataset.NEW,
                                        languageHelper.toFriendlyName(Dataset.NEW),
                                        languageHelper.toFriendlyName(DatasetText.NEW),
                                        Dataset.NEW.getIcon()),
                                MenuModel.item(
                                        Dataset.OPEN,
                                        languageHelper.toFriendlyName(Dataset.OPEN),
                                        languageHelper.toFriendlyName(DatasetText.OPEN),
                                        Dataset.OPEN.getIcon()),
                                MenuModel.item(
                                        Dataset.CLOSE,
                                        languageHelper.toFriendlyName(Dataset.CLOSE),
                                        languageHelper.toFriendlyName(DatasetText.CLOSE))),
                        MenuModel.group(
                                MenuModel.item(
                                        Dataset.SAVE,
                                        languageHelper.toFriendlyName(Dataset.SAVE),
                                        languageHelper.toFriendlyName(DatasetText.SAVE),
                                        Dataset.SAVE.getIcon())),
                        MenuModel.group(
                                MenuModel.item(
                                        Dataset.IMPORT_EXIF,
                                        languageHelper.toFriendlyName(Dataset.IMPORT_EXIF),
                                        languageHelper.toFriendlyName(DatasetText.IMPORT_EXIF)),
                                MenuModel.item(
                                        Dataset.IMPORT_LOG_ONLY,
                                        languageHelper.toFriendlyName(Dataset.IMPORT_LOG_ONLY),
                                        languageHelper.toFriendlyName(DatasetText.IMPORT_LOG_ONLY)),
                                MenuModel.item(
                                        Dataset.SPARSE,
                                        languageHelper.toFriendlyName(Dataset.SPARSE),
                                        languageHelper.toFriendlyName(DatasetText.SPARSE))),
                        MenuModel.menu(
                                Dataset.EXPORT_FILES,
                                languageHelper.toFriendlyName(Dataset.EXPORT_FILES),
                                MenuModel.item(
                                        Dataset.EXPORT_CSV,
                                        languageHelper.toFriendlyName(Dataset.EXPORT_CSV),
                                        languageHelper.toFriendlyName(DatasetText.EXPORT_CSV)),
                                MenuModel.item(
                                        Dataset.EXPORT_EXIF,
                                        languageHelper.toFriendlyName(Dataset.EXPORT_EXIF),
                                        languageHelper.toFriendlyName(DatasetText.EXPORT_EXIF))),
                        MenuModel.menu(
                                Dataset.EXPORT_TO_APPLICATION,
                                languageHelper.toFriendlyName(Dataset.EXPORT_TO_APPLICATION),
                                MenuModel.group(
                                        MenuModel.item(
                                                Dataset.EXPORT_PHOTOSCAN,
                                                languageHelper.toFriendlyName(Dataset.EXPORT_PHOTOSCAN),
                                                languageHelper.toFriendlyName(DatasetText.EXPORT_PHOTOSCAN)),
                                        MenuModel.item(
                                                Dataset.EXPORT_METASHAPE,
                                                languageHelper.toFriendlyName(Dataset.EXPORT_METASHAPE),
                                                languageHelper.toFriendlyName(DatasetText.EXPORT_METASHAPE)),
                                        MenuModel.item(
                                                Dataset.EXPORT_PHOTOSCAN_WITH_DEBUG,
                                                languageHelper.toFriendlyName(Dataset.EXPORT_PHOTOSCAN_WITH_DEBUG),
                                                languageHelper.toFriendlyName(DatasetText.EXPORT_PHOTOSCAN_WITH_DEBUG)),
                                        MenuModel.item(
                                                Dataset.EXPORT_CONTEXT_CAPTURE,
                                                languageHelper.toFriendlyName(Dataset.EXPORT_CONTEXT_CAPTURE),
                                                languageHelper.toFriendlyName(DatasetText.EXPORT_CONTEXT_CAPTURE)),
                                        MenuModel.item(
                                                Dataset.EXPORT_PIX4D,
                                                languageHelper.toFriendlyName(Dataset.EXPORT_PIX4D),
                                                languageHelper.toFriendlyName(DatasetText.EXPORT_PIX4D))),
                                MenuModel.item(
                                        Dataset.EXPORT_SETUP,
                                        languageHelper.toFriendlyName(Dataset.EXPORT_SETUP),
                                        languageHelper.toFriendlyName(DatasetText.EXPORT_SETUP),
                                        Dataset.EXPORT_SETUP.getIcon())),
                        MenuModel.menu(
                                Dataset.UPLOAD_INSIGHT,
                                languageHelper.toFriendlyName(Dataset.UPLOAD_INSIGHT),
                                MenuModel.group(
                                        MenuModel.item(
                                                Dataset.UPLOAD_INSIGHT_PROCESSING,
                                                languageHelper.toFriendlyName(Dataset.UPLOAD_INSIGHT_PROCESSING),
                                                languageHelper.toFriendlyName(DatasetText.UPLOAD_INSIGHT_PROCESSING)),
                                        MenuModel.item(
                                                Dataset.UPLOAD_INSIGHT_NOTPROCESSING,
                                                languageHelper.toFriendlyName(Dataset.UPLOAD_INSIGHT_NOTPROCESSING),
                                                languageHelper.toFriendlyName(DatasetText.UPLOAD_INSIGHT_NOTPROCESSING)),
                                        MenuModel.item(
                                                Dataset.INSIGHT_OPEN,
                                                languageHelper.toFriendlyName(Dataset.INSIGHT_OPEN),
                                                languageHelper.toFriendlyName(DatasetText.INSIGHT_OPEN))),
                                MenuModel.item(
                                        Dataset.INSIGHT_SETTINGS,
                                        languageHelper.toFriendlyName(Dataset.INSIGHT_SETTINGS),
                                        languageHelper.toFriendlyName(DatasetText.INSIGHT_SETTINGS),
                                        Dataset.INSIGHT_SETTINGS.getIcon())),
                        MenuModel.group(
                                MenuModel.item(
                                        Dataset.SHOW,
                                        languageHelper.toFriendlyName(Dataset.SHOW),
                                        languageHelper.toFriendlyName(DatasetText.SHOW)))
                ),
                MenuModel.menu(
                        Help.MENU_CAPTION,
                        languageHelper.toFriendlyName(Help.MENU_CAPTION),
                        MenuModel.item(
                                Help.USER_MANUAL,
                                languageHelper.toFriendlyName(Help.USER_MANUAL),
                                languageHelper.toFriendlyName(HelpText.USER_MANUAL),
                                Help.USER_MANUAL.getIcon(),
                                new KeyCodeCombination(KeyCode.F1)),
                        MenuModel.item(
                                Help.QUICK_START_GUIDE,
                                languageHelper.toFriendlyName(Help.QUICK_START_GUIDE),
                                languageHelper.toFriendlyName(HelpText.QUICK_START_GUIDE),
                                Help.QUICK_START_GUIDE.getIcon()),
                        MenuModel.item(
                                Help.DEMO_MISSION,
                                languageHelper.toFriendlyName(Help.DEMO_MISSION),
                                languageHelper.toFriendlyName(HelpText.DEMO_MISSION)),
                        MenuModel.item(
                                Help.ABOUT,
                                languageHelper.toFriendlyName(Help.ABOUT),
                                languageHelper.toFriendlyName(HelpText.ABOUT),
                                Help.ABOUT.getIcon())),
                MenuModel.menu(
                        Debug.MENU_CAPTION,
                        languageHelper.toFriendlyName(Debug.MENU_CAPTION),
                        MenuModel.item(Debug.RELOAD_CSS, languageHelper.toFriendlyName(Debug.RELOAD_CSS)),
                        MenuModel.checkItem(Debug.REPORT_DIAGNOSTICS, languageHelper.toFriendlyName(Debug.REPORT_DIAGNOSTICS)),
                        MenuModel.menu(
                                Debug.BREAK_AFTER,
                                languageHelper.toFriendlyName(Debug.BREAK_AFTER),
                                MenuModel.checkGroup(
                                        MenuModel.checkItem(Debug.BREAK_AFTER_NEVER, "never"),
                                        MenuModel.checkItem(Debug.BREAK_AFTER_50, "50 ms"),
                                        MenuModel.checkItem(Debug.BREAK_AFTER_100, "100 ms"),
                                        MenuModel.checkItem(Debug.BREAK_AFTER_250, "250 ms"),
                                        MenuModel.checkItem(Debug.BREAK_AFTER_500, "500 ms"),
                                        MenuModel.checkItem(Debug.BREAK_AFTER_1000, "1000 ms"))),
                        MenuModel.checkItem(Debug.WIREFRAME, languageHelper.toFriendlyName(Debug.WIREFRAME))));
    }

    public enum Project implements IKeepAll {
        MENU_CAPTION,
        NEW,
        OPEN,
        CLOSE,
        RENAME,
        CLONE,
        EXIT,
        SHOW;

        @Nullable
        URL getIcon() {
            switch (this) {
                case NEW:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_new.svg");
                case OPEN:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_open.svg");
                case RENAME:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_rename.svg");
                default:
                    return null;
            }
        }
    }

    private enum ProjectText implements IKeepAll {
        NEW,
        OPEN,
        CLOSE,
        RENAME,
        CLONE,
        EXIT,
        SHOW
    }

    public enum FlightPlan implements IKeepAll {
        MENU_CAPTION,
        NEW,
        OPEN,
        CLOSE,
        SAVE,
        SAVE_AND_FlY,
        SAVE_AS,
        SAVE_AS_TEMPLATE,
        RENAME,
        CLONE,
        UPDATE_PARENT_TEMPLATE,
        EXPORT,
        AIRMAP_LAANC,
        RECALCULATE,
        REVERT_CHANGES,
        SHOW;

        @Nullable
        URL getIcon() {
            switch (this) {
                case NEW:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_new.svg");
                case OPEN:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_open.svg");
                case SAVE:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_save.svg");
                case EXPORT:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_export.svg");
                case RENAME:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_rename.svg");
                default:
                    return null;
            }
        }
    }

    private enum FlightPlanText implements IKeepAll {
        NEW,
        OPEN,
        CLOSE,
        SAVE,
        SAVE_AND_FlY,
        SAVE_AS,
        SAVE_AS_TEMPLATE,
        RENAME,
        CLONE,
        UPDATE_PARENT_TEMPLATE,
        EXPORT,
        AIRMAP_LAANC,
        RECALCULATE,
        REVERT_CHANGES,
        SHOW
    }

    public enum Dataset implements IKeepAll {
        MENU_CAPTION,
        NEW,
        OPEN,
        CLOSE,
        SAVE,
        RENAME,
        EXPORT_FILES,
        EXPORT_TO_APPLICATION,
        SHOW,
        SPARSE,
        EXPORT_EXIF,
        EXPORT_CSV,
        EXPORT_PHOTOSCAN,
        EXPORT_METASHAPE,
        EXPORT_CONTEXT_CAPTURE,
        EXPORT_PIX4D,
        EXPORT_SETUP,
        EXPORT_PHOTOSCAN_WITH_DEBUG,
        UPLOAD_INSIGHT,
        UPLOAD_INSIGHT_PROCESSING,
        UPLOAD_INSIGHT_NOTPROCESSING,
        INSIGHT_OPEN,
        INSIGHT_SETTINGS,
        IMPORT_EXIF,
        IMPORT_LOG_ONLY;

        @Nullable
        URL getIcon() {
            switch (this) {
                case NEW:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_new.svg");
                case OPEN:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_open.svg");
                case SAVE:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_save.svg");
                case RENAME:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_rename.svg");
                case EXPORT_SETUP:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_tool_settings.svg");
                case INSIGHT_SETTINGS:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_tool_settings.svg");
                default:
                    return null;
            }
        }
    }

    private enum DatasetText implements IKeepAll {
        NEW,
        OPEN,
        CLOSE,
        SAVE,
        RENAME,
        SHOW,
        SPARSE,
        EXPORT_EXIF,
        EXPORT_CSV,
        EXPORT_PHOTOSCAN,
        EXPORT_METASHAPE,
        EXPORT_CONTEXT_CAPTURE,
        EXPORT_PIX4D,
        EXPORT_SETUP,
        EXPORT_PHOTOSCAN_WITH_DEBUG,
        UPLOAD_INSIGHT,
        UPLOAD_INSIGHT_PROCESSING,
        UPLOAD_INSIGHT_NOTPROCESSING,
        INSIGHT_OPEN,
        INSIGHT_SETTINGS,
        IMPORT_EXIF,
        IMPORT_LOG_ONLY
    }

    public enum Help implements IKeepAll {
        MENU_CAPTION,
        USER_MANUAL,
        QUICK_START_GUIDE,
        DEMO_MISSION,
        ABOUT;

        @Nullable
        URL getIcon() {
            switch (this) {
                case USER_MANUAL:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_menu-read-manual.svg");
                case SUPPORT_REQUEST:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_menu-send-support-request.svg");
                case ABOUT:
                    return Help.class.getResource("/com/intel/missioncontrol/icons/icon_menu-about-IMC.svg");
                default:
                    return null;
            }
        }
    }

    private enum HelpText implements IKeepAll {
        USER_MANUAL,
        QUICK_START_GUIDE,
        DEMO_MISSION,
        ABOUT
    }

    public enum Debug implements IKeepAll {
        MENU_CAPTION,
        RELOAD_CSS,
        REPORT_DIAGNOSTICS,
        BREAK_AFTER,
        BREAK_AFTER_NEVER,
        BREAK_AFTER_50,
        BREAK_AFTER_100,
        BREAK_AFTER_250,
        BREAK_AFTER_500,
        BREAK_AFTER_1000,
        WIREFRAME
    }

}
