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

#ifndef LTI_IMAGE_READER_H
#define LTI_IMAGE_READER_H

// lt_lib_mrsid_core
#include "lti_imageStage.h"
#include "lti_imageStageOverrides.h"


LT_BEGIN_NAMESPACE(LizardTech)

/**
 * abstract class for implementing an image reader
 *
 * The LTIImageReader abstract class extends the LTIImageStage so that it can
 * be used as a decoder for an image format, i.e. the "end" of an image
 * pipeline.  This is the base class for such classes as the MrSIDImageReader.
 */
#ifdef SWIG
class LTIImageReader : public LTIImageStage
#else
class LTIImageReader : public LTIOverrideDimensions
                              <LTIOverridePixelProps
                              <LTIOverrideBackgroundPixel
                              <LTIOverrideGeoCoord
                              <LTIOverrideMagnification
                              <LTIOverrideIsSelective
                              <LTIOverrideStripHeight
                              <LTIOverrideDelegates
                              <LTIOverridePixelLookupTables
                              <LTIOverrideMetadata
                              <LTIImageStage> > > > > > > > > >
#endif
{
   LT_DISALLOW_COPY_CONSTRUCTOR(LTIImageReader);
public:
   // LTIImage
   LT_STATUS getDimsAtMag(double mag,
                          lt_uint32& width,
                          lt_uint32& height) const;

   lt_uint32 getModifications(const LTIScene &scene) const;
   LT_STATUS getMetadataBlob(const char *type, LTIOStreamInf *&stream) const;


   // LTIImageStage
   LTIMaskSource *getMask() const;
   bool getPipelineInfo(LTIPipelineInfo info) const;

   lt_int64 getEncodingCost(const LTIScene& scene) const;
   bool getReaderScene(const LTIScene &decodeScene,
                       LTIScene &readerScene) const;

   // override too support ImageReaders that don't support band selection
   LT_STATUS readBegin(const LTIPixel &pixelProps, const LTIScene &fullScene);
   LT_STATUS readStrip(LTISceneBuffer &buffer, const LTIScene &stripScene);
   LT_STATUS readEnd(void);

protected:
   LTIImageReader(bool supportBandSelection);

   LT_STATUS init(const LTIImage *image = NULL);

   
   LT_STATUS loadMetadataIntoObjects(const LTIMetadataDatabase &fileMetadata,
                                     LTIPixel &pixelProps,
                                     bool updatePixelProps,
                                     bool updateGeoCoord);
   virtual const char *getSourceName(void) const = 0;

protected:
   const bool m_supportBandSelection;
};


LT_END_NAMESPACE(LizardTech)


#endif // LTI_IMAGE_READER_H
