/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.ui.MainViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;

/**
 * This interface defines the basic back-end operations to be performed on a mission, like: get recent missions, create
 * new mission, delete empty mission, clone a mission.
 *
 * @author aiacovici
 */
public interface IMissionManager {

    /**
     * This method creates a new mission and returns it's name. Creating a mission basically means creating the folder
     * structure with the default mission name (date-time of mission creation) on disk.
     */
    Mission createNewMission() throws IOException;

    void refreshRecentMissionInfos();

    Mission openMission(Path directory);

    Mission openMission(MissionInfo missionInfo);

    void renameMission(Mission mission, String newName);

    /**
     * Creates a copy of mission selected in a list. A copy of the selected mission folder is created on disk, removing
     * the flight plans ( AOIs are kept), matching and other contents. The default name of the cloned mission is "Clone
     * of original-mission-name". Returns true if the current mission was successfully copied.
     */
    Mission cloneMission(Mission mission) throws IOException;

    /**
     * Deletes blank missions, that don't have any AOIs, flight plans or matchings. Calling this method will remove the
     * directories of the empty missions, as well as the directory structure inside Returns true, if all the empty
     * mission folders were successfully deleted, and false otherwise
     *
     * @return true if and only if all the empty mission folders were successfully deleted; false otherwise
     */
    boolean deleteEmptyMissions();

    /**
     * Deletes the mission if it is empty.
     *
     * @param mission mission to be deleted
     * @return true if and only if the mission could be successfully deleted; false otherwise
     */
    boolean deleteEmptyMission(Mission mission);

    boolean isMissionFolder(File parentDir);

    boolean isMissionEmpty(MissionInfo mission);

    boolean isMissionCloneable(MissionInfo mission);

    void saveMission(Mission currentMission);

    /**
     * Verify if mission has clone.
     *
     * @param mission mission to check
     * @return
     */
    boolean missionHasClone(Mission mission);

    boolean missionExists(String missionName);

    AsyncListProperty<MissionInfo> recentMissionInfosProperty();

    Mission loadMissionInTemplateMode(Mission currentMission, List<FlightPlanTemplate> templates);

    Mission unloadMissionFromTemplateMode(Mission currentMission);

    boolean isValidMissionName(String name);

    /**
     * Deletes the Demo project from the projects folder and recent missions if present, returns true if it was
     * successfully deleted
     */
    boolean deleteDemoMission(Mission mission);

    void makeDefaultScreenshot(Mission mission);

    void makeScreenshot(Mission mission);

    void refreshRecentMissionListItems();

    ObservableValue<? extends AsyncObservableList<ViewModel>> recentMissionListItems();

    void moveRecentMissions(String oldPath, String newPath);

    MissionInfo getByRemoteId(String remoteId);

    void setMainViewModel(MainViewModel mainViewModel);

    MainViewModel getMainViewModel();

}
