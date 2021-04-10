/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2010 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef NITF_TYPES_H
#define NITF_TYPES_H

// lt_lib_base
#include "lt_base.h"

// lt_lib_mrsid_core

LT_BEGIN_LIZARDTECH_NAMESPACE
namespace Nitf {


/**
 * @file
 *
 * This file contains a number of enums, typedefs, etc, that are used within the NITF support classes.
 */

/**
 * version
 *
 * This enum is used to represent the version of the NITF file.
 */
enum Version
{
   VERSION_INVALID,
   VERSION_11, 
   VERSION_20,
   VERSION_21
};


/**
 * image/compression format
 *
 * This enum is used to represent the compression format of an image segment (IC field).
 */
enum Format
{
   FORMAT_INVALID,
   FORMAT_RAW,
   FORMAT_BILEVEL,    /**< not supported */
   FORMAT_JPEG,       /**< supported only for reading */
   FORMAT_VQ,         /**< not supported */
   FORMAT_JPEGLS,     /**< not supported */
   FORMAT_JPEGDS,     /**< not ssupported */
   FORMAT_JP2
};


/**
 * layout
 *
 * This enum is used to represent the data layout of an image segment (IMODE field).
 */
enum Layout
{
   LAYOUT_INVALID,
   LAYOUT_BLOCK,      /**<  B: interleaved by block */
   LAYOUT_PIXEL,      /**<  P: interleaved by pixel */
   LAYOUT_ROW,        /**<  R: interleaved by row (line) */
   LAYOUT_SEQ         /**<  S: sequential (not interleaved) */
};


/**
 * TRE location
 *
 * This enum is used to indicate where/how to position certain TREs.
 */
enum TRELocation
{
   TRE_OMIT,
   TRE_USER,
   TRE_EXTENDED
};


/**
 * ORIG field of J2KLRA TRE
 *
 * This enum is used for setting the ORIG field of the J2KLRA TRE.
 */
enum J2klraOrigin
{
   J2KLRA_ORIGINAL_NPJE = 0,
   J2KLRA_PARSED_NPJE = 1,
   J2KLRA_ORIGINAL_EPJE = 2,
   J2KLRA_PARSED_EPJE = 3,
   J2KLRA_ORIGINAL_TPJE = 4,
   J2KLRA_PARSED_TPJE = 5,
   J2KLRA_ORIGINAL_LPJE = 6,
   J2KLRA_PARSED_LPJE = 7,
   J2KLRA_ORIGINAL_OTHER = 8,
   J2KLRA_PARSED_OTHER = 9
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // NITF_TYPES_H
