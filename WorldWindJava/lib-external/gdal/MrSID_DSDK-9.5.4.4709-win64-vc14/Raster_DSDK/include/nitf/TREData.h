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

#ifndef TREData_H
#define TREData_H

// lt_lib_base
#include "lt_base.h"

// lt_lib_io
#include "lt_ioStreamInf.h"

#include <string.h>
#include <stdio.h>

LT_BEGIN_LIZARDTECH_NAMESPACE
namespace Nitf {


/**
 * representation of a single TRE in a NITF file
 *
 * This class is used to represent a single TRE item.
 */
class TREData
{
public:
   // not for public use
   TREData();

   // not for public use
   TREData(const TREData* tre);

   /**
    * constructor for a TRE object
    *
    * This function creates the TRE object.
    *
    * @param tag the 6-byte tag name
    * @param dataLength the length of the data field
    * @param data the data payload (bytes) of the TRE
    */
   TREData(const char* tag, int dataLength, const lt_uint8* data);

   // not for public use
   ~TREData();

   // not for public use
   lt_uint8* serialize(lt_uint8* p) const;

   // not for public use
   LT_STATUS serialize(LTIOStreamInf& stream) const;

   /**
    * returns the tag name (a 6-char array)
    */
   const char* getTag() const;

   /**
    * returns the length in bytes of the data payload
    */
   lt_uint32 getDataLength() const;

   /**
    * returns the data payload
    */
   const lt_uint8* getData() const;

private:
   void set(const char* t, int l, const lt_uint8* d);

   char* m_tag;     // 6 chars only
   int m_length;    // length of data array only; serialized as "%05d"
   lt_uint8* m_data;

   TREData(TREData&);
   TREData& operator=(const TREData&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // TREData_H
