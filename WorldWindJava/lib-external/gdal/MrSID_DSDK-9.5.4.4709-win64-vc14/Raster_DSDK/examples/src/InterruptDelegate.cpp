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


// This demonstrates how to derive your own interrupt delegate and use it
// inside of a decode request.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_delegates.h"
#include "lti_scene.h"
#include "lti_sceneBuffer.h"
#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);

// This class will simulate an interrupt event after N calls by the decoder
// to query the interrupt status.  In a real system, the if-test would really
// be checking some external event, e.g. a ^C or button-press.
class MyInterrupt : public LTIInterruptDelegate
{
public:
   MyInterrupt(int n) : 
      LTIInterruptDelegate(),
      m_cnt(0),
      m_max(n)
   {
      return;
   }

   LT_STATUS getInterruptStatus()
   {
      printf("interrupt called %d times\n", m_cnt);
      if (m_cnt == 10) return 999;
      ++m_cnt;
      return LT_STS_Success;
   }

public:
   int m_cnt;   // public so we can query from the test routine

private:
   const int m_max;
};


LT_STATUS InterruptDelegate()
{
   LT_STATUS sts = LT_STS_Uninit;

   // stop after 10 calls
   MyInterrupt interrupt(10);

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.sid"));
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));
   
   reader->setInterruptDelegate(&interrupt);

   const LTIScene scene(0, 0, 640, 480, 1.0);
   LTISceneBuffer bufData(reader->getPixelProps(),
                          scene.getNumCols(), scene.getNumRows(),
                          NULL);
   
   // the decode will fail, return 999 from within the delegate
   sts = reader->read(scene, bufData);
   TEST_BOOL(sts == 999);

   // verify the interrupt handler was called several times
   TEST_BOOL(interrupt.m_cnt == 10);
   
   reader->release();
   reader = NULL;
   
   return LT_STS_Success;
}
