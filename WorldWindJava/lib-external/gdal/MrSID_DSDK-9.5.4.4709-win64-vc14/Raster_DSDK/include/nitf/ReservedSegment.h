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

#ifndef ReservedSegment_H
#define ReservedSegment_H

// lt_lib_base
#include "lt_base.h"

// local
#include "Segment.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIOStreamInf;

namespace Nitf {


/**
 * represents a Reserved segment in an NITF file
 *
 * This class represents a RES segment in an existing NITF file.  To access the
 * image segment of a file, retrieve the ReservedSegment from the
 * NITFReaderManager class.
 */
class ReservedSegment : public Segment
{
public:
   // not for public use -- to get a DataSegment, use NITFReaderManager::getResSegment
   ReservedSegment(NITFReaderManager&, LTIOStreamInf&,
              int segmentNumber,
              lt_int64 headerOffset, lt_int64 headerLength,
              lt_int64 dataOffset, lt_int64 dataLength);

   // not for public use
   ~ReservedSegment();

   // not for public use
   LT_STATUS initialize();

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db) const;

private:
   LT_STATUS readHeader();
   LT_STATUS readData();

   char* m_RESID;    // 25
   char* m_RESVER;   // 2
   int m_RESSHL;
   lt_uint8* m_RESSHF;

   // nope
   ReservedSegment(ReservedSegment&);
   ReservedSegment& operator=(const ReservedSegment&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // ReservedSegment_H
