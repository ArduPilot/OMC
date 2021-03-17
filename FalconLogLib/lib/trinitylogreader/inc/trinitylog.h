/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef TRINITYLOG_H
#define TRINITYLOG_H

#include <memory>
#include <string>
#include <list>
#include <map>
#include <unordered_map>
#include <atostypes.h>

#include "trinityloginfo.h"
#include "trinitylogframe.h"
#include "trinitylogmessageinfo.h"
#include "trinitylogvariableinfo.h"
#include "logexception.h"

#include "trinitylogreader_global.h"

#pragma warning( push )
#pragma warning( disable : 4251 )


namespace trinityLog {

class TrinityLog;
typedef std::shared_ptr<TrinityLog> TrinityLogPtr;



/*!
 * \brief The TrinityLog class provides access to AscTec Triniy logfiles
 */
class TRINITYLOGREADER_EXPORT TrinityLog
{
  friend class TrinityLogReader;

public:
  /*!
   * \return vector of all timestamps of \a message
   */
  std::vector<Timestamp> getTimeStamps(std::string message) const;

  /*!
   * \return raw data of message \a name, variable \a subname at timestamp \a time
   */
  char* getRaw(std::string name, std::string subname, Timestamp time) const;

  template<typename T>
  std::vector<T> getRawVector(std::string name) const;

  /*!
   * \return data of message \a name, variable \a subname at timestamp \a time as string
   */
  std::string getAsString(std::string name, std::string subname, Timestamp time) const;

  /*!
   * \return value of message \a name, variable \a subname at timestamp \a time as double
   */
  double get(std::string name, std::string subname, Timestamp time) const;

  /*!
   * \return first nonzero value of message \a name, variable \a subname at timestamp \a time as double
   * and timestamp
   */
  std::pair<Timestamp, double> getFirstNonZero(std::string name, std::string subname) const;

  template<typename T>
  T get(std::string name, std::string subname, Timestamp time) const;
  template<typename T>
  T getLast(std::string name, std::string subname) const;
  template<typename T>
  T getFirst(std::string name, std::string subname) const;
  template<typename T>
  std::vector<T> getVector(std::string name, std::string subname) const;

  /*!
   * \return id of message \a name
   */
  int getId(std::string name) const;

private:
  TrinityLogInfo info_;

  std::map<unsigned int, std::map<Timestamp, TrinityLogFrame> > data_;

  struct {
    int frameCount;
    int syncCount;

    int crcErrors;
    int unknownIdErrors;
    int sizeErrors;
  } stats_;
};
/** \example main.cpp */

template<typename T>
std::vector<T> TrinityLog::getRawVector(std::string name) const
{
  auto msgInfo =  info_.getMessageInfo(name);

  if (msgInfo->size() != sizeof (T)) {
    throw LogException("Size doesnt match on \""+name+"\"");
  }

  std::vector<T> vec;
  for(auto frame : data_.at(msgInfo->id())) {
    vec.emplace_back(*(T*)frame.second.data.data());
  }
  return vec;
}

template<typename T>
T TrinityLog::getFirst(std::string name, std::string subname) const
{
  auto msgInfo =  info_.getMessageInfo(name);

  auto varInfo = msgInfo->getVariableInfo(subname);

  if (asl::atos::Type2VarType<T>::value != varInfo.type()) {
    throw LogException("Types dont match on \""+subname+"\" in \""+name+"\"");
  }

  if (!data_.count(msgInfo->id())) {
    throw LogException("Message \""+name+"\" has no data!");
  }

  char* rawPtr = (char*)(data_.at(msgInfo->id()).begin()->second.data.data()) + varInfo.dataOffset();
  return *((T*)rawPtr);
}

template<typename T>
T TrinityLog::getLast(std::string name, std::string subname) const
{
  auto msgInfo =  info_.getMessageInfo(name);

  auto varInfo = msgInfo->getVariableInfo(subname);

  if (asl::atos::Type2VarType<T>::value != varInfo.type()) {
    throw LogException("Types dont match on \""+subname+"\" in \""+name+"\"");
  }

  if (!data_.count(msgInfo->id())) {
    throw LogException("Message \""+name+"\" has no data!");
  }

  char* dPtr = (char*)(data_.at(msgInfo->id()).rbegin()->second.data.data());
  char* rawPtr = dPtr + varInfo.dataOffset();
  return *((T*)rawPtr);
}

template<typename T>
std::vector<T> TrinityLog::getVector(std::string name, std::string subname) const
{
  auto msgInfo =  info_.getMessageInfo(name);

  auto varInfo = msgInfo->getVariableInfo(subname);

  if (asl::atos::Type2VarType<T>::value != varInfo.type()) {
    throw LogException("Types dont match on \""+subname+"\" in \""+name+"\"");
  }

  std::vector<T> vec;
  for(auto frame : data_.at(msgInfo->id())) {
    T* val = (T*)&frame.second.data.data()[varInfo.dataOffset()];
    vec.push_back(*val);
  }
  return vec;
}

template<typename T>
T TrinityLog::get(std::string name, std::string subname, Timestamp time) const
{
  auto msgInfo =  info_.getMessageInfo(name);

  auto varInfo = msgInfo->getVariableInfo(subname);

  if (asl::atos::Type2VarType<T>::value != varInfo.type()) {
    throw LogException("Types dont match on \""+subname+"\" in \""+name+"\"");
  }

  if (!data_.count(msgInfo->id())) {
    throw LogException("Message \""+name+"\" has no data!");
  }

  auto it = data_.at(msgInfo->id()).lower_bound(time);
  if (it == data_.at(msgInfo->id()).end()) {
    it--; // returns the last item.
    //throw LogException("Message \""+name+"\" has no data at "+time);
  }

  char* rawPtr = (char*)it->second.data.data() + varInfo.dataOffset();
  return *((T*)rawPtr);
}
}

#pragma warning(pop)
#endif // TRINITYLOG_H
