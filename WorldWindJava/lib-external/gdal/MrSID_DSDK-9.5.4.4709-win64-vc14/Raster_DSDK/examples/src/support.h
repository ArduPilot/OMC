/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "lt_base.h"

#ifdef __ANDROID__
#include <android/log.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "DSDK-examples", __VA_ARGS__))
#define printf(...) ((void)__android_log_print(ANDROID_LOG_INFO, "DSDK-examples", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "DSDK-examples", __VA_ARGS__))
#endif

#ifdef __cplusplus
extern "C" {
#endif

#define TEST_SUCCESS(expression) \
   do { \
      if(!LT_SUCCESS(sts = (expression))) \
         return DisplayError(sts, #expression, __FILE__, __LINE__); \
   } while(0)

#define TEST_FAILURE(expression) \
   do { \
      if(!LT_FAILURE(sts = (expression))) \
         return DisplayError(LT_STS_Failure, #expression, __FILE__, __LINE__); \
   } while(0)

#define TEST_BOOL(expression) \
   do { \
      if(!(expression)) \
         return DisplayError(LT_STS_Failure, #expression, __FILE__, __LINE__); \
   } while(0)


LT_STATUS DisplayError(LT_STATUS sts, const char *expression, const char *file, int line);

void Remove(const char* file);
int Compare(const char* file1, const char* file2);
int GetNumDiffs(const char* file1, const char* file2);
void checkCwd();

#if defined(__IPHONE_OS_VERSION_MIN_REQUIRED)
const char *INPUT_PATH(const char *filename);
#define OUTPUT_PATH(filename) ("Documents/" filename)
#elif defined(__ANDROID__)
const char *INPUT_PATH(const char *filename);
const char *OUTPUT_PATH(const char *filename);
#else
#define INPUT_PATH(filename) ("data/" filename)
#define OUTPUT_PATH(filename) (filename)
#endif

#ifdef __cplusplus
}
#endif
