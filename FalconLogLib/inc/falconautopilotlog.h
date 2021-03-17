/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef FALCONAUTOPILOTLOG_H
#define FALCONAUTOPILOTLOG_H

#include <vector>
#include <map>

#include "falconlogbase.h"
#include "autopilotlogtypes.h"

class FalconAutopilotLog : public FalconLogBase
{
public:
  FalconAutopilotLog(const std::string& path);

  bool read();

  double airTime() const;
  asl::Position position() const;
  int64_t time() const;
  std::vector<PhotoTag> photoTags() const;
  std::vector<PathPoint> flightPath(int maxPoints = 0) const;

  struct GPSLOG {
    struct autopilot::LOGFILE_GPS_TIME time;
    struct autopilot::LOGFILE_GPS_DATA data;
    GPSLOG(autopilot::LOGFILE_GPS_TIME t,
           autopilot::LOGFILE_GPS_DATA& d) :
      time(t),
      data(d) {}
  };
  std::map<int64_t, struct GPSLOG> gpsLog_;

  struct ASCTECLOG {
    struct autopilot::LOGFILE_GPS_TIME time;
    struct autopilot::LOGFILE_LL_ATTITUDE_DATA data;
    ASCTECLOG(autopilot::LOGFILE_GPS_TIME t,
           autopilot::LOGFILE_LL_ATTITUDE_DATA& d) :
      time(t),
      data(d) {}
  };
  std::map<int64_t, struct ASCTECLOG> asctecLog_;

  // other log types
private:
  void readGPSLog(std::string file);
  void readASCTECLog(std::string filename);
};

#endif // FALCONAUTOPILOTLOG_H
