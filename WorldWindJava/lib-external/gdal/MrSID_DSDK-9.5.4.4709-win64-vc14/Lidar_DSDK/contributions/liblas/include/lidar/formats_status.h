/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2009-2009 LizardTech, Inc, 1008 Western      //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */     
/* PUBLIC */

#ifndef LTL_FORMATS_STATUS_H
#define LTL_FORMATS_STATUS_H
// See BuildHarness/status_codes.txt
//   40600 -  40799     lt_lib_lidar_formats


#define LTL_FORMATS_STATUS_BASE  40600

// Failed to create a LASWriter object.
#define LTL_STATUS_FORMATS_LIBLAS_WRITER   40601

// Failed to load LAS Header
#define LTL_STATUS_FORMATS_LIBLAS_HEADER 40602

// Failed to load VLR Header in LAS file
#define LTL_STATUS_FORMATS_LIBLAS_VLRHEADER 40603

// Failed to load a WKT
#define LTL_STATUS_FORMATS_LOAD_WKT 40604

// Failed to load class names
#define LTL_STATUS_FORMATS_LOAD_CLASSNAMES 40605

// Something wrong with the liblas reader
#define LTL_STATUS_FORMATS_LIBLAS_READER 40606

// Something wrong with a parameter
#define LTL_STATUS_FORMATS_INVALID_PARAM 40607

// Full Waveform not supported
#define LTL_STATUS_FORMATS_FWF_NOT_SUPPORTED 40608

// Something wrong with the pdal reader
#define LTL_STATUS_FORMATS_PDAL_READER 40609

// Full Waveform not supported
#define LTL_STATUS_FORMATS_COULD_NOT_CREATE_LAS_WRITER 40610

#endif // LTL_FORMATS_STATUS_H
