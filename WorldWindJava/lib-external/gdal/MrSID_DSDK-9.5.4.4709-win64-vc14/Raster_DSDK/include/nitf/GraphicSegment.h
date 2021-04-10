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

#ifndef GraphicSegment_H
#define GraphicSegment_H

// lt_lib_base
#include "lt_base.h"

// local
#include "Segment.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIOStreamInf;

namespace Nitf {


/**
 * represents a graphic segment in an existing NITF file
 *
 * This class represents a graphic segment in an NITF file.  To access the
 * graphic segment of a file, retrieve the GraphicSegment from the
 * NITFReaderManager class.
 *
 * Graphics segments are not fully supported: no rendering mechanism is
 * provided, only the raw bytes of the segment can be obtained.
 */
class GraphicSegment : public Segment
{
public:
   // not for public use -- to get a DataSegment, use NITFReaderManager::getGraphicSegment
   GraphicSegment(NITFReaderManager&, LTIOStreamInf&,
                  int segmentNumber,
                  lt_int64 headerOffset, lt_int64 headerLength,
                  lt_int64 dataOffset, lt_int64 dataLength);
   ~GraphicSegment();

   // not for public use
   LT_STATUS initialize();

   /**
    * returns the graphic segment contents
    *
    * This function returns the raw, uninterpreted bytes stored as the payload of the graphics segment.
    *
    * @param graphicDataLen [out] returns the length of the data array
    * @return the data, as a byte array
    */
   lt_uint8* getGraphicData(lt_uint32& graphicDataLen) const;

   // not for public use
   LT_STATUS addMetadataLocal(LTIMetadataDatabase& db) const;

private:
   LT_STATUS readHeader();
   LT_STATUS readData();

   char* m_SID;      // 10
   char* m_SNAME;    // 20
   char* m_STYPE;    // 1
   char* m_SSTRUCT;  // 13
   int m_NLIPS;
   int m_NPIXPL;
   int m_NWDTH;
   int m_NBPP;

   int m_SDLVL;
   int m_SALVL;
   int m_SLOCr;
   int m_SLOCc;
   char* m_SBND1;    // 10
   char* m_SCOLOR;   // 1
   char* m_SBND2;    // 10
   char* m_SRES2;    // 2

   int m_SNUM;
   int m_SROT;
   int m_NELUT;
   lt_uint8* m_DLUT;

   lt_uint8* m_graphicData;
   lt_uint32 m_graphicDataLen;

   // nope
   GraphicSegment(GraphicSegment&);
   GraphicSegment& operator=(const GraphicSegment&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // GraphicSegment_H
