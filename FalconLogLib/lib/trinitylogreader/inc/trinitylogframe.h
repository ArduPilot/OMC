/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef LOGFRAME_H
#define LOGFRAME_H

#include <vector>
#include <string>

#include "atostypes.h"
#include "trinitylogmessageinfo.h"

namespace trinityLog {

typedef int64_t Timestamp;

class TrinityLogFrame
{
public:
    TrinityLogFrame(struct asl::atos::MESSAGE_HEAD& head);

    double valueAsDouble(std::string subName, int arrayElem) const;

    const TrinityLogMessageInfo* info;
    const unsigned int msgId;
    const Timestamp timestamp; // in microseconds
    const unsigned short flags;
    std::vector<char> data;
};
}
#endif // LOGFRAME_H
