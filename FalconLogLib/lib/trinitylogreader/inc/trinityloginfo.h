/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef TRINITYLOGINFO_H
#define TRINITYLOGINFO_H

#include <unordered_map>

#include "trinitylogmessageinfo.h"
#include "logexception.h"

#pragma warning( push )
#pragma warning( disable : 4251 )

namespace trinityLog {
class TrinityLogInfo;
typedef std::shared_ptr<TrinityLogInfo> TrinityLogInfoPtr;

class TRINITYLOGREADER_EXPORT TrinityLogInfo
{
public:
  TrinityLogInfo();

  void addMessageInfo(TrinityLogMessageInfoPtr info);

  TrinityLogMessageInfoPtr getMessageInfo(int id) const;
  TrinityLogMessageInfoPtr getMessageInfo(std::string name) const;
  std::list<TrinityLogMessageInfoPtr> getMessageList() const;

  bool exists(int id) const {
    return infoById_.count(id) > 0;
  }

private:
  std::unordered_map<unsigned int, TrinityLogMessageInfoPtr> infoById_;
  std::unordered_map<std::string, unsigned int> nameToId_;
};
}

#pragma warning(pop)

#endif // TRINITYLOGINFO_H
