/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2004 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


// This demonstrates some of the features of the LTISceneBuffer class,
// by decoding an image into a larger "frame" colored grey.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_scene.h"
#include "lti_sceneBuffer.h"
#include "MrSIDImageReader.h"
#include "lti_pixel.h"

LT_USE_NAMESPACE(LizardTech);

//
//  For more examples on how to use the LTISceneBuffer class see the
//    following:
//  
//  * Loading BIP data into a SceneBuffer
//     DerivedImageReader.cpp: MyReader::decodeStrip()
//
//  * Exporting a SceneBuffer to BIP
//     DerivedImageWriter.cpp: MyWriter::writeStrip()
//
//  * Accessing the BSQ bands of a SceneBuffer
//     DerivedImageFilter.cpp: MyFilter::decodeStrip()
//
//  * Allocating the BSQ band data that the SceneBuffer uses
//     DecodeMrSIDToMemory.cpp and DecodeJP2ToMemory.cpp
//

LT_STATUS SceneBuffer()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.sid"));
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));

   // Construct the buffer we're decoding into: we are going to
   // use a buffer that is 150x150 and put the image into the
   // middle of it.  Note we let the buffer allocate its own
   // storage.

   const LTIPixel &pixelProps = reader->getPixelProps();
   
   LTISceneBuffer sceneBuffer(pixelProps, 150, 150, NULL);

   // some sanity checks
   TEST_BOOL(sceneBuffer.getNumCols() == 150);
   TEST_BOOL(sceneBuffer.getNumRows() == 150);
   TEST_BOOL(sceneBuffer.getTotalNumCols() == 150);

   // fill the buffer with pink
   LTIPixel pink(reader->getPixelProps());
   pink.getSample(0).setValueUint8(255);
   pink.getSample(1).setValueUint8(128);
   pink.getSample(2).setValueUint8(128);
   sceneBuffer.fill(pink);

   // decode a 100x100 scene from the middle of the 640x480 image
   const LTIScene scene(270, 190, 100, 100, 1.0);

   // make a window into the larger buffer, at offset (25, 25)
   LTISceneBuffer subSceneBuffer(sceneBuffer, 25, 25, 100, 100);

   // some more sanity checks
   TEST_BOOL(subSceneBuffer.getNumCols() == 100);
   TEST_BOOL(subSceneBuffer.getNumRows() == 100);
   TEST_BOOL(subSceneBuffer.getTotalNumCols() == 150);

   // perform the decode
   TEST_SUCCESS(reader->read(scene, subSceneBuffer));

   // export the image in BSQ form to disk
   {
      FILE *file = fopen(OUTPUT_PATH("meg_framed_bsq.raw"), "wb");
      for(lt_uint16 band = 0; band < reader->getNumBands(); band++)
      {
         lt_uint32 bytesPerSample = pixelProps.getSample(band).getNumBytes();
         lt_uint32 numPixels = sceneBuffer.getNumCols() *
                               sceneBuffer.getNumRows();
         void *bandBuffer = sceneBuffer.getBandData(band);

         if(fwrite(bandBuffer, bytesPerSample, numPixels, file) != numPixels)
            return LT_STS_Failure;
      }
      fclose(file);
   }
   
   TEST_BOOL( Compare(OUTPUT_PATH("meg_framed_bsq.raw"), INPUT_PATH("meg_framed_bsq.raw")) );
   
   Remove(OUTPUT_PATH("meg_framed_bsq.raw"));

   reader->release();
   reader = NULL;

   return LT_STS_Success;
}

