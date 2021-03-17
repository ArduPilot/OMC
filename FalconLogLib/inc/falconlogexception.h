/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef FALCONLOGEXCEPTION
#define FALCONLOGEXCEPTION

#include "falconlog_global.h"

#include <exception>

#pragma warning( push )
#pragma warning( disable : 4275 )

class FALCONLOGSHARED_EXPORT LogNotFoundException : std::exception
{
public:
  virtual const char* what() {
    return msg_;
  }
private:
  const char msg_[18] = "Logfile not found";
};

#pragma warning(pop)

#endif // FALCONLOGEXCEPTION

