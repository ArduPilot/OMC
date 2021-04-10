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

#include "support.h"
#include <stdlib.h>

void
Assert(const char* file, int line, const char* str, int cond)
{
   if (cond) return;
   printf("*** assertion failed: %s  (%s:%d)\n",
          str, file, line);
   exit(1);
}

void
checkCwd()
{
   FILE* fp = fopen("README.txt","rb");
   if (!fp)
   {
      printf("*** example must be run from ./examples directory\n");
      exit(1);
   }
   fclose(fp);
}


