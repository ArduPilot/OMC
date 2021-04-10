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

#ifndef __LIDAR_MUTEX_H__
#define __LIDAR_MUTEX_H__

#include "lidar/Base.h"

#ifdef _WIN32
#define NOMINMAX
#include <windows.h>
#else
#include <pthread.h>
#endif

LT_BEGIN_LIDAR_NAMESPACE

/**
 * Mutex is a cross platform wrapper for mutexes.
 */
class Mutex
{
   SIMPLE_OBJECT(Mutex);
public:
   /** Create unnamed mutex. */
   Mutex(void);
   /** Destroy mutex. */
   ~Mutex(void);

   /** Lock the mutex and block until it becomes available. */
   void lock(void);
   /** Unlock the mutex. */
   void unlock(void);
private:
#ifdef _WIN32
   HANDLE m_mutex;
#else
   pthread_mutex_t m_mutex;
#endif
};


/**
 * MutexMonitor use Resource Acquisition Is Initialization (RAII) to
 * mutex lock blocks of code.
 */
class MutexMonitor
{
   DISABLE_COPY(MutexMonitor);
public:
   /** Lock the mutex */
   MutexMonitor(Mutex &mutex) :
      m_mutex(mutex)
   {
      m_mutex.lock();
   }
   
   /** Unlock the mutex */
   ~MutexMonitor(void)
   {
      m_mutex.unlock();
   }
private:
   Mutex &m_mutex;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_MUTEX_H__
