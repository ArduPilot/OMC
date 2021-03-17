/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "falconlogbase.h"

#include <nowide/fstream.hpp>

#include <conversions.h>
#include <phototag.h>

bool FalconLogBase::readMeta()
{
  std::string metaFileName = path_ + "/meta.cache";
  nowide::ifstream meta(metaFileName.c_str(), std::ios::binary);

  if(!meta.is_open()) {
    return false;
  }

  int version;
  meta.read((char *)&version, sizeof(version));
  if(version == METADATA_VERSION) {
    meta.read((char *)&meta_, sizeof(meta_));
    return true;
  }

  return false;
}

void FalconLogBase::setMeta()
{
  try {
    meta_.airTime = airTime();
  } catch(...) {
    meta_.airTime =  0;
  }
  try {
    meta_.position = position();
  } catch(...) {
    meta_.position =  asl::Position();
  }
  try {
    meta_.numberOfTags = (int)photoTags().size();
  } catch(...) {
    meta_.numberOfTags = 0;
  }
  try {
    meta_.timestamp = asl::gpsTimeStampToUtcTimeStamp(time());
  } catch(...) {
    meta_.timestamp = 0;
  }
}

bool FalconLogBase::writeMeta()
{
  std::string metaFileName = path_ + "/meta.cache";
  nowide::ofstream meta(metaFileName.c_str(), std::ios::binary);

  if(meta.is_open()) {
    int version = METADATA_VERSION;
    meta.write((char *)&version, sizeof(version));
    meta.write((char *)&meta_, sizeof(meta_));
    return true;
  }

  return false;
}
