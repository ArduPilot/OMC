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

#ifndef ImageSegmentMetadata_H
#define ImageSegmentMetadata_H

// lt_lib_base
#include "lt_base.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
namespace Nitf {


/**
 * container for Image Segment metadata
 *
 * This class is a container for all the image segment-level metadata
 * for an existing NITF file.  It is used with the various NITF image
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
class ImageSegmentMetadata
{
public:
   // not for public use
   ImageSegmentMetadata();

   // not for public use
   ImageSegmentMetadata(const ImageSegmentMetadata&);

   // not for public use
   ~ImageSegmentMetadata();

   // not for public use
   ImageSegmentMetadata& operator=(const ImageSegmentMetadata&);

   /**
    * sets the IID1 field
    */
   LT_STATUS setIID1(const char*);

   /**
    * gets the IID1 field
    */
   const char* getIID1() const;

   /**
    * sets the IDATIM field
    */
   LT_STATUS setIDATIM(const char*);

   /**
    * gets the IDATIM field
    */
   const char* getIDATIM() const;

   /**
    * sets the TGTID field
    */
   LT_STATUS setTGTID(const char*);

   /**
    * gets the TGTID field
    */
   const char* getTGTID() const;

   /**
    * sets the IID2 field
    */
   LT_STATUS setIID2(const char*);

   /**
    * gets the IID2 field
    */
   const char* getIID2() const;

   /**
    * sets the ISORCE field
    */
   LT_STATUS setISORCE(const char*);

   /**
    * gets the ISORCE field
    */
   const char* getISORCE() const;

   /**
    * sets the number of ICOM fields (NICOM)
    *
    * This function sets the number of ICOM fields.
    *
    * @param count the number of ICOM fields to add
    * @return success or failure status code
    */
   LT_STATUS setNICOM(int count);

   /**
    * sets an ICOM field
    *
    * This function sets an ICOM field.
    *
    * @param index the number of ICOM feild to record
    * @param data the data contents of the COM field
    * @return success or failure status code
    */
   LT_STATUS setICOM(int index, const char* data);

   /**
    * gets the number of ICOM fields
    */
   int getNICOM() const;

   /**
    * gets an ICOM field
    *
    * This function gets an ICOM field.
    *
    * @param index the index of the ICOM field to access
    * @return the ICOM data string
    */
   const char* getICOM(int index) const;
  
private:
   char* m_IID1;      // 10
   char* m_IDATIM;    // 14
   char* m_TGTID;     // 17
   char* m_IID2;      // 80
   char* m_ISORCE;    // 42
   int m_NICOM;
   char** m_ICOM;     // 80
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // ImageSegmentMetadata_H
