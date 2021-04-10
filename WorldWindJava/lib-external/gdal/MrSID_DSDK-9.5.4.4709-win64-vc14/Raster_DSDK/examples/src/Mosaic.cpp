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


// This demonstrates some of the features of the LTISceneBuffer class,
// by decoding an image into a larger "frame" colored grey.

#include "main.h"
#include "support.h"

#include "lti_pixel.h"
#include "lt_fileSpec.h"
#include "lt_ioFileStream.h"
#include "lti_scene.h"
#include "lti_geoCoord.h"
#include "lti_sceneBuffer.h"
#include "lti_bbbImageReader.h"
#include "lti_imageStageManager.h"
#include "lti_mosaicFilter.h"
#include "lti_rawImageWriter.h"


LT_USE_NAMESPACE(LizardTech);

/**
 * This LTIImageStageManager implementation holds an array of previously-
 * instantiated LTIImageStage pointers and does not destroy them until the
 * destructor.  The createImageStage() a method is essentially accessor.
 *
 * Such a strategy for managing image stages should not be used in production,
 * but it is useful for validation code.
 */
class SimpleImageStageManager : public LTIImageStageManager
{
   LT_DISALLOW_COPY_CONSTRUCTOR(SimpleImageStageManager);
protected:
   SimpleImageStageManager(void) : m_images(NULL) { }

   ~SimpleImageStageManager(void)
   {
      if(m_images != NULL)
      {
         for(lt_uint32 i = 0; i < getNumImages(); i++)
            LTI_RELEASE(m_images[i]);
         ::free(m_images);
      }
   }
public:
   static SimpleImageStageManager *create(void)
   {      
      return new SimpleImageStageManager();
   }

   LT_STATUS addImageStage(LTIImageStage *imageStage)
   {
      lt_uint32 numImages = getNumImages();
      m_images = static_cast<LTIImageStage **>
         (::realloc(m_images, (numImages + 1) * sizeof(LTIImageStage *)));
      
      if(m_images == NULL)
         return LT_STS_OutOfMemory;

      m_images[numImages] = LTI_RETAIN(imageStage);
      setNumImages(numImages + 1);
      return LT_STS_Success;
   }

   LT_STATUS createImageStage(lt_uint32 imageNumber,
                              LTIImageStage *&imageStage)
   {
      imageStage = LTI_RETAIN(m_images[imageNumber]);
      return LT_STS_Success;
   }
          
private:
   LTIImageStage **m_images;
};


LT_STATUS Mosaic()
{
   LT_STATUS sts = LT_STS_Uninit;
   LTIPixel pixelGrey(LTI_COLORSPACE_GRAYSCALE, 1, LTI_DATATYPE_UINT16);

   RC<SimpleImageStageManager> manager;

   // add the first image to the imagestage manager
   {
      RC<LTIRawImageReader> src;
      TEST_SUCCESS(src->initialize(INPUT_PATH("r16.raw"), pixelGrey, 64, 64));
      LTIGeoCoord geo(10.0, 5.0, 1.0, -1.0, 0.0, 0.0, NULL);
      src->overrideGeoCoord(geo);
      TEST_SUCCESS(manager->addImageStage(src));
   }
   // add the second image to the imagestage manager
   {
      RC<LTIRawImageReader> src;
      TEST_SUCCESS(src->initialize(INPUT_PATH("g16.raw"), pixelGrey, 64, 64));
      const LTIGeoCoord geo(100.0, 50.0, 1.0, -1.0, 0.0, 0.0, NULL);
      src->overrideGeoCoord(geo);
      TEST_SUCCESS(manager->addImageStage(src));
   }

   // initialize a mosaic filter which takes as input the imagestage manager we created above
   RC<LTIMosaicFilter> mosaic;
   TEST_SUCCESS(mosaic->initialize(manager, NULL, false));
      
   LTIRawImageWriter writer;
   TEST_SUCCESS(writer.initialize(mosaic));
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("x.raw")));

   const LTIScene scene(0,0,154,109,1.0);
   
   TEST_SUCCESS(writer.write(scene));
   
   return LT_STS_Success;
}
