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


// This demonstrates how to decode a scene from a MrSID file into a
// byte array in memory, by directly performing a decode() operation
// on a MrSIDImageReader object.


#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_scene.h"
#include "lti_sceneBuffer.h"
#include "lti_utils.h"

#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);

LT_STATUS DecodeMrSIDToMemory()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.sid"));
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));
   
   // decode the whole image at full resolution
   const LTIScene scene(0, 0, 640, 480, 1.0);
   
   // construct the buffer we're decoding into
   // note we choose to allocate our own buffer, rather than let
   // LTISceneBuffer implicitly allocate one for us
   
   lt_uint16 numBands = reader->getNumBands();
   LTIDataType datatype = reader->getDataType();
   
   const lt_uint32 bytesPerSample = LTIUtils::getNumBytes(datatype);
   const lt_uint32 numPixels = scene.getNumCols() * scene.getNumRows();
   const lt_uint32 bytesPerBands = numPixels * bytesPerSample;
   
   lt_uint8 **bsqData = new lt_uint8 *[numBands];
   for(lt_uint16 i = 0; i < numBands; i++)
      bsqData[i] = new lt_uint8[bytesPerBands];
   
   LTISceneBuffer sceneBuffer(reader->getPixelProps(),
                              scene.getNumCols(),
                              scene.getNumRows(),
                              reinterpret_cast<void **>(bsqData));
 
   // only let the decoder use the calling thread 
   // otherwise it will try to uses all the cores
   reader->setMaxWorkerThreads(1);
 
   // perform the decode
   TEST_SUCCESS(reader->read(scene, sceneBuffer));
   
   // store the BSQ data as BIP
   FILE* fp = fopen(OUTPUT_PATH("meg_cr20.raw"), "wb");
   TEST_BOOL(fp != NULL);
   for(lt_uint32 offset = 0; offset < bytesPerBands; offset += bytesPerSample)
   {
      for(lt_uint16 i = 0; i < numBands; i++)
         TEST_BOOL(fwrite(bsqData[i] + offset, bytesPerSample, 1, fp) == 1);
   }
   fclose(fp);
   
   // clean up
   for(lt_uint16 i = 0; i < numBands; i++)
      delete [] bsqData[i];
   delete [] bsqData;
   
   // make sure got the right thing
   TEST_BOOL(Compare(OUTPUT_PATH("meg_cr20.raw"), INPUT_PATH("meg_cr20.raw")));
   
   Remove(OUTPUT_PATH("meg_cr20.raw"));

   return LT_STS_Success;
}
