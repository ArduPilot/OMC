/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.flightplantemplate;

import com.intel.missioncontrol.helper.MavinciObjectFactory;
import com.intel.missioncontrol.mission.FlightPlan;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.InvalidFlightPlanFileException;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FlightPlanTemplate {

    private StringProperty name = new SimpleStringProperty();

    private FlightPlan flightPlan;

    private BooleanProperty system = new SimpleBooleanProperty(false);

    /**
     * @param name - Displayable name for template
     * @param flightPlan - Flightplan which will be used as a template
     */
    public FlightPlanTemplate(String name, FlightPlan flightPlan) {
        this.name.set(name);
        this.flightPlan = flightPlan;
        this.flightPlan.isNameSetProperty().set(true);
    }

    public boolean isSystem() {
        return system.get();
    }

    public BooleanProperty systemProperty() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system.set(system);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public FlightPlan getFlightPlan() {
        return flightPlan;
    }

    public LinkedHashMap<AreasOfInterestType, List<PlanType>> getAvailableAreasOfInterest() {
        return null;
    }

    public FlightPlan produceFlightPlan() {
        Flightplan copyOfLegacyPlan = new Flightplan();
        copyOfLegacyPlan.setFile(flightPlan.getLegacyFlightplan().getFile());
        try {
            copyOfLegacyPlan.open(flightPlan.getLegacyFlightplan().getFile());
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog()
                .log(Level.WARNING, "Cannot create a flightplan based on a template " + this + " : " + e.getCause());
            FlightPlan plan = new FlightPlan(copyOfLegacyPlan, false);
            return plan;
        }

        FlightPlan plan = new FlightPlan(copyOfLegacyPlan, false);
        plan.setBasedOnTemplate(this);
        return plan;
    }

    public void reload(FlightPlanCreator<File, Flightplan> legacyFpCretor) {
        if (legacyFpCretor == null) {
            return;
        }

        Flightplan legacyFpReloaded = null;
        try {
            legacyFpReloaded = legacyFpCretor.apply(flightPlan.getLegacyFlightplan().getFile());
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog()
                .severe(
                    "Cannot reload a flightplan from a file "
                        + flightPlan.getLegacyFlightplan().getFile()
                        + " : "
                        + e.getCause());
            return;
        }

        flightPlan = new FlightPlan(legacyFpReloaded, false);
    }

    public void reload(MavinciObjectFactory mavinciObjectFactory) {
        Flightplan legacyFpReloaded = null;
        try {
            legacyFpReloaded = mavinciObjectFactory.flightPlanFromTemplate(flightPlan.getLegacyFlightplan().getFile());
        } catch (InvalidFlightPlanFileException e) {
            Debug.getLog()
                .severe(
                    "Cannot create a flightplan from a file "
                        + flightPlan.getLegacyFlightplan().getFile()
                        + " : "
                        + e.getCause());
            legacyFpReloaded = new Flightplan();
        }

        flightPlan = new FlightPlan(legacyFpReloaded, false);
    }

    /*public FlightPlanTemplate clone() {
        FlightPlan flightPlan = new FlightPlan((Flightplan) this.getFlightPlan().getLegacyFlightplan().getCopy());
        flightPlan.getLegacyFlightplan().setFile(this.getFlightPlan().getLegacyFlightplan().getFile());
        flightPlan.nameProperty().set(this.flightPlan.getName());
        flightPlan.isNameSetProperty().set(this.flightPlan.isNameSetProperty().get());
        FlightPlanTemplate clonedTemplate = new FlightPlanTemplate(this.getName(), flightPlan);
        clonedTemplate.setSystem(this.isSystem());
        return clonedTemplate;
    }*/

    @Override
    public String toString() {
        return getName();
    }
}
