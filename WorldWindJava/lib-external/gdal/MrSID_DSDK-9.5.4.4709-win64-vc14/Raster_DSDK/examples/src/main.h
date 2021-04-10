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

#include "lt_base.h"


LT_STATUS DecodeJP2ToBBB();
LT_STATUS DecodeJP2ToJPG();
LT_STATUS DecodeJP2ToMemory();

LT_STATUS DecodeMrSIDBandSelection();
LT_STATUS DecodeMrSIDToMemory();
LT_STATUS DecodeMrSIDLidar();
LT_STATUS DecodeMrSIDToRaw();
LT_STATUS DecodeMrSIDToTIFF();

LT_STATUS DecodeNITFToBBB();

LT_STATUS DerivedImageFilter();
LT_STATUS DerivedImageReader();
LT_STATUS DerivedImageWriter();
LT_STATUS DerivedStream();

LT_STATUS ErrorHandling();
LT_STATUS GeoScene();
LT_STATUS ImageInfo();
LT_STATUS InterruptDelegate();
LT_STATUS MetadataDump();
LT_STATUS Pipeline();
LT_STATUS ProgressDelegate();
LT_STATUS SceneBuffer();
LT_STATUS Mosaic();
LT_STATUS UserTest();

#ifdef __cplusplus
extern "C" {
#endif

LT_STATUS UsingCInterface(void);
LT_STATUS UsingCStream(void);

#ifdef __cplusplus
}
#endif

LT_STATUS UsingStreams();
