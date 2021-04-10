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

#ifndef FileMetadata_H
#define FileMetadata_H

// lt_lib_base
#include "lt_base.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
namespace Nitf {


/**
 * container for File Header metadata
 *
 * This class is a container for all the file-level metadata
 * for an NITF file.  It is used with the various NITF image
 * manager, reader, and writer classes.
 *
 * For details on the length, formatting, etc, of each field,
 * consult the NITF specification.
 *
 * Usage Notes:
 * - all "set" calls will make local copies of their strings
 * - caller is responsible for blank-padding of strings
 * - caller is repsonsible for checking proper formatting
 * - all strings are initialized to reasonable defaults (usually just blanks)
 */  
class FileMetadata
{
public:
   // not for public use
   FileMetadata();

   // not for public use
   FileMetadata(const FileMetadata&);

   // not for public use
   ~FileMetadata();

   // not for public use
   FileMetadata& operator=(const FileMetadata&);

   /**
    * sets the OSAID field
    */
   LT_STATUS setOSTAID(const char*);

   /**
    * gets the OSAID field
    */
   const char* getOSTAID() const;

   /**
    * sets the FDT field
    */
   LT_STATUS setFDT(const char*);

   /**
    * gets the FDT field
    */
   const char* getFDT() const;

   /**
    * sets the FTITLE field
    */
   LT_STATUS setFTITLE(const char*);

   /**
    * gets the FTITLE field
    */
   const char* getFTITLE() const;

   /**
    * sets the ONAME field
    */
   LT_STATUS setONAME(const char*);

   /**
    * gets the ONAME field
    */
   const char* getONAME() const;

   /**
    * sets the famous OPHONE field
    */
   LT_STATUS setOPHONE(const char*);

   /**
    * gets the famous OPHONE field
    */
   const char* getOPHONE() const;
  
private:
   char* m_OSTAID;    // 10
   char* m_FDT;       // 14
   char* m_FTITLE;    // 80
   char* m_ONAME;         // v20 is 27, v21 is 24
   char* m_OPHONE;    // 18
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // FileMetadata_H
