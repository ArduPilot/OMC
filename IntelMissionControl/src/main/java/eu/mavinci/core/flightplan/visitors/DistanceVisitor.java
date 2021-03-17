/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhoto;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.CWaypointLoop;
import eu.mavinci.core.flightplan.IFlightplanLatLonReferenced;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.flightplan.LandingPoint;
import java.util.Stack;
import java.util.TreeMap;

public class DistanceVisitor extends AFlightplanVisitor {

    public double distance = 0;
    public int picCount = 0;
    public double distanceChildren = 0;
    public double timeChildren = 0;
    public int picCountChildren = 0;
    public double timeInSec = 0;

    public IFlightplanPositionReferenced first = null;
    public IFlightplanPositionReferenced last = null;

    double airplaneSpeedInMperSec;
    private boolean isPhotoOn = false;
    boolean isInCopterMode;
    private double lastOnDistance = 0;
    private double lastOnTime = 0;
    private double lastIntervall = 0;

    private final double maxClimbRatio;
    private final double maxDiveRatio;

    public TreeMap<Integer, Double> progressMapDistance = new TreeMap<Integer, Double>();
    public TreeMap<Integer, Double> progressTime = new TreeMap<Integer, Double>();
    public TreeMap<Integer, IFlightplanPositionReferenced> posMap =
        new TreeMap<Integer, IFlightplanPositionReferenced>();

    private class LoopMapEntry {
        double distBegin;
        int picBegin;
        IFlightplanPositionReferenced first;
    }

    public Stack<LoopMapEntry> loopStack = new Stack<>();

    public DistanceVisitor(IPlatformDescription platformDescription) {
        skipIgnoredPaths = true;
        this.airplaneSpeedInMperSec =
            platformDescription.getMaxPlaneSpeed().convertTo(Unit.METER_PER_SECOND).getValue().doubleValue();
        maxClimbRatio =
            Math.tan(platformDescription.getMaxClimbAngle().convertTo(Unit.RADIAN).getValue().doubleValue());
        maxDiveRatio = Math.tan(platformDescription.getMaxDiveAngle().convertTo(Unit.RADIAN).getValue().doubleValue());
    }

    @Override
    public void postVisit() {
        if (isPhotoOn && lastIntervall > 0) {
            picCount += Math.floor((distance - lastOnDistance) / lastIntervall);
        }

        if (!progressMapDistance.containsKey(0)) {
            progressMapDistance.put(0, 0.);
        }

        if (!progressTime.containsKey(0)) {
            progressTime.put(0, 0.);
        }
    }

    // experimentelle daten hierzu, achtung, definitio ist lecht anders..:
    // Steigrate: 4,66 m/s @ 50 km/h / 13,9m/s
    // Sinkrate: 3,4 m/s @ 65 km/h / 18 m/s
    // ich muss das jetzt alles auf die 55km/h in den camrea settings beziehen!
    // mittlere stratchfaktor ist etwa 3.8, daher passen die 20% \approx faktor 5 ganz gut
    // public static final double STRETCH_DISTANCE_CLIMB = (55 / 3.6) / 4.66;
    // public static final double STRETCH_DISTANCE_FALL = (55 / 3.6) / 3.4;

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CWaypoint) {
            CWaypoint tmpWp = (CWaypoint)fpObj;
            double speed = tmpWp.getSpeedMpSec();
            if (speed < 0) {
                speed = airplaneSpeedInMperSec;
            }

            if (last != null) {
                double d = CFlightplan.getDistanceInMeter(last, tmpWp);
                double h = last.getAltInMAboveFPRefPoint() - tmpWp.getAltInMAboveFPRefPoint();
                if (h != 0) {
                    double ratio = h > 0 ? maxClimbRatio : maxDiveRatio;
                    double g = Math.abs(h) / ratio;
                    d =
                        Math.max(
                            Math.sqrt(g * g + h * h),
                            d); // in case of copters, climb ratio is infinite, but they still have to travel that
                    // distance...
                }

                distance += d;
                timeInSec += d / speed;
            }

            double r = tmpWp.getRadiusWithinM();
            if (r > 0) {
                distance += r * Math.PI * 2;
                timeInSec += (r * Math.PI * 2) / speed;
            }

            last = tmpWp;

            if (isInCopterMode && isPhotoOn && tmpWp.isTriggerImageHereCopterMode()) {
                picCount++;
            }
        } else if (fpObj instanceof LandingPoint) {
            LandingPoint tmpWp = (LandingPoint)fpObj;
            if (!tmpWp.isActive()) return false;
            double alt = tmpWp.getAltInMAboveFPRefPoint();
            if (last != null) {
                // cast makes the altitude of the second one not used for distance calculation!
                double d = CFlightplan.getDistanceInMeter(last, (IFlightplanLatLonReferenced)tmpWp);
                double h = last.getAltInMAboveFPRefPoint() - tmpWp.getAltInMAboveFPRefPoint();
                if (h != 0) {
                    double ratio = h > 0 ? maxClimbRatio : maxDiveRatio;
                    double g = Math.abs(h) / ratio;
                    d =
                        Math.max(
                            Math.sqrt(g * g + h * h),
                            d); // in case of copters, climb ratio is infinite, but they still have to travel that
                    // distance...
                }

                distance += d;
                timeInSec += d / airplaneSpeedInMperSec;
            }

            last = tmpWp;
        } else if (fpObj instanceof CWaypointLoop) {
            CWaypointLoop loop = (CWaypointLoop)fpObj;
            LoopMapEntry entry = new LoopMapEntry();
            entry.distBegin = distance;
            entry.picBegin = picCount;
            // System.out.println("pic_count-enter:"+pic_count);
            FirstWaypointVisitor vis = new FirstWaypointVisitor();
            vis.startVisit(loop);
            entry.first = vis.firstWaypoint;
            loopStack.push(entry);
        } else if (fpObj instanceof CPhoto) {
            CPhoto photo = (CPhoto)fpObj;
            if (isPhotoOn && lastIntervall > 0 && !isInCopterMode) {
                picCount += Math.floor((distance - lastOnDistance) / lastIntervall);
            }

            isPhotoOn = photo.isPowerOn();
            if (isPhotoOn) {
                lastOnDistance = distance;
                lastIntervall = photo.getDistanceInCm() / 100.;
            }

            isInCopterMode = photo.isTriggerOnlyOnWaypoints();
        }

        if (first == null && last != null) {
            first = last;
            // cast makes the altitude of the second one not used for distance calculation!
            double h = last.getAltInMAboveFPRefPoint();
            double ratio = h > 0 ? maxClimbRatio : maxDiveRatio;
            double g = Math.abs(h) / ratio;
            double d = Math.sqrt(g * g + h * h);
            // to travel that distance...
            distance += d;
            timeInSec += d / airplaneSpeedInMperSec;
        }

        if (fpObj instanceof IReentryPoint) {
            IReentryPoint rp = (IReentryPoint)fpObj;
            int id = ReentryPointID.getIDPureWithoutCell(rp.getId());
            progressMapDistance.put(id, distance);
            progressTime.put(id, timeInSec);
            posMap.put(id, last);
        }

        // System.out.println("cur distance="+distance + " pic_count="+pic_count);
        return false;
    }

    @Override
    public boolean visitExit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CWaypointLoop) {
            CWaypointLoop loop = (CWaypointLoop)fpObj;
            LoopMapEntry entry = loopStack.pop();

            // do a virtual photo statement if isPhotoOn
            if (isPhotoOn && lastIntervall > 0 && !isInCopterMode) {
                picCount += Math.floor((distance - lastOnDistance) / lastIntervall);
            }

            double diffDist = distance - entry.distBegin;
            int diffPic = picCount - entry.picBegin;
            // System.out.println("diffPic:"+diffPic);

            double distanceLooped = diffDist;
            int picLooped = diffPic;
            // System.out.println("picLooped:"+pic_looped);
            if (last != entry.first && last != null && entry.first != null) {
                double d = CFlightplan.getDistanceInMeter(last, entry.first);
                distanceLooped += d;
                if (isPhotoOn && lastIntervall > 0) {
                    picLooped += Math.floor(d / lastIntervall);
                }
            }
            // System.out.println("picLooped2:"+pic_looped);

            int t = loop.getTime() <= 0 ? Integer.MAX_VALUE : loop.getTime();
            double distTime = t * airplaneSpeedInMperSec;
            int timeCount =
                (int)(Math.ceil((distTime - diffDist) / distanceLooped)) + 1; // numbers of loops based on maxTime

            int c = loop.getCount() <= 0 ? Integer.MAX_VALUE : loop.getCount(); // number of loops maximal
            int count = Math.min(c, timeCount) - 1; // real flown number of loops minus on2
            double distCount = diffDist + distanceLooped * count;
            int picCountLocal = diffPic + picLooped * count;

            distance = entry.distBegin + distCount;
            picCount = entry.picBegin + picCountLocal;

            if (isPhotoOn) {
                lastOnDistance = distance;
            }
        }

        return false;
    }

}
