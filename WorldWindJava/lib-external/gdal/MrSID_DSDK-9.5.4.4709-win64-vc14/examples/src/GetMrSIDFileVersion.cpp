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


// This demonstrates how to link in both the Raster and Lidar MrSID sdk's
// in the same application

#include "support.h"

#ifndef NO_LIDAR_SDK
// lidar includes
#include "lidar/Version.h"
#endif

// raster includes
#include "lt_fileSpec.h"
#include "lti_scene.h"
#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);

void GetMrSIDFileVersionWithLidarSDK()
{
#ifndef NO_LIDAR_SDK
   try
   {
      int gen = 0;
      bool raster = false;
   
      // call this to determine whether a MrSID file is a raster file or contains LiDAR data
      bool isMrSIDImage = Version::getMrSIDFileVersion("data/meg_cr20.sid", gen, raster);

      ASSERT(isMrSIDImage);
      ASSERT(raster == true);   
      ASSERT(gen == 3);

      isMrSIDImage = Version::getMrSIDFileVersion("data/Tetons_200k.xyz.sid", gen, raster);
      ASSERT(isMrSIDImage);
      ASSERT(raster != true);
      ASSERT(gen = 4);
         
      
   }
   catch(...)
   {
      throw;
   }
#endif
}

void GetMrSIDFileVersionWithRasterSDK()
{   
   LT_STATUS sts = LT_STS_Uninit;

   const LTFileSpec fsLidar("data/Tetons_200k.xyz.sid");
   const LTFileSpec fsRaster("data/meg_cr20.sid");

   lt_uint8 gen = 0;
   bool raster = true;

   // call this to determine if a MrSID file contains LiDAR data
   sts = MrSIDImageReaderInterface::getMrSIDGeneration(fsLidar, gen, raster);
   ASSERT(LT_SUCCESS(sts));
   ASSERT(raster == false);
   ASSERT(gen == 4);
   
   sts = MrSIDImageReaderInterface::getMrSIDGeneration(fsRaster, gen, raster);
   ASSERT(LT_SUCCESS(sts));
   ASSERT(raster == true);
   ASSERT(gen == 3);

   // if we open a LiDAR file with the raster SDK MrSIDImageReader, it will fail.
   {
      MrSIDImageReader *reader = MrSIDImageReader::create();
      ASSERT(reader != NULL);

      sts = reader->initialize(fsLidar);
      ASSERT(LT_FAILURE(sts));

      reader->release();
      reader = NULL;
   }  
}

