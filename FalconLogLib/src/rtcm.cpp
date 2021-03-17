/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include <iostream>
#include <memory>
#include <iostream>
#include <string>
#include <fstream>
#include <nowide/fstream.hpp>

#include "trinitylogreader.h"

#define _USE_MATH_DEFINES
#include <cmath>

#include <math.h>
#include <inttypes.h>

#include "rtcm.h"

trinityLog::TrinityLogPtr getLogReader(std::string path_){

    std::string infoFile = path_ + "\\ASCTEC.IFO";
    std::string logFile = path_ + "\\ASCTEC.LOG";
    std::string logHpFile = path_ + "\\ASCHP.LOG";
    using namespace trinityLog;

    auto logreader = std::make_unique<TrinityLogReader>();

    logreader->setInfoFilename(infoFile);
    logreader->setLogFilename(logFile);

    trinityLog::TrinityLogPtr log_;

    log_ = logreader->read();
    return log_;
}


ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT* get_ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_ONE(int* size, trinityLog::TrinityLogPtr log){

    std::vector<trinityLog::Timestamp> timestamps = log->getTimeStamps("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT");

    ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT*  antStruct = new ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT();

    *size = timestamps.size();
    if(timestamps.size() > 0){

        antStruct->Ant_RefPt_X = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Ant_RefPt_X", timestamps.at(0));
        antStruct->Ant_RefPt_Y = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Ant_RefPt_Y", timestamps.at(0));
        antStruct->Ant_RefPt_Z = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Ant_RefPt_Z", timestamps.at(0));

        antStruct->GLONASS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "GLONASS_Ind", timestamps.at(0));
        antStruct->GPS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "GPS_Ind", timestamps.at(0));
        antStruct->Msg_Number = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Msg_Number", timestamps.at(0));

        antStruct->RefSt_ID = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "RefSt_ID", timestamps.at(0));
        antStruct->RefSt_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "RefSt_Ind", timestamps.at(0));
        antStruct->Reserved = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved", timestamps.at(0));

        antStruct->Reserved2 = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved2", timestamps.at(0));
        antStruct->Reserved_Galileo = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved_Galileo", timestamps.at(0));
        antStruct->Reserved_ITRF = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved_ITRF", timestamps.at(0));
        antStruct->SingleRecOsc_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "SingleRecOsc_Ind", timestamps.at(0));
    }
    return antStruct;
}

ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT* get_ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT_ONE(int* size, trinityLog::TrinityLogPtr log){

    std::vector<trinityLog::Timestamp> timestamps = log->getTimeStamps("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT");

    *size = timestamps.size();
    ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT* antenna = new ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT();

    if (timestamps.size() > 0) {

        antenna->Ant_RefPt_X = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_RefPt_X", timestamps.at(0));
        antenna->Ant_RefPt_Y = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_RefPt_Y", timestamps.at(0));
        antenna->Ant_RefPt_Z = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_RefPt_Z", timestamps.at(0));

        antenna->GLONASS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "GLONASS_Ind", timestamps.at(0));
        antenna->GPS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "GPS_Ind", timestamps.at(0));
        antenna->Msg_Number = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Msg_Number", timestamps.at(0));

        antenna->RefSt_ID = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "RefSt_ID", timestamps.at(0));
        antenna->RefSt_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "RefSt_Ind", timestamps.at(0));
        antenna->Reserved = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved", timestamps.at(0));

        antenna->Reserved2 = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved2", timestamps.at(0));
        antenna->Reserved_Galileo = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved_Galileo", timestamps.at(0));
        antenna->Reserved_ITRF = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved_ITRF", timestamps.at(0));
        antenna->SingleRecOsc_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "SingleRecOsc_Ind", timestamps.at(0));
        antenna->Ant_Height = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_Height", timestamps.at(0));

    }
    return antenna;
}

ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT* get_ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT(std::string path, int* size, trinityLog::TrinityLogPtr log){

    std::vector<trinityLog::Timestamp> timestamps = log->getTimeStamps("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT");

    ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT  *structs = NULL;
    structs = (ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT*)realloc(structs, timestamps.size() * sizeof(ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT));

    *size = timestamps.size();
    for (int i = 0; i < timestamps.size(); i++) {

        structs[i].Ant_RefPt_X = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Ant_RefPt_X", timestamps.at(i));
        structs[i].Ant_RefPt_Y = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Ant_RefPt_Y", timestamps.at(i));
        structs[i].Ant_RefPt_Z = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Ant_RefPt_Z", timestamps.at(i));        

        structs[i].GLONASS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "GLONASS_Ind", timestamps.at(i));
        structs[i].GPS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "GPS_Ind", timestamps.at(i));
        structs[i].Msg_Number = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Msg_Number", timestamps.at(i));

        structs[i].RefSt_ID = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "RefSt_ID", timestamps.at(i));
        structs[i].RefSt_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "RefSt_Ind", timestamps.at(i));
        structs[i].Reserved = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved", timestamps.at(i));

        structs[i].Reserved2 = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved2", timestamps.at(i));
        structs[i].Reserved_Galileo = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved_Galileo", timestamps.at(i));
        structs[i].Reserved_ITRF = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "Reserved_ITRF", timestamps.at(i));
        structs[i].SingleRecOsc_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT", "SingleRecOsc_Ind", timestamps.at(i));
    }
    return structs;
}

ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT* get_ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT(std::string path, int* size, trinityLog::TrinityLogPtr log){

    std::vector<trinityLog::Timestamp> timestamps = log->getTimeStamps("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT");

    ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT  *structs = NULL;
    structs = (ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT*)realloc(structs, timestamps.size() * sizeof(ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT));

    *size = timestamps.size();
    for (int i = 0; i < timestamps.size(); i++) {

        structs[i].Ant_RefPt_X = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_RefPt_X", timestamps.at(i));
        structs[i].Ant_RefPt_Y = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_RefPt_Y", timestamps.at(i));
        structs[i].Ant_RefPt_Z = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_RefPt_Z", timestamps.at(i));

        structs[i].GLONASS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "GLONASS_Ind", timestamps.at(i));
        structs[i].GPS_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "GPS_Ind", timestamps.at(i));
        structs[i].Msg_Number = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Msg_Number", timestamps.at(i));

        structs[i].RefSt_ID = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "RefSt_ID", timestamps.at(i));
        structs[i].RefSt_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "RefSt_Ind", timestamps.at(i));
        structs[i].Reserved = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved", timestamps.at(i));

        structs[i].Reserved2 = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved2", timestamps.at(i));
        structs[i].Reserved_Galileo = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved_Galileo", timestamps.at(i));
        structs[i].Reserved_ITRF = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Reserved_ITRF", timestamps.at(i));
        structs[i].SingleRecOsc_Ind = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "SingleRecOsc_Ind", timestamps.at(i));
        structs[i].Ant_Height = log->get("ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT", "Ant_Height", timestamps.at(i));

    }
    return structs;
}
ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR* get_ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR(std::string path, int* size, trinityLog::TrinityLogPtr log){
    std::vector<trinityLog::Timestamp> timestamps = log->getTimeStamps("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR");

    ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR  *structs = NULL;
    structs = (ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR*)realloc(structs, timestamps.size() * sizeof(ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR));

    *size = timestamps.size();
    for (int i = 0; i < timestamps.size(); i++) {

        structs[i].Msg_Number = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR", "Msg_Number", timestamps.at(i));
        structs[i].RefSt_ID = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR", "RefSt_ID", timestamps.at(i));
        structs[i].Ant_Setup_ID = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR", "Ant_Setup_ID", timestamps.at(i));
        structs[i].Descr_Cnt = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR", "Descr_Cnt", timestamps.at(i));

    }
    return structs;
}

ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER* get_ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER(std::string path, int* size, trinityLog::TrinityLogPtr log){

    std::vector<trinityLog::Timestamp> timestamps = log->getTimeStamps("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER");

    ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER  *structs = NULL;
    structs = (ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER*)realloc(structs, timestamps.size() * sizeof(ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER));

    *size = timestamps.size();
    for (int i = 0; i < timestamps.size(); i++) {

        structs[i].Msg_Number = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER", "Msg_Number", timestamps.at(i));
        structs[i].RefSt_ID = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER", "RefSt_ID", timestamps.at(i));
        structs[i].Ant_Setup_ID = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER", "Ant_Setup_ID", timestamps.at(i));
        structs[i].Descr_Cnt = log->get("ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER", "Descr_Cnt", timestamps.at(i));

    }
    return structs;
}



 double* cartesianToGeodetic(double X, double Y, double Z)
     {
         // Contributed by Nathan Kronenfeld. Integrated 1/24/2011. Brings this calculation in line with Vermeille's
         // most recent update.

         // According to
         // H. Vermeille,
         // "An analytical method to transform geocentric into geodetic coordinates"
         // http://www.springerlink.com/content/3t6837t27t351227/fulltext.pdf
         // Journal of Geodesy, accepted 10/2010, not yet published
         double XXpYY = X * X + Y * Y;
         double sqrtXXpYY = sqrt(XXpYY);

     double equatorialRadius = 6378137.0; // ellipsoid equatorial getRadius, in meters
        // 	double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
     double es = 0.00669437999013; // eccentricity squared, semi-major axis

         double a = equatorialRadius;
         double ra2 = 1 / (a * a);
         double e2 = es;
         double e4 = e2 * e2;

         // Step 1
         double p = XXpYY * ra2;
         double q = Z * Z * (1 - e2) * ra2;
         double r = (p + q - e4) / 6;

         double h;
         double phi;

         double evoluteBorderTest = 8 * r * r * r + e4 * p * q;
         if (evoluteBorderTest > 0 || q != 0)
         {
             double u;

             if (evoluteBorderTest > 0)
             {
                 // Step 2: general case
                 double rad1 = sqrt(evoluteBorderTest);
                 double rad2 = sqrt(e4 * p * q);

                 // 10*e2 is my arbitrary decision of what Vermeille means by "near... the cusps of the evolute".
                 if (evoluteBorderTest > 10 * e2)
                 {
                     double rad3 = cbrt((rad1 + rad2) * (rad1 + rad2));
                     u = r + 0.5 * rad3 + 2 * r * r / rad3;
                 }
                 else
                 {
                     u = r + 0.5 * cbrt((rad1 + rad2) * (rad1 + rad2)) + 0.5 * cbrt(
                         (rad1 - rad2) * (rad1 - rad2));
                 }
             }
             else
             {
                 // Step 3: near evolute
                 double rad1 = sqrt(-evoluteBorderTest);
                 double rad2 = sqrt(-8 * r * r * r);
                 double rad3 = sqrt(e4 * p * q);
                 double atan = 2 * atan2(rad3, rad1 + rad2) / 3;

                 u = -4 * r * sin(atan) * cos(M_PI / 6 + atan);
             }

             double v = sqrt(u * u + e4 * q);
             double w = e2 * (u + v - q) / (2 * v);
             double k = (u + v) / (sqrt(w * w + u + v) + w);
             double D = k * sqrtXXpYY / (k + e2);
             double sqrtDDpZZ = sqrt(D * D + Z * Z);

             h = (k + e2 - 1) * sqrtDDpZZ / k;
             phi = 2 * atan2(Z, sqrtDDpZZ + D);
         }
         else
         {
             // Step 4: singular disk
             double rad1 = sqrt(1 - e2);
             double rad2 = sqrt(e2 - p);
             double e = sqrt(e2);

             h = -a * rad1 * rad2 / e;
             phi = rad2 / (e * rad2 + rad1 * sqrt(p));
         }

         // Compute lambda
         double lambda;
         double s2 = sqrt(2);
         if ((s2 - 1) * Y < sqrtXXpYY + X)
         {
             // case 1 - -135deg < lambda < 135deg
             lambda = 2 * atan2(Y, sqrtXXpYY + X);
         }
         else if (sqrtXXpYY + Y < (s2 + 1) * X)
         {
             // case 2 - -225deg < lambda < 45deg
             lambda = -M_PI * 0.5 + 2 * atan2(X, sqrtXXpYY - Y);
         }
         else
         {
             // if (sqrtXXpYY-Y<(s2=1)*X) {  // is the test, if needed, but it's not
             // case 3: - -45deg < lambda < 225deg
             lambda = M_PI * 0.5 - 2 * atan2(X, sqrtXXpYY + Y);
         }
      double* dat = new double[3];

      dat[0] = phi/ M_PI * 180.0;
      dat[1] = lambda/ M_PI * 180.0;
      dat[2] = (h);
      //std::cout << "Coords Corn func: " << phi/ M_PI * 180.0  << " " << lambda/ M_PI * 180.0 << " " <<h << " " << std::endl;

         return dat;
     }


void free_antenna_struct(AntennaInformation *structure, int num_elements) {
    //free char arrays if any
    free(structure);
}

void getSingleAntennaInformation(AntennaInformation *antennaInformation, trinityLog::TrinityLogPtr log, int* num){

    int structs2Size;
    ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT* antenna1 = get_ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT_ONE(&structs2Size, log);

    int structs3Size;
    ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT* antenna2 = get_ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_ONE(&structs3Size, log);

    if(structs2Size != 0){
            *num = structs2Size;
            double antX = antenna1->Ant_RefPt_X  / 10000.0;
            double antY = antenna1->Ant_RefPt_Y  / 10000.0;
            double antZ = antenna1->Ant_RefPt_Z  / 10000.0;

            double* coords2 = cartesianToGeodetic(antX, antY, antZ);
            antennaInformation->lat = coords2[0];
            antennaInformation->lon = coords2[1];
            antennaInformation->height = coords2[2];


    }else if (structs3Size != 0){
            *num = structs3Size;
            double antX = antenna2->Ant_RefPt_X  / 10000.0;
            double antY = antenna2->Ant_RefPt_Y  / 10000.0;
            double antZ = antenna2->Ant_RefPt_Z  / 10000.0;

            double* coords2 = cartesianToGeodetic(antX, antY, antZ);
            antennaInformation->lat = coords2[0];
            antennaInformation->lon = coords2[1];
            antennaInformation->height = coords2[2];


    }
}

