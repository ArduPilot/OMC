/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef CONVERSIONS
#define CONVERSIONS

#include "inttypes.h"

namespace asl {

inline int64_t towWeek2GpsTimestamp(int week, int tow) {
  return (int64_t)tow + (int64_t)week * 7 * 24 * 60 * 60 * 1000;
}

inline int64_t gpsTimeStampToUtcTimeStamp(int64_t t) {
  return t + 315964800000; ///> \todo add leapseconds to the equation
}

inline int64_t towWeek2UtcTimestamp(int week, int tow) {
  return gpsTimeStampToUtcTimeStamp(towWeek2GpsTimestamp(week, tow));
}

}

#endif // CONVERSIONS

