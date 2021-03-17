/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef LOGEXCEPTION
#define LOGEXCEPTION

#include <string>

namespace trinityLog {

class LogReaderException {
  std::string message;
public:
  LogReaderException(const std::string& __arg){message = __arg;}
  const char* what() const {return message.c_str();}
};

class LogException {
  std::string message;
public:
  LogException(const std::string& __arg){message = __arg;}
  const char* what() const {return message.c_str();}
};
}

#endif // LOGEXCEPTION

