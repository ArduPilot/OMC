/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008-2010 LizardTech, Inc, 1008 Western      //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */     
/* PUBLIC */

#ifndef __LIDAR_KERNEL_STATUS_H__
#define __LIDAR_KERNEL_STATUS_H__

#define LTL_KERNEL_STATUS_BASE  40200

#define LTL_KERNEL_STATUS_CHUNK_HEADER_READ        40201
#define LTL_KERNEL_STATUS_CHUNK_HEADER_WRITE       40202

#define LTL_KERNEL_STATUS_WAVELET_HEADER_READ      40203
#define LTL_KERNEL_STATUS_WAVELET_HEADER_WRITE     40204
#define LTL_KERNEL_STATUS_INDEX_READ               40205
#define LTL_KERNEL_STATUS_INDEX_WRITE              40206
#define LTL_KERNEL_STATUS_SUBBAND_READ             40207
#define LTL_KERNEL_STATUS_SUBBAND_WRITE            40208
#define LTL_KERNEL_STATUS_SUBBLOCK_READ            40207
#define LTL_KERNEL_STATUS_SUBBLOCK_WRITE           40208

#define LTL_KERNEL_STATUS_FILE_HEADER_READ         40209
#define LTL_KERNEL_STATUS_FILE_HEADER_WRITE        40210

#define LTL_KERNEL_STATUS_BAD_VERSION              40211

#define LTL_KERNEL_STATUS_METADATA_READ            40212
#define LTL_KERNEL_STATUS_METADATA_WRITE           40213
#define LTL_KERNEL_STATUS_WKT_READ                 40214
#define LTL_KERNEL_STATUS_WKT_WRITE                40215
#define LTL_KERNEL_STATUS_CLASS_NAMES_READ         40216
#define LTL_KERNEL_STATUS_CLASS_NAMES_WRITE        40217

#define LTL_KERNEL_STATUS_BAD_DATATYPE             40218

#define LTL_KERNEL_STATUS_EXTENDED_HEADER_READ     40219
#define LTL_KERNEL_STATUS_EXTENDED_HEADER_WRITE    40220

#define LTL_KERNEL_STATUS_DEFLATE_ERROR            40230
#define LTL_KERNEL_STATUS_INFLATE_ERROR            40231

#define LTL_KERNEL_STATUS_SUBBLOCK_SIZE            40232

#define LTL_KERNEL_STATUS_FILE_EMPTY               40233




#endif // __LIDAR_KERNEL_STATUS_H__
