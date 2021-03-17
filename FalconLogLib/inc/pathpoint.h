/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef PATHPOINT_H
#define PATHPOINT_H

#include "falconlog_global.h"

#include <vector3d.h>
#include <quaternion.h>
#include <position.h>
#include <inttypes.h>

#pragma warning( push )
#pragma warning( disable : 4251 )

/*!
 * \brief The PathPoint class.
 * container class for Falcon position and state througout the flight.
 * \todo Add health status, e.g. for corrupt positions, illegal coordinates etc
 */
class FALCONLOGSHARED_EXPORT PathPoint
{
public:
  enum class FlightMode {
    Unknown = 0,
    Manual,
    Height,
    GPS
  };

  asl::Vector3D position;
  asl::Position positionGPS;      ///< gps WGS84 (lat, long, height), calculated from E_x
  asl::Quaternion orientation; ///< orientation quaternion in earth frame
  asl::Vector3D rpy;           ///< orientation as roll / pitch / yaw
  int64_t timestamp;           ///< timestamp in milliseconds from Falcon boot
  int64_t GPSTime;             ///< timestamp in GPS milliseconds
  FlightMode flightmode;
};

/*!
 * \brief The PathPointEx class
 * extended state of the Falcon, extends \c PathPoint
 */
class FALCONLOGSHARED_EXPORT PathPointEx : public PathPoint {
public:
  asl::Vector3D velocity;      ///< Velocity in earth frame
  asl::Vector3D accelleration; ///< accelleration in earth frame, gravity included
  asl::Quaternion w;           ///< angular velocity
  asl::Quaternion w_dot;       ///< angular accelleration
  asl::Vector3D wind;          ///< wind speed and direction
};

#pragma warning( pop )
#endif // PATHPOINT_H
