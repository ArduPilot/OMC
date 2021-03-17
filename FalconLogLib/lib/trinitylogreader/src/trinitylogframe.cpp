/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "trinitylogframe.h"

#include <math.h>

namespace trinityLog {

using namespace asl::atos;

TrinityLogFrame::TrinityLogFrame(MESSAGE_HEAD& head) :
  msgId(head.messageID),
  timestamp(head.timeStampHHours * 30 * 60 * 1000 * 1000 + head.timeStampUs / 2),
  flags(head.flags)
{
}

double TrinityLogFrame::valueAsDouble(std::string subName, int arrayElem) const
{
  const char *dataPtr = data.data();
  size_t arrOffset = info->getVariableInfo(subName).size() * arrayElem;

  switch(info->getVariableInfo(subName).type())
  {
  case VARTYPE_QUAT:
      arrOffset /=4;
      break;
  case VARTYPE_VECTOR3F:
  case VARTYPE_VECTOR3I:
      arrOffset /=3;
      break;
  default:
      ;
  }

  const void *d = dataPtr + info->getVariableInfo(subName).dataOffset() + arrOffset;

//qDebug() << "valueAsDouble" << subName << " " << info->getVariableInfo(subName)->type();

  double res = 0.0;
  switch(info->getVariableInfo(subName).type())
  {
  case VARTYPE_INT8:
      res = (double)*(int8_t*)d;
      break;
  case VARTYPE_UINT8:
      res = (double)*(uint8_t*)d;
      break;
  case VARTYPE_INT16:
      res = (double)*(int16_t*)d;
      break;
  case VARTYPE_UINT16:
      res = (double)*(uint16_t*)d;
      break;
  case VARTYPE_INT32:
      res = (double)*(int32_t*)d;
      break;
  case VARTYPE_UINT32:
      res = (double)*(uint32_t*)d;
      break;
  case VARTYPE_INT64:
      res = (double)*(int64_t*)d;
      break;
  case VARTYPE_UINT64:
      res = (double)*(uint64_t*)d;
      break;
  case VARTYPE_SINGLE:
      res = (double)*(float*)d;
      break;
  case VARTYPE_DOUBLE:
      res = *(double*)d;
      break;
  case VARTYPE_QUAT:
  case VARTYPE_VECTOR3F:
      res = (double)*(float*)d;
      break;
  case VARTYPE_VECTOR3I:
      res = (double)*(int32_t*)d;
      break;
  case VARTYPE_UNKNOWN:
      res = 0.0;
  }
  if(isfinite(res))
      return res;
  return 0.0;
}
}

