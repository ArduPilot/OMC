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

#ifndef SecurityBlock_H
#define SecurityBlock_H

// lt_lib_base
#include "lt_base.h"

// local
#include "nitf_types.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTIMetadataDatabase;

namespace Nitf {

class FieldReader;
class MetadataHelper;
class SecurityMetadata;


/**
 * container for security-related metadata
 *
 * This class is a container for all the security-related metadata
 * in an NITF file, including the file header security fields and the
 * security fields of the various segments.
 *
 * The actual security properties can be obtained via the
 * SecurityMetadata object returned from the getMetadata()
 * function in this class.
 */  
class SecurityBlock
{
public:
   SecurityBlock(FieldReader&, Version, const char* tagPrefix);
   ~SecurityBlock();

   LT_STATUS addMetadata(LTIMetadataDatabase&);

   const SecurityMetadata* getMetadata() const;

private:
   void read20();
   void read21();

   const Version m_version;
   FieldReader& m_reader;

   MetadataHelper* m_mdHelper;

   SecurityMetadata* m_metadata;

   // nope
   SecurityBlock(SecurityBlock&);
   SecurityBlock& operator=(const SecurityBlock&);
};


}
LT_END_LIZARDTECH_NAMESPACE

#endif // SecurityBlock_H
