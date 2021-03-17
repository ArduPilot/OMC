/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef RTCM_H
#define RTCM_H

#include "trinitylogreader.h"

// See Table 3.5-6
// Msg Type Stationary Antenna Reference Point, No Height Information
typedef struct {
 double Msg_Number; // Message Number
 double RefSt_ID; // Reference Station ID
 double Reserved_ITRF; // Reserved for ITRF Realization Year
 double GPS_Ind; // GPS Indicator
 double GLONASS_Ind; // GLONASS Indicator
 double Reserved_Galileo; // Reserved for Galileo Indicator
 double RefSt_Ind; // Reference-Station Indicator
 double Ant_RefPt_X; // Antenna Reference Point ECEF-X
 double SingleRecOsc_Ind; // Single Receiver Oscillator Indicator
 double Reserved; // Reserved
 double Ant_RefPt_Y; // Antenna Reference Point ECEF-Y
 double Reserved2; // Reserved
 double Ant_RefPt_Z; // Antenna Reference Point ECEF-Z
}ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT;


// See Table 3.5-7
// Msg Type Stationary Antenna Reference Point, with Height Information
typedef struct {

 double Msg_Number; // Message Number
 double RefSt_ID; // Reference Station ID
 double Reserved_ITRF; // Reserved for ITRF Realization Year
 double GPS_Ind; // GPS Indicator
 double GLONASS_Ind; // GLONASS Indicator
 double Reserved_Galileo; // Reserved for Galileo Indicator
 double RefSt_Ind; // Reference-Station Indicator
 double Ant_RefPt_X; // Antenna Reference Point ECEF-X
 double SingleRecOsc_Ind; // Single Receiver Oscillator Indicator
 double Reserved; // Reserved
 double Ant_RefPt_Y; // Antenna Reference Point ECEF-Y
 double Reserved2; // Reserved
 double Ant_RefPt_Z; // Antenna Reference Point ECEF-Z
 double Ant_Height; // Antenna Height
}ATOS_MSG_RTCM_STATIONARY_ANTENNA_REF_PT_HEIGHT;

// See Table 3.5-8
// Msg Type Antenna Descriptor
// Note: N <= 31
typedef struct {

 double Msg_Number; // Message Number
 double RefSt_ID; // Reference Station ID
 double Descr_Cnt; // Descriptor Counter N
 double Ant_Descr[31]; // Antenna Descriptor
 double Ant_Setup_ID; // Antenna Setup ID
}ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR;

// See Table 3.5-9
// Msg Type Antenna Descriptor & Serial Number
// Note: N <= 31, M <= 31
typedef struct {
 double Msg_Number; // Message Number
 double RefSt_ID; // Reference Station ID
 double Descr_Cnt; // Descriptor Counter N
 double Ant_Descr[31]; // Antenna Descriptor
 double Ant_Setup_ID; // Antenna Setup ID
 double SerialNr_Cnt; // Serial Number Counter M
 double Ant_Serial_Nr[31]; // Antenna Serial Number
}ATOS_MSG_RTCM_ANTENNA_DESCRIPTOR_SERIAL_NUMBER;

typedef struct{
    double lat;
    double lon;
    double height;
    double timestamp;
    double ID;
}AntennaInformation;


extern "C" __declspec(dllexport) void free_antenna_struct(AntennaInformation *structure, int num_elements);

extern "C" __declspec(dllexport) AntennaInformation* get_AntennaInformation(char *path, int *num);

void getSingleAntennaInformation(AntennaInformation *AntennaInformation, trinityLog::TrinityLogPtr log, int *num);

#endif // RTCM_H
