/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "trinityloginfo.h"
#include "iostream"
namespace trinityLog {
TrinityLogInfo::TrinityLogInfo()
{

}

void TrinityLogInfo::addMessageInfo(TrinityLogMessageInfoPtr info)
{
  //std::cout << info->name() << std::endl;
  infoById_.insert({info->id(), info});
  nameToId_.insert({info->name(), info->id()});
}

TrinityLogMessageInfoPtr TrinityLogInfo::getMessageInfo(int id) const
{
  if(!exists(id))
    throw LogException("Message id not found!");
  return infoById_.at(id);
}

TrinityLogMessageInfoPtr TrinityLogInfo::getMessageInfo(std::string name) const
{
  auto it = nameToId_.find(name);
  if (it == nameToId_.end())
  {
    throw LogException("Message \""+name+"\" not found!");
  }

  return infoById_.at(it->second);
}

std::list<TrinityLogMessageInfoPtr> TrinityLogInfo::getMessageList() const
{
  std::list<TrinityLogMessageInfoPtr> list;
  for(auto v : infoById_) {
    list.push_back(v.second);
  }
  return list;
}
}
