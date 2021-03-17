/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.api;

import com.intel.missioncontrol.flightplantemplate.FlightPlanTemplate;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface IFlightPlanTemplateService {

    List<FlightPlanTemplate> getFlightPlanTemplates();

    FlightPlanTemplate findByName(String templateName);

    void saveTemplate(FlightPlanTemplate flightPlanTemplate);

    FlightPlanTemplate duplicate(FlightPlanTemplate source) throws IOException;

    boolean delete(FlightPlanTemplate template) throws IOException;

    boolean revert(FlightPlanTemplate template) throws IOException, URISyntaxException;

    FlightPlanTemplate importFrom(File file) throws IOException;

    void exportTo(FlightPlanTemplate template, File destination) throws IOException;

    void updateTemplateWith(FlightPlanTemplate template, File file);

    FlightPlanTemplate saveAs(FlightPlanTemplate template, File targetFile)
            throws IOException, InvalidFlightPlanFileException;

    void rename(FlightPlanTemplate template, String newName) throws IOException;

    File generateTemplateFile(File candidate);

    String generateTemplateName(String nameBase);
}
