/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef ASL_GLOBAL_H
#define ASL_GLOBAL_H

#if defined(ASL_LIBRARY_EXPORTING)
#  define ASLSHARED_EXPORT __declspec(dllexport)
#else
#  define ASLSHARED_EXPORT __declspec(dllimport)
#endif

#endif // ASL_GLOBAL_H
