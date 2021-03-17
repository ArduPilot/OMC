/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef FALCONLOGBASE_H
#define FALCONLOGBASE_H

#include <string>
#include <vector>
#include <ctime>
#include <position.h>

class PhotoTag;
class PathPoint;

static const int METADATA_VERSION = 91;

struct LOGMETA {
  double airTime;
  int numberOfTags;
  int64_t timestamp;
  asl::Position position;
};

class FalconLogBase
{
  friend class FalconLog;
public:
  FalconLogBase(const std::string& path) :
    path_(path),
    read_(false),
    readHP_(false)
  {}
  virtual ~FalconLogBase() {}

  bool readMeta();
  void setMeta();
  bool writeMeta();

  virtual bool read() = 0;

  virtual double airTime() const = 0;
  virtual asl::Position position() const = 0;
  virtual int64_t time() const = 0;
  virtual std::vector<PhotoTag> photoTags() const = 0;
  virtual std::vector<PathPoint> flightPath(int maxPoints = 0) const = 0;

protected:
  std::string path_;
  bool read_;
  bool readHP_;
  struct LOGMETA meta_;
};

#endif // FALCONLOGBASE_H
