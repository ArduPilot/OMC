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


#include "main.h"
#include "support.h"
#include "lt_utilStatusStrings.h"

int main()
{
   checkCwd();

   int errors = 0;

   if (LT_SUCCESS(initializeStatusStrings()))
   {
	   errors += LT_FAILURE(UserTest());

	   errors += LT_FAILURE(DecodeJP2ToBBB());
	   errors += LT_FAILURE(DecodeJP2ToJPG());
	   errors += LT_FAILURE(DecodeJP2ToMemory());

	   errors += LT_FAILURE(DecodeMrSIDBandSelection());
	   errors += LT_FAILURE(DecodeMrSIDLidar());
	   errors += LT_FAILURE(DecodeMrSIDToMemory());
	   errors += LT_FAILURE(DecodeMrSIDToRaw());
	   errors += LT_FAILURE(DecodeMrSIDToTIFF());

	   errors += LT_FAILURE(DecodeNITFToBBB());

	   errors += LT_FAILURE(DerivedImageFilter());
	   errors += LT_FAILURE(DerivedImageReader());
	   errors += LT_FAILURE(DerivedImageWriter());
	   errors += LT_FAILURE(DerivedStream());

	   errors += LT_FAILURE(GeoScene());
	   errors += LT_FAILURE(ImageInfo());
	   errors += LT_FAILURE(InterruptDelegate());
	   errors += LT_FAILURE(MetadataDump());
	   errors += LT_FAILURE(Pipeline());
	   errors += LT_FAILURE(ProgressDelegate());
	   errors += LT_FAILURE(SceneBuffer());
	   errors += LT_FAILURE(UsingCInterface());
	   errors += LT_FAILURE(UsingCStream());
	   errors += LT_FAILURE(UsingStreams());
	   errors += LT_FAILURE(ErrorHandling());  // calls terminateStatusStrings()

	   printf("%s DSDK tests\n", errors == 0 ? "passed" : "FAILED");
   }
   else
   {
	   errors += 1;
	   printf("DSDK tests failed initializing status strings\n");
   }
   
   return errors == 0 ? EXIT_SUCCESS : EXIT_FAILURE;
}
