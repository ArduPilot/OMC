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


// This example shows some of the basic operations supported by
// the LTIOStreamInf class.

#include "main.h"
#include "support.h"

#include "lt_ioFileStream.h"
#include "lt_ioSubStream.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS UsingStreams()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make and open a file-based stream
   LTIOFileStream stream;
   TEST_SUCCESS(stream.initialize(INPUT_PATH("meg.hdr"), "r"));
   TEST_SUCCESS(stream.open());

   // read the first five bytes
   lt_uint8 buf[5];
   TEST_BOOL(stream.read(buf, 5) == 5);
   TEST_BOOL(memcmp(buf, "NROWS", 5)==0);

   // seek ahead two bytes
   TEST_SUCCESS(stream.seek(2, LTIO_SEEK_DIR_CUR));

   TEST_BOOL(stream.tell() == 7);

   // read two more bytes
   TEST_BOOL(stream.read(buf, 2) == 2);
   TEST_BOOL(memcmp(buf, "80", 2) == 0);

   // all done
   TEST_SUCCESS(stream.close());

   //
   // now test the substream class
   //
   
   TEST_SUCCESS(stream.open());

   LTIOSubStream substream;
   TEST_SUCCESS(substream.initialize(&stream, 7, 9, false));
   TEST_SUCCESS(substream.open());

   TEST_BOOL(substream.read(buf, 2) == 2);
   TEST_BOOL(memcmp(buf, "80", 2)==0);
   
   TEST_BOOL(stream.tell() == 9);
   TEST_BOOL(substream.tell() == 2);
   
   substream.close();
   stream.close();

   return LT_STS_Success;
}
