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


// This demonstrates how to derive your own stream class.  This example
// stream takes a standard FILE* and wraps the LTIOStreamInf stream interface
// around it.


#include "main.h"
#include "support.h"

#include "lt_base.h"
#include "lt_ioStreamInf.h"

LT_USE_NAMESPACE(LizardTech);


//---------------------------------------------------------------------------
// simple stdio/FILE* stream
//---------------------------------------------------------------------------


class MyStream : public LTIOStreamInf
{
public:
   MyStream()  :
      m_file(NULL)
   {
   }

   ~MyStream()
   {
   }

   LT_STATUS initialize(FILE* fp)
   {
      m_file = fp;
      return m_file != NULL ? LT_STS_Success : LT_STS_Failure;
   }

	bool isEOF()
   {
      return feof(m_file) != 0;
   }

	bool isOpen()
   {
      return (m_file != NULL);
   }
	
	LT_STATUS open()
   {
      return m_file != NULL ? LT_STS_Success : LT_STS_Failure;
   }

	LT_STATUS close()
   {
      return LT_STS_Success;
   }

   lt_uint32 read(lt_uint8 *pDest, lt_uint32 numBytes)
   {
      return (lt_uint32)fread(pDest, sizeof(lt_uint8), numBytes, m_file);
   }

   lt_uint32 write(const lt_uint8 *pSrc, lt_uint32 numBytes)
   {
      return (lt_uint32)fwrite(pSrc, sizeof(lt_uint8), numBytes, m_file);
   }

   LT_STATUS seek(lt_int64 offset, LTIOSeekDir origin)
   {
      int stdOrigin;
      switch (origin)
      {
         case (LTIO_SEEK_DIR_BEG):
            stdOrigin = SEEK_SET;
            break;
            
         case (LTIO_SEEK_DIR_CUR):
            stdOrigin =  SEEK_CUR;
            break;
            
         case (LTIO_SEEK_DIR_END):
            stdOrigin = SEEK_END;
            break;
            
         default:
            return LT_STS_Failure;
      }
      
      // stdio doesn't support 64-bit files
      if (offset > LT_LONG_MAX || offset < LT_LONG_MIN)
         return LT_STS_Failure;

      if(::fseek(m_file, (long)offset, stdOrigin) == 0)
         return LT_STS_Success;
      else
         return LT_STS_Failure;
   }

   lt_int64 tell()
   {
      return ftell(m_file);
   }

	LTIOStreamInf* duplicate()
   {
      // not supported by this class
      return NULL;
   }

	LT_STATUS getLastError() const
   {
      if(ferror(m_file))
         return LT_STS_Failure; //return errno;
      else
         return LT_STS_Success;
   }
	
	const char* getID() const
   {
      return "my_stdio_stream:";
   }
   
private:
	FILE* m_file;
};


//---------------------------------------------------------------------------

LT_STATUS DerivedStream()
{
   LT_STATUS sts = LT_STS_Uninit;

   // read in a file using the stdio stream, then write it
   // to disk the same way
   
#if defined(LT_OS_WIN32) || defined(LT_OS_WIN64)
   const lt_uint32 len = 44;
#else
   const lt_uint32 len = 40;
#endif
   lt_uint8* buf = new lt_uint8[len];

   {
      FILE* inFP = fopen(INPUT_PATH("meg.hdr"), "rb");
      MyStream inStream;
      TEST_SUCCESS(inStream.initialize(inFP));
      TEST_SUCCESS(inStream.open());
      TEST_BOOL(inStream.read(buf, len) == len);
      inStream.close();
      fclose(inFP);
   }

   {
      FILE* outFP = fopen(OUTPUT_PATH("foo"), "wb");
      MyStream outStream;
      TEST_SUCCESS(outStream.initialize(outFP));
      TEST_SUCCESS(outStream.open());
      TEST_BOOL(outStream.write(buf, len) == len);
      outStream.close();
      fclose(outFP);
   }

   delete [] buf;

   // verify we got the right output
   TEST_BOOL(Compare(OUTPUT_PATH("foo"), INPUT_PATH("meg.hdr")));
   Remove(OUTPUT_PATH("foo"));

   return LT_STS_Success;
}
