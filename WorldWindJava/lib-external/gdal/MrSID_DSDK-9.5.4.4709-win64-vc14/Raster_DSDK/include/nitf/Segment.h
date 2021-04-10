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

#ifndef Segment_H
#define Segment_H

// lt_lib_base
#include "lt_base.h"

// lt_lib_mrsid_nitf
#include "nitf_types.h"


LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIOStreamInf;
class LTIMetadataDatabase;
class NITFReaderManager;

namespace Nitf {
class SecurityBlock;
class FieldReader;
class FileHeader;
class MetadataHelper;
class SecurityMetadata;
class TREData;


/**
 * base class for segment data in an NITF file
 *
 * This class is the base class for representing all the segment types
 * in an NITF file.  It is not be used directly.
 */
class Segment
{
public:
   // not for public use
   Segment(NITFReaderManager&, LTIOStreamInf&,
           lt_int64 headerOffset, lt_int64 headerLength,
           lt_int64 dataOffset, lt_int64 dataLength,
           const char* code, int segmentNumber,
           bool doUserSubheader, bool doExtendedSubheader);

   // not for public use
   virtual ~Segment();

   /**
    * initializer
    *
    * This function must be called immediately after the constructor.
    *
    * @returns success or failure
    */
   virtual LT_STATUS initialize();

   int getSegmentNumber() const;
   const char* getSegmentID();

   // not for public use
   lt_int64 getHeaderOffset() const;

   // not for public use
   lt_int64 getHeaderLength() const;

   // not for public use
   lt_int64 getDataOffset() const;

   // not for public use
   lt_int64 getDataLength() const;

   // not for public use
   const SecurityBlock* getSecurityBlock() const;

   /**
    * returns the security information about the segment
    */
   const SecurityMetadata* getSecurityMetadata() const;

   // not for public use
   virtual LT_STATUS addMetadata(LTIMetadataDatabase&) const;

   // not for public use
   const char* getMetadataTagPrefix() const;

   /**
    * returns the number of TREs in this segment
    */
   int getNumTREs() const;

   /**
    * TRE accessor
    *
    * This function is used to access a TRE in the segment.
    *
    * @param index the (zero-based) index of the TRE to access
    * @returns the TRE data
    */
   const TREData* getTRE(int index) const;

protected:
   virtual LT_STATUS readHeader() = 0;
   virtual LT_STATUS readData() = 0;

   virtual LT_STATUS addMetadataLocal(LTIMetadataDatabase&) const =0;

   MetadataHelper* m_mdHelper;

   FieldReader* m_field;

   LTIOStreamInf& m_stream;
   const lt_int64 m_headerOffset;
   lt_int64 m_headerLength;  // not const, because FileHeader length not known
   const lt_int64 m_dataOffset;
   const lt_int64 m_dataLength;

   NITFReaderManager& m_manager;
   Version m_version;

   SecurityBlock* m_securityBlock;

   const int m_segmentNumber;
   const char* m_segmentID;

private:
   void addTREChunk(const lt_uint8* bytes, int length);
   void addTRE(const char* tag, const lt_uint8* bytes, int length);
   TREData** m_treData;
   int m_treCount;

   LT_STATUS readExtFields();
   LT_STATUS processTREs();

   const char* m_code;

   const bool m_doUserSubheader;
   const bool m_doExtendedSubheader;
   lt_int64 m_offsetUD;
   lt_int64 m_lenUD;
   int m_overflowUD;
   lt_int64 m_offsetX;
   lt_int64 m_lenX;
   int m_overflowX;

   char* m_tagPrefix;

   // nope
   Segment(Segment&);
   Segment& operator=(const Segment&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // Segment_H
