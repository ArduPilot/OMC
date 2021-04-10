/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2004 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef LT_UTILSTATUSDATA_H
#define LT_UTILSTATUSDATA_H

// lt_lib_base
#include "lt_base.h"

LT_BEGIN_NAMESPACE(LizardTech)

class LTFileSpec;


/**
 * Interface to the error data stack.
 *
 * This class provides functions for pushing and popping data to and from the
 * "error stack", used to pass information to accompany a simple status code.
 *
 * @note For most users, the functions provided in the file
 * lt_utilStatusStrings.h are sufficient for error reporting.  Only
 * applications which need to add their own error strings or perform
 * internationalization need to use this class.
 *
 * @note This error management system is expected to be substantially changed
 * in a future release.
 *
 * Example usage:
 *
 * @verbatim
// IN APPLICATION
LTUtilStatus::initialize();
...

// IN SOME LIBRARY
LT_STATUS sts = someFunction("foo.txt",99);
if (!LT_SUCCESS(sts))
{
  LTUtilStatusData::pushBegin(sts);
  LTUtilStatusData::pushString("foo.txt");
  LTUtilStatusData::pushEnd();
  return sts;
}
...

// IN LT_LIB_UTILS
LT_STATUS sts;
char* str;
int i;
LTUtilStatusData::popBegin(sts);
// parse string for code sts to determine needed data items...
LTUtilStatusData::popString(str);
LTUtilStatusData::popEnd();
...

// IN APPLICATION
LTUtilStatusData::terminate();
 * @endverbatim
 *
 * To set error data, you must first do pushBegin(), then push zero or
 * more other data items (called Records), then do a pushEnd().
 *
 * To retrieve error data, you must first do a popBegin(), followed by
 * pops of whatever you pushed, then do a popEnd().
 *
 * Note it is assumed the popper knows the order and type of things to be
 * popped off.  (This is not really a problem, since the status code is
 * associated with a string which will contain %d, %s, etc telling him what
 * to do.  Furthermore, the only person who should ever need to use the pop
 * calls will be lt_lib_statusStrings.)
 *
 * If you do not call initialize(), the push and pop operations will do
 * nothing.  This way, apps need not use the StatusData system if they do
 * not wish to.
 */
class LTUtilStatusData
{
public:
   /**
    * initialize the error stack
    *
    * Applications should call this once prior to any other LizardTech
    * functions, to enable the error reporting system.  If not called, then
    * any calls to pushData() will be no-ops, and the integral status code
    * will map to an unintepretted string.
    *
    * @return success or failure status code
    */
   static LT_STATUS initialize();

   /**
    * cleanup
    *
    * Applications should call this once after all other LizardTech functions
    * have been called, to clean up memory.
    *
    * @return success or failure success code
    */
   static LT_STATUS terminate();

   // returns true iff initialize() was called, e.g. system is being used
   static bool isActive();

   // push data associated with an error onto the error frame stack
   // do begin(), data..., end()
   static void pushBegin(LT_STATUS status);
   static void pushContext(LT_STATUS status, const char *context);
   static void pushUint32(lt_uint32 value);
   static void pushInt32(lt_int32 value);
   static void pushString(const char *value);
   static void pushDouble(double value);
   static void pushFileSpec(const LTFileSpec &value);
   static void pushEnd();

   // get the top data item off the error frame stack
   // returns failure if the data itemis not of the specified type
   static LT_STATUS popBegin(LT_STATUS &status);
   static LT_STATUS popContext(const char *&context);
   static LT_STATUS popString(char *&value);         // caller takes ownership of string
   static LT_STATUS popEnd();

   // remove the current error frame, if any (this is like doing the pop
   // begin/end sequence, if there is an active frame)
   static void clear();

   class ErrorState;
};


LT_END_NAMESPACE(LizardTech)

#endif // LT_UTILSTATUSDATA_H
