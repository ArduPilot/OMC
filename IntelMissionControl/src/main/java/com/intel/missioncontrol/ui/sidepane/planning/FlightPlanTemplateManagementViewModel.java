/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.api.IFlightPlanTemplateService;
import com.intel.missioncontrol.common.IPathProvider;
import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressLinter(value = "IllegalViewModelMethod", reviewer = "mstrauss", justification = "legacy file")
public class FlightPlanTemplateManagementViewModel extends DialogViewModel {

    private static final Logger logger = LoggerFactory.getLogger(FlightPlanTemplateManagementViewModel.class);

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private PlanningScope planningScope;

    @Inject
    private ILanguageHelper languageHelper;

    @Inject
    private NotificationCenter notificationCenter;

    @Inject
    private IFlightPlanTemplateService flightPlanTemplateService;

    @Inject
    private IMissionManager missionManager;

    @Inject
    private IApplicationContext applicationContext;

    @Inject
    private IPathProvider pathProvider;

    private ObservableList<FlightPlanTemplateManagementItem> templates = FXCollections.observableArrayList();

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        templates.clear();
        List<FlightPlanTemplate> fpTemplates = flightPlanTemplateService.getFlightPlanTemplates();
        if (fpTemplates != null) {
            List<FlightPlanTemplateManagementItem> list =
                fpTemplates
                    .stream()
                    .map(t -> new FlightPlanTemplateManagementItem(t, languageHelper))
                    .collect(Collectors.toList());
            templates.addAll(list);
        }
    }

    public ObservableList<FlightPlanTemplateManagementItem> getTemplates() {
        return templates;
    }

    public boolean useTemplate(FlightPlanTemplateManagementItem item) {
        if (item == null) {
            return false;
        }

        FlightPlanTemplate template = item.getFpTemplate();
        notificationCenter.publish(
            StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT, StartPlanningViewModel.USE_ACTION, template);
        return true;
    }

    void duplicateTemplate(FlightPlanTemplateManagementItem source) {
        if (source == null) {
            return;
        }

        FlightPlanTemplate sourceTemplate = source.getFpTemplate();
        try {
            FlightPlanTemplate targetTemplate = flightPlanTemplateService.duplicate(sourceTemplate);
            templates.add(new FlightPlanTemplateManagementItem(targetTemplate, languageHelper));
            notificationCenter.publish(
                StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT,
                StartPlanningViewModel.CREATE_ACTION,
                targetTemplate);
        } catch (IOException e) {
            logger.error(String.format("Failure on duplicate the flight plan '%s'", source.getFpName()), e);
        }
    }

    void importTemplate(File file) {
        try {
            FlightPlanTemplate templateImportted = flightPlanTemplateService.importFrom(file);
            templates.add(new FlightPlanTemplateManagementItem(templateImportted, languageHelper));
            notificationCenter.publish(
                StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT,
                templateImportted,
                StartPlanningViewModel.CREATE_ACTION);
        } catch (IOException e) {
            logger.error(String.format("Failure on a flight plan import from %s", file), e);
        }
    }

    void exportTemplate(File destination, final Collection<FlightPlanTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }

        templates.forEach(
            t -> {
                try {
                    flightPlanTemplateService.exportTo(t, destination);
                } catch (Exception ex) {
                    logger.error("Failure on export of '%s' flight plan template", ex);
                }
            });
    }

    boolean deleteTemplate(int index) {
        try {
            FlightPlanTemplate template = templates.get(index).getFpTemplate();
            if (flightPlanTemplateService.delete(template)) {
                notificationCenter.publish(
                    StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT, StartPlanningViewModel.DELETE_ACTION, template);
                return templates.remove(index) != null;
            }
        } catch (Exception e) {
            logger.error("Unable to delete a flight plan template", e);
        }

        return false;
    }

    void revertTemplate(int index) {
        try {
            FlightPlanTemplateManagementItem item = templates.get(index);
            FlightPlanTemplate template = item.getFpTemplate();
            if (flightPlanTemplateService.revert(template)) {
                item.refresh();
                notificationCenter.publish(
                    StartPlanningViewModel.FLIGHT_PLAN_TEMPLATE_EVENT, StartPlanningViewModel.REFRESH_ACTION, template);
            }
        } catch (Exception e) {
            logger.error("Unable to delete a flight plan template", e);
        }
    }

    boolean alreadyHasSuchFiles(List<File> selectedTemplates) {
        if (selectedTemplates == null || selectedTemplates.isEmpty()) {
            return true;
        }

        final Path templateFolder = pathProvider.getTemplatesDirectory();
        return selectedTemplates.stream().map(file -> templateFolder.resolve(file.getName())).anyMatch(Files::exists);
    }

    void editTemplate(FlightPlanTemplateManagementItem selectedItem) {
        Mission mission =
            missionManager.loadMissionInTemplateMode(
                applicationContext.getCurrentMission(), flightPlanTemplateService.getFlightPlanTemplates());
        mission.currentFlightPlanTemplateProperty().set(selectedItem.getFpTemplate());
    }
}
