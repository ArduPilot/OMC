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


#include "lidar/Error.h"
#include "lidar/PointSource.h"

#ifdef __ANDROID__
#include <android/log.h>

#define printf(...) ((void)__android_log_print(ANDROID_LOG_INFO, "LidarExample", __VA_ARGS__))
// drop the first (stderr) argument used in UserTutorial.cpp
// #define fprintf(x, ...) (void)__android_log_print(ANDROID_LOG_INFO, "LidarExample", __VA_ARGS__))

#endif



LT_USE_LIDAR_NAMESPACE

#define ASSERT(b) do { if(!(b)) THROW_LIBRARY_ERROR(-1)(#b); } while(0)

void removeFile(const char* file);
void compareFiles(const char* file1, const char* file2);
void compareTXTFiles(const char* file1, const char* file2);
void checkCwd(void);

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
