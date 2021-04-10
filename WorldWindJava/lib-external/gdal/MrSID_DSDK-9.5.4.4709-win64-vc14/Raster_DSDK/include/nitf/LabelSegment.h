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

#ifndef LabelSegment_H
#define LabelSegment_H

// lt_lib_base
#include "lt_base.h"

// local
#include "Segment.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIOStreamInf;

namespace Nitf {


/**
 * represents a label segment in an existing NITF file
 *
 * This class represents a label segment in an NITF file.  To access the
 * label segment of a file, retrieve the LabelSegment from the
 * NITFReaderManager class.
 */
class LabelSegment : public Segment
{
public:
   // not for public use -- to get a DataSegment, use NITFReaderManager::getLabelSegment
   LabelSegment(NITFReaderManager&, LTIOStreamInf&,
                int segmentNumber,
                lt_int64 headerOffset, lt_int64 headerLength,
                lt_int64 dataOffset, lt_int64 dataLength);
   ~LabelSegment();

   // not for public use
   LT_STATUS initialize();

   /**
    * returns the length (in bytes) of the label data
    */
   lt_uint32 getLabelDataLength() const;

   /**
    * returns the label data as raw bytes
    */
   lt_uint8* getLabelData() const;

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db) const;

private:
   LT_STATUS readHeader();
   LT_STATUS readData();
   
   char* m_LID;      // 10
   char* m_LFS;      // 1
   char* m_LCW;      // 2
   char* m_LCH;      // 2
   char* m_LDLVL;    // 3
   char* m_LALVL;    // 3
   char* m_LLOC;     // 3
   lt_uint8* m_LTC;      // 3
   lt_uint8* m_LBC;      // 3

   lt_uint8* m_labelData;
   lt_uint32 m_labelDataLen;

   // nope
   LabelSegment(LabelSegment&);
   LabelSegment& operator=(const LabelSegment&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // LabelSegment_H
