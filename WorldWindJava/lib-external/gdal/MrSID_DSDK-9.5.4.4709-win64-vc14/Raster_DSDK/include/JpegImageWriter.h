/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2004 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef JPEGIMAGEWRITER_H
#define JPEGIMAGEWRITER_H

// lt_lib_mrsid_core
#include "lti_geoFileImageWriter.h"

#if defined(LT_COMPILER_MS)
   #pragma warning(push,4)
#endif

LT_BEGIN_NAMESPACE(LizardTech)

/**
 * writes an image stage to a JPEG file
 *
 * This class writes an image stage to a JPEG file.
 *
 */
class JpegImageWriter : public LTIGeoFileImageWriter
{
   LT_DISALLOW_COPY_CONSTRUCTOR(JpegImageWriter);
public:
   JpegImageWriter(void);
   virtual ~JpegImageWriter(void);
   /**
    * initialize
    *
    * Initialize a writer for JPEG images.
    *
    * @param  imageStage the image to write from
    * @param  quality    sets the JPEG "quality" encoding parameter;
    *                    this is a value between 0 and 100
    * @param  smoothing  sets the JPEG "smoothing" encoding parameter;
    *                    this is a value between 0 and 100
    */
   LT_STATUS initialize(LTIImageStage *imageStage,
                        lt_int32 quality = 0,      // 0-100
                        lt_int32 smoothing = 0);   // 0-100   

   LT_STATUS writeBegin(const LTIScene& scene);
   LT_STATUS writeStrip(LTISceneBuffer& stripBuffer, const LTIScene& stripScene);
   LT_STATUS writeEnd();

private:
   struct ErrorManager;
   struct StreamManager;
   
   LT_STATUS writeBegin8(const LTIScene& scene);
   LT_STATUS writeStrip8(LTISceneBuffer& stripBuffer, const LTIScene& stripScene);
   LT_STATUS writeEnd8();

   LT_STATUS writeBegin12(const LTIScene& scene);
   LT_STATUS writeStrip12(LTISceneBuffer& stripBuffer, const LTIScene& stripScene);
   LT_STATUS writeEnd12();
   
   //two parameters for setting compression quality
   lt_int32 m_quality;
   lt_int32 m_smoothingFactor;
   
   void *m_jpeg;
   ErrorManager *m_error;
   bool m_use8;
};


LT_END_NAMESPACE(LizardTech)

#if defined(LT_COMPILER_MS)
   #pragma warning(pop)
#endif

#endif // JPEGIMAGEWRITER_H
