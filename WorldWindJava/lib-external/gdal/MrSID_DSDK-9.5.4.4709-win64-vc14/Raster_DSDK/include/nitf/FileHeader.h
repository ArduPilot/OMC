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

#ifndef FileHeader_H
#define FileHeader_H

// local
#include "Segment.h"


LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIOStreamInf;

namespace Nitf {
class ImageSegment;
class GraphicSegment;
class LabelSegment;
class TextSegment;
class DataSegment;
class ReservedSegment;
class FieldReader;
class FileMetadata;


/**
 * represents the file header properties of a NITF file
 *
 * This class represents the file header properties of an NITF file.  To access the
 * file header of a file, retrieve the FileHeader from the
 * NITFReaderManager class.  The actual properties are available from the
 * FileMetadata class obtained from this object.
 */
class FileHeader : public Segment
{
public:
   // not for public use
   FileHeader(NITFReaderManager&, LTIOStreamInf&, lt_int64 fileSize);
   ~FileHeader();

   // not for public use
   LT_STATUS initialize();

   /**
    * returns the version of the file
    *
    * This function returns the version NITF specifcation the file conforms to.
    *
    * @return the version of the file
    */
   Version getVersion() const;

   // not for public use
   LT_STATUS createSegments(ImageSegment**& imageSegments, lt_uint32& numImageSegments,
                            GraphicSegment**& graphicSegments, lt_uint32& numGraphicSegments,
                            LabelSegment**& labelSegments, lt_uint32& numLabelSegments,
                            TextSegment**& textSegments, lt_uint32& numTextSegments,
                            DataSegment**& dataSegments, lt_uint32& numDataSegments,
                            ReservedSegment**& resSegments, lt_uint32& numResSegments);

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db) const;

   /**
    * returns the background pixel for the overall file
    *
    * This function returns the background pixel, as stored in the the FBKGC field.
    * It consists of three values (R, G, B) with a range of 0 to 255.
    *
    * @return the RGB pixel values
    */
   const lt_uint8* getFBKGC() const;

   /**
    * returns the object holding the file metadata
    *
    * This function returns an object holding the basic file metadata.
    *
    * @return the file metadata object
    */
   const FileMetadata* getFileMetadata() const;

private:
   LT_STATUS readHeader();
   LT_STATUS readData();
   LT_STATUS readHeader_v2x();
   LT_STATUS parseVersion();

   const lt_int64 m_fileSize;

   Version m_ver;
   char* m_FHDR;     // 4
   char* m_FVER;     // 5
   int m_CLEVEL;
   char* m_STYPE;       // 4
   int m_FSCOP;
   int m_FSCPYS;
   lt_uint8 m_FBKGC[3];
   lt_int64 m_FL;
   lt_int64 m_HL;
   lt_int32 m_NUMI;
   lt_int64* m_LISH;
   lt_int64* m_LI;
   lt_int32 m_NUMS;
   lt_int64* m_LSSH;
   lt_int64* m_LS;
   lt_int32 m_NUML;
   lt_int64* m_LLSH;
   lt_int64* m_LL;
   lt_int32 m_NUMT;
   lt_int64* m_LTSH;
   lt_int64* m_LT;
   lt_int32 m_NUMDES;
   lt_int64* m_LDSH;
   lt_int64* m_LD;
   lt_int32 m_NUMRES;
   lt_int64* m_LRESH;
   lt_int64* m_LRE;

   char* m_FTITLE;

   FileMetadata* m_fileMetadata;

   // nope
   FileHeader(FileHeader&);
   FileHeader& operator=(const FileHeader&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // FileHeader_H
