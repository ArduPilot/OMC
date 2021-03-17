/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.doublepanel.sunangles;

import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.desktop.gui.wwext.sun.SunPositionProviderSingleton;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SunComputer {
    public final long[] times = new long[24 * 60 + 1];
    public final double[] elevationsDeg = new double[24 * 60 + 1];
    public final double[] azimuthsDeg = new double[24 * 60 + 1];
    public final long offset;

    private static final Globe globe =
        DependencyInjector.getInstance().getInstanceOf(IWWGlobes.class).getDefaultGlobe();
    /**
     * generates a run movage curve for a given location and a given time and day
     *
     * @param refPos
     * @param day start of day, given in UTC time frame
     * @param timeZone
     */
    public SunComputer(LatLon refPos, Date day, TimeZone timeZone) {

        // https://www.calsky.com/cs.cgi
        // http://lexikon.astronomie.info/zeitgleichung/
        // -0,833° -> sunset
        // Deshalb wird der Untergang und auch der Aufgang der Sonne füreine geometrische
        // Horizonthöhe h von -50 Bogenminuten berechnet (-50 Bogenminuten sind -50/60°=-0.833° und -50/60/57.29578
        // rad=-0.0145 rad).
        // Von bürgerlicher Dämmerung spricht man, wenn h= -6° ist, nautische Dämmerung entspricht h = -12° und
        // schliesslich astronomische
        // Dämmerung entspricht h = -18°.

        long timeBase = day.getTime();
        Calendar cal = new GregorianCalendar();
        Vec4 normal = globe.computeSurfaceNormalAtLocation(refPos.latitude, refPos.longitude).normalize3();
        Vec4 north = globe.computeNorthPointingTangentAtLocation(refPos.latitude, refPos.longitude).normalize3();
        Vec4 east = north.cross3(normal).normalize3();

        offset = timeBase - timeZone.getOffset(timeBase + 1000); // plus 1000 makes sure the correct day is requested
        // System.out.println("use time offset:"+offset);
        Angle elevationh = null;
        for (int min = 0; min != 24 * 60; min++) {
            long time = min * 60 * 1000 + offset;
            cal.setTimeInMillis(time);
            LatLon sunPos = SunPositionProviderSingleton.getInstance().getPosition(cal);
            Vec4 sun = globe.computePointFromLocation(sunPos).normalize3();
            Angle elevation = Angle.POS90.subtract(sun.angleBetween3(normal));

            if (elevationh == null) {
                elevationh = elevation;
            }

            if (elevation.compareTo(elevationh) > 0) {
                elevationh = elevation;
            }

            Vec4 sunOnSurface = sun.perpendicularTo3(normal).normalize3();
            double sunNorthing = sunOnSurface.dot3(north);
            double sunEasting = sunOnSurface.dot3(east);
            Angle azimuth = Angle.fromXY(sunNorthing, sunEasting);
            if (azimuth.degrees < 0) {
                azimuth = azimuth.add(Angle.POS360);
            }

            times[min] = time;
            elevationsDeg[min] = elevation.degrees;
            azimuthsDeg[min] = azimuth.degrees;
            // if (min%60==0){
            // System.out.println("...\n"+min/60);
            // System.out.println("sun:"+sun);
            // System.out.println("sunOnSurface:"+sunOnSurface);
            // System.out.println("declination:"+elevation);
            // System.out.println("inclination:"+azimuth);
            // }

        }

        times[times.length - 1] = times[0];
        elevationsDeg[times.length - 1] = elevationsDeg[0];
        azimuthsDeg[times.length - 1] = azimuthsDeg[0];
    }

    public static class SunComputerResults {
        public double thresholdAngleDeg;
        public int idxRise = -1;
        public int idxSet = -1;
        public long timeRise = -1;
        public long timeSet = -1;

    }

    public SunComputerResults getSunRiseSet(double thresholdAngleDeg) {
        SunComputerResults result = new SunComputerResults();
        result.thresholdAngleDeg = thresholdAngleDeg;

        for (int i = 0; i != times.length - 1; i++) {
            // sun rise
            if (elevationsDeg[i] <= thresholdAngleDeg && elevationsDeg[i + 1] > thresholdAngleDeg) {
                result.timeRise =
                    Math.round(
                        times[i]
                            + (times[i + 1] - times[i])
                                * (thresholdAngleDeg - elevationsDeg[i])
                                / (elevationsDeg[i + 1] - elevationsDeg[i]));
                result.idxRise = i;
            }

            // sunset
            if (elevationsDeg[i] >= thresholdAngleDeg && elevationsDeg[i + 1] < thresholdAngleDeg) {
                result.timeSet =
                    Math.round(
                        times[i]
                            + (times[i + 1] - times[i])
                                * (thresholdAngleDeg - elevationsDeg[i])
                                / (elevationsDeg[i + 1] - elevationsDeg[i]));
                result.idxSet = i;
            }
        }

        return result;
    }

}
