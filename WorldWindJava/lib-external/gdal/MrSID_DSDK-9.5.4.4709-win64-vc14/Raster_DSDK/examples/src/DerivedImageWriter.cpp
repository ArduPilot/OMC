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


// This demonstrates how to derive your own image writer.  This example
// class just writes raw (BIP) files.


#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lt_ioStreamInf.h"
#include "lti_bbbImageReader.h"
#include "lti_geoFileImageWriter.h"
#include "lti_sceneBuffer.h"
#include "lti_pixel.h"
#include "lti_utils.h"

LT_USE_NAMESPACE(LizardTech);


//---------------------------------------------------------------------------
// simple BIP (raw) writer take always write little endian data
//---------------------------------------------------------------------------

class MyWriter : public LTIGeoFileImageWriter
{
public:
   MyWriter(void) :
      LTIGeoFileImageWriter(true),
      m_rowBytes(0),
      m_rowBuffer(NULL)
   {
   }

   ~MyWriter()
   {
   }

   LT_STATUS initialize(LTIImageStage* image)
   {
      return LTIGeoFileImageWriter::init(image);
   }

   LT_STATUS writeBegin(const LTIScene& scene)
   {
      const lt_uint32 numCols = scene.getNumCols();
      const lt_uint32 bytesPerPixel = m_image->getPixelProps().getNumBytes();
      m_rowBytes = numCols * bytesPerPixel;
      m_rowBuffer = new lt_uint8[m_rowBytes];

      return LTIGeoFileImageWriter::writeBegin(scene);
   }

   LT_STATUS writeStrip(LTISceneBuffer& stripBuffer,
                        const LTIScene& stripScene)
   {
      // swab data if not in little endian
      if(LTIUtils::needsSwapping(stripBuffer.getPixelProps().getDataType(), LTI_ENDIAN_LITTLE))
         stripBuffer.byteSwap();
      
      const lt_uint32 numCols = stripScene.getNumCols();
      const lt_uint32 numRows = stripScene.getNumRows();
      const lt_uint32 totalNumCols = stripBuffer.getTotalNumCols();

      const LTIPixel &pixel = stripBuffer.getPixelProps();
      const lt_uint32 bytesPerPixel = pixel.getNumBytes();
      const lt_uint32 bytesPerSample = pixel.getSample(0).getNumBytes();
      
      const lt_uint16 numBands = stripBuffer.getNumBands();

      LTIOStreamInf &stream = *getStream();
      for (lt_uint32 row = 0; row < numRows; row++)
      {
         // export the BSQ SceneBuffer to BIP (one row at a time)
         for(lt_uint16 band = 0; band < numBands; band++)
         {
            lt_uint8 *dst = m_rowBuffer + band * bytesPerSample;
            const lt_uint8 *src = static_cast<lt_uint8 *>(stripBuffer.getBandData(band));
            // skip down to the row of interest
            src += row * totalNumCols * bytesPerSample;
            
            for(lt_uint32 col = 0; col < numCols; col++, src += bytesPerSample, dst += bytesPerPixel)
               memcpy(dst, src, bytesPerSample);
         }
         
         // write the BIP row to disk
         if(stream.write(m_rowBuffer, m_rowBytes) != m_rowBytes)
            return stream.getLastError();
      }

      return LT_STS_Success;
   }
   
   LT_STATUS writeEnd(void)
   {
      delete [] m_rowBuffer;
      m_rowBuffer = NULL;
      
      return LTIGeoFileImageWriter::writeEnd();
   }

private:
   lt_uint32 m_rowBytes;
   lt_uint8 *m_rowBuffer;

};

//---------------------------------------------------------------------------

LT_STATUS DerivedImageWriter()
{
   LT_STATUS sts = LT_STS_Uninit;

   // read in a raw rgb image, and write it out as grayscale

   // make the reader
   const LTFileSpec fileSpec(INPUT_PATH("meg.bip"));
   LTIBBBImageReader *reader = LTIBBBImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));

   // make the raw writer
   MyWriter writer;
   TEST_SUCCESS(writer.initialize(reader));

   // set up the output file
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("meg.bip")));
   
   const LTIScene scene(0, 0, 640, 480, 1.0);

   // write the scene to the file   
   TEST_SUCCESS(writer.write(scene));

   // verify we got the right output
   TEST_BOOL(Compare(OUTPUT_PATH("meg.bip"), INPUT_PATH("meg.bip")));
   Remove(OUTPUT_PATH("meg.bip"));

   reader->release();
   reader = NULL;

   return LT_STS_Success;
}
