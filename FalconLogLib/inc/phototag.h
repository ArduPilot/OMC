/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef PHOTOTAG_H
#define PHOTOTAG_H

#include "falconlog_global.h"

#include <string>
#include <position.h>
#include <quaternion.h>

#include "pathpoint.h"

#pragma warning( push )
#pragma warning( disable : 4251 )

/*!
 * \brief The PhotoTag class
 * container class for a Phototag, contains description, position and state for a triggerpoint
 * (e.x. photo) from a Falcon
 * \todo Add health status, e.g. for corrupt tags, illegal coordinates etc
 */
class FALCONLOGSHARED_EXPORT PhotoTag
{
public:
  /*!
   * \brief source of trigger command
   */
  enum TriggerSource {
    TriggerSource_MGS,       ///< User-trigger via Mobile Ground Station
    TriggerSource_Navigator, ///< Programmatic trigger via Navigator
    TriggerSource_Gamepad,   ///< User trigger via Gamepad or Joystick attached to MGS
    TriggerSource_Falcon,    ///< Programmatic trigger from Falcons navigation or peripheral
  };

  int64_t GPSTime;              ///< timestamp in GPS milliseconds
  asl::Position positionGPS;    ///< gps WGS84 (lat, long, height), calculated from E_x
  asl::Vector3D position;
  asl::Quaternion orientation;  ///< orientation quaternion in earth frame
  asl::Vector3D rpy;            ///< camera orientation roll-pitch-yaw
  int number;                   ///< number of waypoint
  TriggerSource source;         ///< source of trigger \sa TriggerSource
  std::string project;          ///< project name or source of mission (e.x. Navigator project name)
};

#pragma warning(pop)

#endif // PHOTOTAG_H
