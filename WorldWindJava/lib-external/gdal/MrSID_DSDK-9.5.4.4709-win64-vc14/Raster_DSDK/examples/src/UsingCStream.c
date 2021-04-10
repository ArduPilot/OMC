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


/*
 * This demonstrates how to use the C-callable stream class.  We provide
 * functions which implement a FILE*-based stream, similar in spirit to
 * the example class derived in DerivedStream.cpp.  Note also that the
 * test workflow in this example is the same as in UsingStreams.cpp.
 */

#include "main.h"
#include "support.h"

#include "lt_ioCStream.h"

/*---------------------------------------------------------------------------
 * user-level object storing stream data
 *-------------------------------------------------------------------------*/

typedef struct StreamData
{
   FILE* fp;
   char* name;
   char* mode;
} StreamData;

static StreamData*
createStreamData(const char* name, const char* mode)
{
   StreamData *streamData = (StreamData *)calloc(1, sizeof(StreamData));
   
   streamData->fp = NULL;
   streamData->name = strdup(name);
   streamData->mode = strdup(mode);
   
   return streamData;
}

static void
deleteStreamData(StreamData *streamData)
{
   free(streamData->name);
   free(streamData->mode);
   free(streamData);
}


/*---------------------------------------------------------------------------
 * user functions for C stream
 *-------------------------------------------------------------------------*/

static LT_STATUS myOpen(void *user)
{
   StreamData *streamData = (StreamData *)user;
   streamData->fp = fopen(streamData->name, streamData->mode);
   return streamData->fp ? LT_STS_Success : LT_STS_Failure;
}

static LT_STATUS myClose(void *user)
{
   StreamData *streamData = (StreamData *)user;
   fclose(streamData->fp);
   streamData->fp = NULL;
   return LT_STS_Success;
}

static lt_uint32 myRead(void *user, lt_uint8* buf, lt_uint32 len)
{
   StreamData *streamData = (StreamData *)user;
   return (lt_uint32)fread(buf, 1, len, streamData->fp);
}

static lt_uint32 myWrite(void *user, const lt_uint8* buf, lt_uint32 len)
{
   StreamData *streamData = (StreamData *)user;
   return (lt_uint32)fwrite(buf, 1, len, streamData->fp);
}

static LT_STATUS mySeek(void *user, lt_int64 pos, LTIOSeekDir dir)
{
   StreamData *streamData = (StreamData *)user;
   int mydir = 0;

   switch (dir)
   {
      case LTIO_SEEK_DIR_BEG: mydir = SEEK_SET; break;
      case LTIO_SEEK_DIR_CUR: mydir = SEEK_CUR; break;
      case LTIO_SEEK_DIR_END: mydir = SEEK_END; break;
      default: return LT_STS_BadParam;
   };

   return fseek(streamData->fp, (long)pos, mydir) ?
         LT_STS_Failure : LT_STS_Success;
}

static lt_int64 myTell(void *user)
{
   StreamData *streamData = (StreamData *)user;
   return ftell(streamData->fp);
}

static lt_uint8 myIsEOF(void *user)
{
   StreamData *streamData = (StreamData *)user;
   return feof(streamData->fp) != 0;
}

static lt_uint8 myIsOpen(void *user)
{
   StreamData *streamData = (StreamData *)user;
   return streamData->fp != NULL;
}

static void *myDuplicate(void *user)
{
   /* this will leak -- proper implementation would keep a list of
    * allocated StreamData objects...
    */
   StreamData *streamData = (StreamData *)user;
   return createStreamData(streamData->name, streamData->mode);
}


/*---------------------------------------------------------------------------
 * show use of C streams
 *-------------------------------------------------------------------------*/

LT_STATUS UsingCStream(void)
{
   LT_STATUS sts = LT_STS_Uninit;
   lt_uint8 buf[5];

   /* make and open a file-based stream */
   StreamData *streamData = createStreamData(INPUT_PATH("meg.hdr"), "r");

   LTIOStreamH stream  = lt_ioCallbackStreamCreate(myOpen, myClose,
                                                   myRead, myWrite,
                                                   mySeek, myTell,
                                                   myIsEOF, myIsOpen,
                                                   myDuplicate,
                                                   streamData);
   TEST_BOOL(stream != NULL);

   TEST_SUCCESS(lt_ioCStreamOpen(stream));
   TEST_BOOL(streamData->fp!=NULL);

   /* read the first five bytes */
   TEST_BOOL(lt_ioCStreamRead(stream,buf, 5) == 5);
   TEST_BOOL(strncmp((char*)buf, "NROWS", 5) == 0);

   /* seek ahead two bytes */
   TEST_SUCCESS(lt_ioCStreamSeek(stream, 2, LTIO_SEEK_DIR_CUR));

   TEST_BOOL(lt_ioCStreamTell(stream) == 7);

   /* read two more bytes */
   TEST_BOOL(lt_ioCStreamRead(stream,buf, 2) == 2);
   TEST_BOOL(strncmp((char*)buf, "80", 2)==0);

   {
      /* duplicate the stream */
      LTIOStreamH stream2 = lt_ioCStreamDuplicate(stream);

      TEST_SUCCESS(lt_ioCStreamOpen(stream2));

      TEST_BOOL(lt_ioCStreamRead(stream2,buf, 5) == 5);
      TEST_BOOL(strncmp((char*)buf, "NROWS", 5)==0);

   }

   /* all done */
   TEST_SUCCESS(lt_ioCStreamClose(stream));

   deleteStreamData(streamData);

   return LT_STS_Success;
}

