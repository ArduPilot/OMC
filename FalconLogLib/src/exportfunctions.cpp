/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "exportfunctions.h"
#include <iostream>
#include <memory>
#include <iostream>
#include <string>
#include <fstream>
#include <nowide/fstream.hpp>

#include "falconlog.h"
#include "falcon8trinitylog.h"
#include "trinitylogreader.h"
#include <math.h>
#include <inttypes.h>

void get_PhotoTags(char* path, int *num, APhotoTagStructure** structs, AntennaInformation *antennaInformation, int* numAntennas) {

    if(path && num && structs && antennaInformation && numAntennas){
        std::string str(path);

        FalconLog log(str);

        auto tags = log.photoTags();
        getSingleAntennaInformation(antennaInformation, log.log(), numAntennas);
        int i = 0;

        *(num) = tags.size();
        *structs = (APhotoTagStructure*)malloc(sizeof(APhotoTagStructure) * tags.size());
        memset(*structs, 0, sizeof(APhotoTagStructure) * tags.size());

        asl::Position takeoffPosition = log.position();

        for (i = 0; i < tags.size(); i++) {

            (*structs)[i].lat = tags[i].positionGPS.latitude();
            (*structs)[i].lon = tags[i].positionGPS.longitude();
            (*structs)[i].height = tags[i].positionGPS.height();

            (*structs)[i].relX = tags[i].position.x();
            (*structs)[i].relY = tags[i].position.y();
            (*structs)[i].relH = tags[i].position.z();

            (*structs)[i].roll = tags[i].rpy.x();
            (*structs)[i].pitch = tags[i].rpy.y();
            (*structs)[i].yaw = tags[i].rpy.z();

            (*structs)[i].x = tags[i].orientation.x();
            (*structs)[i].y = tags[i].orientation.y();
            (*structs)[i].z = tags[i].orientation.z();
            (*structs)[i].w = tags[i].orientation.w();

            (*structs)[i].timestamp = (double)tags[i].GPSTime;
            (*structs)[i].num = tags[i].number;

            //removing null check completely, because log.position() of type asl::Position is never null
            (*structs)[i].lat0 = takeoffPosition.latitude();
            (*structs)[i].lon0 = takeoffPosition.longitude();
            (*structs)[i].height0 = takeoffPosition.height();
        }

    }
}

void free_struct(APhotoTagStructure *structure, int num_elements) {
    printf("(C) cleaning up memory...\n");
    free(structure);
}

