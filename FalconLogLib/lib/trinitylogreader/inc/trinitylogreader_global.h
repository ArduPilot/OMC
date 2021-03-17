/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef ASCTECPROJECTDATABASE_GLOBAL_H
#define ASCTECPROJECTDATABASE_GLOBAL_H

#if defined(dynamiclib)
#  if defined(TRINITYLOGREADER_EXPORTING)
#    if defined _WIN32 || defined __CYGWIN__
#      define TRINITYLOGREADER_EXPORT __declspec(dllexport)
#    elif __GNUC__ >= 4
#      define TRINITYLOGREADER_EXPORT __attribute__ ((visibility ("default")))
#    endif
#  else
#    if defined _WIN32 || defined __CYGWIN__
#      define TRINITYLOGREADER_EXPORT __declspec(dllimport)
#    elif __GNUC__ >= 4
#      define TRINITYLOGREADER_EXPORT __attribute__ ((visibility ("default")))
#    endif
#  endif // defined(TRINITYLOGREADER_EXPORTING)
#else
#  define TRINITYLOGREADER_EXPORT
#endif // defined(dynamiclib)

#endif // ASCTECPROJECTDATABASE_GLOBAL_H
