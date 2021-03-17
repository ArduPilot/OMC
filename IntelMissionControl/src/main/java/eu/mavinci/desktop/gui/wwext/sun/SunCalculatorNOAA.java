/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.sun;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Peter Schauß translated to java from javascript - from @see http://www.esrl.noaa.gov/gmd/grad/solcalc/
 *     license: Public Domain regarding NOAA email
 * @see http://www.esrl.noaa.gov/gmd/grad/solcalc/glossary.html latitude - an angular measurement of north-south
 *     location on Earth's surface. Latitude ranges from 90° south (at the south pole), through 0° (all along the
 *     equator), to 90° north (at the north pole). Latitude is usually defined as a positive value in the northern
 *     hemisphere and a negative value in the southern hemisphere longitude - an angular measurement of east-west
 *     location on Earth's surface. Longitude is defined from the prime meridian, which passes through Greenwich,
 *     England. The international date line is defined around +/- 180° longitude. (180° east longitude is the same as
 *     180° west longitude, because there are 360° in a circle.) Many astronomers define east longitude as positive. For
 *     our new solar calculator, we conform to the international standard, with east longitude positive.
 * @version $Id: SunCalculator.java 10406 2009-04-22 18:28:45Z patrickmurris $
 */
public class SunCalculatorNOAA {

    public static class Month {

        public Month(String name, int numDays, String id) {
            this.numDays = numDays;
        }

        int numDays;
    }

    protected static List<Month> monthList = new ArrayList<Month>(12);

    static {
        monthList.add(new Month("January", 31, "Jan"));
        monthList.add(new Month("February", 28, "Feb"));
        monthList.add(new Month("March", 31, "Mar"));
        monthList.add(new Month("April", 30, "Apr"));
        monthList.add(new Month("May", 31, "May"));
        monthList.add(new Month("June", 30, "Jun"));
        monthList.add(new Month("July", 31, "Jul"));
        monthList.add(new Month("August", 31, "Aug"));
        monthList.add(new Month("September", 30, "Sep"));
        monthList.add(new Month("October", 31, "Oct"));
        monthList.add(new Month("November", 30, "Nov"));
        monthList.add(new Month("December", 31, "Dec"));
    }

    /**
     * @param jd Julian Day
     * @return Julian Cent
     */
    static double calcTimeJulianCent(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        return T;
    }

    /**
     * @param Julian Cent
     * @return Julian Day
     */
    static double calcJDFromJulianCent(double t) {
        double JD = t * 36525.0 + 2451545.0;
        return JD;
    }

    /**
     * @param yr the year Gregorian Calendar
     * @return if it is a leap year
     */
    static boolean isLeapYear(int yr) {
        return ((yr % 4 == 0 && yr % 100 != 0) || yr % 400 == 0);
    }

    static double calcDoyFromJD(double jd) {
        int z = (int)Math.floor(jd + 0.5);
        double f = (jd + 0.5) - z;
        int A;
        if (z < 2299161) {
            A = z;
        } else {
            int alpha = (int)Math.floor((z - 1867216.25) / 36524.25);
            A = z + 1 + alpha - (int)Math.floor(alpha / 4.);
        }

        double B = A + 1524;
        int C = (int)Math.floor((B - 122.1) / 365.25);
        int D = (int)Math.floor(365.25 * C);
        int E = (int)Math.floor((B - D) / 30.6001);
        double day = B - D - (int)Math.floor(30.6001 * E) + f;
        int month = (E < 14) ? E - 1 : E - 13;
        int year = (month > 2) ? C - 4716 : C - 4715;

        double k = (isLeapYear(year) ? 1 : 2);
        double doy = Math.floor((275 * month) / 9.) - k * Math.floor((month + 9) / 12.) + day - 30;
        return doy;
    }

    static double calcGeomMeanLongSun(double t) {
        double L0 = 280.46646 + t * (36000.76983 + t * (0.0003032));
        while (L0 > 360.0) {
            L0 -= 360.0;
        }

        while (L0 < 0.0) {
            L0 += 360.0;
        }

        return L0; // in degrees
    }

    static double calcGeomMeanAnomalySun(double t) {
        double M = 357.52911 + t * (35999.05029 - 0.0001537 * t);
        return M; // in degrees
    }

    static double calcEccentricityEarthOrbit(double t) {
        double e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t);
        return e; // unitless
    }

    static double calcSunEqOfCenter(double t) {
        double m = calcGeomMeanAnomalySun(t);
        double mrad = Math.toRadians(m);
        double sinm = Math.sin(mrad);
        double sin2m = Math.sin(mrad + mrad);
        double sin3m = Math.sin(mrad + mrad + mrad);
        double C =
            sinm * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin2m * (0.019993 - 0.000101 * t) + sin3m * 0.000289;
        return C; // in degrees
    }

    static double calcSunTrueLong(double t) {
        double l0 = calcGeomMeanLongSun(t);
        double c = calcSunEqOfCenter(t);
        return l0 + c; // in degrees
    }

    static double calcSunTrueAnomaly(double t) {
        double m = calcGeomMeanAnomalySun(t);
        double c = calcSunEqOfCenter(t);
        double v = m + c;
        return v; // in degrees
    }

    /**
     * distance of the sun from earth
     *
     * @param t
     * @return
     */
    static double calcSunRadVector(double t) {
        double v = calcSunTrueAnomaly(t);
        double e = calcEccentricityEarthOrbit(t);
        double R = (1.000001018 * (1 - e * e)) / (1 + e * Math.cos(Math.toRadians(v)));
        return R; // in AUs
    }

    static double calcSunApparentLong(double t) {
        double o = calcSunTrueLong(t);
        double omega = 125.04 - 1934.136 * t;
        double lambda = o - 0.00569 - 0.00478 * Math.sin(Math.toRadians(omega));
        return lambda; // in degrees
    }

    static double calcMeanObliquityOfEcliptic(double t) {
        double seconds = 21.448 - t * (46.8150 + t * (0.00059 - t * (0.001813)));
        double e0 = 23.0 + (26.0 + (seconds / 60.0)) / 60.0;
        return e0; // in degrees
    }

    static double calcObliquityCorrection(double t) {
        double e0 = calcMeanObliquityOfEcliptic(t);
        double omega = 125.04 - 1934.136 * t;
        double e = e0 + 0.00256 * Math.cos(Math.toRadians(omega));
        return e; // in degrees
    }

    /**
     * @param t Julian centuries since J2000.0
     * @return solar right ascension in degrees
     */
    static double calcSunRtAscension(double t) {
        double e = calcObliquityCorrection(t);
        double lambda = calcSunApparentLong(t);
        double tananum = (Math.cos(Math.toRadians(e)) * Math.sin(Math.toRadians(lambda)));
        double tanadenom = (Math.cos(Math.toRadians(lambda)));
        double alpha = Math.toDegrees(Math.atan2(tananum, tanadenom));
        return alpha; // in degrees
    }

    /**
     * Calculate the declination of the sun. Declination is analogous to latitude on Earth's surface, and measures an
     * angular displacement north or south from the projection of Earth's equator on the celestial sphere to the
     * location of a celestial body.
     *
     * @param t Julian Cent
     * @return sun declination
     */
    static double calcSunDeclination(double t) {
        double e = calcObliquityCorrection(t);
        double lambda = calcSunApparentLong(t);

        double sint = Math.sin(Math.toRadians(e)) * Math.sin(Math.toRadians(lambda));
        double theta = Math.toDegrees(Math.asin(sint));
        return theta; // in degrees
    }

    /**
     * equation of time - an astronomical term accounting for changes in the time of solar noon for a given location
     * over the course of a year. Earth's elliptical orbit and Kepler's law of equal areas in equal times are the
     * culprits behind this phenomenon.
     *
     * @param t JulianCent
     * @return
     */
    static double calcEquationOfTime(double t) {
        double epsilon = calcObliquityCorrection(t);
        double l0 = calcGeomMeanLongSun(t);
        double e = calcEccentricityEarthOrbit(t);
        double m = calcGeomMeanAnomalySun(t);

        double y = Math.tan(Math.toRadians(epsilon) / 2.0);
        y *= y;

        double sin2l0 = Math.sin(2.0 * Math.toRadians(l0));
        double sinm = Math.sin(Math.toRadians(m));
        double cos2l0 = Math.cos(2.0 * Math.toRadians(l0));
        double sin4l0 = Math.sin(4.0 * Math.toRadians(l0));
        double sin2m = Math.sin(2.0 * Math.toRadians(m));

        double Etime =
            y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0 - 0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m;
        return Math.toDegrees(Etime) * 4.0; // in minutes of time
    }

    static double calcHourAngleSunrise(double lat, double solarDec) {
        double latRad = Math.toRadians(lat);
        double sdRad = Math.toRadians(solarDec);
        double HAarg =
            (Math.cos(Math.toRadians(90.833)) / (Math.cos(latRad) * Math.cos(sdRad))
                - Math.tan(latRad) * Math.tan(sdRad));
        double HA = Math.acos(HAarg);
        return HA; // in radians (for sunset, use -HA)
    }

    // double readTextBox(inputId, numchars, intgr, pad, min, max, def)
    // {
    // double number = document.getElementById(inputId).value.substring(0,numchars)
    // if (intgr) {
    // number = Math.floor(parseFloat(number))
    // } else { // float
    // number = parseFloat(number)
    // }
    // if (number < min) {
    // number = min
    // } else if (number > max) {
    // number = max
    // } else if (number.toString() == "NaN") {
    // number = def
    // }
    // if ((pad) && (intgr)) {
    // document.getElementById(inputId).value = zeroPad(number,2)
    // } else {
    // document.getElementById(inputId).value = number
    // }
    // return number
    // }

    // double month(name, numdays, abbr)
    // {
    // this.name = name;
    // this.numdays = numdays;
    // this.abbr = abbr;
    // }
    // double monthList = new Array();
    // double i = 0;

    /**
     * Julian day - a time period used in astronomical circles, defined as the number of days since 1 January, 4713 BCE
     * (Before Common Era), with the first day defined as Julian day zero. The Julian day begins at noon UTC. Some
     * scientists use the term julian day to mean the numerical day of the current year, where January 1 is defined as
     * day 001.
     *
     * @return julian day
     */
    static double getJD(int year, int month, int day) {
        // double docmonth = document.getElementById("mosbox").selectedIndex + 1
        // double docday = document.getElementById("daybox").selectedIndex + 1
        // double docyear = readTextBox("yearbox", 5, 1, 0, -2000, 3000, 2009)
        if ((isLeapYear(year)) && (month == 2)) {
            if (day > 29) {
                day = 29;
            }
        } else {
            if (day > monthList.get(month - 1).numDays) {
                day = monthList.get(month - 1).numDays;
            }
        }

        if (month <= 2) {
            year -= 1;
            month += 12;
        }

        double A = Math.floor(year / 100);
        double B = 2 - A + Math.floor(A / 4);
        double JD = Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
        return JD;
    }

    /**
     * @param hr 0..12
     * @param mn 0..59
     * @param sc 0..59
     * @param pm use american pm time
     * @param dst Daylight Saving Time
     * @return local time of the day in minutes
     */
    static double getTimeLocal(int hr, int mn, int sc, boolean pm, boolean dst) {
        if ((pm) && (hr < 12)) {
            hr += 12;
        }

        if (dst) {
            hr -= 1;
        }

        double mins = hr * 60 + mn + sc / 60.0;
        return mins;
    }

    /**
     * Calculate azimuth and elevation of the sun
     *
     * @param t Julian Cent
     * @prama localtime local time of the day in minutes
     * @param latlon latitude and longitude of observer position
     * @param zone timezone (germany is +1)
     * @return AzEl
     */
    static AzEl calcAzEl(double T, double localtime, LatLon latlon, double zone, boolean doRefractionCorrection) {
        double eqTime = calcEquationOfTime(T);
        double theta = calcSunDeclination(T);
        // if (output) {
        // document.getElementById("eqtbox").value = Math.floor(eqTime*100 +0.5)/100.0
        // document.getElementById("sdbox").value = Math.floor(theta*100+0.5)/100.0
        // }
        double solarTimeFix = eqTime + 4.0 * latlon.longitude.degrees - 60.0 * zone;
        // double earthRadVec = calcSunRadVector(T);
        /**
         * solar time - is defined as the time elapsed since the most recent meridian passage of the sun. This system is
         * based on the rotation of the Earth with respect to the sun. A mean solar day is defined as the time between
         * one solar noon and the next, averaged over the year.
         */
        double trueSolarTime = localtime + solarTimeFix;
        while (trueSolarTime > 1440) {
            trueSolarTime -= 1440;
        }

        double hourAngle = trueSolarTime / 4.0 - 180.0;
        if (hourAngle < -180) {
            hourAngle += 360.0;
        }

        double haRad = Math.toRadians(hourAngle);
        double csz =
            Math.sin(Math.toRadians(latlon.latitude.degrees)) * Math.sin(Math.toRadians(theta))
                + Math.cos(latlon.latitude.radians) * Math.cos(Math.toRadians(theta)) * Math.cos(haRad);
        if (csz > 1.0) {
            csz = 1.0;
        } else if (csz < -1.0) {
            csz = -1.0;
        }

        double zenith = Math.toDegrees(Math.acos(csz));
        double azDenom = (Math.cos(latlon.latitude.radians) * Math.sin(Math.toRadians(zenith)));
        double azRad;
        double azimuth;
        if (Math.abs(azDenom) > 0.001) {
            azRad =
                ((Math.sin(latlon.latitude.radians) * Math.cos(Math.toRadians(zenith)))
                        - Math.sin(Math.toRadians(theta)))
                    / azDenom;
            if (Math.abs(azRad) > 1.0) {
                if (azRad < 0) {
                    azRad = -1.0;
                } else {
                    azRad = 1.0;
                }
            }

            azimuth = 180.0 - Math.toDegrees(Math.acos(azRad));
            if (hourAngle > 0.0) {
                azimuth = -azimuth;
            }
        } else {
            if (latlon.latitude.degrees > 0.0) {
                azimuth = 180.0;
            } else {
                azimuth = 0.0;
            }
        }

        if (azimuth < 0.0) {
            azimuth += 360.0;
        }

        double exoatmElevation = 90.0 - zenith;

        // Atmospheric Refraction correction
        double refractionCorrection = 0;
        if (doRefractionCorrection) {
            if (exoatmElevation > 85.0) {
                refractionCorrection = 0.0;
            } else {
                double te = Math.tan(Math.toRadians(exoatmElevation));
                if (exoatmElevation > 5.0) {
                    refractionCorrection = 58.1 / te - 0.07 / (te * te * te) + 0.000086 / (te * te * te * te * te);
                } else if (exoatmElevation > -0.575) {
                    refractionCorrection =
                        1735.0
                            + exoatmElevation
                                * (-518.2
                                    + exoatmElevation * (103.4 + exoatmElevation * (-12.79 + exoatmElevation * 0.711)));
                } else {
                    refractionCorrection = -20.774 / te;
                }

                refractionCorrection = refractionCorrection / 3600.0;
            }
        }

        double solarZen = zenith - refractionCorrection;

        // if ((output) && (solarZen > 108.0) ) {
        // document.getElementById("azbox").value = "dark"
        // document.getElementById("elbox").value = "dark"
        // } else if (output) {
        // document.getElementById("azbox").value = Math.floor(azimuth*100 +0.5)/100.0
        // document.getElementById("elbox").value = Math.floor((90.0-solarZen)*100+0.5)/100.0
        // if (document.getElementById("showae").checked) {
        // showLineGeodesic("#ffff00", azimuth)
        // }
        // }
        return AzEl.fromDegrees(azimuth, 90.0 - solarZen);
    }

    static double calcSolNoon(double jd, double longitude, double timezone, boolean dst) {
        double tnoon = calcTimeJulianCent(jd - longitude / 360.0);
        double eqTime = calcEquationOfTime(tnoon);
        double solNoonOffset = 720.0 - (longitude * 4) - eqTime; // in minutes
        double newt = calcTimeJulianCent(jd + solNoonOffset / 1440.0);
        eqTime = calcEquationOfTime(newt);
        double solNoonLocal = 720 - (longitude * 4) - eqTime + (timezone * 60.0); // in minutes
        if (dst) {
            solNoonLocal += 60.0;
        }

        return solNoonLocal;
        // document.getElementById("noonbox").value = timeString(solNoonLocal, 3);
    }

    // String dayString(double jd, double next, double flag)
    // {
    // String output;
    // // returns a string in the form DDMMMYYYY[ next] to display prev/next rise/set
    // // flag=2 for DD MMM, 3 for DD MM YYYY, 4 for DDMMYYYY next/prev
    // if ( (jd < 900000) || (jd > 2817000) ) {
    // output = "error";
    // } else {
    // double z = Math.floor(jd + 0.5);
    // double f = (jd + 0.5) - z;
    // double A;
    // if (z < 2299161) {
    // A = z;
    // } else {
    // double alpha = Math.floor((z - 1867216.25)/36524.25);
    // A = z + 1 + alpha - Math.floor(alpha/4);
    // }
    // double B = A + 1524;
    // double C = Math.floor((B - 122.1)/365.25);
    // double D = Math.floor(365.25 * C);
    // double E = Math.floor((B - D)/30.6001);
    // double day = B - D - Math.floor(30.6001 * E) + f;
    // double month = (E < 14) ? E - 1 : E - 13;
    // double year = ((month > 2) ? C - 4716 : C - 4715);
    // if (flag == 2)
    // output = zeroPad(day,2) + " " + monthList[month-1].abbr;
    // if (flag == 3)
    // output = zeroPad(day,2) + monthList[month-1].abbr + year.toString();
    // if (flag == 4)
    // output = zeroPad(day,2) + monthList[month-1].abbr + year.toString() + ((next) ? " next" : " prev");
    // }
    // return output;
    // }

    // String timeDateString(double JD, double minutes)
    // {
    // return timeString(minutes, 2) + " " + dayString(JD, 0, 2);
    // }
    //
    // double timeString(float minutes, int flag)
    // // timeString returns a zero-padded string (HH:MM:SS) given time in minutes
    // // flag=2 for HH:MM, 3 for HH:MM:SS
    // {
    // if ( (minutes >= 0) && (minutes < 1440) ) {
    // double floatHour = minutes / 60.0;
    // double hour = Math.floor(floatHour);
    // double floatMinute = 60.0 * (floatHour - Math.floor(floatHour));
    // double minute = Math.floor(floatMinute);
    // double floatSec = 60.0 * (floatMinute - Math.floor(floatMinute));
    // double second = Math.floor(floatSec + 0.5);
    // if (second > 59) {
    // second = 0;
    // minute += 1;
    // }
    // if ((flag == 2) && (second >= 30)) minute++;
    // if (minute > 59) {
    // minute = 0;
    // hour += 1;
    // }
    // double output = zeroPad(hour,2) + ":" + zeroPad(minute,2);
    // if (flag > 2) output = output + ":" + zeroPad(second,2);
    // } else {
    // double output = "error"
    // }
    // return output;
    // }

    static double calcSunriseSetUTC(boolean rise, double JD, double latitude, double longitude) {
        double t = calcTimeJulianCent(JD);
        double eqTime = calcEquationOfTime(t);
        double solarDec = calcSunDeclination(t);
        double hourAngle = calcHourAngleSunrise(latitude, solarDec);
        // alert("HA = " + Math.toDegrees(hourAngle));
        if (!rise) {
            hourAngle = -hourAngle;
        }

        double delta = longitude + Math.toDegrees(hourAngle);
        double timeUTC = 720 - (4.0 * delta) - eqTime; // in minutes
        return timeUTC;
    }

    // double calcSunriseSet(boolean rise, double JD, double latitude, double longitude, double timezone, boolean dst)
    // // rise = 1 for sunrise, 0 for sunset
    // {
    // String id = ((rise) ? "risebox" : "setbox");
    // double timeUTC = calcSunriseSetUTC(rise, JD, latitude, longitude);
    // double newTimeUTC = calcSunriseSetUTC(rise, JD + timeUTC/1440.0, latitude, longitude);
    //
    // double timeLocal = newTimeUTC + (timezone * 60.0);
    // if (document.getElementById(rise ? "showsr" : "showss").checked) {
    // double riseT = calcTimeJulianCent(JD + newTimeUTC/1440.0);
    // double riseAz = calcAzEl(0, riseT, timeLocal, latitude, longitude, timezone)
    // showLineGeodesic(rise ? "#66ff00" : "#ff0000", riseAz);
    // }
    // timeLocal += ((dst) ? 60.0 : 0.0);
    // if ( (timeLocal >= 0.0) && (timeLocal < 1440.0) ) {
    // document.getElementById(id).value = timeString(timeLocal,2)
    // } else {
    // double jday = JD
    // double increment = ((timeLocal < 0) ? 1 : -1)
    // while ((timeLocal < 0.0)||(timeLocal >= 1440.0)) {
    // timeLocal += increment * 1440.0
    // jday -= increment
    // }
    // document.getElementById(id).value = timeDateString(jday,timeLocal)
    // }
    //
    // }
    //
    // double calcJDofNextPrevRiseSet(double next, double rise, double JD, double latitude, double longitude, double tz,
    // double dst)
    // {
    // double julianday = JD;
    // double increment = ((next) ? 1.0 : -1.0);
    //
    // double time = calcSunriseSetUTC(rise, julianday, latitude, longitude);
    // while(!isNumber(time)){
    // julianday += increment;
    // time = calcSunriseSetUTC(rise, julianday, latitude, longitude);
    // }
    // double timeLocal = time + tz * 60.0 + ((dst) ? 60.0 : 0.0);
    // while ((timeLocal < 0.0) || (timeLocal >= 1440.0))
    // {
    // double incr = ((timeLocal < 0) ? 1 : -1);
    // timeLocal += (incr * 1440.0);
    // julianday -= incr;
    // }
    // return julianday;
    // }

    // double calculate(double lat, double lng) {
    // //refreshMap()
    // //clearOutputs()
    // //map.clearOverlays()
    // //showMarkers()
    // double jday = getJD();
    // double tl = getTimeLocal();
    // double tz = readTextBox("zonebox", 5, 0, 0, -14, 13, 0);
    // // double dst = document.getElementById("dstCheckbox").checked
    // double total = jday + tl/1440.0 - tz/24.0;
    // double T = calcTimeJulianCent(total);
    // // double lat = parseFloat(document.getElementById("latbox").value.substring(0,9))
    // // double lng = parseFloat(document.getElementById("lngbox").value.substring(0,10))
    // calcAzEl(1, T, tl, lat, lng, tz);
    // calcSolNoon(jday, lng, tz, dst);
    // double rise = calcSunriseSet(1, jday, lat, lng, tz, dst);
    // double set = calcSunriseSet(0, jday, lat, lng, tz, dst);
    // //alert("JD " + jday + " " + rise + " " + set + " ")
    // }

    public static LatLon azEl2LatLon(Position observer, AzEl azel, double dist) {
        double R = 6371.009; // earth radius in m
        double theta = Math.PI / 2. - observer.latitude.radians;
        double phi = observer.longitude.radians;
        double A = -azel.azimuth.radians;
        double z = Math.PI / 2. - azel.elevation.radians;

        Vec4 u = new Vec4(Math.sin(theta) * Math.cos(phi), Math.sin(theta) * Math.sin(phi), Math.cos(theta));
        u = u.multiply3(observer.elevation + R);
        // System.out.println("x1 = "+u);

        Vec4 v = new Vec4(Math.sin(z) * Math.cos(A), Math.sin(z) * Math.sin(A), Math.cos(z));
        v = v.multiply3(dist);
        // System.out.println("x2 = "+v);

        Quaternion roty = Quaternion.fromAxisAngle(Angle.fromRadians(-theta), 0, 1, 0);
        Quaternion rotz = Quaternion.fromAxisAngle(Angle.fromRadians(Math.PI + phi), 0, 0, 1);
        Vec4 w = v.transformBy3(roty).transformBy3(rotz).add3(u);

        // System.out.println("x3 = "+w);
        double r4 = w.getLength3();

        double theta4;
        if (r4 != 0) {
            theta4 = Math.acos(w.z / r4);
        } else {
            theta4 = 0;
        }

        double phi4;
        if (w.x != 0) {
            phi4 = Math.atan2(w.y, w.x);
        } else {
            phi4 = 0;
        }
        // System.out.println("r4 = "+r4+" lat = "+(Math.PI-theta4)+" lon = "+phi4);
        return LatLon.fromRadians(Math.PI / 2. - theta4, phi4);
        // Return[{r4, (90 - θ4/Degree), ϕ4 /Degree}]
    }

    /**
     * @param time
     * @param timezone num of offset hours (germany: +1)
     * @return
     */
    public static LatLon subsolarPoint(Calendar time) {
        double dist = 10000000;
        LatLon latlon = LatLon.fromDegrees(50, 11); // value does not matter
        double jday = getJD(time.get(Calendar.YEAR), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
        double tl =
            getTimeLocal(
                time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE),
                time.get(Calendar.SECOND),
                false,
                time.get(Calendar.DST_OFFSET) != 0);
        double tz = time.get(Calendar.ZONE_OFFSET) / (1000. * 60. * 60.); // in days
        double total = jday + tl / 1440.0 - tz / 24.0; // in days
        double T = calcTimeJulianCent(total);
        AzEl azel = calcAzEl(T, tl, latlon, tz, false);

        // System.out.println("declination: "+calcSunRtAscension(T));
        // System.out.println(calcAzEl(T, tl, latlon, tz));
        return azEl2LatLon(new Position(latlon, 0), azel, dist);
    }

    public static void main(String[] args) {
        System.out.println(TimeZone.getDefault());
        System.out.println(new GregorianCalendar());
        // for (int i = 0; i < 24; i++) {
        // LatLon latlon = LatLon.fromDegrees(50,11); // germany
        // double jday = getJD(2009,12,12);
        // double tl = getTimeLocal(14,0,0,false,false);
        // double tz = +1;
        // double total = jday + tl/1440.0 - tz/24.0;
        // double T = calcTimeJulianCent(total);
        //// System.out.println("good:"+calcAzEl(T,tl,latlon,tz));
        //
        // Calendar cal = new GregorianCalendar(TimeZone.getDefault());
        // cal.set(2009, Calendar.DECEMBER, 12, i, 0, 0);
        // System.out.println(subsolarPoint(cal));
        // //System.out.println(subsolarPoint2(new GregorianCalendar()));
        // }
        // 6371.009 + 100, 20, 30, 39646.455, 36.187, 59.222
        System.out.println(
            azEl2LatLon(new Position(LatLon.fromDegrees(20, 30), 100), AzEl.fromDegrees(36.187, 59.222), 39646.455));
    }
}
