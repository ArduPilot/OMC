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

#ifndef __LIDAR_ATOMIC_H__
#define __LIDAR_ATOMIC_H__

#include "lidar/Base.h"

#if defined _WIN32
#define NOMINMAX
#include <windows.h>
#elif defined __GNUG__
#if __GNUC__ * 10 + __GNUC_MINOR__ < 42
#include <bits/atomicity.h>
#endif
#else
#error unknown platform
#endif

LT_BEGIN_LIDAR_NAMESPACE

#if defined _WIN32

typedef LONG AtomicInt;

inline AtomicInt AtomicIncrement(AtomicInt &value)
{
   return ::InterlockedIncrement(&value);
}

inline AtomicInt AtomicDecrement(AtomicInt &value)
{
   return ::InterlockedDecrement(&value);
}

template<typename TYPE> bool AtomicCompareAndSwap(TYPE *&value, TYPE *oldValue, TYPE *newValue)
{
   return InterlockedCompareExchangePointer(reinterpret_cast<void **>(&value), newValue, oldValue) == oldValue;
}

#elif defined __GNUG__

#if __GNUC__ * 10 + __GNUC_MINOR__ < 42
typedef _Atomic_word AtomicInt;
#else
typedef int AtomicInt;
#endif

inline AtomicInt AtomicIncrement(AtomicInt &value)
{
#if __GNUC__ * 10 + __GNUC_MINOR__ < 34
   return __exchange_and_add(&value, 1) + 1;
#elif __GNUC__ * 10 + __GNUC_MINOR__ < 42
   return __gnu_cxx::__exchange_and_add(&value, 1) + 1;
#else
   return __sync_add_and_fetch(&value, 1);
#endif
}

inline AtomicInt AtomicDecrement(AtomicInt &value)
{
#if __GNUC__ * 10 + __GNUC_MINOR__ < 34
   return __exchange_and_add(&value, -1) - 1;
#elif __GNUC__ * 10 + __GNUC_MINOR__ < 42
   return __gnu_cxx::__exchange_and_add(&value, -1) - 1;
#else
   return __sync_sub_and_fetch(&value, 1);
#endif
}

template<typename TYPE> bool AtomicCompareAndSwap(TYPE *&value, TYPE *oldValue, TYPE *newValue)
{
#if __GNUC__ * 10 + __GNUC_MINOR__ < 42
   // BUG: Old versions of GCC don't have a CAS
   if(value == oldValue)
   {
      value = newValue;
      return true;
   }
   else
      return false;
#else
   return __sync_bool_compare_and_swap(&value, oldValue, newValue);
#endif
}

#else
#error 
#endif

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_ATOMIC_H__
