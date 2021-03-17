/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef FALCONLOG_GLOBAL_H
#define FALCONLOG_GLOBAL_H

#if defined (dynamiclib)
#  if defined(FALCONLOG_LIBRARY_EXPORTING)
#    if defined _WIN32 || defined __CYGWIN__
#      define FALCONLOGSHARED_EXPORT __declspec(dllexport)
#    elif __GNUC__ >= 4
#      define FALCONLOGSHARED_EXPORT __attribute__ ((visibility ("default")))
#    endif
#  else
#    if defined _WIN32 || defined __CYGWIN__
#      define FALCONLOGSHARED_EXPORT __declspec(dllimport)
#    elif __GNUC__ >= 4
#      define FALCONLOGSHARED_EXPORT __attribute__ ((visibility ("default")))
#    endif
#  endif // defined(FALCONLOG_LIBRARY_EXPORTING)
#else
# define FALCONLOGSHARED_EXPORT
#endif // defined(dynamiclib)

#endif // FALCONLOG_GLOBAL_H
