/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CFlightplanManager;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.visitors.ExtractPicAreasVisitor;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.listeners.IAirplaneListenerGuiClose;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.helper.MFileFilter;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.plane.IAirplane;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FlightplanManager extends CFlightplanManager implements IAirplaneListenerGuiClose {

    public FlightplanManager(final IAirplane plane) {
        super(plane);
    }

    @Override
    public IAirplane getPlane() {
        return (IAirplane)plane;
    }

    public void resetupFlightplan(CFlightplan fp) {
        ExtractPicAreasVisitor vis = new ExtractPicAreasVisitor();
        vis.startVisit(fp);
        for (CPicArea cPicArea : vis.picAreas) {
            if (cPicArea instanceof PicArea) {
                PicArea picArea = (PicArea)cPicArea;
                picArea.computeFlightLines(true);
            }
        }
    }

    @Override
    public void add(CFlightplan fp) {
        if (contains(fp)) {
            return;
        }

        resetupFlightplan(fp);
        super.add(fp);
    }

    public static final String KEY_ONAIRFP = "onAirFlightplan";
    public static final String KEY_FP_VISIBILITY = "FlightplanVisibility.";

    @Override
    public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed) {
        super.recv_setFlightPlanXML(plan, reentry, succeed);
        if (!succeed) {
            return;
        }

        // Automatically store every flightplanchange to disk
        // also store empty flightplans, to document there wasnt a flightplan in
        // the plane at that time
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String prefix = "";
        Flightplan lastOnAirRelatedLocal = (Flightplan)onAirRelatedLocalFP;
        if (lastOnAirRelatedLocal != null) {
            File file = lastOnAirRelatedLocal.getFile();
            if (file != null) {
                prefix += MFileFilter.fmlFilter.removeExtension(file.getName()) + "_";
            }
        }

        prefix += df.format(Calendar.getInstance().getTime());
        // try {
        // prefix += "_"+ plane.getAirplaneCache().getFileName();
        // } catch (AirplaneCacheEmptyException e) {
        // }
        File file = new File(getPlane().getFlightplanAutosaveFolder(), prefix + ".fml");
        try (OutputStreamWriter out = new OutputStreamWriter(new PrintStream(file, "UTF-8"))) {
            // hash + comment removage will be done in tagging step, so just store original here!
            // plan = CDump.removeHashDumpAndCommentsFromXML(plan);
            out.write(plan);
        } catch (Exception e1) {
            Debug.getLog().log(Level.WARNING, "Error Writing Flightplan on receive", e1);
            return;
        }
    }
    @Override
    public boolean remove(CFlightplan fp) {
        return super.remove(fp);
    }

    @Override
    public boolean guiCloseRequest() {
        return true;
    }

    @Override
    public void guiClose() {
        // and removing this gui as callback from them
        // -> because this flightplan objects may still exist in other planes,
        // -> so we have no memory leak that the reference to the old planes
        // exist in there listeners

        for (CFlightplan fp : loadedPlans) {
            fp.removeFPChangeListener(this);
        }

        getOnAirFlightplan().removeFPChangeListener(this);
    }

    @Override
    public void storeToSessionNow() {
    }

    /** @return the onAirFlightplan */
    @Override
    public Flightplan getOnAirFlightplan() {
        return (Flightplan)onAirFlightplan;
    }

    public static final double BOUNDING_BOX_SAFETY_MARGIN_M = 10;

    @SuppressWarnings("deprecation")
    @Override
    public void sendFP(CFlightplan fp0, int reentryPoint) {
        Flightplan fp = (Flightplan)fp0;
        Debug.getLog()
            .log(Level.FINE, "try send FP (not hashed now) reentryPoint=" + reentryPoint + ":\n" + fp0.toXML());

        try {
            ((Flightplan)fp).getLandingpoint().updateFromUAV(getPlane());
        } catch (AirplaneCacheEmptyException e) {
            Debug.getLog()
                .log(
                    Level.SEVERE,
                    "Can not send flight plan with abolute altitude while no start elevation of UAV is known",
                    e);
            return;
        }

        if (fp.getRefPoint().isDefined()) { // TODO make protected, add getter
            // do the magic, shift the object (not nessesary for falcon, but for sirius)
            // not do if falcon is in local coordinate / indoor mode
            // TODO FIXME by ELENA ;-)
            // will call something liek FP. public void setValues(COrigin origin) {
        }

        /*ReassignIdsVisitor visR = new ReassignIdsVisitor();
        visR.startVisit(fp);
        if (!visR.wasValid()) {
            // TODO FIXME reinclude this test for productive version
            Debug.getLog().log(Level.INFO, "flightplan had duplicated IDs which had been fixed");
        }*/

        // try {
        // FileHelper.writeStringToFile(fp.toXMLwithHash(), new File("/home/marco/test.fml"));
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        super.sendFP(fp0, reentryPoint);
    }

}
