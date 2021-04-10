/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2010 LizardTech, Inc, 1008 Western           //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */    

#ifndef __GEO_TIFF_UTILS_H__
#define __GEO_TIFF_UTILS_H__

#include "lidar/Metadata.h"
#include <stdio.h>

LT_BEGIN_LIDAR_NAMESPACE

namespace GeoTIFFUtils
{
   void dumpKeys(FILE *file, const Metadata &metadata);
}

LT_END_LIDAR_NAMESPACE

#endif // __GEO_TIFF_UTILS_H__
