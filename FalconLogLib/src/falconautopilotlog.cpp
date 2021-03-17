/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "falconautopilotlog.h"

#include <exception>
#include <nowide/fstream.hpp>
#include <string.h>
#include <conversions.h>

#include "falconlogbase.h"
#include "phototag.h"
#include "pathpoint.h"

FalconAutopilotLog::FalconAutopilotLog(const std::string& path) :
  FalconLogBase(path)
{
}

bool FalconAutopilotLog::read()
{
  using namespace std;

  string gpsLog = path_ + "\\GPS.LOG";
  string asctecLog = path_ + "\\ASCTEC.LOG";

  readGPSLog(gpsLog);
  readASCTECLog(asctecLog);

  read_ = true;

  return true;
}

double FalconAutopilotLog::airTime() const
{
  double airtime = 0;
  int64_t from = 0;
  bool inAir = false;
  for(auto log : asctecLog_) {
    if (log.second.data.status2 & 1) {
      if(!inAir) {
        inAir = true;
        from = log.first;
      }
    } else if(inAir) {
      inAir = false;
      airtime += log.first - from;
    }
  }
  return airtime / 1000.0;
}

asl::Position FalconAutopilotLog::position() const
{
  for(auto log : gpsLog_) {
    if (log.second.data.status & 1)
      return asl::Position(log.second.data.latitude / 10000000.0,
                           log.second.data.longitude / 10000000.0,
                           log.second.data.height / 1000.0);
  }
  return asl::Position();
}

int64_t FalconAutopilotLog::time() const
{
  for(auto log : gpsLog_) {
    if (log.second.data.status & 1)
      return log.first;
  }
  return 0;
}

std::vector<PhotoTag> FalconAutopilotLog::photoTags() const
{
  auto tags = std::vector<PhotoTag>();
  bool triggerActive = false;
  int number = 1;
  for(auto log : asctecLog_) {
    if (log.second.data.status2 & 1) {
      if (log.second.data.cam_status == 0x04) {
        if (!triggerActive) {
          triggerActive = true;
          PhotoTag pt;
          int height = gpsLog_.lower_bound(log.first)->second.data.height;
          pt.position = asl::Vector3D(0,0, log.second.data.height / 1000.0);
          pt.positionGPS = asl::Position(log.second.data.latitude_best_estimate / 10000000.0,
                                      log.second.data.longitude_best_estimate / 10000000.0,
                                      height / 1000.0);
          pt.orientation = asl::Quaternion::fromRPY(log.second.data.cam_angle_roll / 100.0,
                                                    -log.second.data.cam_angle_pitch / 100.0,
                                                    log.second.data.angle_yaw / 100.0);
          pt.rpy = asl::Vector3D(log.second.data.cam_angle_roll / 100.0,
                                 -log.second.data.cam_angle_pitch / 100.0,
                                 log.second.data.angle_yaw / 100.0);
          pt.project = "";
          pt.number = number++;
          pt.source = PhotoTag::TriggerSource_MGS;
          pt.GPSTime = log.first;
          tags.push_back(pt);
        }
      } else {
        triggerActive = false;
      }
    }
  }
  return tags;
}

std::vector<PathPoint> FalconAutopilotLog::flightPath(int) const
{
  auto path = std::vector<PathPoint>();
  for(auto log : asctecLog_) {
    if (log.second.data.status2 & 1) {
      PathPoint pp;
      int height;
      auto it = gpsLog_.lower_bound(log.first);
      if(gpsLog_.size() && it != gpsLog_.end()) {
        --it;
        int h0 = asctecLog_.lower_bound(it->first)->second.data.height;
        int dh = log.second.data.height - h0;
        int h0gps = it->second.data.height;
        height = h0gps + dh;
      } else {
        height = log.second.data.height;
      }
      pp.position = asl::Vector3D(0,0,log.second.data.height / 1000.0);
      pp.positionGPS = asl::Position(log.second.data.latitude_best_estimate / 10000000.0,
                                  log.second.data.longitude_best_estimate / 10000000.0,
                                  height / 1000.0);
      pp.orientation = asl::Quaternion::fromRPY(log.second.data.angle_roll / 100.0,
                                                log.second.data.angle_pitch / 100.0,
                                                log.second.data.angle_yaw / 100.0);
      pp.rpy = asl::Vector3D(log.second.data.angle_roll / 100.0,
                             log.second.data.angle_pitch / 100.0,
                             log.second.data.angle_yaw / 100.0);
      pp.GPSTime = log.first;
      pp.timestamp = 0;
      if(log.second.data.flightMode & autopilot::FM_POS) {
        pp.flightmode = PathPoint::FlightMode::GPS;
      } else if(log.second.data.flightMode & autopilot::FM_HEIGHT) {
        pp.flightmode = PathPoint::FlightMode::Height;
      } else if(log.second.data.flightMode & autopilot::FM_ACC) {
        pp.flightmode = PathPoint::FlightMode::Manual;
      } else {
        pp.flightmode = PathPoint::FlightMode::Unknown;
      }
      path.push_back(pp);
    }
  }
  return path;
}

bool sync(nowide::ifstream& file) {
  enum state {
    Start1,
    Start2,
    Start3
  };

  unsigned int state = Start1;
  char c;

  while(file.read(&c, 1) && !file.eof())
  {
    switch (state)
    {
    case Start1:
      if (c=='>')
        state = Start2;
      break;
    case Start2:
      if (c=='*')
        state = Start3;
      else if (c != '>')           // also sync on >>*>
        state = Start2;
      break;
    case Start3:
      if (c=='>')
        return true;
      else
        state = Start1;
      break;
    }
  }
  return false;
}

void FalconAutopilotLog::readGPSLog(std::string filename)
{
  using namespace std;
  using namespace autopilot;
  nowide::ifstream file;
  file.open(filename.c_str(), ios::binary);
  if (!file.is_open()) {
    throw string("GPS Log not found! " + string(strerror(errno)));
  }
  string header(44, '\0');
  file.read(&header[0], 44);
  if (!header.compare("AscTec FALCON 8 GPS logfile, Firmware")) {
    throw string("GPS log header doesnt match");
  }

  struct LOGFILE_GPS_TIME timeBuf;
  struct LOGFILE_GPS_DATA dataBuf;

  while (file.good()) {
    sync(file);

    file.read((char*)&timeBuf, sizeof(timeBuf));
    file.read((char*)&dataBuf, sizeof(dataBuf));

    int64_t timestamp = asl::towWeek2GpsTimestamp(timeBuf.week, timeBuf.time_of_week);
    if(gpsLog_.count(timestamp))
    {
      gpsLog_.emplace_hint(gpsLog_.end(), timestamp + 100,
                           GPSLOG(timeBuf, dataBuf));
    } else {
      gpsLog_.emplace_hint(gpsLog_.end(), timestamp,
                           GPSLOG(timeBuf, dataBuf));
    }
  }
}

void FalconAutopilotLog::readASCTECLog(std::string filename)
{
  using namespace std;
  using namespace autopilot;
  nowide::ifstream file;
  file.open(filename.c_str(), ios::binary);
  if (!file.is_open()) {
    throw string("ASCTEC Log not found! " + string(strerror(errno)));
  }
  string header(40, '\0');
  file.read(&header[0], 40);
  if (!header.compare("AscTec FALCON 8 logfile, Firmware")) {
    throw string("GPS log header doesnt match");
  }

  struct LOGFILE_GPS_TIME timeBuf;
  struct LOGFILE_LL_ATTITUDE_DATA dataBuf;

  while (file.good()) {
    sync(file);

    file.read((char*)&timeBuf, sizeof(timeBuf));
    file.read((char*)&dataBuf, sizeof(dataBuf));

    int64_t timestamp = asl::towWeek2GpsTimestamp(timeBuf.week, timeBuf.time_of_week);
    if(asctecLog_.count(timestamp))
    {
      asctecLog_.emplace_hint(asctecLog_.end(), timestamp + 100,
                              ASCTECLOG(timeBuf, dataBuf));
    } else {
      asctecLog_.emplace_hint(asctecLog_.end(), timestamp,
                              ASCTECLOG(timeBuf, dataBuf));
    }
  }
}
