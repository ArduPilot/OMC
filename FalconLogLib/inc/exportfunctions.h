/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef EXPORTFUNCTIONS_H
#define EXPORTFUNCTIONS_H

#endif // EXPORTFUNCTIONS_H
#include "rtcm.h"

typedef struct _APhotoTagStrure {

      double lat;
      double lon;
      double height;

      double relX;
      double relY;
      double relH;

      double roll;
      double pitch;
      double yaw;

      //quaternion
      double x;
      double y;
      double z;
      double w;

      double timestamp;

      double num;
	  
	  double lat0;
      double lon0;
      double height0;


}APhotoTagStructure;

extern "C" __declspec(dllexport) void free_struct(APhotoTagStructure *structure, int num_elements);
extern "C" __declspec(dllexport) void get_PhotoTags(char* path, int *num, APhotoTagStructure** structs, AntennaInformation* AntennaInformation, int* numAntennas);
