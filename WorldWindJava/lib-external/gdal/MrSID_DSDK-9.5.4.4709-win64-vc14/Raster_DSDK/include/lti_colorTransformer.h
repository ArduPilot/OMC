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
/* PUBLIC */

#ifndef LTI_COLORTRANSFORMER_H
#define LTI_COLORTRANSFORMER_H

// lt_lib_mrsid_core
#include "lti_imageFilter.h"
#include "lti_imageStageOverrides.h"


LT_BEGIN_NAMESPACE(LizardTech)

/**
 * change the colorspace of the image
 *
 * This class changes the colorspace of the image.
 *
 * The supported color transforms are:
 * \li from RGB to CMYK, GRAYSCALE, or YIQ
 * \li from GRAYSCALE to RGB
 * \li from CMYK to RGB, RGBK, or YIQK
 * \li from YIQ to RGB
 * \li from YIQK to CMYK
 */
#ifdef SWIG
class LTIColorTransformer : public LTIImageFilter
#else
class LTIColorTransformer : public LTIOverridePixelProps
                                   <LTIOverrideBackgroundPixel
                                   <LTIImageFilter> >
#endif
{
   LTI_REFERENCE_COUNTED_BOILERPLATE(LTIColorTransformer);
public:
   /**
    * initializer
    *
    * Creates an image stage with the given colorspace.  The sample values
    * will undergo the requisite color transform function to map from the
    * input colorspace to the output colorspace.
    *
    * @param  srcImage    the base image
    * @param  dstPixel    the desired output pixel properties
    */
   LT_STATUS initialize(LTIImageStage* srcImage,
                        const LTIPixel &dstPixel);

   static bool isSupportedTransform(const LTIPixel &srcPixel,
                                    const LTIPixel &dstPixel);

   // LTIImageStage
   virtual lt_uint32 getModifications(const LTIScene &scene) const;


   static LT_STATUS push(LTIImageStage *&pipeline, const LTIPixel &pixelProps);
   
   static LT_STATUS transformPixel(LTIPixel &newPixel, const LTIPixel &oldPixel);

   static LT_STATUS transformBuffer(LTISceneBuffer &dstData, LTISceneBuffer &srcData);

protected:
   LT_STATUS decodeBegin(const LTIPixel &pixelProps,
                         const LTIScene &fullScene);
   LT_STATUS decodeStrip(LTISceneBuffer &stripBuffer,
                         const LTIScene &stripScene);
   LT_STATUS decodeEnd(void);

private:
   LTIPixel *m_tmpDstPixel;
   LTIPixel *m_tmpSrcPixel;
   bool m_isIdentity;
};


LT_END_NAMESPACE(LizardTech)


#endif // LTI_COLORTRANSFORMER_H
