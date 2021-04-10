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

#ifndef LTI_MOSAIC_FILTER_H
#define LTI_MOSAIC_FILTER_H

// lt_lib_mrsid_core
#include "lti_imageStage.h"
#include "lti_imageStageOverrides.h"
#include "lti_imageStageManager.h"
#include "lti_sceneBuffer.h"



LT_BEGIN_NAMESPACE(LizardTech)

class LTIRTree;
class LTIEmbeddedImage;
class LTIPipelineBuilder;

/**
 * create a single mosaicked image from a set of images
 *
 * This class create a single mosaicked image from a set of images.
 *
 * The set of input images are all assumed to be in the same coordinate
 * space.  In general, all the images must have the same resolution;
 * differences that are within a small epsilon or exactly a power of two
 * are optionally allowed.
 */
#ifdef SWIG
class LTIMosaicFilter : public LTIImageStage
#else
class LTIMosaicFilter : public LTIOverrideDimensions
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
   LTI_REFERENCE_COUNTED_BOILERPLATE(LTIMosaicFilter);
public:
   enum AlphaMode
   {
      DoNothing,
      FromMerge,
      FromSourceRectangle,
   };

   /**
    * initializer
    *
    * Creates an image stage which is a mosaic of the set of input images.
    *
    * In order to create a mosaic, the ground resolution of all component
    * images must match exactly or be offset by a power of two.  For example,
    * two images having 1m resolution can be mosaicked with an image having
    * .5m resolution.  However, an image with 1ft resolution will cause an
    * error condition.  Slight variations in resolution will be automatically
    * tolerated to accommodate differences in floating-point representation.
    *
    * The "useMultires" flag will allow images to be mosaicked that do *not*
    * have compatible ground resolution.  Setting this flag will cause some
    * images in the mosaic to be resampled to achieve compatible resolution.
    *
    * Transparency settings are honored by the mosaic process.
    *
    * @param  imageStageManager manages the set of input images
    * @param  backgroundPixel   color to use for the background of the mosaic
    * @param  useMultires       resample images with incompatible resolution
    * @param  mergeMetadata     merge input-file-name, input-file-size and modifications
    */
   LT_STATUS initialize(LTIImageStageManager *imageStageManager,
                        const LTIPixel* backgroundPixel,
                        bool useMultires,
                        bool mergeMetadata = true,
                        AlphaMode alphaMode = DoNothing);

   /**
    * Check if a set of images can be mosaicked together.  The parameters to
    * this function mirror those of the constructor: this function will
    * return LT_STS_Success if and only if the images' resolutions are such
    * that a mosaic can be produced.
    *
    * @param  imageStageManager manages the set of input images
    * @param  useMultires       allow images with incompatible resolution
    * @return status code indicating success or failure
    */
   static LT_STATUS checkResolutionConformance(LTIImageStageManager *imageStageManager,
                                               bool useMultires);

   // LTIImage
   LT_STATUS getDimsAtMag(double mag,
                          lt_uint32 &width,
                          lt_uint32 &height) const;
   lt_uint32 getModifications(const LTIScene &scene) const;
   LT_STATUS getMetadataBlob(const char *type, LTIOStreamInf *&stream) const;

   // LTIImageStage
   lt_int64 getEncodingCost(const LTIScene& scene) const;
   bool getReaderScene(const LTIScene &decodeScene,
                       LTIScene &readerScene) const;
   LTIMaskSource *getMask() const;
   bool getPipelineInfo(LTIPipelineInfo info) const;

   /**
    * Set the fill method which controls how noData pixels are matched.
    * @param method  fill method enum. See LTIPixelFillMethod
    * @param fuzzyThreshold  fill method enum. See LTIPixelFillMethod
    */
   void setFillMethod(LTIPixelFillMethod method,
                      double fuzzyThreshold /*= LTISceneBuffer::DefaultFuzzyThreshold*/);
   
   /**
    * Get the fill method.  See setFillMethod().
    */
   LTIPixelFillMethod getFillMethod(void) const;
   double getFuzzyThreshold(void) const;

   /**
    * Set resampling method.
    * @param resampleMethod resampling method See LTIResampleMethod
    */
   void setResampleMethod(LTIResampleMethod resampleMethod);

   void setMagSnapThreshold(double threshold);
   
   /**
    * Get the fill method.  See setResampleMethod().
    */
   LTIResampleMethod getResampleMethod(void) const;

   void setResamplePixelCenter(bool usePixelCenter);
   
   // for LizardTech internal use only
   bool getReaderScene(lt_uint32 child,
                       const LTIScene &decodeScene,
                       LTIScene &mosaicScene,
                       LTIScene &readerScene) const;
   // for LizardTech internal use only
   bool getOverviewReaderScene(const LTIScene &decodeScene,
                               LTIScene &mosaicScene,
                               LTIScene &readerScene) const;

   // for LizardTech internal use only
   class InSceneCallback
   {
   public:
      virtual LT_STATUS found(const LTIScene &scene,
                              lt_uint32 imageNum,
                              LTIEmbeddedImage &embedded,
                              LTIImageStage &image) = 0;
   };

   // for LizardTech internal use only
   LT_STATUS forEachImageStageInScene(const LTIScene &scene,
                                      InSceneCallback &callback);

   // for LizardTech internal use only
   void setDeleteImages(bool deleteImages);
   // for LizardTech internal use only
   bool getDeleteImages(void) const;

   // for LizardTech internal use only
   LT_STATUS loadImage(lt_uint32 i,
                       LTIEmbeddedImage *&embedded,
                       LTIImageStage *&raw);
   // for LizardTech internal use only
   LT_STATUS closeImage(lt_uint32 i);

   // for LizardTech internal use only
   // does not take ownship of pipelineBuilder
   // don't call this function in inside a decodeBegin()/decodeStrip()/decodeEnd() loop
   LT_STATUS setPipelineBuilder(LTIPipelineBuilder *pipelineBuilder);

   // for LizardTech internal use only
   const LTIRTree &getRTree(void) const;
   // for LizardTech internal use only
   LTIImageStageManager &getImageStageManager(void) const;

   // for LizardTech internal use only
   bool hasOverviewImage(void) const;
   // for LizardTech internal use only
   double getOverviewMag(void) const;
   
protected:
   LT_STATUS decodeBegin(const LTIPixel &pixelProps,
                         const LTIScene &fullScene);
   LT_STATUS decodeStrip(LTISceneBuffer &stripBuffer,
                         const LTIScene &stripScene);
   LT_STATUS decodeEnd(void);

private:
   class ListImageStagesInSceneCallback;
   struct TileInfo;

   LT_STATUS setupPixelProps(TileInfo *tiles, lt_uint32 numTiles,
                             const LTIPixel *background,
                             bool mergeMetadata);

   LT_STATUS setupGeoProps(TileInfo *tiles, lt_uint32 numTiles,
                           bool useMultires);

   LT_STATUS setupBuffers(lt_uint32 numTiles);
   LT_STATUS setupOverview(void);
   
   LTIImageStageManager *m_imageStageManager;

   LTIRTree *m_rtree;

   // open image book keeping
   LTIImageStage **m_rImage;
   LTIEmbeddedImage **m_fImage;
   int *m_sImage;
   LTIPixelFillMethod m_fillMethod;
   double m_fuzzyThreshold;
   AlphaMode m_alphaMode;
   LTIResampleMethod m_resampleMethod;
   bool m_usePixelCenter;
   double m_magSnapThreshold;
   bool m_deleteImages;

   LTIPipelineBuilder *m_pipelineBuilder;

   // imageInScene
   lt_uint32 *m_inSceneList;

   LTIEmbeddedImage *m_overview;
   double m_overviewMag;
};

#ifndef DOXYGEN_EXCLUDE

class LTIPipelineBuilder
{
public:
   virtual LT_STATUS buildPipeline(lt_uint32 imageNumber,
                                   LTIImageStage *&pipeline) = 0;
};

#endif

LT_END_NAMESPACE(LizardTech)


#endif // LTI_MOSAIC_FILTER_H
