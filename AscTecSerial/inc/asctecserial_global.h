/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#ifndef ASCTECSERIAL_GLOBAL_H
#define ASCTECSERIAL_GLOBAL_H


#if defined(ASCTECSERIAL_LIBRARY)
#  define ASCTECSERIALSHARED_EXPORT Q_DECL_EXPORT
#else
#  define ASCTECSERIALSHARED_EXPORT Q_DECL_IMPORT
#endif

#endif // ASCTECSERIAL_GLOBAL_H
