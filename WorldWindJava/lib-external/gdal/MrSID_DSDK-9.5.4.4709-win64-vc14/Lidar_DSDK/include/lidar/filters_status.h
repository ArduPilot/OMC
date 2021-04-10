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

#ifndef LTL_FILTER_STATUS_H
#define LTL_FILTER_STATUS_H
// See BuildHarness/status_codes.txt
//   40400 -  40599     lt_lib_lidar_filters


#define LTL_FILTER_STATUS_BASE  40400

// Failed because a feature is not implemented
#define LTL_STATUS_FILTER_NOTIMPL 40401

// Tried to Mosaic points with different SRSs, 
#define LTL_STATUS_FILTER_DIVERSE_SOURCES 40402

// A failure of one of the required WKTs when initializing Reprojection Filter
#define LTL_STATUS_FILTER_REPROJECT_INIT 40403

// Failed while actually doing the reprojection
#define LTL_STATUS_FILTER_REPROJECT_FAIL 40404

// Vertical Datum Transform not supported
#define LTL_STATUS_FILTER_VERTICAL_DATUM_TRANSFROM_NOT_SUPPORTED 40405

#endif // LTL_FILTERS_STATUS_H
