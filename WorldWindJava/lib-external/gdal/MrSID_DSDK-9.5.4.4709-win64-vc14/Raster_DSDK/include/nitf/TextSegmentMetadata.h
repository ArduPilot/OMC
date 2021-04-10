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

#ifndef TextSegmentMetadata_H
#define TextSegmentMetadata_H

// lt_lib_base
#include "lt_base.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
namespace Nitf {


/**
 * container for Text Segment metadata
 *
 * This class is a container for all the text segment-level metadata
 * for an NITF file.  It is used with the various NITF image
 * manager, reader, and writer classes.
 *
 * For details on the length, formatting, etc, of each field,
 * consult the 2500C NITF specification.
 *
 * Usage Notes:
 * - all "set" calls will make local copies of their strings
 * - caller is responsible for blank-padding of strings
 * - caller is repsonsible for checking proper formatting
 * - all strings are initialized to reasonable defaults (usually just blanks)
 */  
class TextSegmentMetadata
{
public:
   // not for public use
   TextSegmentMetadata();

   // not for public use
   TextSegmentMetadata(const TextSegmentMetadata&);

   // not for public use
   ~TextSegmentMetadata();

   // not for public use
   TextSegmentMetadata& operator=(const TextSegmentMetadata&);

   /**
    * set the TEXTID field
    */
   LT_STATUS setTEXTID(const char*);

   /**
    * get the TEXTID field
    */
   const char* getTEXTID() const;

   /**
    * set the TEXTDT field
    */
   LT_STATUS setTXTDT(const char*);

   /**
    * get the TEXTDT field
    */
   const char* getTXTDT() const;

   /**
    * set the TXTITL field
    */
   LT_STATUS setTXTITL(const char*);

   /**
    * get the TXTITL field
    */
   const char* getTXTITL() const;

private:
   char* m_TEXTID;      // 7
   char* m_TXTDT;       // 14
   char* m_TXTITL;      // 80
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // TextSegmentMetadata_H
