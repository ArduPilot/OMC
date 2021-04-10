/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008-2010 LizardTech, Inc, 1008 Western      //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef __LIDAR_BASE_H__
#define __LIDAR_BASE_H__

/* this should be a vaild C header file */

#include <stddef.h>

/* Notes on data types */

/* use size_t and ptrdiff_t when dealing memory sizes and offsets */
/* use IO::offset_type when dealing with file sizes and offsets */

/* use the following types when reading and writing persistent data */

/** signed 8-bit integer */
typedef signed char     lt_int8;
/** unsigned 8-bit integer */
typedef unsigned char   lt_uint8;
/** signed 16-bit integer */
typedef signed short    lt_int16;
/** unsigned 16-bit integer */
typedef unsigned short  lt_uint16;
/** signed 32-bit integer */
typedef signed int      lt_int32;
/** unsigned 32-bit integer */
typedef unsigned int    lt_uint32;

#if defined(_MSC_VER)
   /** signed 64-bit integer */
   typedef signed __int64     lt_int64;
   /** unsigned 64-bit integer */
   typedef unsigned __int64   lt_uint64;
#else
   /** signed 64-bit integer */
   typedef long long int              lt_int64;
   /** unsigned 64-bit integer */
   typedef unsigned long long int     lt_uint64;
#endif

#ifdef __cplusplus

/* namespace macros */
#define LT_BEGIN_LIDAR_NAMESPACE namespace LizardTech {
#define LT_END_LIDAR_NAMESPACE }
#define LT_USE_LIDAR_NAMESPACE using namespace LizardTech;

LT_BEGIN_LIDAR_NAMESPACE

void *XALLOC(size_t size);
void *XREALLOC(void *ptr, size_t size);
void XDEALLOC(void *ptr);

/* Macro for disabling copy construtor and assignment opterator */

#define DISABLE_COPY(classname) \
   private: \
      classname(const classname &); \
      classname &operator=(const classname &)

#define SIMPLE_OBJECT(classname) \
   DISABLE_COPY(classname)

LT_END_LIDAR_NAMESPACE

#endif /* __cplusplus */

#ifdef __cplusplus
#define LT_BEGIN_C_NAMESPACE extern "C" {
#define LT_END_C_NAMESPACE };
#else
#define LT_BEGIN_C_NAMESPACE
#define LT_END_C_NAMESPACE
#endif

#endif /* __LIDAR_BASE_H__ */
