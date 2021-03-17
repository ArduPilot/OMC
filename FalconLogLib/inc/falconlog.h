/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef FALCONLOG_H
#define FALCONLOG_H

#include "falconlog_global.h"

#include <memory>
#include <string>
#include <vector>
#include <position.h>
#include <trinitylog.h>

#include "pathpoint.h"
#include "phototag.h"

#pragma warning( push )
#pragma warning( disable : 4251 )

class FalconLogBase;

/*!
 * \brief FalconLog is a library to read various Falcon logfiles.
 *
 * ## Logfile types
 * The type of Falcon logfile is automatically determined.
 * For Trinity logs, the TrinityLogReader library is used.
 */
class FALCONLOGSHARED_EXPORT FalconLog
{
public:
  /*!
   * \brief FalconLog constructor
   * \param path path to directory containing the desired logfile
   *
   * Creates the Class and reads the logs from \a path
   */
  FalconLog(const std::string& path);
  ~FalconLog();

  /*!
   * \return version of logfile meta data
   */
  static int logMetaVersion();

  /*!
   * \brief Enumeration of Logfile Types
   */
  enum class LogType {
    Unknown = 0,
    Falcon8TrinityLog,
    Falcon8AutopilotLog
  };

  /*!
   * \return type of logfile
   */
  LogType logType() const;

  /*!
   * \return type of logfile as string
   * \sa logType()
   * \sa LogType
   */
  std::string logTypeString() const;

  /*!
   * \return time in seconds the Falcon is flying
   * \todo implement for trinitylog!
   */
  double airTime() const;

  /*!
   * \return the position of the Falcon at first GPS lock, (0, 0) if no lock
   */
  asl::Position position() const;

  /*!
   * \return greatest distance from start position of flight
   */
  double maxDistance() const;
  double maxDistance3D() const;

  /*!
   * \return highest height of flight
   */
  double maxHeight() const;

  /*!
   * \return time (in UTC milliseconds) of first GPS lock, 0 if no lock.
   */
  int64_t timeUTC() const;

  int photoTagCount() const;

  /*!
   * \return vector of Phototags, empty vector if no tags
   */
  std::vector<PhotoTag> photoTags() const;

  /*!
   * \return the Flightpath of the Falcon, empty vector if not flying
   */
  std::vector<PathPoint> flightPath(int maxPoints = 0) const;

  trinityLog::TrinityLogPtr log();

private:
  FalconLog();
  std::unique_ptr<FalconLogBase> p_;
  LogType logType_;
  std::string path_;
};
/** \example main.cpp */

typedef std::shared_ptr<FalconLog> LogPtr;

#pragma warning(pop)

#endif // FALCONLOG_H
