/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.desktop.listener.WeakListenerList;
import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionEstablished;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightPlanXML;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.LandingPoint;
import java.util.ArrayList;
import java.util.logging.Level;

public class CFlightplanManager
        implements IAirplaneListenerFlightPlanXML,
            IFlightplanChangeListener,
            IAirplaneListenerStartPos,
            IAirplaneListenerConnectionEstablished {

    protected ICAirplane plane;
    protected CFlightplan onAirFlightplan;
    protected ArrayList<CFlightplan> loadedPlans = new ArrayList<CFlightplan>();
    public CFlightplan onAirRelatedLocalFP = null;

    // final is important, because this object will for ever stay valid, even stored somewhere else
    private final IHardwareConfiguration hardwareConfigurationOnAir;

    private boolean justSendFP = false;
    private final WeakListenerList<IFlightplanChangeListener> fpListeners =
        new WeakListenerList<>("FlightplanManagerListeners");

    public static final String KEY = "eu.mavinci.core.flightplan.CFlightplanManager";

    public ICAirplane getPlane() {
        return plane;
    }

    public void add(CFlightplan fp) {
        loadedPlans.add(fp);
        fp.addFPChangeListener(this);
        informAddLiseners(fp, null);
    }

    public CFlightplan get(int i) {
        return loadedPlans.get(i);
    }

    public boolean contains(CFlightplan fp) {
        return loadedPlans.contains(fp);
    }

    public boolean remove(CFlightplan fp) {
        loadedPlans.remove(fp);
        fp.removeFPChangeListener(this);
        informRemoveLiseners(fp, 0, null);
        return true;
    }

    public void informValuesChangeLiseners(IFlightplanRelatedObject fpObj) {
        for (IFlightplanChangeListener listener : fpListeners) {
            listener.flightplanValuesChanged(fpObj);
        }
    }

    public void informStructureChangeLiseners(IFlightplanRelatedObject fp) {
        for (IFlightplanChangeListener listener : fpListeners) {
            listener.flightplanStructureChanged(fp);
        }
    }

    public void informRemoveLiseners(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
        for (IFlightplanChangeListener listener : fpListeners) {
            listener.flightplanElementRemoved(fp, i, statement);
        }
    }

    public void informAddLiseners(CFlightplan fp, IFlightplanRelatedObject statement) {
        for (IFlightplanChangeListener listener : fpListeners) {
            listener.flightplanElementAdded(fp, statement);
        }
    }

    public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed) {
        // System.out.println("recv_setFl:" + plan + succeed);
        if (!succeed) {
            Debug.getLog().log(Level.WARNING, "problems processing flightplan !!\n" + plan);
            return;
        }

        plan = CDump.removeHashDumpAndCommentsFromXML(plan);

        if (justSendFP) {
            // controll if the sended flightplan is equal to the recieved one..
            getOnAirFlightplan().fromXML(plan);
            // System.out.println("recv" + plan);
            // System.out.println("new OnAir" + getOnAirFlightplan().toXML());
            // System.out.println("onAirLocal" + onAirRelatedLocalFP.toXML());
            // if not equal, kill the local editable flightplan relation to the air one.
            // System.out.println("xml-EqualToSendet:"+getOnAirFlightplan().toXML().equals(onAirRelatedLocalFP.toXML()));
            if (!onAirRelatedLocalFP.equals(getOnAirFlightplan())) {
                onAirRelatedLocalFP = null;
            }
            // System.out.println("isEqualToSendet="+(onAirRelatedLocalFP!=null));
            justSendFP = false;
        } else {
            CFlightplan fp = FlightplanFactory.getFactory().newFlightplan();

            fp.fromXML(plan);
            if (plan.equals("")) {
                IHardwareConfigurationManager manager =
                    DependencyInjector.getInstance().getInstanceOf(IHardwareConfigurationManager.class);
                fp.updateHardwareConfiguration(manager.getImmutableDefault());
            }

            if (!fp.equals(getOnAirFlightplan())) {
                // received a different fligplan then sended myself, so kill the local relation
                onAirRelatedLocalFP = null;
                getOnAirFlightplan().fromXML(plan);
                if (plan.equals("")) {
                    getOnAirFlightplan().updateHardwareConfiguration(plane.getNativeHardwareConfiguration());
                }
                // onAirFlightplan.addFPChangeListener(me);
                // onAirFlightplan.addMapListener(FlightplanManager.this);
                // onAirFlightplan.setColor(onAirFPColor);
            }
        }

        hardwareConfigurationOnAir.initializeFrom(onAirFlightplan.hardwareConfiguration);
        informStructureChangeLiseners(null);
    }

    public IHardwareConfiguration getHardwareConfigurationOnAir() {
        return hardwareConfigurationOnAir;
    }

    public CFlightplan getOnAirRelatedLocalFP() {
        return onAirRelatedLocalFP;
    }

    public CFlightplanManager(final ICAirplane plane) {
        this.plane = plane;
        IHardwareConfigurationManager manager =
            DependencyInjector.getInstance().getInstanceOf(IHardwareConfigurationManager.class);
        hardwareConfigurationOnAir = manager.getImmutableDefault();
        initOnAirFlightplan();
        plane.addListener(this);
    }

    protected void initOnAirFlightplan() {
        setOnAirFlightplan(FlightplanFactory.getFactory().newFlightplan());
        getOnAirFlightplan().setIsOnAirFlightplan(true);
        getOnAirFlightplan().addFPChangeListener(this);
    }

    public void sendFP(CFlightplan fp, boolean reentry, Integer startAt) {
        // it is only possible to reenter the on air related fp!!
        if (fp != onAirRelatedLocalFP && fp != getOnAirFlightplan()) {
            reentry = false;
        }

        if (reentry) {
            sendFP(fp, -1); // try to reenter
        } else {
            sendFP(fp, startAt); // start FP at beginning
        }
    }

    public void sendFP(CFlightplan fp, boolean reentry) {
        // it is only possible to reenter the on air related fp!!
        if (fp != onAirRelatedLocalFP && fp != getOnAirFlightplan()) {
            reentry = false;
        }

        if (reentry) {
            sendFP(fp, -1); // try to reenter
        } else {
            sendFP(fp, 0); // start FP at beginning
        }
    }

    public void sendFP(CFlightplan fp, int reentryPoint) {
        Debug.getLog()
            .log(
                Level.FINE,
                "really transmitt FP, alls tests passed or overwritten by user "
                    + fp.toXMLwithHash()
                    + "@"
                    + reentryPoint);

        plane.setFlightPlanXML(fp.toXMLwithHash(), reentryPoint);
        onAirRelatedLocalFP = fp;
        // System.out.println("sedning FP with ID" + fp.hashCode());
        // DON'T set this, because it is set by the plane answere
        // onAirFlightplan = fp;

        justSendFP = true;
        informStructureChangeLiseners(onAirRelatedLocalFP);
    }

    public void jumpToReentryPoint(IReentryPoint rp) {
        CFlightplan fp = rp.getFlightplan();

        // if a pre approach is planned, dont jump to the landing point, always jump to the pre approach point!!
        if (rp instanceof LandingPoint) {
            LandingPoint lp = (LandingPoint)rp;
            CPreApproach pre = lp.getPreApproach();
            if (pre != null) {
                sendFP(fp, pre.getId());
                return;
            }
        }

        // it it is not the current plan, this other plan will be loaded and send!, so it is allowed!!
        // if (!fp.equals(onAirFlightplan)) return;

        // TODO, if fp.equals(Flightplan) it is a LITTLE bit crazy to send the whole FP again!!
        // -> resource wasting! ... but it is more secure!!
        sendFP(fp, rp.getId());
    }

    public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
        informStructureChangeLiseners(fp);
    }

    public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
        informValuesChangeLiseners(fpObj);
    }

    public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
        informRemoveLiseners(fp, i, statement);
    }

    public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
        informAddLiseners(fp, statement);
    }

    /** @param onAirFlightplan the onAirFlightplan to set */
    protected void setOnAirFlightplan(CFlightplan onAirFlightplan) {
        this.onAirFlightplan = onAirFlightplan;
        // plane.onAirFlightPlanChanged();
    }

    /** @return the onAirFlightplan */
    public CFlightplan getOnAirFlightplan() {
        return onAirFlightplan;
    }

    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        // setting the landingpoint of all flightplans to this startingposition, if it is currently still (0,0)
        for (CFlightplan fp : loadedPlans) {
            if (fp.landingpoint.getLon() == 0. && fp.landingpoint.getLat() == 0.) {
                fp.getLandingpoint().setLatLon(lat, lon);
            }
        }
    }

    public void recv_connectionEstablished(String port) {
        onAirFlightplan.clear();
    }

}
