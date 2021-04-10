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


// This demonstrates how to differentiate between MrSID Raster files and those containing LiDAR data

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_scene.h"
#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS DecodeMrSIDLidar()
{
   LT_STATUS sts = LT_STS_Uninit;

   const LTFileSpec fileSpec(INPUT_PATH("mg4lidar.sid"));

   lt_uint8 gen = 0;
   bool raster = true;

   // call this to determine if a MrSID file contains LiDAR data
   TEST_SUCCESS(MrSIDImageReaderInterface::getMrSIDGeneration(fileSpec,
                                                              gen,
                                                              raster));
   TEST_BOOL(raster == false);
   
   // if we try to open this MrSID lidar file, it should fail
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_FAILURE(reader->initialize(fileSpec));

   reader->release();
   reader = NULL;
   
   return LT_STS_Success;
}
