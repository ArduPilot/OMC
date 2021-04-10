/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


// This demonstrates how to derive your own image filter from LTIImageFilter.
// For this example, we make a filter which simply converts RGB images to
// grayscale.  (For clarity of presentation, this filter does not update
// the metadata to reflect the new colorspace, it only works on 8 bit
// samples, and it does it modify the background and nodata pixels to be
// grayscale data.)


#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_imageFilter.h"
#include "lti_pixel.h"
#include "lti_scene.h"
#include "lti_sceneBuffer.h"
#include "lti_bbbImageReader.h"
#include "lti_bbbImageWriter.h"

LT_USE_NAMESPACE(LizardTech);


//---------------------------------------------------------------------------
// RGB to grayscale filter
//---------------------------------------------------------------------------

class MyFilter : public LTIOverridePixelProps<LTIImageFilter>
{
   LTI_REFERENCE_COUNTED_BOILERPLATE(MyFilter);
private:

public:
   LT_STATUS initialize(LTIImageStage* sourceImage)
   {
      LT_STATUS sts = LTIImageFilter::init(sourceImage);
      if (!LT_SUCCESS(sts))
         return sts;

      const LTIImageStage& prev = *getPreviousStage();

      if (prev.getColorSpace() != LTI_COLORSPACE_RGB ||
          prev.getDataType() != LTI_DATATYPE_UINT8)
         return LT_STS_Failure;

      LTIPixel props(LTI_COLORSPACE_GRAYSCALE, 1, prev.getDataType());
      sts = setPixelProps(props);
      if (!LT_SUCCESS(sts))
         return sts;

      return LT_STS_Success;
   }

   // LTIImageStage override
   lt_uint32 getModifications(const LTIScene &scene) const
   {
      lt_uint32 mods = LTI_MODIFICATION_UNKNOWN;
      if (getPreviousStage())
         mods = getPreviousStage()->getModifications(scene);
      mods |= LTI_MODIFICATION_CHANGEDCOLORSPACE;
      return mods;
   }

protected:
   LT_STATUS decodeBegin(const LTIPixel &pixelProps,
                         const LTIScene &fullScene)
   {
      // set up state needed for this decode

      return getPreviousStage()->readBegin(getPreviousStage()->getPixelProps(),
                                           fullScene);
   }

   LT_STATUS decodeStrip(LTISceneBuffer &stripBuffer,
                         const LTIScene &stripScene)
   {
      LT_STATUS sts = LT_STS_Uninit;

      const lt_int32 width = stripScene.getNumCols();
      const lt_int32 height = stripScene.getNumRows();

      // make a buffer to hold the rgb data
      LTISceneBuffer srcData(getPreviousStage()->getPixelProps(),
                             width, height, NULL);

      // read the RGB data from the previous stage
      sts = getPreviousStage()->readStrip(srcData, stripScene);
      if (!LT_SUCCESS(sts))
         return sts;

      // copy the data from the RGB buffer into our grayscale buffer
      lt_uint8 *red = static_cast<lt_uint8 *>(srcData.getBandData(0));
      lt_uint8 *green = static_cast<lt_uint8 *>(srcData.getBandData(1));
      lt_uint8 *blue = static_cast<lt_uint8 *>(srcData.getBandData(2));
      lt_uint8 *gray = static_cast<lt_uint8 *>(stripBuffer.getBandData(0));
      for (lt_int32 r = 0; r < height; r++)
      {
         for (lt_int32 c = 0; c < width; c++)
         {
            gray[c] = static_cast<lt_uint8>(0.3f * red[c] +
                                            0.6f * green[c] +
                                            0.1f * blue[c]);
         }
         red += srcData.getTotalNumCols();
         green += srcData.getTotalNumCols();
         blue += srcData.getTotalNumCols();
         gray += stripBuffer.getTotalNumCols();
      }

      return LT_STS_Success;
   }

   LT_STATUS decodeEnd(void)
   {    
      // clean up state

      return getPreviousStage()->readEnd();
   }
};

MyFilter::MyFilter(void) {}
MyFilter::~MyFilter() {}

MyFilter *MyFilter::create(void)
{
   return new MyFilter;
}

//---------------------------------------------------------------------------

LT_STATUS DerivedImageFilter()
{
   LT_STATUS sts = LT_STS_Uninit;

   // read in a raw rgb image, and write it out as grayscale

   // make the reader
   const LTFileSpec fileSpec(INPUT_PATH("meg.bip"));
   LTIBBBImageReader *reader = LTIBBBImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));

   // connect up the filter
   MyFilter *filter = MyFilter::create();
   TEST_BOOL(filter != NULL);

   TEST_SUCCESS(filter->initialize(reader));

   TEST_BOOL(reader->getColorSpace() == LTI_COLORSPACE_RGB);
   TEST_BOOL(filter->getColorSpace() == LTI_COLORSPACE_GRAYSCALE);

   // make the BBB writer
   LTIBBBImageWriter writer;
   TEST_SUCCESS(writer.initialize(filter));

   // set up the output file
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("meg_gray.bip")));
   
   const LTIScene scene(0, 0, 640, 480, 1.0);

   // write the scene to the file   
   TEST_SUCCESS(writer.write(scene));

   // verify we got the right output
   TEST_BOOL(Compare(OUTPUT_PATH("meg_gray.bip"), INPUT_PATH("meg_gray.bip")));
   Remove(OUTPUT_PATH("meg_gray.bip"));
   Remove(OUTPUT_PATH("meg_gray.hdr"));

   filter->release();
   filter = NULL;
   reader->release();
   reader = NULL;
   
   return LT_STS_Success;
}
