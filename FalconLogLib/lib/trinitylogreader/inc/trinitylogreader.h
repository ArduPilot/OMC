/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef TRINITYLOGREADER_H
#define TRINITYLOGREADER_H

#include <memory>
#include <iostream>
#include <nowide/fstream.hpp>

#include "trinitylog.h"
#include "trinityloginfo.h"

#pragma warning( push )
#pragma warning( disable : 4251 )

namespace trinityLog {

class TRINITYLOGREADER_EXPORT TrinityLogReader
{
public:
  TrinityLogReader();
  TrinityLogReader(std::string logFilename);
  TrinityLogReader(std::string logFilename, std::string infoFilename);

  void setLogFilename(std::string logFilename);
  void setInfoFilename(std::string infoFilename);

  TrinityLogPtr read();

private:
  nowide::ifstream infoFile_;
  nowide::ifstream logFile_;

  bool infoRead_ = false;
  TrinityLogInfoPtr info_;

  uint64_t lastPos_;

  bool sync();
  TrinityLogInfoPtr readInfo();
  TrinityLogPtr readLog();
};
}
#pragma warning(pop)
#endif // TRINITYLOGREADER_H
