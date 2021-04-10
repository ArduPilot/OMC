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

// This demonstrates how to decode a sub-set of the bands in a MrSID file.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_scene.h"
#include "lti_pixel.h"
#include "lti_sceneBuffer.h"
#include "lti_utils.h"

#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS DecodeMrSIDBandSelection()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the image reader
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(LTFileSpec(INPUT_PATH("Tile7_rgbn_utm15.sid"))));
   
   // decode the upper left corner
   const LTIScene scene(0, 0, 256, 256, 1.0);
   
   // construct a buffer that only has the near-infrared and alpha bands
   lt_uint16 bandSelection[] = { 3, 4 };
   LTIPixel pixelProps(reader->getPixelProps(), bandSelection, 2);
   LTISceneBuffer sceneBuffer(pixelProps,
                              scene.getNumCols(),
                              scene.getNumRows(),
                              NULL);

   // perform the decode
   TEST_SUCCESS(reader->read(scene, sceneBuffer));

   // save the buffer as little endian BSQ
   if(LTIUtils::needsSwapping(pixelProps.getDataType(), LTI_ENDIAN_LITTLE))
      sceneBuffer.byteSwap();

   FILE *file = fopen(OUTPUT_PATH("Tile7_rgbn_utm15-ia.bsq"), "wb");
   lt_uint16 numBands = pixelProps.getNumBands();
   for(lt_uint16 band = 0; band < numBands; band++)
   {
      lt_uint32 bytesPerSample = pixelProps.getSample(band).getNumBytes();
      lt_uint32 numPixels = sceneBuffer.getNumCols() *
                            sceneBuffer.getNumRows();
      void *bandBuffer = sceneBuffer.getBandData(band);

      // make sure the "rowBytes" is the same width as the scene
      if(sceneBuffer.getNumCols() != sceneBuffer.getTotalNumCols())
         return LT_STS_Failure;

      if(fwrite(bandBuffer, bytesPerSample, numPixels, file) != numPixels)
         return LT_STS_Failure;
   }
   fclose(file);

   // make sure got the right thing
   TEST_BOOL(Compare(OUTPUT_PATH("Tile7_rgbn_utm15-ia.bsq"), INPUT_PATH("Tile7_rgbn_utm15-ia.bsq")));
   Remove(OUTPUT_PATH("Tile7_rgbn_utm15-ia.bsq"));

   return LT_STS_Success;
}
