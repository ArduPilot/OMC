/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef FALCON8TRINITYLOG_H
#define FALCON8TRINITYLOG_H

#include <string>
#include <vector>
#include <ctime>
#include <trinitylog.h>

#include "falconlogbase.h"



#endif // FALCON8TRINITYLOG_H

class Falcon8TrinityLog : public FalconLogBase
{
public:
  Falcon8TrinityLog(const std::string& path);

  bool read();

  double airTime() const;
  asl::Position position() const;
  int64_t time() const;
  std::vector<PhotoTag> photoTags() const;
  std::vector<PathPoint> flightPath(int maxPoints = 0) const;

  trinityLog::TrinityLogPtr log() {return log_;}

private:
  trinityLog::TrinityLogPtr log_;
  trinityLog::TrinityLogPtr logHP_;
};


