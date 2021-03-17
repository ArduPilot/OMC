/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.plane.nmea;

import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import eu.mavinci.core.plane.protocol.CNMEA;
import eu.mavinci.desktop.helper.MathHelper;
import gov.nasa.worldwind.geom.Position;
import java.util.concurrent.TimeUnit;

public class NMEA extends CNMEA {

    private static final IEgmModel egmModel = StaticInjector.getInstance(IEgmModel.class);

    public static String createGPGGA(Position refPositionAboveMSL) {

        // if (true) return "$GPGGA,121621,4900.0000,N,00800.0000,E,1,05,1.00,100.0,M,10.000,M,,*74";
        // System.out.println("$GPGGA,121621,4900.0000,N,00800.0000,E,1,05,1.00,100.0,M,10.000,M,,*74");

        if (refPositionAboveMSL == null) {
            return null;
        }

        long l = System.currentTimeMillis();
        final long day = TimeUnit.MILLISECONDS.toDays(l);
        l -= TimeUnit.DAYS.toMillis(day);
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        l -= TimeUnit.HOURS.toMillis(hr);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l);
        l -= TimeUnit.MINUTES.toMillis(min);
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l);
        // l -= TimeUnit.SECONDS.toMillis(sec);
        // final long ms = TimeUnit.MILLISECONDS.toMillis(l);
        // String time = String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
        String time = String.format("%02d%02d%02d", hr, min, sec);
        // int latD = (int) Math.abs(refPosition.latitude.degrees);
        // double latM = (Math.abs(refPosition.latitude.degrees) - latD)*60.;
        //
        // int lonD = (int) Math.abs(refPosition.longitude.degrees);
        // double lonM = (Math.abs(refPosition.longitude.degrees) - latD)*60.;

        String gga = "GPGGA," + time + ",";
        // String gga = "GPGGA,000001,";

        double posnum = Math.abs(refPositionAboveMSL.latitude.degrees);
        double latmins = posnum % 1;
        int ggahours = (int)(posnum - latmins);
        latmins = latmins * 60;
        double latfracmins = latmins % 1;
        int ggamins = (int)(latmins - latfracmins);
        int ggafracmins = (int)(latfracmins * 10000);
        ggahours = ggahours * 100 + ggamins;
        if (ggahours < 1000) {
            gga += "0";
            if (ggahours < 100) {
                gga += "0";
            }
        }

        gga += ggahours + ".";
        if (ggafracmins < 1000) {
            gga += "0";
            if (ggafracmins < 100) {
                gga += "0";
                if (ggafracmins < 10) {
                    gga += "0";
                }
            }
        }

        gga += ggafracmins;
        if (refPositionAboveMSL.latitude.degrees > 0) {
            gga += ",N,";
        } else {
            gga += ",S,";
        }

        posnum = Math.abs(refPositionAboveMSL.longitude.degrees);
        latmins = posnum % 1;
        ggahours = (int)(posnum - latmins);
        latmins = latmins * 60;
        latfracmins = latmins % 1;
        ggamins = (int)(latmins - latfracmins);
        ggafracmins = (int)(latfracmins * 10000);
        ggahours = ggahours * 100 + ggamins;
        if (ggahours < 10000) {
            gga += "0";
            if (ggahours < 1000) {
                gga += "0";
                if (ggahours < 100) {
                    gga += "0";
                }
            }
        }

        gga += ggahours + ".";
        if (ggafracmins < 1000) {
            gga += "0";
            if (ggafracmins < 100) {
                gga += "0";
                if (ggafracmins < 10) {
                    gga += "0";
                }
            }
        }

        gga += ggafracmins;
        if (refPositionAboveMSL.longitude.degrees > 0) {
            gga += ",E,";
        } else {
            gga += ",W,";
        }

        //

        double ellipseSep = MathHelper.round(egmModel.getEGM96Offset(refPositionAboveMSL), 1);
        double alt = MathHelper.round(refPositionAboveMSL.elevation - ellipseSep, 1);

        gga += "1,07,2.0," + alt + ",M," + ellipseSep + ",M,,";
        // if I add additional fields as in the previous way, sapos BW is not working (trimble caster)
        String checksum = NMEA.getNmeaSum(gga);

        // Log.i("Manual GGA", "$" + gga + "*" + checksum);
        // System.out.println("$" + gga + "*" + checksum);
        return "$" + gga + "*" + checksum;
    }

}
