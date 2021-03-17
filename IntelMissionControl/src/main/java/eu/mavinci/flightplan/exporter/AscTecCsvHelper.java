/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.exporter;

import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanStatement;
import eu.mavinci.core.flightplan.PlanType;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;

/**
 * TODO: This class contains code which has been fetched from ActionManager with a slight attempt to refactor. Looks
 * like it should be rewritten from scratch when we will have UI design.
 */
public class AscTecCsvHelper {

    private static final IDialogService dialogService =
        DependencyInjector.getInstance().getInstanceOf(IDialogService.class);

    private AscTecCsvHelper() {}

    public static String getXsl(Flightplan flightplan) {
        PicArea area = null;
        IFlightplanContainer container = flightplan.getParent();
        if (container == null) {
            container = flightplan;
        }

        for (IFlightplanStatement statement : container) {
            if (statement instanceof PicArea) {
                area = (PicArea)statement;
                break;
            }
        }

        if (area == null
                || (area.getPlanType() != PlanType.TOWER
                    && area.getPlanType() != PlanType.WINDMILL
                    && area.getPlanType() != PlanType.FACADE
                    && area.getPlanType() != PlanType.BUILDING
                    && area.getPlanType() != PlanType.POINT_OF_INTEREST
                    && area.getPlanType() != PlanType.PANORAMA)) {
            // String msg = "Set heading to follow?";// TODO
            // String title = "csv file type";
            // if (dialogService.requestConfirmation(title, msg)) {
            return "eu/mavinci/core/xml/toAscTecCsvFollow.xsl";
            // } else {
            //    return getMatrixXsl();
            // }
        } else if (area.getPlanType() == PlanType.TOWER || area.getPlanType() == PlanType.WINDMILL) { // hochschraubend
            return getMatrixXsl();
        } else if (area.getPlanType() == PlanType.BUILDING) { // fixe Zwischenhöhen
            // String msg = "Yes / Single 100: Use fast speed\n" + "No / Single: Use calculated speed?\n"
            //        + "Cancel / Matrix: use Matrix where identical height)";// TODO
            // String title = "csv file type";
            // DialogResult heading2 = dialogService.requestCancelableConfirmation(title, msg);
            // if (heading2 == DialogResult.YES) {
            //    return "eu/mavinci/core/xml/toAscTecCsvSingle100.xsl";
            // } else if (heading2 == DialogResult.NO) {
            return "eu/mavinci/core/xml/toAscTecCsvSingle.xsl";
            // } else {
            //    return "eu/mavinci/core/xml/toAscTecCsv.xsl";
            // }
        }

        return "eu/mavinci/core/xml/toAscTecCsv.xsl";
    }

    private static String getMatrixXsl() {
        // String msg = "Yes / Matrix+: Use Matrix flightplan where possible (Tolerance 10cm)?\n"
        //        + "No / Matrix: Use Matrix flightplan if identical heights)?\n"
        //        + "Cancel / Single: Use only Single Waypoints?";// TODO
        // String title = "csv file type";

        // DialogResult heading2 = dialogService.requestCancelableConfirmation(title, msg);
        // if (heading2 == DialogResult.YES) {
        //    return "eu/mavinci/core/xml/toAscTecCsvMatrix.xsl";
        // } else if (heading2 == DialogResult.NO) {
        return "eu/mavinci/core/xml/toAscTecCsv.xsl";
        // } else {
        //    msg = "Yes / Single 100: Use fast speed\n" + "No / Single: Use calculated speed?";// TODO
        //    title = "csv file type";

        //    if (dialogService.requestConfirmation(title, msg)) {
        //        return "eu/mavinci/core/xml/toAscTecCsvSingle100.xsl";
        //    } else {
        //        return "eu/mavinci/core/xml/toAscTecCsvSingle.xsl";
        //    }
        // }
    }

}
