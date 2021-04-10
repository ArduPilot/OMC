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
 * This demonstrates how to use the C API to decode a scene from a
 * MrSID image.
 */

#include "main.h"
#include "support.h"

#include "ltic_api.h"


LT_STATUS UsingCInterface(void)
{
   LT_STATUS sts = LT_STS_Uninit;
   LTICImageH image = NULL;

   lt_uint8 redBand[4];
   lt_uint8 greenBand[4];
   lt_uint8 blueBand[4];
   void *bandData[3] = { redBand, greenBand, blueBand };

   TEST_SUCCESS(ltic_openMrSIDImageFile(&image, INPUT_PATH("meg_cr20.sid")));

   TEST_BOOL(ltic_getWidth(image) == 640);
   TEST_BOOL(ltic_getHeight(image) == 480);
   TEST_BOOL(ltic_getNumBands(image) == 3);
   TEST_BOOL(ltic_getColorSpace(image) == LTI_COLORSPACE_RGB);
   TEST_BOOL(ltic_getDataType(image) == LTI_DATATYPE_UINT8);

   /* just read the upper-left 4 pixels */
   TEST_SUCCESS(ltic_decode(image, 0, 0, 2, 2, 1.0, bandData));

   TEST_BOOL(redBand[0] == 17);
   TEST_BOOL(redBand[1] == 18);
   TEST_BOOL(redBand[2] == 16);
   TEST_BOOL(redBand[3] == 17);
   TEST_BOOL(greenBand[0] == 29);
   TEST_BOOL(greenBand[1] == 28);
   TEST_BOOL(greenBand[2] == 26);
   TEST_BOOL(greenBand[3] == 26);
   TEST_BOOL(blueBand[0] == 20);
   TEST_BOOL(blueBand[1] == 20);
   TEST_BOOL(blueBand[2] == 18);
   TEST_BOOL(blueBand[3] == 19);

   TEST_SUCCESS(ltic_closeImage(image));

   return LT_STS_Success;
}
