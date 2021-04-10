/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


// This demonstrates how to handle errors and convert them into strings.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lt_utilStatusStrings.h"
#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS ErrorHandling()
{
   LT_STATUS sts = LT_STS_Uninit;

   // initialize error string system (required for formatted strings)
   TEST_SUCCESS(initializeStatusStrings());

   // make the image reader: we will use an invalid file
   const LTFileSpec fileSpec(INPUT_PATH("meg.hdr"));
   MrSIDImageReader* reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_FAILURE(reader->initialize(fileSpec));

   // retrieve the formatted string
   const char* str = getLastStatusString(sts);
   TEST_BOOL(strcmp(str,"invalid mrsid file format [50607]")==0);

   // close up the error string system
   TEST_SUCCESS(terminateStatusStrings());

   reader->release();
   reader = NULL;

   return LT_STS_Success;
}
