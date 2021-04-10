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


// This demonstrates how to decode a scene from a NITF file to a
// BBB (raw) file.  Note the use of the NITFReaderManager class.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_scene.h"
#include "NITFReaderManager.h"
#include "lti_bbbImageWriter.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS DecodeNITFToBBB()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg.ntf"));
   NITFReaderManager *manager = NITFReaderManager::create();
   TEST_BOOL(manager != NULL);

   TEST_SUCCESS(manager->initialize(fileSpec));

   LTIImageStage *reader = NULL;
   TEST_SUCCESS(manager->createImageStage(0, reader)); // first image segment
   TEST_BOOL(reader != NULL);

   // make the BBB writer
   LTIBBBImageWriter writer;
   TEST_SUCCESS(writer.initialize(reader));

   // set up the output file
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("meg.bip")));
   
   // we will decode the whole image
   const LTIScene scene(0, 0, 640, 480, 1.0);

   // write the scene to the file   
   TEST_SUCCESS(writer.write(scene));

   // verify we got the right output
   TEST_BOOL(Compare(OUTPUT_PATH("meg.bip"), INPUT_PATH("meg.bip")));
   Remove(OUTPUT_PATH("meg.bip"));
   Remove(OUTPUT_PATH("meg.hdr"));

   reader->release();
   reader = NULL;

   manager->release();
   manager = NULL;
   
   return LT_STS_Success;
}
