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


// This example shows the basic image information available from an LTIImageStage.


#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_geoCoord.h"
#include "lti_utils.h"
#include "MrSIDImageReader.h"
#include "lti_pixel.h"
#include "lti_sample.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS ImageInfo()
{
   LT_STATUS sts = LT_STS_Uninit;
   
   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.sid"));
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));

   // get some information about the image
   TEST_BOOL(reader->getWidth() == 640);
   TEST_BOOL(reader->getHeight() == 480);
   TEST_BOOL(reader->getColorSpace() == LTI_COLORSPACE_RGB);
   TEST_BOOL(reader->getNumBands() == 3);
   TEST_BOOL(reader->getDataType() == LTI_DATATYPE_UINT8);
   
   TEST_BOOL(reader->getNominalImageSizeWithAlpha() == 640 * 480 * 3 * 1);
   
   TEST_BOOL(reader->getMinMagnification() == 0.0625);
   TEST_BOOL(LTIUtils::magToLevel(0.0625) == 4);
   TEST_BOOL(reader->getMaxMagnification() == 1048576);
   TEST_BOOL(LTIUtils::magToLevel(1048576) == -20); // (an arbitrarily large value)

   const LTIGeoCoord& geo = reader->getGeoCoord();
   TEST_BOOL(geo.getX() == 0.0);
   TEST_BOOL(geo.getY() == 479.0);
   TEST_BOOL(geo.getXRes() == 1.0);
   TEST_BOOL(geo.getYRes() == -1.0);

   const LTIPixel *nd = reader->getNoDataPixel();
   if (nd != NULL)
   {
      for (lt_uint16 b = 0; b < nd->getNumBands(); b++)
         TEST_BOOL(nd->getSample(b).getValueAsDouble() == 0); // black
   }
   reader->release();
   reader = NULL;
   
   return LT_STS_Success;
}
