/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef LOGMESSAGEINFO_H
#define LOGMESSAGEINFO_H

#include <memory>
#include <string>
#include <list>
#include <unordered_map>

#include "trinitylogvariableinfo.h"

#include "trinitylogreader_global.h"

#pragma warning( push )
#pragma warning( disable : 4251 )

namespace trinityLog {

class TrinityLogMessageInfo;
typedef std::shared_ptr<TrinityLogMessageInfo> TrinityLogMessageInfoPtr;
typedef std::weak_ptr<TrinityLogMessageInfo> TrinityLogMessageInfoWeak;

class TRINITYLOGREADER_EXPORT TrinityLogMessageInfo
{
public:
    TrinityLogMessageInfo(struct asl::atos::ATOS_MSG_INFO& info);

    void addVariable(TrinityLogVariableInfo& var);

    TrinityLogVariableInfo getVariableInfo(std::string name) const;
    std::list<TrinityLogVariableInfo> getVariableList() const;

    size_t size() const {return size_;}
    int id() const {return id_;}
    std::string name() const { return name_; }

private:
    int id_;
    std::string name_;
    std::string description_;
    size_t size_;
    std::unordered_map<std::string, TrinityLogVariableInfo> variableInfos;
};
}
#pragma warning(pop)

#endif // LOGMESSAGEINFO_H
