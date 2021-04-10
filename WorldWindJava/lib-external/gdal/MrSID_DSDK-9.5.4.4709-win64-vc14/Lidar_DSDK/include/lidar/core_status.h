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

#ifndef LTL_CORE_STATUS_H
#define LTL_CORE_STATUS_H
// See BuildHarness/status_codes.txt
//  40000 -  40199     lt_lib_lidar_core


// Success
#define LTL_STATUS_CORE_OK 0

#define LTL_CORE_STATUS_BASE  40000

// Failed because a feature is not implemented
#define LTL_STATUS_CORE_NOTIMPL 40001

// Failed to write to stream
#define LTL_STATUS_CORE_WRITE 40002

// Failed to read from stream
#define LTL_STATUS_CORE_READ 40003

// Failed to read a Point
#define LTL_STATUS_CORE_READ_POINT 40004

// Failed to parse a TXT file
#define LTL_STATUS_CORE_TXT_PARSE 40005

// user requested to cancel current write operation.
#define LTL_STATUS_CORE_OPERATION_CANCELLED   40006

// user requested to cancel current write operation.
#define LTL_STATUS_CORE_INVALID_PARAM   40007


// Metadata read error
#define LTL_STATUS_CORE_METADATA_READ   40008

// Metadata write error
#define LTL_STATUS_CORE_METADATA_WRITE   40009

// Datatype mismatch
#define LTL_STATUS_CORE_DATATYPE_MISMATCH   40010

// Unsupported datatype
#define LTL_STATUS_CORE_UNSUPPORTED_DATATYPE   40011

// Unsupported channel
#define LTL_STATUS_CORE_UNSUPPORTED_CHANNEL   40012

// Unsupported version
#define LTL_STATUS_CORE_UNSUPPORTED_VERSION   40013

// RTree read error
#define LTL_STATUS_CORE_RTREE_READ   40014

// RTree write error
#define LTL_STATUS_CORE_RTREE_WRITE   40015

// RTree write but for 0 points
#define LTL_STATUS_CORE_RTREE_EMPTY   40016



#endif // LTL_CORE_STATUS_H
