/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "falcon8trinitylog.h"
#include "trinitylogreader.h"

#include <math.h>
#include <cstring>
#include <conversions.h>
#include <atostypes.h>
#include "trinitylog.h"

#include "pathpoint.h"
#include "phototag.h"

Falcon8TrinityLog::Falcon8TrinityLog(const std::string& path) :
  FalconLogBase(path)
{
}

bool Falcon8TrinityLog::read()
{
  using namespace std;

  string infoFile = path_ + "\\ASCTEC.IFO";
  string logFile = path_ + "\\ASCTEC.LOG";
  string logHpFile = path_ + "\\ASCHP.LOG";

  using namespace trinityLog;
//  TrinityLogReader* logreader = new TrinityLogReader();
  auto logreader = std::make_unique<TrinityLogReader>();

  logreader->setInfoFilename(infoFile);

  try {
    logreader->setLogFilename(logFile);
    log_ = logreader->read();
  } catch (...) {
    log_ = nullptr;
  }
  try {
    logreader->setLogFilename(logHpFile);
    logHP_ = logreader->read();
  } catch (...) {
    logHP_ = nullptr;
  }

  read_ = true;
  return true;
}

double Falcon8TrinityLog::airTime() const
{
  if(log_ == nullptr) {
    return 0;
  }

  // no convenient way to get the flight time from usb-log
  uint32_t time = 0;
  try {
    time = log_->getLast<uint32_t>("ATOS_MSG_FLIGHTTIME", "flighttime");
  } catch(...) {
    try {
      time = log_->getLast<uint32_t>("ATOS_MSG_FLIGHTTIME", "flightmode");
    } catch(...) {

    }
  }

  return time / 10.0;
}

asl::Position Falcon8TrinityLog::position() const
{
  double lat0;
  double lon0;
  double height0;
  bool useGpsCoords = false;
  if(log_ == nullptr) {
    if(logHP_ == nullptr) {
      return asl::Position();
    }
    useGpsCoords = true;
  }

  if(useGpsCoords) {
    trinityLog::Timestamp time;
    std::tie(time, lat0) = logHP_->getFirstNonZero("ATOS_MSG_TRIGGER_GEOTAG", "lat0");
    lon0 = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "lon0", time);
    height0 = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "gps_height", time);
  } else {
    trinityLog::Timestamp time;
    std::tie(time, lat0) = log_->getFirstNonZero("ATOS_MSG_ATTITUDE_INPUT", "lat0");
    lat0 /= 10000000.0;
    lon0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "lon0", time) / 10000000.0;
    height0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "gps_height0", time);
  }
  return asl::Position(lat0, lon0, height0);
}



int64_t Falcon8TrinityLog::time() const
{
  double tow = 0;
  double week = 0;
  trinityLog::Timestamp time = 0;
  if(log_ != nullptr) {
    try {
      std::tie(time, week) = log_->getFirstNonZero("ATOS_MSG_GPS_DATA", "week");
      tow = log_->get("ATOS_MSG_GPS_DATA", "time_of_week", time);
    } catch(...) {
      if(logHP_ != nullptr) {
        auto timestamps = logHP_->getTimeStamps("ATOS_MSG_TRIGGER_GEOTAG");
        if(timestamps.empty())
          return 0;
        time = timestamps.front();
        week = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "week", 0);
        tow = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "tow", 0);
      } else {
        return 0;
      }
    }
  } else if(logHP_ != nullptr) {
    auto timestamps = logHP_->getTimeStamps("ATOS_MSG_TRIGGER_GEOTAG");
    if(timestamps.empty())
      return 0;
    time = timestamps.front();
    week = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "week", 0);
    tow = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "tow", 0);
  } else {
    return 0;
  }

  if(week && tow) {
    return (int64_t) asl::towWeek2GpsTimestamp((int)week, (int)tow) - time / 1000;
  }
  return 0;
}

std::vector<PhotoTag> Falcon8TrinityLog::photoTags() const
{
  if(logHP_ == nullptr) {
    return std::vector<PhotoTag>();
  }

  auto timeRef = this->time();
  std::vector<PhotoTag> tags;

  bool useGpsCoords = true;
//  if(log_ == nullptr) {
//    useGpsCoords = true;
//  }

  double lat0 = 0;
  double lon0 = 0;
  double height0 = 0;
  double magic_number = 1 / 12756274.0 * 360 / M_PI; // helper to calculate lat/lon from E_x
  double coslat0 = 0;

  if(!useGpsCoords) {
    trinityLog::Timestamp time;
    try {
      std::tie(time, lat0) = log_->getFirstNonZero("ATOS_MSG_ATTITUDE_INPUT", "lat0");
      lat0 /= 10000000.0;
      lon0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "lon0", time) / 10000000.0;
      height0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "gps_height0", time);
      coslat0 = cos(lat0 * M_PI / 180);
    } catch(...) {
      useGpsCoords = true;
    }
  }

  auto timestamps = logHP_->getTimeStamps("ATOS_MSG_TRIGGER_GEOTAG");
  for(auto time : timestamps){
    double latitude;
    double longitude;
    double height;

    if(useGpsCoords) {
      latitude = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "latitude", time);
      longitude = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "longitude", time);
      if (std::abs(latitude - position().latitude()) > 0.1 ||
          std::abs(longitude - position().longitude()) > 0.1) {
        continue;
      }
      height = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "gps_height", time);
    } else {
      auto E_x = log_->get<asl::atos::vector3f>("ATOS_MSG_ATTITUDE_STATE", "E_x", time);
      latitude = lat0 + E_x.y * magic_number;
      longitude = lon0 + E_x.x * magic_number / coslat0;
      height = height0 + E_x.z;
    }
    auto roll = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "camAngleRoll", time) / 100.0;
    auto pitch = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "camAnglePitch", time) / 100.0;
    auto yaw = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "camAngleYaw", time) / 100.0;

    //returning back some magic code for the case of some internal overflow
    if(yaw < -180) { // ifo file contained wrong datatype
          auto yawi16 = logHP_->get<int16_t>("ATOS_MSG_TRIGGER_GEOTAG", "camAngleYaw", time);
          yaw = (double) *(uint16_t*) &yawi16;
          yaw /= 100.0;
        }
    auto project = logHP_->getAsString("ATOS_MSG_TRIGGER_GEOTAG", "pathName", time);
    int number = (int) logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "triggerCnt", time);
    auto source = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "triggerSrc", time);

    auto x = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "x", time);
    auto y = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "y", time);
    auto relative_height = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "pressure_height", time);

    auto week = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "week", time);
    auto tow = logHP_->get("ATOS_MSG_TRIGGER_GEOTAG", "tow", time);

    PhotoTag tag;

    tag.orientation = asl::Quaternion::fromRPY(roll, pitch, yaw);
    tag.rpy = asl::Vector3D(roll, pitch, yaw);
    tag.positionGPS = asl::Position(latitude, longitude, height);
    tag.position = asl::Vector3D(x, y, relative_height);
    tag.project = project;
    tag.number = number;
    tag.source = (PhotoTag::TriggerSource)(int) source;
    tag.GPSTime = (int64_t) asl::towWeek2GpsTimestamp((int)week, (int)tow);
    tags.push_back(tag);
  }
  return tags;
}

std::vector<PathPoint> Falcon8TrinityLog::flightPath(int maxPoints) const
{
  if(log_ == nullptr) {
    return std::vector<PathPoint>();
  }

  auto timeRef = this->time();
//  uint64_t time;
//  double lat0;
//  std::tie(time, lat0) = log_->getFirstNonZero("ATOS_MSG_ATTITUDE_INPUT", "lat0");
//  lat0 /= 10000000.0;
//  double lon0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "lon0", time) / 10000000.0;
//  double height0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "gps_height0", time);


  auto timestamps = log_->getTimeStamps("ATOS_MSG_ATTITUDE_STATE");
  auto E_x = log_->getVector<asl::atos::vector3f>("ATOS_MSG_ATTITUDE_STATE", "E_x");
  auto orientation = log_->getVector<asl::atos::quaternion>("ATOS_MSG_ATTITUDE_STATE", "SE_orientation");
  auto rpy = log_->getVector<asl::atos::vector3f>("ATOS_MSG_ATTITUDE_STATE", "euler_RPY");

  int inc = 1;
  if(maxPoints && maxPoints < (int)timestamps.size()) {
    inc = (int) timestamps.size() / maxPoints;
  }

  double magic_number = 1 / 12756274.0 * 360 / M_PI;

  std::vector<PathPoint> path;
  for(size_t i = 0; i < timestamps.size(); i += inc) {
    double lat0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "lat0", timestamps[i]) / 10000000.0;
    double lon0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "lon0", timestamps[i]) / 10000000.0;
    double height0 = log_->get("ATOS_MSG_ATTITUDE_INPUT", "gps_height0", timestamps[i]);

    if(lat0 == 0 && lon0 == 0 && height0 == 0) {
      continue;
    }
    double coslat0 = cos(lat0 * M_PI / 180);

    double latitude = lat0 + E_x[i].y * magic_number;
    double longitude = lon0 + E_x[i].x * magic_number / coslat0;
    double height = height0 + E_x[i].z;

    PathPoint pp;
    pp.position = asl::Vector3D(E_x[i].x, E_x[i].y, E_x[i].z);
    pp.positionGPS = asl::Position(latitude, longitude, height);
    pp.orientation = asl::Quaternion(orientation[i]);
    pp.rpy = rpy[i];
    pp.timestamp = timestamps[i] / 1000;
    pp.GPSTime = pp.timestamp + timeRef;

    uint32_t flightmode = 0;
    try {
      flightmode = log_->get<uint32_t>("ATOS_MSG_FLIGHT_MODE", "flight_mode", timestamps[i]);
    } catch(...) {

    }

    if(flightmode & (uint32_t) asl::atos::FlightmodeFlags::POS) {
      pp.flightmode = PathPoint::FlightMode::GPS;
    } else if(flightmode & (uint32_t) asl::atos::FlightmodeFlags::HEIGHT) {
      pp.flightmode = PathPoint::FlightMode::Height;
    } else if(flightmode & (uint32_t) asl::atos::FlightmodeFlags::ACC) {
      pp.flightmode = PathPoint::FlightMode::Manual;
    } else {
      pp.flightmode = PathPoint::FlightMode::Unknown;
    }
    path.push_back(pp);
  }
  return path;
}
