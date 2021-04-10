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

#ifndef TextSegment_H
#define TextSegment_H

// lt_lib_base
#include "lt_base.h"

// local
#include "Segment.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIOStreamInf;

namespace Nitf {

class TextSegmentMetadata;


/**
 * represents a text segment in an NITF file
 *
 * This class represents a text segment in an existing NITF file.  To access the
 * text segment of a file, retrieve the TextSegment from the
 * NITFReaderManager class.
 */
class TextSegment : public Segment
{
public:
   // not for public use -- to get a DataSegment, use NITFReaderManager::getTextSegment
   TextSegment(NITFReaderManager&, LTIOStreamInf&,
               int segmentNumber,
               lt_int64 headerOffset, lt_int64 headerLength,
               lt_int64 dataOffset, lt_int64 dataLength);

   // not for public use
   ~TextSegment();

   // not for public use
   LT_STATUS initialize();

   /**
    * get the text data
    *
    * This function returns the textual data stored in the text segment.
    *
    * @returns the text data
    */
   lt_uint8* getTextData() const;

   /**
    * get the text data length
    *
    * This function returns the length (in bytes) of the textual data stored in the text segment.
    *
    * @returns the text data
    */
   lt_uint32 getTextDataLength() const;

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db) const;

private:
   LT_STATUS readHeader();
   LT_STATUS readData();

   TextSegmentMetadata* m_textMetadata;

   char* m_TXTALVL;  // 3
   char* m_TXTFMT;   // 3

   lt_uint8* m_textData;
   lt_uint32 m_textDataLen;

   // nope
   TextSegment(TextSegment&);
   TextSegment& operator=(const TextSegment&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // TextSegment_H
