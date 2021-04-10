/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2005 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


// This demonstrates how to decode a scene from a JPEG 2000 file to a
// (old-style) JPEG file, by constructing a pipeline consisting of a
// J2KImageReader which feeds into a JpegImageWriter.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_scene.h"
#include "J2KImageReader.h"
#include "JpegImageWriter.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS DecodeJP2ToJPG()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.jp2"));
   J2KImageReader *reader = J2KImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));

   // make the JPG writer
   JpegImageWriter writer;
   TEST_SUCCESS(writer.initialize(reader));

   // set up the output file
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("meg_cr20_magP5.jpg")));
   
   // we will decode the whole image at half resolution
   lt_uint32 w = 0, h = 0;
   TEST_SUCCESS(reader->getDimsAtMag(0.5, w, h));
   const LTIScene scene(0, 0, w, h, 0.5);

   // write the scene to the file   
   TEST_SUCCESS(writer.write(scene));

   // verify we got the right output
   TEST_BOOL(Compare(OUTPUT_PATH("meg_cr20_magP5.jpg"), INPUT_PATH("meg_cr20_magP5.jpg")));

   Remove(OUTPUT_PATH("meg_cr20_magP5.jpg"));
   
   reader->release();
   reader = NULL;

   return LT_STS_Success;
}
