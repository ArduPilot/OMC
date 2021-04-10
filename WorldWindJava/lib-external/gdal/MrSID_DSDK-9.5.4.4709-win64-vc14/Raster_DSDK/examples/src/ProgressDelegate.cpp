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


// This demonstrates how to derive your own progress meter class.
// This example class just prints out the current percent-complete
// each time it is called.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_delegates.h"
#include "lti_scene.h"
#include "lti_sceneBuffer.h"
#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);

class MyProgress : public LTIProgressDelegate
{
public:
   MyProgress() :
      LTIProgressDelegate(),
      m_cnt(0)
   {
      return;
   }

   LT_STATUS setProgressStatus(float x)
   {
      printf("%d: %f\n", m_cnt, x);
      ++m_cnt;
      return LT_STS_Success;
   }

public:
   int m_cnt;
};


LT_STATUS ProgressDelegate()
{
   LT_STATUS sts = LT_STS_Uninit;

   // 
   MyProgress progress;

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.sid"));
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));
   
   reader->setProgressDelegate(&progress);

   const LTIScene scene(0, 0, 640, 480, 1.0);
   LTISceneBuffer bufData(reader->getPixelProps(),
                          scene.getNumCols(),
                          scene.getNumRows(),
                          NULL);
   
   // the decode will fail, return 999 from within the delegate
   sts = reader->read(scene, bufData);
   TEST_BOOL(sts == LT_STS_Success);

   // verify the interrupt handler was called several times
   TEST_BOOL(progress.m_cnt == 30);

   reader->release();
   reader = NULL;
   
   return LT_STS_Success;
}
