/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2004 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC - C*/

#ifndef LTI_TYPES_H
#define LTI_TYPES_H

#include "lt_base.h"
#include <stddef.h> // NULL



#ifdef LT_CPLUSPLUS
extern "C" {
#endif

/**
 * @file
 *
 * This file contains a number of enums, typedefs, etc, that are used
 * throughout the MrSID SDK.
 */


/**
 * colorspaces
 *
 * This enum is used to represent colorspaces.
 */
typedef enum LTIColorSpace
{
  LTI_COLORSPACE_INVALID       = 0x000000,
  LTI_COLORSPACE_MASK_ALPHA    = 0x010000,
  LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED = 0x020000,

  // 1-banded colorspaces
  LTI_COLORSPACE_GRAYSCALE     = 0x000101,
  LTI_COLORSPACE_PALETTE       = 0x000102,
  LTI_COLORSPACE_GRAYSCALEA = LTI_COLORSPACE_GRAYSCALE | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_GRAYSCALEA_PM = LTI_COLORSPACE_GRAYSCALEA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED,

  // 3-banded colorspaces
  LTI_COLORSPACE_RGB           = 0x000301,
  LTI_COLORSPACE_CMY           = 0x000302,
  LTI_COLORSPACE_YIQ           = 0x000303,
  LTI_COLORSPACE_RGBA = LTI_COLORSPACE_RGB | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_RGBA_PM = LTI_COLORSPACE_RGBA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED,
  LTI_COLORSPACE_CMYA = LTI_COLORSPACE_CMY | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_CMYA_PM = LTI_COLORSPACE_CMYA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED,
  LTI_COLORSPACE_YIQA = LTI_COLORSPACE_YIQ | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_YIQA_PM = LTI_COLORSPACE_YIQA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED,
  
  // 4-banded colorspaces
  LTI_COLORSPACE_RGBK          = 0x000401,
  LTI_COLORSPACE_CMYK          = 0x000402,
  LTI_COLORSPACE_YIQK          = 0x000403,
  LTI_COLORSPACE_RGBKA = LTI_COLORSPACE_RGBK | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_RGBKA_PM = LTI_COLORSPACE_RGBKA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED,
  LTI_COLORSPACE_CMYKA = LTI_COLORSPACE_CMYK | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_CMYKA_PM = LTI_COLORSPACE_CMYKA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED,
  LTI_COLORSPACE_YIQKA = LTI_COLORSPACE_YIQK | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_YIQKA_PM = LTI_COLORSPACE_YIQKA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED,
   
  // N-banded colorspaces
  LTI_COLORSPACE_MULTISPECTRAL = 0x00FF01,
  LTI_COLORSPACE_MULTISPECTRALA = LTI_COLORSPACE_MULTISPECTRAL | LTI_COLORSPACE_MASK_ALPHA,
  LTI_COLORSPACE_MULTISPECTRALA_PM = LTI_COLORSPACE_MULTISPECTRALA | LTI_COLORSPACE_MASK_ALPHA_PRE_MULTIPLIED
} LTIColorSpace;

/**
 * colors
 *
 * This enum is used to represent colors.
 */
typedef enum LTIColor
{
  LTI_COLOR_INVALID   = 0,
  LTI_COLOR_RED       = 1,
  LTI_COLOR_GREEN     = 2,
  LTI_COLOR_BLUE      = 3,
  LTI_COLOR_CYAN      = 4,
  LTI_COLOR_MAGENTA   = 5,
  LTI_COLOR_YELLOW    = 6,
  LTI_COLOR_BLACK     = 7,
  LTI_COLOR_GRAY      = 8,
  LTI_COLOR_UNKNOWN   = 9,
  LTI_COLOR_ALPHA     = 10,
  LTI_COLOR_PRE_MULTIPLIED_ALPHA = 11
} LTIColor;


/**
 * datatypes
 *
 * This enum is used to represent datatypes.
 */
typedef enum LTIDataType
{
  LTI_DATATYPE_INVALID  = 0,
  LTI_DATATYPE_UINT8    = 1,
  LTI_DATATYPE_SINT8    = 2,
  LTI_DATATYPE_UINT16   = 3,
  LTI_DATATYPE_SINT16   = 4,
  LTI_DATATYPE_UINT32   = 5,
  LTI_DATATYPE_SINT32   = 6,
  LTI_DATATYPE_FLOAT32  = 7,
  LTI_DATATYPE_FLOAT64  = 8
  // not supported at this time 
  //LTI_DATATYPE_UINT64   = 9,
  //LTI_DATATYPE_SINT64   = 10,
  //LTI_DATATYPE_COMPLEX32 = 11,   // (float32 Real, float32 Imaginary)
  //LTI_DATATYPE_COMPLEX64 = 12    // (float64 Real, float64 Imaginary)
} LTIDataType;



/**
 * well-known points
 *
 * This enum is used to represent the well-known points on a rectangle.
 *
 * @note The constant name can be mentally read as "LTI_POSITION_y_x".
 */
typedef enum LTIPosition
{
   LTI_POSITION_UPPER_LEFT     = 1,
   LTI_POSITION_UPPER_CENTER   = 2,
   LTI_POSITION_UPPER_RIGHT    = 3,
   LTI_POSITION_LOWER_LEFT     = 4,
   LTI_POSITION_LOWER_CENTER   = 5,
   LTI_POSITION_LOWER_RIGHT    = 6,
   LTI_POSITION_CENTER_LEFT    = 7,
   LTI_POSITION_CENTER         = 8,
   LTI_POSITION_CENTER_RIGHT   = 9
} LTIPosition;


/**
 * constants representing data layout
 *
 * These are used by classes like
 * LTIRawImageReader and LTIRawImageWriter.
 */
typedef enum LTILayout
{
   LTI_LAYOUT_INVALID  = 0,
   LTI_LAYOUT_BIP      = 1,   /**< band interleaved by pixel */
   LTI_LAYOUT_BSQ      = 2,   /**< band sequential */
   LTI_LAYOUT_BIL      = 3    /**< band interleaved by line */
} LTILayout;


/**
 * constants representing endianness (byte order)
 */
typedef enum LTIEndian
{
   LTI_ENDIAN_INVALID = 0,
   LTI_ENDIAN_HOST    = 1,
   LTI_ENDIAN_LITTLE  = 2,
   LTI_ENDIAN_BIG     = 3
} LTIEndian;

/**
 * constants representing resampling methods
 */

typedef enum LTIResampleMethod
{
   LTI_RESAMPLE_INVALID   = 0,
   LTI_RESAMPLE_NEAREST   = 1,
   LTI_RESAMPLE_BILINEAR  = 2,
   LTI_RESAMPLE_BICUBIC   = 3
} LTIResampleMethod;

/**
 * constants representing pixel filling methods
 */
typedef enum LTIPixelFillMethod
{
   LTI_PIXELFILL_HARD  = 0,  // use NoData
   LTI_PIXELFILL_FUZZY = 1,  // use fuzzy NoData
   LTI_PIXELFILL_COPY  = 2   // ignore NoData
} LTIPixelFillMethod;

/*
 * constants representing possible pixel modifications
 */
typedef enum LTIEncodingModification
{
   LTI_MODIFICATION_NONE              = 0x00000000,
   LTI_MODIFICATION_LOSSLESS          = 0x00000000,
   LTI_MODIFICATION_UNKNOWN           = 0x00000001,
   LTI_MODIFICATION_COMPRESSED        = 0x00000002,
   LTI_MODIFICATION_CROPPED           = 0x00000004,
   LTI_MODIFICATION_EMBEDDED          = 0x00000008,
   LTI_MODIFICATION_SCALED            = 0x00000010,
   LTI_MODIFICATION_MASKED            = 0x00000020,
   LTI_MODIFICATION_INTERPRETEDALPHA  = 0x00000040,
   LTI_MODIFICATION_REORDEREDBANDS    = 0x00000080,
   LTI_MODIFICATION_CHANGEDCOLORSPACE = 0x00000100,
   LTI_MODIFICATION_CHANGEDDATATYPE   = 0x00000200,
   LTI_MODIFICATION_ALTEREDCOLOR      = 0x00000400,
   LTI_MODIFICATION_MOSAICKED         = 0x00000800,
   LTI_MODIFICATION_REPROJECTED       = 0x00001000,
   LTI_MODIFICATION_WATERMARKED       = 0x00002000,
   LTI_MODIFICATION_OVERLAID          = 0x00004000,
   LTI_MODIFICATION_COMPRESSEDPERBAND = 0X00008000,
   LTI_MODIFICATION_QUANTIZED         = 0X00010000,
   LTI_MODIFICATION_OPAQUEALPHA       = 0x00020000,
   LTI_MODIFICATION_LOSSLESS_MASK     = LTI_MODIFICATION_OPAQUEALPHA,
   LTI_MODIFICATION_LOSSY_MASK        = ~LTI_MODIFICATION_LOSSLESS_MASK,
   LTI_MODIFICATION_ANY_MASK          = ~LTI_MODIFICATION_NONE
   
} LTIEncodingModifications;

/*
 * constants representing the constructed pipeline
 */
typedef enum LTIPipelineInfo
{
   LTI_PIPELINE_INFO_NON_LEVEL_DECODES = 1
} LTIPipelineInfo;

#ifdef LT_CPLUSPLUS
}

LT_BEGIN_NAMESPACE(LizardTech)

// fwd decls (utils)
class LTFileSpec;
class LTIOStreamInf;

// fwd decls (SDK core)
class LTIGeoCoord;
class LTIImage;
class LTIImageFilter;
class LTIImageReader;
class LTIImageStage;
class LTIImageWriter;
class LTIInterruptDelegate;
class LTIMetadataDatabase;
class LTINavigator;
class LTIPixel;
class LTIPixelLookupTable;
class LTIProgressDelegate;
class LTISample;
class LTIScene;
class LTISceneBuffer;
class LTIMaskSource;
class LTIMask;

LT_END_NAMESPACE(LizardTech)

#endif

#endif
