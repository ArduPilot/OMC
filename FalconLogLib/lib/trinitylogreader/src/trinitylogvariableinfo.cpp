/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#include "trinitylogvariableinfo.h"

namespace trinityLog {
using namespace asl::atos;
TrinityLogVariableInfo::TrinityLogVariableInfo(ATOS_MSG_STRUCT_ENTRY &entry,
                                               int id,
                                               int offset,
                                               TrinityLogMessageInfo *parent)
{
    parent_ = parent;
    name_ = entry.name;
    description_ = entry.desc;
    unit_ = entry.unit;
    subId_ = id;
    type_ = (varTypes) entry.varType;
    arrCnt_ = entry.arrayCnt == 0 ? 1 : entry.arrayCnt;
    dataOffset_ = offset;
    size_ = varSize(type_)/8 * arrCnt_;
}
}
