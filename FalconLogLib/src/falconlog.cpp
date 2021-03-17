/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "falconlog.h"

#include <memory>
#include <nowide/fstream.hpp>
#include <conversions.h>
#include <iostream>
#include <algorithm>

#include "falconlogbase.h"
#include "falcon8trinitylog.h"
#include "falconautopilotlog.h"
#include "falconlogexception.h"

// those are needed for unique_ptr to work with pimpl
FalconLog::FalconLog() = default;
FalconLog::~FalconLog() = default;

FalconLog::FalconLog(const std::string& path)
{
  path_ = path;

  auto ifoPath = path + "\\ASCTEC.IFO";
  auto gpsPath = path + "\\GPS.LOG";

  // check what logfile we have:
  nowide::ifstream trinityIFO(ifoPath.c_str(), std::ios::binary);
  nowide::ifstream autopilotLog(gpsPath.c_str(), std::ios::binary);

  if(trinityIFO.good()) {
    logType_ = LogType::Falcon8TrinityLog;
    p_ = std::make_unique<Falcon8TrinityLog>(path);
  } else if (autopilotLog.good()) {
    logType_ = LogType::Falcon8AutopilotLog;
    p_ = std::make_unique<FalconAutopilotLog>(path);
  } else {
    logType_ = LogType::Unknown;
    throw LogNotFoundException();
  }

  if(!p_->readMeta()) {
    p_->read();
    p_->setMeta();
    p_->writeMeta();
  }
}

int FalconLog::logMetaVersion()
{
  return METADATA_VERSION;
}

FalconLog::LogType FalconLog::logType() const
{
  return logType_;
}

std::string FalconLog::logTypeString() const
{
  switch(logType_) {
  case LogType::Falcon8AutopilotLog:
    return "AscTec AutoPilot Log";
  case LogType::Falcon8TrinityLog:
    return "AscTec Trinity Log";
  default:
    return "Unknown Logtype";
  }
}

double FalconLog::airTime() const
{
  return p_->meta_.airTime;
}

asl::Position FalconLog::position() const
{
  return p_->meta_.position;
}

std::vector<PhotoTag> FalconLog::photoTags() const
{
  if(!p_->read_) {
    p_->read();
  }
  return p_->photoTags();
}

std::vector<PathPoint> FalconLog::flightPath(int maxPoints) const
{
  if(!p_->read_) {
    p_->read();
  }
  return p_->flightPath(maxPoints);
}

trinityLog::TrinityLogPtr FalconLog::log() {
  if(logType_ != LogType::Falcon8TrinityLog) {
    throw trinityLog::LogException("Function not implemented for this logtype");
  }
  return static_cast<Falcon8TrinityLog*>(p_.get())->log();
}

int64_t FalconLog::timeUTC() const
{
  return p_->meta_.timestamp;
}

int FalconLog::photoTagCount() const
{
  return p_->meta_.numberOfTags;
}

double FalconLog::maxDistance() const
{
  if(!p_->read_) {
    p_->read();
  }
  double d = 0.0;
  try {
    asl::Position p0 = p_->position();

    for (auto p : p_->flightPath(500)) {
      double distance = p0.distance2D(p.positionGPS);
      if(distance > 10000) {
//        std::cout << "max!!" << p_->path_ << std::endl;
        continue;
      }
      d = std::max(distance, d);
    }
  } catch(trinityLog::LogException e) {
    std::cout << "error: " << e.what() << std::endl;
  }
  return d;
}

double FalconLog::maxDistance3D() const
{
  if(!p_->read_) {
    p_->read();
  }
  double d = 0.0;
  try {
    asl::Position p0 = p_->position();

    for (auto p : p_->flightPath(500)) {
      double distance = p0.distance2D(p.positionGPS);
      double dHeight = p0.height() - p.positionGPS.height();
      distance = sqrt(distance * distance + dHeight * dHeight);
      if(distance > 10000) {
//        std::cout << "max!!" << p_->path_ << std::endl;
        continue;
      }
      d = std::max(distance, d);
    }
  } catch(trinityLog::LogException e) {
    std::cout << "error: " << e.what() << std::endl;
  }
  return d;
}

double FalconLog::maxHeight() const
{
  if(!p_->read_) {
    p_->read();
  }
  double dh = 0.0;
  try {
    for (auto p : p_->flightPath(500)) {
      double height = p.position.z();
      if(height > 50000) {
        continue;
      }
      dh = std::max(dh, height);
    }
  } catch(trinityLog::LogException e) {
    std::cout << "error: " << e.what() << std::endl;
  }
  return dh;
}
