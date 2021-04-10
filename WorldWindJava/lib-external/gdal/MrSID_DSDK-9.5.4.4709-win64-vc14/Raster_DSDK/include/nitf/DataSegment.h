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

#ifndef DataSegment_H
#define DataSegment_H

// lt_lib_base
#include "lt_base.h"

// local
#include "Segment.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIOStreamInf;

namespace Nitf {


/**
 * represents a data segment in an existing NITF file
 *
 * This class represents a data segment in an NITF file.  To access the
 * data segment of a file, retrieve the DataSegment from the
 * NITFReaderManager class.
 */
class DataSegment : public Segment
{
public:
   // not for public use -- to get a DataSegment, use NITFReaderManager::getDataSegment
   DataSegment(NITFReaderManager&, LTIOStreamInf&,
               int segmentNumber,
               lt_int64 headerOffset, lt_int64 headerLength,
               lt_int64 dataOffset, lt_int64 dataLength);
   ~DataSegment();

   // not for public use
   LT_STATUS initialize();

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db) const;

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db,
                              const char* prefixOverride) const;

   // not for public use
   bool verifyIsOverflow(const char* code, bool isU, int segmentNum) const;

   /**
    * returns the ID
    *
    * This function returns the ID of the data segment.
    *
    * @return the ID of the segment, as a string
    */
   const char* getDESID() const;

   /**
    * returns the version
    *
    * This function returns the version of the data segment.
    *
    * @return the version of the segment, as a string
    */
   const char* getDESVER() const;

   /**
    * returns the length of the data from the segment
    *
    * This function returns the length of actual data of the data segment, in bytes.
    *
    * @return the number of bytes of the data
    */
   lt_uint32 getDataLen() const;

   /**
    * returns the data from the segment
    *
    * This function returns the actual data of the data segment, as an array of bytes.
    * The length of the array can be found by calling getDataLen().
    *
    * @return the array of bytes of the data
    */
   const lt_uint8* getData() const;

private:
   LT_STATUS readHeader();
   LT_STATUS readData();

   char* m_DESID;    // 25
   char* m_DESVER;   // 2
   char* m_DESOFLW;  // 6
   int m_DESITEM;
   int m_DESSHL;
   lt_uint8* m_DESSHF;

   lt_uint32 m_bytesLen;
   lt_uint8* m_bytes;

   // nope
   DataSegment(DataSegment&);
   DataSegment& operator=(const DataSegment&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // DataSegment_H
