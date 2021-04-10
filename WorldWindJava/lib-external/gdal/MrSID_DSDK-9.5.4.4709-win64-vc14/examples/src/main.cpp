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

#include "main.h"
#include <stdlib.h>
#include <stdio.h>

#ifndef NO_LIDAR_SDK
#include "lidar/Error.h"
LT_USE_LIDAR_NAMESPACE;
#else
#include <exception>
#endif

int main(int argc, char *argv[])
{
   try
   {
      checkCwd();

      GetMrSIDFileVersionWithRasterSDK();
      GetMrSIDFileVersionWithLidarSDK();

      UserTest();

      printf("passed DSDK tests\n");
      return EXIT_SUCCESS;
   }
#ifndef NO_LIDAR_SDK
   catch(Error &e)
   {
      ::fprintf(stderr, "Error: %s:%d:%s: %s\n",
                e.filename(), e.line(), e.function(), e.what());
   }
#endif
   catch(std::exception &e)
   {
      ::fprintf(stderr, "std::exception: %s\n", e.what());
   }
   catch(...)
   {
      ::fprintf(stderr, "unknown exception\n");
   }
   return EXIT_FAILURE;
}
