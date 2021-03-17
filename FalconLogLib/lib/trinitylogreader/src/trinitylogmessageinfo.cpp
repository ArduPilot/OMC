/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "trinitylogmessageinfo.h"
#include "logexception.h"

namespace trinityLog {
using namespace asl::atos;
TrinityLogMessageInfo::TrinityLogMessageInfo(ATOS_MSG_INFO &info)
{
    this->description_ = info.desc;
    this->id_ = info.msgId;
    this->name_ = info.name;
    this->size_ = 0;
}

void TrinityLogMessageInfo::addVariable(TrinityLogVariableInfo& var)
{
  variableInfos.insert({var.name(), var});
  size_ += var.size();
}

TrinityLogVariableInfo TrinityLogMessageInfo::getVariableInfo(std::string name) const
{
  auto it = variableInfos.find(name);
  if(it == variableInfos.end())
      throw LogException("Variable \""+name+"\" not found in \""+name_+"\"");

    return it->second;
}

std::list<TrinityLogVariableInfo> TrinityLogMessageInfo::getVariableList() const
{
  std::list<TrinityLogVariableInfo> list;
  for(auto v : variableInfos)
    list.push_back(v.second);
  return list;
}
}
