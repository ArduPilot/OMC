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

int main(int argc, char *argv[])
{
   try
   {
      checkCwd();

      UserTutorial_iterator();
      UserTutorial_read();

      DumpMG4Info();
      IterateOverPoints();
      DecodeMG4ToTXT();
      DecodeMrSIDRaster();

      UserTest();

      printf("passed DSDK tests\n");
      return EXIT_SUCCESS;
   }
   catch(Error &e)
   {
      ::fprintf(stderr, "Error: %s:%d:%s: %s\n",
                e.filename(), e.line(), e.function(), e.what());
   }
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
