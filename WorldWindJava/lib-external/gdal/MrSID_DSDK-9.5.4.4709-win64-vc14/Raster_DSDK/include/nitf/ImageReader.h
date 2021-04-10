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

#ifndef ImageReader_H
#define ImageReader_H

// lt_lib_base
#include "lt_base.h"

// lt_lib_mrsid_core
#include "lti_geoImageReader.h"

// local
#include "nitf_types.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTFileSpec;
class LTIOStreamInf;
class LTReusableBuffer;
class NITFReaderManager;

namespace Nitf {
class FileHeader;
class ImageSegment;
class ImageSegmentMetadata;
class SecurityMetadata;


/**
 * class for representing an NITF image segment as an LTIImageReader
 *
 * This class provides support for representing an NITF
 * image segment as an LTIImageReader, from which you can extract pixel data.
   ImageReader objects are not
 * to be created directly; the NITFReaderManager class
 * contains a createReader() function for this purpose.
 */
class ImageReader : public LTIGeoImageReader
{
   LT_DISALLOW_COPY_CONSTRUCTOR(ImageReader);
public:
   /**
    * returns the IID1 field for the segment
    */
   const char* getIID1() const;

   lt_int64 getPhysicalFileSize() const;

   // not for public use
   NITFReaderManager& getManager() const;

   /**
    * returns the compression format of the segment
    */
   Format getFormat() const;

   /**
    * returns the pixel layout of the segment
    */
   Layout getLayout() const;

   /**
    * returns true iff the image segment is in blocked form
    */
   bool isBlocked() const;

   /**
    * returns true iff block masking is used in the image segment
    */
   bool isMasked() const;

   /**
    * returns the index of this image segment 
    */
  int getSegmentNumber() const;

   /**
    * returns the image segment metadata object for this segment
    */
  const ImageSegmentMetadata* getImageMetadata() const;

   /**
    * returns the security metadata object for this segment
    */
  const SecurityMetadata* getSecurityMetadata() const;

  // inherited
  LT_STATUS getDimsAtMag(double mag,
                         lt_uint32& width,
                         lt_uint32& height) const = 0;

protected:
   ~ImageReader(void);
   ImageReader(bool supportBandSelection);

   const char *getSourceName(void) const;
   
   LT_STATUS init(LTIOStreamInf *stream,
                  NITFReaderManager *manager,
                  const ImageSegment *imageSegment,
                  bool useWorldFile);

   LT_STATUS addUnderlyingMetadata(const LTIImageStage &image);

   // blocked image support
   LTIScene computeBlockedScene(const LTIScene& scene) const;
   LT_STATUS copyIntoUserBuffer(const LTIScene& dstScene,
                                LTISceneBuffer& dstBuffer) const;
   bool activeSceneContains(const LTIScene& scene) const;
   void putBlockIntoBuffer_SEQ(LTISceneBuffer& cBuffer,
                               lt_uint8* buf,
                               lt_uint32 blockRow,
                               lt_uint32 blockCol,
                               lt_uint32 blockBand) const;
   void putBlockIntoBuffer_BLOCK(LTISceneBuffer& cBuffer,
                                 lt_uint8* buf,
                                 lt_uint32 blockRow,
                                 lt_uint32 blockCol) const;
   void putBlockIntoBuffer_PIXEL(LTISceneBuffer& cBuffer,
                               lt_uint8* buf,
                               lt_uint32 blockRow,
                               lt_uint32 blockCol) const;
   void putBlockIntoBuffer_ROW(LTISceneBuffer& cBuffer,
                               lt_uint8* buf,
                               lt_uint32 blockRow,
                               lt_uint32 blockCol) const;

   
   
   LTIOStreamInf *m_stream;
   NITFReaderManager *m_manager;
   
   const ImageSegment *m_imageSegment;
   const FileHeader *m_fileHeader;
   
   LTReusableBuffer *m_reusableBuffer;
   LTIScene *m_activeScene;
   LTISceneBuffer *m_activeSceneBuffer;
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // ImageReader_H
