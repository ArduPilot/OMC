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


// This example shows the use of a complex image pipeline: we will take an
// RGB image, convert it to greyscale, crop it, and write it out to disk.
// The input and output formats are both raw.

#include "main.h"
#include "support.h"

#include "lti_pixel.h"
#include "lti_scene.h"
#include "lti_rawImageReader.h"
#include "lti_colorTransformer.h"
#include "lti_cropFilter.h"
#include "lti_rawImageWriter.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS Pipeline()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the raw image reader
   const LTIPixel inputProps(LTI_COLORSPACE_RGB, 3, LTI_DATATYPE_UINT8);
   LTIRawImageReader *reader = LTIRawImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(INPUT_PATH("meg.bip"), inputProps, 640, 480));

   // make the RGB -> greyscale filter
   LTIColorTransformer *colorFilter = LTIColorTransformer::create();
   TEST_BOOL(colorFilter != NULL);

   const LTIPixel grayPixel(LTI_COLORSPACE_GRAYSCALE, 1, reader->getDataType());
   TEST_SUCCESS(colorFilter->initialize(reader, grayPixel));

   // prove the color transform worked
   TEST_BOOL(colorFilter->getColorSpace() == LTI_COLORSPACE_GRAYSCALE);

   // crop it to remove the outer 20 pixels on each edge
   LTICropFilter *cropFilter = LTICropFilter::create();
   TEST_BOOL(cropFilter != NULL);

   TEST_SUCCESS(cropFilter->initialize(colorFilter, 20, 20, 600, 440));

   // prove the crop worked
   TEST_BOOL(cropFilter->getWidth() == 600);
   TEST_BOOL(cropFilter->getHeight() == 440);
   
   // make the raw writer
   LTIRawImageWriter writer;
   TEST_SUCCESS(writer.initialize(cropFilter));

   // set up the output file
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("meg_filter.raw")));
   
   // we will use the whole (cropped) image
   const LTIScene scene(0, 0, 600, 440, 1.0);

   // write the scene to the file   
   TEST_SUCCESS(writer.write(scene));

   // verify we got the right output
   TEST_BOOL(Compare(OUTPUT_PATH("meg_filter.raw"), INPUT_PATH("meg_filter.raw")));

   Remove(OUTPUT_PATH("meg_filter.raw"));

   cropFilter->release();
   cropFilter = NULL;

   colorFilter->release();
   colorFilter = NULL;

   reader->release();
   reader = NULL;
   
   return LT_STS_Success;
}
