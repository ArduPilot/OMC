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

#ifndef LTI_IMAGESTAGEMANAGER_H
#define LTI_IMAGESTAGEMANAGER_H

// lt_lib_mrsid_core
#include "lti_imageStage.h"


LT_BEGIN_NAMESPACE(LizardTech)

/**
 * abstract class for managing a list of image stages
 *
 * The two most common uses of this class are passing a set of images to
 * the LTIMosaicFilter and a wrapper around image formats that support
 * multiple images in one file.
 *
 * LTIImageStageManager was first added to replace the static list of
 * LTIImageStage * passed to the LTIMosaicFilter to fix memory and
 * file handle resource limitations.
 */
class LTIImageStageManager : public LTIReferenceCountedObject
{
   LTI_REFERENCE_COUNTED_BOILERPLATE_BASE(LTIImageStageManager);
public:
   /**
    * create an image properties
    *
    * Note when done with the image stage call image->release().
    *
    * @param  imageNumber  number of the image (zero based)
    * @param  image        a pointer to the created image properties
    *
    */
   virtual LT_STATUS createImageProps(lt_uint32 imageNumber,
                                      LTIImage *&imageProps);
   
   /**
    * create an image stage
    *
    * Note when done with the image stage call imageStage->release().
    *
    * @param  imageNumber  number of the image (zero based)
    * @param  imageStage   a pointer to the created image stage
    *
    */
   // HACK to get SWIG working
#ifndef SWIG
   virtual LT_STATUS createImageStage(lt_uint32 imageNumber,
                                      LTIImageStage *&imageStage);
#endif
   virtual LTIImageStage *createImageStage(lt_uint32 imageNumber);

   /**
    * get the number of images the object is managing
    */
   lt_uint32 getNumImages(void) const;

   /**
    * create an image stage that can be used as an overview of all
    * other images.  If an overview does not exsit this method returns
    * LT_STS_Success and sets imageStage to NULL.
    *
    * @param  imageStage   a pointer to the created image stage
    */
   virtual LT_STATUS createOverviewImageStage(LTIImageStage *&imageStage);

protected:
   /**
    * The derived class needs to call this
    */
   virtual void setNumImages(lt_uint32 numImages);

private:
   lt_uint32 m_numImages;
};

#ifndef DOXYGEN_EXCLUDE

class LTIImageStageManager2 : public LTIImageStageManager
{
   LTI_REFERENCE_COUNTED_BOILERPLATE_BASE(LTIImageStageManager2);
public:
   // HACK to get SWIG working
#ifndef SWIG
   /**
    * get the name of a given image/tile
    *
    * @param  imageNumber  id/number of the image
    * @param  imageName    a fileSpec to store the name into
    */
   virtual LT_STATUS getImageName(lt_uint32 imageNumber,
                                  LTFileSpec &imageName);

   /**
    * get the name of a given image/tile
    *
    * @param  imageNumber  id/number of the image
    * @param  baseName    a fileSpec to store the name into
    */
   virtual LT_STATUS getSupportFileBaseName(lt_uint32 imageNumber,
                                            LTFileSpec &baseName);
#endif
   virtual LTFileSpec getImageName(lt_uint32 imageNumber);
   virtual LTFileSpec getSupportFileBaseName(lt_uint32 imageNumber);

protected:
   virtual void setNumImages(lt_uint32 numImages)
   {
      LTIImageStageManager::setNumImages(numImages);
   }
   
};

#endif

LT_END_NAMESPACE(LizardTech)

#endif // LTI_IMAGESTAGEMANAGER_H
