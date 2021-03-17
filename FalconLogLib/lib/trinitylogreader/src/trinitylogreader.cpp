/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "trinitylogreader.h"

#include <string.h>
#include <crc32.h>

#include "atostypes.h"
#include "trinitylogframe.h"
#include "trinitylogmessageinfo.h"
#include "trinitylogvariableinfo.h"

namespace trinityLog {
using namespace asl::atos;

TrinityLogReader::TrinityLogReader()
{
}

// for single file usage
TrinityLogReader::TrinityLogReader(std::string logFilename) :
  TrinityLogReader()
{
  setLogFilename(logFilename);
  setInfoFilename(logFilename);
}

TrinityLogReader::TrinityLogReader(std::string logFilename, std::string infoFilename) :
  TrinityLogReader()
{
  setLogFilename(logFilename);
  setInfoFilename(infoFilename);
}

void TrinityLogReader::setLogFilename(std::string logFilename)
{
  if (logFile_.is_open())
    logFile_.close();
  logFile_.open(logFilename.c_str(), std::ios::binary);
  if(!logFile_.is_open()) {
      //throw LogReaderException("Infofile not found! " + std::string(strerror(errno))
        //                       + ":\n\t" + infoFilename);
        std::cout << "Cannot open a file: " << logFilename << std::endl;
  }
}

void TrinityLogReader::setInfoFilename(std::string infoFilename)
{
  if (infoFile_.is_open())
    infoFile_.close();

  infoFile_.open(infoFilename.c_str(), std::ios::binary);
  if(!infoFile_.is_open()) {
    //throw LogReaderException("Infofile not found! " + std::string(strerror(errno))
      //                       + ":\n\t" + infoFilename);
      std::cout << "Cannot open a file: " << infoFilename << std::endl;
  }
  infoRead_ = false;
}

TrinityLogPtr TrinityLogReader::read()
{
  lastPos_ = 0;

  if(!infoRead_)
    info_ = readInfo();

  return readLog();
}

TrinityLogInfoPtr TrinityLogReader::readInfo()
{
  TrinityLogInfoPtr logInfo = std::make_shared<TrinityLogInfo>();

  uint16_t noMsgs;
  infoFile_.read((char*)&noMsgs, 2);

  int msgCnt = 0;

//  std::vector<std::string> msgs;

//  msgs.emplace_back("ATOS_MSG_TRIGGER_GEOTAG");
//  msgs.emplace_back("ATOS_MSG_ATTITUDE_INPUT");
//  msgs.emplace_back("ATOS_MSG_GPS_DATA");
//  msgs.emplace_back("ATOS_MSG_ATTITUDE_STATE");
//  msgs.emplace_back("ATOS_MSG_FLIGHT_MODE");
//  msgs.emplace_back("ATOS_MSG_FLIGHTTIME");
//  msgs.emplace_back("ATOS_MSG_BATTERY_STATE");

  struct ATOS_MSG_INFO msgInfo;
  struct ATOS_MSG_STRUCT_ENTRY entry;
  while(!infoFile_.eof() && msgCnt != noMsgs)
  {
    infoFile_.read((char*)&msgInfo, sizeof(msgInfo));
    if(infoFile_.gcount() != sizeof(struct ATOS_MSG_INFO)) {
      std::cout << infoFile_.gcount() << " error: " << strerror(errno) << std::endl;
      break;
    }


    TrinityLogMessageInfoPtr info(new TrinityLogMessageInfo(msgInfo));

    int offset = 0;

    for(int i=0;i<msgInfo.noOfStructEntries;i++)
    {
      infoFile_.read((char*)&entry, sizeof(struct ATOS_MSG_STRUCT_ENTRY));
      if(infoFile_.gcount() != sizeof(struct ATOS_MSG_STRUCT_ENTRY))
      {
        throw LogException("Info-file corrupt!");
      }
      if(entry.arrayCnt == 0)
        entry.arrayCnt = 1;

      TrinityLogVariableInfo variable(entry, i, offset, info.get());
      info->addVariable(variable);

      offset += varSize((varTypes)entry.varType)/8 * entry.arrayCnt;
    }

//    if(std::find(msgs.begin(), msgs.end(), std::string(msgInfo.name)) != msgs.end()) {
      logInfo->addMessageInfo(info);
//    }
    msgCnt++;
  }
  if(msgCnt != noMsgs)
    std::cout << "Attention! Message count mismatch" << std::endl;
  infoRead_ = true;

  return logInfo;
}

TrinityLogPtr TrinityLogReader::readLog()
{
  TrinityLogPtr log = std::make_shared<TrinityLog>();

  log->info_ = *info_;
  //  std::cout << "readFile: " << logFile_.fileName() << std::endl;

  log->stats_.syncCount = 0;
  log->stats_.frameCount = 0;

  log->stats_.unknownIdErrors = 0;
  log->stats_.crcErrors = 0;
  log->stats_.sizeErrors = 0;

  struct MESSAGE_HEAD head;

  ::Crc32 crc32_;

  while(!logFile_.eof())
  {
    if(!sync())
      break;
    log->stats_.syncCount++;

//    int64_t pos = (int64_t)logFile_.tellg() - 1;

    logFile_.read((char*)&head, sizeof(struct MESSAGE_HEAD));

    TrinityLogFrame frame(head);

    // check if ID is known
    if(!info_->exists(frame.msgId)){
      log->stats_.unknownIdErrors++;
//      logFile_.seekg(pos);
      continue;
    }

    // check if size is as expected
    if(head.size != info_->getMessageInfo(head.messageID)->size())
    {
      log->stats_.sizeErrors++;
//      logFile_.seekg(pos);
      continue;
    }

    frame.info = info_->getMessageInfo(head.messageID).get();

    // read data
    frame.data.resize(head.size);
    logFile_.read(frame.data.data(), head.size);

    uint32_t crc;
    logFile_.read((char*)&crc, 4);

    // check CRC

    crc32_.reset();
    // head has to be word aligned!
    crc32_.addData(&head, sizeof(head));
    crc32_.addData(frame.data.data(), head.size);

    if(crc32_.getHash() != crc)
    {
      log->stats_.crcErrors++;
//      logFile_.seekg(pos);
      continue;
    }

    log->data_[frame.msgId].insert(std::make_pair(frame.timestamp, frame));
    log->stats_.frameCount++;
  }

  if(0) {
    std::cout << "SyncCount:" << log->stats_.syncCount << std::endl;
    std::cout << "Frames:" << log->stats_.frameCount << std::endl;

    int errCnt = log->stats_.unknownIdErrors
        + log->stats_.sizeErrors
        + log->stats_.crcErrors;

    std::cout << "Errors:" << errCnt
              << "("
              << 100.0 * errCnt / (double) (errCnt + log->stats_.frameCount)
              << "%)" << std::endl;
    std::cout << "\tUnknown-id errors:" << log->stats_.unknownIdErrors << std::endl;
    std::cout << "\tUnexpected-size errors:" << log->stats_.sizeErrors << std::endl;
    std::cout << "\tCrc errors:" << log->stats_.crcErrors << std::endl;
    std::cout << "Unhandled Frames:"
              << log->stats_.syncCount
                 - log->stats_.frameCount
                 - log->stats_.crcErrors
                 - log->stats_.unknownIdErrors
                 - log->stats_.sizeErrors << std::endl;
  }
  return log;
}

bool TrinityLogReader::sync()
{
  enum state {
    Start1,
    Start2,
    Start3
  };

  unsigned int state = Start1;
  char c;

  while(logFile_.read(&c, 1) && !logFile_.eof())
  {
    switch (state)
    {
    case Start1:
      if (c=='>')
        state = Start2;
      break;
    case Start2:
      if (c=='*')
        state = Start3;
      else if (c != '>')           // also sync on >>*>
        state = Start1;
      break;
    case Start3:
      if (c=='>')
        return true;
      else
        state = Start1;
      break;
    }
  }
  return false;
}

}
