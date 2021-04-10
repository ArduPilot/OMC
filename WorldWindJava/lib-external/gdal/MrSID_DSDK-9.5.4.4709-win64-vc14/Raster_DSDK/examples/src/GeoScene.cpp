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


// This demonstrates how to use geographic coordinate information
// to make decode requests.  We will write the middle of the image
// out to a raw file.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_navigator.h"
#include "MrSIDImageReader.h"
#include "lti_rawImageWriter.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS GeoScene()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.sid"));
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));

   // create a navigator; initially it is set to the whole image
   LTINavigator nav(*reader);

   const double pixelWidth = reader->getWidth();
   const double pixelHeight = reader->getHeight();
   const double halfPixelWidth = pixelWidth / 2.0;
   const double halfPixelHeight = pixelHeight / 2.0;
   const double quarterPixelWidth = pixelWidth / 4.0;
   const double quarterPixelHeight = pixelHeight / 4.0;

   // set up a scene in the middle of the image, using geo coordinates
   const LTIGeoCoord& geo = reader->getGeoCoord();
   double geoCenterX = geo.getX() + (geo.getXRes() * halfPixelWidth);
   double geoCenterY = geo.getY() + (geo.getYRes() * halfPixelHeight);
   double ulx = geoCenterX - (geo.getXRes() * quarterPixelWidth);
   double uly = geoCenterY - (geo.getYRes() * quarterPixelHeight);
   double lrx = geoCenterX + (geo.getXRes() * quarterPixelWidth);
   double lry = geoCenterY + (geo.getYRes() * quarterPixelHeight);
   
   TEST_SUCCESS(nav.setSceneAsGeoULLR(ulx, uly, lrx, lry, 2.0));
   
   const LTIScene& scene = nav.getScene();
   
   // make the raw writer
   LTIRawImageWriter writer;
   TEST_SUCCESS(writer.initialize(reader));

   // set up the output file
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("meg_geo.raw")));
   
   // write the scene to the file   
   TEST_SUCCESS(writer.write(scene));

   TEST_BOOL(Compare(OUTPUT_PATH("meg_geo.raw"), INPUT_PATH("meg_geo.raw")));   

   Remove(OUTPUT_PATH("meg_geo.raw"));
   
   reader->release();
   reader = NULL;
      
   return LT_STS_Success;
}
