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

#ifndef LTI_IMAGEFILTER_H
#define LTI_IMAGEFILTER_H

// lt_lib_mrsid_core
#include "lti_imageStage.h"
#include "lti_imageStageOverrides.h"

LT_BEGIN_NAMESPACE(LizardTech)

/**
 * abstract class for implementing an image transform
 *
 * The LTIImageFilter abstract class extends the LTIImageStage so that it can
 * be used as an intermediate stage in an image pipeline.  This is the base
 * class for such classes as the colorspace transformer, image cropper, etc.
 */
class LTIImageFilter : public LTIImageStage
{
   LTI_REFERENCE_COUNTED_BOILERPLATE_BASE(LTIImageFilter);
public:
   // LTIImage
   lt_uint32 getWidth() const;
   lt_uint32 getHeight() const;
   LT_STATUS getDimsAtMag(double mag,
                          lt_uint32 &width,
                          lt_uint32 &height) const;
   const LTIPixel &getPixelProps() const;
   const LTIPixel *getBackgroundPixel() const;
   const LTIPixel *getNoDataPixel() const;
   const LTIPixelLookupTable *getPixelLookupTable() const;
   const LTIPixel &getMinDynamicRange() const;
   const LTIPixel &getMaxDynamicRange() const;
   const LTIGeoCoord &getGeoCoord() const;
   bool isGeoCoordImplicit() const;
   const LTIMetadataDatabase &getMetadata() const;
   double getMinMagnification() const;
   double getMaxMagnification() const;
   bool isSelective() const;
   lt_uint32 getModifications(const LTIScene &scene) const;
   LT_STATUS getMetadataBlob(const char *type, LTIOStreamInf *&stream) const;



   // LTIImageStage
   void setProgressDelegate(LTIProgressDelegate* delegate);
   LTIProgressDelegate* getProgressDelegate() const;
   void setInterruptDelegate(LTIInterruptDelegate* delegate);
   LTIInterruptDelegate* getInterruptDelegate() const;
   lt_uint32 getStripHeight() const;
   LT_STATUS setStripHeight(lt_uint32 stripHeight);
   lt_int64 getEncodingCost(const LTIScene& scene) const;
   bool getReaderScene(const LTIScene &decodeScene,
                       LTIScene &readerScene) const;

   LT_STATUS overrideBackgroundPixel(const LTIPixel *backgroundPixel);
   LT_STATUS overrideNoDataPixel(const LTIPixel *nodataPixel);
   LT_STATUS overrideGeoCoord(const LTIGeoCoord &geoCoord);
   LT_STATUS overrideDynamicRange(const LTIPixel& drmin,
                                  const LTIPixel& drmax);
   LT_STATUS overridePixelBPS(lt_uint8);
   LT_STATUS overridePixelLookupTable(const LTIPixelLookupTable* pixelLookupTable);
   LT_STATUS overrideMetadata(const LTIMetadataDatabase &metadata);
   LTIMaskSource *getMask() const;
   bool getPipelineInfo(LTIPipelineInfo info) const;

   LTIImageStage *getPreviousStage(void) const;
protected:
   LT_STATUS decodeBegin(const LTIPixel &pixelProps,
                         const LTIScene &fullScene);
   LT_STATUS decodeStrip(LTISceneBuffer &stripBuffer,
                         const LTIScene &stripScene);
   LT_STATUS decodeEnd(void);

protected:
   LT_STATUS init(LTIImageStage *previousStage);

   // HACK: use when the filter owns resources needed by the previousStage
   void releasePreviousStageNow(void);

   // Changes the prev pointer of this filter.  Probably not a good idea.
   void changePreviousStage(LTIImageStage* newPref);

private:
   LTIImageStage* m_previousStage;
};


LT_END_NAMESPACE(LizardTech)

#endif // LTI_IMAGEFILTER_H
