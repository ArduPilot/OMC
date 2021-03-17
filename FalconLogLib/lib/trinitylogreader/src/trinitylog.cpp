/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#include "crc32.h"
#include <iostream>
#include "trinitylog.h"

namespace trinityLog {

std::vector<Timestamp> TrinityLog::getTimeStamps(std::string message) const
{
  std::vector<Timestamp> list;

  try{
      auto messageInfo = info_.getMessageInfo(message);

      if(data_.count(messageInfo->id())) {
        for(auto pair : data_.at(messageInfo->id())) {
          list.push_back(pair.first);
        }
      }
  }
  catch (LogException e){
       return list;
  }

  return list;
}

char* TrinityLog::getRaw(std::string name, std::string subname, Timestamp time) const
{
  TrinityLogMessageInfoPtr msgInfo =  info_.getMessageInfo(name);

  TrinityLogVariableInfo varInfo = msgInfo->getVariableInfo(subname);

  if (!data_.count(msgInfo->id())) {
    throw LogException("Message \""+name+"\" has no data!");
  }

  auto it = data_.at(msgInfo->id()).lower_bound(time);
  if (it == data_.at(msgInfo->id()).end()) {
    it--; // returns the last item.
    //throw LogException("Message \""+name+"\" has no data at "+time);
  }

  char* rawPtr = (char*)it->second.data.data() + varInfo.dataOffset();
  return rawPtr;
}

std::string trinityLog::TrinityLog::getAsString(std::string name, std::string subname, Timestamp time) const
{
  TrinityLogMessageInfoPtr msgInfo =  info_.getMessageInfo(name);

  TrinityLogVariableInfo varInfo = msgInfo->getVariableInfo(subname);

  if (!data_.count(msgInfo->id())) {
    throw LogException("Message \""+name+"\" has no data!");
  }

  auto it = data_.at(msgInfo->id()).lower_bound(time);
  if (it == data_.at(msgInfo->id()).end()) {
    it--; // returns the last item.
    //throw LogException("Message \""+name+"\" has no data at "+time);
  }

  char* rawPtr = (char*)it->second.data.data() + varInfo.dataOffset();
  std::string str;
  str.append(rawPtr, varInfo.size());
  return str;
}

double TrinityLog::get(std::string name, std::string subname, Timestamp time) const
{
  auto msgInfo =  info_.getMessageInfo(name);

  auto varInfo = msgInfo->getVariableInfo(subname);

  if (!data_.count(msgInfo->id())) {
    throw LogException("Message \""+name+"\" has no data!");
  }

  auto it = data_.at(msgInfo->id()).lower_bound(time);
  if (it == data_.at(msgInfo->id()).end()) {
    it--; // returns the last item.
    //throw LogException("Message \""+name+"\" has no data at "+time);
  }

  return it->second.valueAsDouble(subname, 0);
}

std::pair<Timestamp, double> TrinityLog::getFirstNonZero(std::string name, std::string subname) const
{
  TrinityLogMessageInfoPtr msgInfo =  info_.getMessageInfo(name);

  if (!data_.count(msgInfo->id())) {
    throw LogException("Message \""+name+"\" has no data!");
  }

  for(auto frame : data_.at(msgInfo->id())) {
    double val = frame.second.valueAsDouble(subname, 0);
    if(val != 0)
      return std::make_pair(frame.first, val);
  }
  return std::make_pair(0, 0.0);
}
}
