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

#include "support.h"
#include "lt_utilStatusStrings.h"

#ifndef _WIN32
#include <unistd.h>
#endif


LT_STATUS DisplayError(LT_STATUS sts,
                       const char *expression, 
                       const char *filename, int line)
{
   // strip the path in the filename
   const char *basename = strrchr(filename, '/');
   if(basename == NULL)
   {
      basename = strrchr(filename, '\\');
      if(basename == NULL)
         basename = filename;
   }

#ifdef __ANDROID__
   LOGE("*** test failed: %s:%d: %s\n", basename, line, expression);
#else
   fprintf(stderr, "*** test failed: %s:%d: %s\n", basename, line, expression);
#endif

   const char *message = getLastStatusString(sts);
   if(message != NULL)
   {
#ifdef __ANDROID__
	   LOGE("      error message: %s\n", message);
#else
	   fprintf(stderr, "      error message: %s\n", message);
#endif
   }

   return sts;
}

void
Remove(const char* file)
{
   unlink(file);
}

static long
readFile(const char* file, char*& buf)
{
   FILE* fp = fopen(file,"rb");
   if (!fp)
   {
      printf("*** file does not exist: %s\n", file);
      exit(1);
   }
   fseek(fp,0,SEEK_END);
   long len = ftell(fp);
   fseek(fp,0,SEEK_SET);
   buf = new char[len];
   int cnt = (int)fread(buf, 1, len, fp);
   TEST_BOOL(cnt==len);
   fclose(fp);
   return len;
}


int
Compare(const char* file1,
        const char* file2)
{
   return (GetNumDiffs(file1, file2) == 0);
}


int
GetNumDiffs(const char *file1,
            const char *file2)
{
   char* buf1 = NULL;
   long len1 = readFile(file1, buf1);
   char* buf2 = NULL;
   long len2 = readFile(file2, buf2);
   int diffs = 0;
   for (int i = 0; i < len1 && i < len2; i++)
      if (buf1[i] != buf2[i])
         diffs++;
   if (len1 > len2)
      diffs += (len1 - len2);
   else if (len2 > len1)
      diffs += (len2 - len1);
   delete[] buf1;
   delete[] buf2;
   return diffs;
}


void
checkCwd()
{
   FILE* fp = fopen("README-examples.txt","rb");
   if (!fp)
   {
      printf("*** example must be run from ./examples directory\n");
      exit(1);
   }
   fclose(fp);
}
