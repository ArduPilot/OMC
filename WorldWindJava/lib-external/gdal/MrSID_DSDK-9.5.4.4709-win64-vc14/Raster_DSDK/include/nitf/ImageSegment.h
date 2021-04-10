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

#ifndef ImageSegment_H
#define ImageSegment_H

// lt_lib_base
#include "lt_base.h"

// lt_lib_mrsid_nitf
#include "nitf_types.h"
#include "Segment.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIPixel;
class LTIOSubStream;
class LTIGeoCoord;

namespace Nitf {
class ImageSegmentMetadata;


/**
 * represents a image segment in an existing NITF file
 *
 * This class represents an image segment in an NITF file.  To access the
 * image segment of a file, retrieve the ImageSegment from the
 * NITFReaderManager class.
 *
 * This class exposes some basic properties of the image in the segment.

 * This class does not implement the renderable image, however.  Use the
 * createImage() function of the NITFReaderManager to access an ImageReader
 * (which is an LTIImageReader).
 */
class ImageSegment : public Segment
{
public:
   // not for public use -- to get a DataSegment, use NITFReaderManager::getImageSegment
   ImageSegment(NITFReaderManager&, LTIOStreamInf&,
                int segmentNumber,
                lt_int64 headerOffset, lt_int64 headerLength,
                lt_int64 dataOffset, lt_int64 dataLength);
   ~ImageSegment();
   LT_STATUS initialize();

   /**
    * returns the IID1 field of the image
    */
   const char* getIID1() const;

   /**
    * returns the format (JPEG, RAW, JP2, etc) of the image
    */
   Format getFormat() const;

   /**
    * returns the layout (interleaved, blocked, etc) of the image
    */
   Layout getLayout() const;

   /**
    * returns true iff the image is blocked
    */
   bool isBlocked() const;

   /**
    * returns true iff the image is masked
    */
   bool isMasked() const;

   /**
    * returns the pixel type of the image
    */
   const LTIPixel& getPixelProps() const;

   /**
    * returns the width of the image, in pixels
    */
   lt_uint32 getWidth() const;

   /**
    * returns the height of the image, in pixels
    */
   lt_uint32 getHeight() const;

   /**
    * returns the width of a block, in pixels (corresponds to the NPPBH field)
    */
   int getBlockWidth() const;

   /**
    * returns the height of a block, in pixels (corresponds to the NPPBV field)
    */
   int getBlockHeight() const;

   /**
    * returns the number of blocks per row in the image (corresponds to the NBPR field)
    */
   int getBlocksPerRow() const;

   /**
    * returns the number of blocks per row in the image (corresponds to the NBPC field)
    */
   int getBlocksPerCol() const;

   /**
    * returns the number of bits per pixel used in the image (corresponds to the NBPP field)
    */
   int getNBPP() const;

   /**
    * returns the number of bands in the image (corresponds to the NBANDS field)
    */
   int getNBANDS() const;

   // not for public use
   int getILOCr() const;

   // not for public use
   int getILOCc() const;

   // not for public use
   const lt_uint8* getPadValue() const;

   // not for public use
   lt_uint64 getBlockOffset(lt_uint32 index) const;

   // not for public use
   lt_uint64 getNextBlockOffset(lt_uint32 index) const;

   // not for public use
   bool isMaskedBlock(lt_uint32 index) const;

   // not for public use
   LT_STATUS isSupported() const;

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db) const;

   // not for public use
   LT_STATUS createDataStream(LTIOSubStream*&) const;

   // not for public use
   bool hasGeoInfo(void) const;

   // not for public use
   LT_STATUS setGeoCoord(LTIGeoCoord&) const;

   /**
    * returns the metadata object associated with the image
    */
   const ImageSegmentMetadata* getImageMetadata() const;

private:
   LT_STATUS readHeader();
   LT_STATUS readData();

   LT_STATUS readMaskTable();
   LT_STATUS constructOffsetTable_RAW(lt_int64, bool BUGGY);
   LT_STATUS constructOffsetTable_JPG(lt_int64, bool BUGGY);
   LT_STATUS determinePixelFormat();
   LT_STATUS determineImageFormat();

   LTIPixel* m_pixel;
   bool m_hasLUT;
   Format m_format;    // IC
   bool m_isMasked;
   Layout m_layout;    // IMODE

   LT_STATUS m_isSupported;

   ImageSegmentMetadata* m_imageMetadata;

   int m_NROWS;
   int m_NCOLS;
   char* m_PVTYPE;      // 3
   char* m_IREP;        // 8
   char* m_ICAT;        // 8
   int m_ABPP;          // just precision
   char* m_PJUST;       // 1
   char* m_ICORDS;      // 1
   char* m_IGEOLO;      // 60
   char* m_IC;          // 2
   char* m_COMRAT;      // 4
   int m_NBANDS;
   char** m_IREPBAND;
   char** m_ISUBCAT;
   lt_uint8*** m_LUTD;
   int* m_NLUTS;
   int* m_NELUT;
   char* m_IMODE;       // 1
   int m_NBPR;
   int m_NBPC;
   int m_NPPBH;
   int m_NPPBV;
   int m_NBPP;       // actual bytes used
   int m_IDLVL;
   int m_IALVL;
   int m_ILOCr;
   int m_ILOCc;
   char* m_IMAG;     // 4
   
   // mask stuff
   lt_uint32 m_IMDATOFF;
   lt_uint16 m_BMRLNTH;
   lt_uint16 m_TMRLNTH;
   lt_uint16 m_TPXCDLNTH;
   lt_uint8* m_TPXCD;
   int m_TPXCDlen;
   lt_uint64* m_BMRBND;
   lt_uint32* m_TMRBND;

   // geo stuff
   double *m_geoPoints; // ul(lat,lon), ur(lat,lon), lr(lat,lon), ll(lat,lon)
   int m_zone;
   bool m_igeoloValid;

   // nope
   ImageSegment(ImageSegment&);
   ImageSegment& operator=(const ImageSegment&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // ImageSegment_H
