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
#include "lidar/Version.h"

LT_USE_LIDAR_NAMESPACE

void
DecodeMrSIDRaster()
{
   int gen = 0;
   bool raster = false;

   // call this to determine whether a MrSID file is a raster file or contains LiDAR data
   bool isMrSIDImage = Version::getMrSIDFileVersion(INPUT_PATH("meg_cr20.sid"), gen, raster);

   ASSERT(isMrSIDImage);
   ASSERT(raster == true);   
   ASSERT(gen == 3);
}
