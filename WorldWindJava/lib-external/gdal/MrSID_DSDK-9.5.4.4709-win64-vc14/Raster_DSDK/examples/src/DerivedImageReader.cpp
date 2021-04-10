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


// This demonstrates how to derive a simple reader.  This class reads raw
// files (BIP), with the colorspace, dimension, etc, passed in via the
// constructor.  To make the example simple, we do not supplort duplicate(),
// metadata, background/nodata pixels, we only support 8-bit samples, etc.


#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lt_ioFileStream.h"
#include "lti_imageReader.h"
#include "lti_pixel.h"
#include "lti_scene.h"
#include "lti_sceneBuffer.h"
#include "lti_bbbImageWriter.h"
#include "lti_utils.h"

LT_USE_NAMESPACE(LizardTech);

#ifdef WIN32_DLL_WORKAROUND
template <class T>
class LTIDLLFileStream : public T
{
public:
   LTIDLLFileStream() {}
   virtual ~LTIDLLFileStream() {}
};
#endif

//---------------------------------------------------------------------------
// reader for simple BIP raw files
//---------------------------------------------------------------------------

class MyReader : public LTIImageReader
{
   LTI_REFERENCE_COUNTED_BOILERPLATE(MyReader);
public:
   LT_STATUS initialize(const LTFileSpec& fileSpec,
                        const LTIPixel& pixelProps,
                        lt_uint32 width,
                        lt_uint32 height)
   {
      LT_STATUS sts = LTIImageReader::init();
      if (!LT_SUCCESS(sts))
         return sts;

      // only support LTI_DATATYPE_UINT8 or LTI_DATATYPE_UINT16
      if (pixelProps.getDataType() != LTI_DATATYPE_UINT8 &&
          pixelProps.getDataType() != LTI_DATATYPE_UINT16)
         return LT_STS_Failure;

#ifdef WIN32_DLL_WORKAROUND
      m_stream = new LTIDLLFileStream<LTIOFileStream>;
#else
      m_stream = new LTIOFileStream;
#endif
      m_stream->initialize(fileSpec, "rb");
      if (!m_stream)
         return LT_STS_Failure;

      sts = m_stream->open();
      if (!LT_SUCCESS(sts))
         return sts;

      sts = setPixelProps(pixelProps);
      if (!LT_SUCCESS(sts))
         return sts;

      sts = setDimensions(width, height);
      if (!LT_SUCCESS(sts))
         return sts;

      sts = setDefaultGeoCoord(*this);
      if (!LT_SUCCESS(sts))
         return sts;

      m_rowBytes = pixelProps.getNumBytes() * getWidth();

      return LT_STS_Success;
   }

   lt_int64 getPhysicalFileSize(void) const
   {
      const lt_int64 pos = m_stream->tell();
      m_stream->seek(0,LTIO_SEEK_DIR_END);
      const lt_int64 siz = m_stream->tell();
      m_stream->seek(pos,LTIO_SEEK_DIR_BEG);
      return siz;
   }

protected:
   LT_STATUS decodeBegin(const LTIPixel &pixelProps,
                         const LTIScene &fullScene)
   {
      const lt_uint32 numCols = fullScene.getNumCols();
      const lt_uint32 bytesPerPixel = getPixelProps().getNumBytes();
      const lt_uint32 sceneRowBytes = numCols * bytesPerPixel;
      m_rowBuffer = new lt_uint8[sceneRowBytes];
      return LT_STS_Success;
   }

   LT_STATUS decodeStrip(LTISceneBuffer &stripBuffer,
                         const LTIScene &stripScene)
   {
      LT_STATUS sts = LT_STS_Uninit;

      const lt_uint32 numCols = stripScene.getNumCols();
      const lt_uint32 numRows = stripScene.getNumRows();
      const lt_uint32 totalNumCols = stripBuffer.getTotalNumCols();
      
      const LTIPixel &pixel = stripBuffer.getPixelProps();
      const lt_uint32 bytesPerPixel = pixel.getNumBytes();
      const lt_uint32 bytesPerSample = pixel.getSample(0).getNumBytes();
      
      const lt_uint32 sceneRowBytes = numCols * bytesPerPixel;
      const lt_uint16 numBands = stripBuffer.getNumBands();
        
      
      lt_int64 curPos = stripScene.getUpperLeftRow() * m_rowBytes +
                        stripScene.getUpperLeftCol() * bytesPerPixel;
      for (lt_uint32 row = 0; row < numRows; row++)
      {
         // load a BIP row from disk
         sts = m_stream->seek(curPos, LTIO_SEEK_DIR_BEG);
         if (!LT_SUCCESS(sts))
            return sts;

         if (m_stream->read(m_rowBuffer, sceneRowBytes) != sceneRowBytes)
            return m_stream->getLastError();

         // load the BIP row into the BSQ SceneBuffer
         for(lt_uint16 band = 0; band < numBands; band++)
         {
            const lt_uint8 *src = m_rowBuffer + band * bytesPerSample;
            lt_uint8 *dst = static_cast<lt_uint8 *>(stripBuffer.getBandData(band));
            // skip down to the row of interest
            dst += row * totalNumCols * bytesPerSample;

            for(lt_uint32 col = 0; col < numCols; col++, src += bytesPerPixel, dst += bytesPerSample)
               memcpy(dst, src, bytesPerSample);
         }
         curPos += m_rowBytes;
      }

      if(LTIUtils::needsSwapping(getDataType(), LTI_ENDIAN_LITTLE))
         stripBuffer.byteSwap();

      return LT_STS_Success;
   }
   
   LT_STATUS decodeEnd()
   {
      delete [] m_rowBuffer;
      m_rowBuffer = NULL;
      return LT_STS_Success;
   }

   const char *getSourceName(void) const
   {
      return m_stream != NULL ? m_stream->getID() : NULL;
   }

private:
#ifdef WIN32_DLL_WORKAROUND
   LTIDLLFileStream<LTIOFileStream> *m_stream;
#else
   LTIOFileStream *m_stream;
#endif

   lt_uint32 m_rowBytes;
   lt_uint8 *m_rowBuffer;
};

MyReader::MyReader() :
   LTIImageReader(false),
   m_stream(NULL),
   m_rowBuffer(NULL),
   m_rowBytes(0)
{
}

MyReader::~MyReader()
{
   m_stream->close();
   delete m_stream;
   delete[] m_rowBuffer;
}

MyReader* 
MyReader::create()
{
   return new MyReader;
}
//---------------------------------------------------------------------------

LT_STATUS DerivedImageReader()
{
   LT_STATUS sts = LT_STS_Uninit;

   // read in a raw rgb image, and write it out as grayscale

   // make the reader
   const LTFileSpec fileSpec(INPUT_PATH("meg.bip"));
   const LTIPixel pixel(LTI_COLORSPACE_RGB, 3, LTI_DATATYPE_UINT8);
   MyReader *reader = MyReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec, pixel, 640, 480));

   // make the BBB writer
   LTIBBBImageWriter writer;
   TEST_SUCCESS(writer.initialize(reader));

   // set up the output file
   TEST_SUCCESS(writer.setOutputFileSpec(OUTPUT_PATH("meg.bip")));
   
   const LTIScene scene(0, 0, 640, 480, 1.0);

   // write the scene to the file   
   TEST_SUCCESS(writer.write(scene));

   // verify we got the right output
   TEST_BOOL(Compare(OUTPUT_PATH("meg.bip"), INPUT_PATH("meg.bip")));
   Remove(OUTPUT_PATH("meg.bip"));
   Remove(OUTPUT_PATH("meg.hdr"));

   reader->release();
   reader = NULL;

   return LT_STS_Success;
}
