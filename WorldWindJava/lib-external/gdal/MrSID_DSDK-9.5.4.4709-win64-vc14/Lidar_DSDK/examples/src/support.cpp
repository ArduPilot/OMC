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
#include "lidar/FileIO.h"

#include <string.h>


void removeFile(const char *file)
{
   FileIO::deleteFile(file);
}

template<class T> class Closer
{
   DISABLE_COPY(Closer);
public:
   Closer(T &obj) : m_obj(obj) { m_obj.open(); }
   ~Closer(void) { m_obj.close(); }
private:
   T &m_obj;
};

void compareFiles(const char *file1, const char *file2)
{
   printf("compareFile:  %s and %s ", file1, file2);
   Scoped<FileIO> io1, io2;

   io1->init(file1, "r");
   io2->init(file2, "r");

   // close the files on leaving the function
   Closer<IO> c1(*io1), c2(*io2);

   IO::offset_type offset = 0;
   while(true)
   {
      const size_t size = 4096;
      lt_uint8 buf1[size], buf2[size];

      size_t len1 = io1->pread(offset, buf1, size);
      size_t len2 = io2->pread(offset, buf2, size);
      ASSERT(len1 == len2);
      if(len1 == 0 && len2 == 0)
         break;
      offset += len1;
      ASSERT(memcmp(buf1, buf2, len1) == 0);
   }
   printf("... OK\n");
}

static bool isNewLine(int c)
{
   return c == '\r' || c == '\n';
}

void compareTXTFiles(const char *file1, const char *file2)
{
   printf("compareTXTFile:  %s and %s ", file1, file2);
   Scoped<FileIO> io1, io2;
   io1->init(file1, "r");
   io2->init(file2, "r");

   StreamReader stream1, stream2;
   stream1.init(io1, false);
   stream2.init(io2, false);

   // close the files on leaving the function
   Closer<StreamReader> c1(stream1), c2(stream2);

   char *line1 = NULL, *line2 = NULL;
   size_t size1 = 0, size2 = 0;
   size_t lineNum = 0;

   try
   {
      while(true)
      {
         // read the next line in the files.
         bool ok1 = stream1.get_str(line1, size1);
         bool ok2 = stream2.get_str(line2, size2);
         lineNum += 1;

         ASSERT(ok1 == ok2);
         if(!ok1 || !ok2)
            break;

         // need to handle UNIX/Windows new lines right
         int i = 0;
         while(line1[i] != '\0' && line2[i] != '\0' && line1[i] == line2[i])
            i += 1;

         if(line1[i] == line2[i])
            continue;

         if(isNewLine(line1[i]) && isNewLine(line2[i]))
            continue;

         THROW_LIBRARY_ERROR(-1)("line %lu: don't match\n", lineNum);
      }
      XDEALLOC(line1);
      XDEALLOC(line2);
      printf("... OK\n");
   }
   catch(...)
   {
      XDEALLOC(line1);
      XDEALLOC(line2);
      throw;
   }
}


void checkCwd(void)
{
   if(!FileIO::fileExists("README.txt"))
      THROW_LIBRARY_ERROR(-1)
         ("*** example must be run from ./examples directory");
}

