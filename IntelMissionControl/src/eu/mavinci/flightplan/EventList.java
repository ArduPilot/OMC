/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.helper.ILanguageHelper;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IFlightplanStatement;

public class EventList extends CEventList {

    public static final String KEY = "eu.mavinci.flightplan.EventList";
    public static final String KEY_ALT = KEY + ".safetyAlt";
    public static final String KEY_TO_STRING = KEY + ".toString";

    public EventList(Flightplan parent) {
        super(parent);
    }

    /*public EventList() {
        // FIXME: Need to check this new Object 07-Nov-2017
        // FIXME: it used to be null and klocwork did not like it!
        // CEvent isLandingEnabled it will fail fast if given NULL here.
        super(new Flightplan());
        // super(null); // avoid null in CFlightplan
    }*/

    public EventList(EventList source) {
        super(source.fp);
        for (IFlightplanStatement statement : source.elements) {
            IFlightplanRelatedObject statementCopy = statement.getCopy();
            this.elements.add((IFlightplanStatement)statementCopy);
            statementCopy.setParent(this);
        }

        this.safetyAltitude_CM = source.safetyAltitude_CM;
    }

    @Override
    public String toString() {
        return DependencyInjector.getInstance()
            .getInstanceOf(ILanguageHelper.class)
            .getString(KEY_TO_STRING, (getAltWithinCM() / 100.) + "m");
    }

    public void flightplanStatementAdded(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (fp != null) {
            fp.flightplanStatementAdded(statement);
        }
    }

    public void flightplanStatementChanged(IFlightplanRelatedObject statement) {
        // System.out.println("changedMASTER:" + this.hashCode() + " mute:"+mute + " parent:"+fp + " camera:"+camera + "
        // stm: " +
        // statement.hashCode());
        if (mute) {
            return;
        }

        if (fp != null) {
            // System.out.println("changedMASTER2:" + this.hashCode() + " mute:"+mute + " parent:"+fp + "
            // camera:"+camera + " stm: " +
            // statement.hashCode());
            fp.flightplanStatementChanged(statement);
        }
    }

    public void flightplanStatementRemove(int i, IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (fp != null) {
            fp.flightplanStatementRemove(i, statement);
        }
    }

    public void flightplanStatementStructureChanged(IFlightplanRelatedObject statement) {
        if (mute) {
            return;
        }

        if (fp != null) {
            fp.flightplanStatementStructureChanged(statement);
        }
    }

    public void setDefaultsFromCamera(CFlightplan fp) {
        EventList other = (EventList)fp.getEventList();
        overwriteFromOther(other);
    }

    public Flightplan getFlightplan() {
        return (Flightplan)fp;
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new EventList(this);
    }

}
