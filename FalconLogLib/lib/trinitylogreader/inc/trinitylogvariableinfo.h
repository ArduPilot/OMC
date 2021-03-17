/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef LOGVARIABLEINFO_H
#define LOGVARIABLEINFO_H

#include <string>

#include <atostypes.h>

#include "trinitylogreader_global.h"

#pragma warning( push )
#pragma warning( disable : 4251 )

namespace trinityLog {

class TrinityLogMessageInfo;

class TRINITYLOGREADER_EXPORT TrinityLogVariableInfo
{
public:
    TrinityLogVariableInfo(struct asl::atos::ATOS_MSG_STRUCT_ENTRY& entry,
                           int id,
                           int offset,
                           TrinityLogMessageInfo* parent);
    std::string name() const {return name_;}
    size_t size() const {return size_;}
    asl::atos::varTypes type() const {return type_;}
    int dataOffset() const {return dataOffset_;}
    int subId() {return subId_;}

private:
    TrinityLogMessageInfo* parent_;
    std::string name_;
    std::string description_;
    std::string unit_;
    int subId_;
    asl::atos::varTypes type_;
    int arrCnt_;
    size_t size_;
    int dataOffset_;
};
}

#pragma warning(pop)

#endif // LOGVARIABLEINFO_H
