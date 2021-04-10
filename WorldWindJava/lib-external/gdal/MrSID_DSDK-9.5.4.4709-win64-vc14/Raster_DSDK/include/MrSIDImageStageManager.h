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

#ifndef __MRSID_IMAGE_STAGE_MANAGER_H__
#define __MRSID_IMAGE_STAGE_MANAGER_H__

// lt_lib_mrsid_core
#include "lti_imageStageManager.h"
#include "MrSIDImageReaderBase.h"

LT_BEGIN_NAMESPACE(LizardTech)

class MG3Container;
class MG3SingleImageReader;
class MG4SingleImageReader;
class MG2ImageReader;

class MrSIDImageStageManager : public LTIImageStageManager
{
   LTI_REFERENCE_COUNTED_BOILERPLATE(MrSIDImageStageManager);
public:
   /**
    * initializer
    *
    * @param fileSpec      file containing MrSID image
    * @param memoryUsage   control memory resource usage
    * @param streamUsage   control stream resource usage
    */
   LT_STATUS initialize(const LTFileSpec &fileSpec,
                        MrSIDMemoryUsage memoryUsage = MRSID_MEMORY_USAGE_DEFAULT,
                        MrSIDStreamUsage streamUsage = MRSID_STREAM_USAGE_DEFAULT);

   /**
    * initializer
    *
    * @param stream           stream containing MrSID image (may not be NULL)
    * @param memoryUsage      control memory resource usage
    * @param streamUsage      control stream resource usage
    */
   LT_STATUS initialize(LTIOStreamInf *stream,
                        MrSIDMemoryUsage memoryUsage = MRSID_MEMORY_USAGE_DEFAULT,
                        MrSIDStreamUsage streamUsage = MRSID_STREAM_USAGE_DEFAULT);

   // LTIImageStageManager overrides
   LT_STATUS createImageStage(lt_uint32 imageNumber,
                              LTIImageStage *&imageStage);
   LT_STATUS createOverviewImageStage(LTIImageStage *&imageStage);

   LT_STATUS createImageStage(lt_uint32 imageNumber,
                              MrSIDSingleImageReaderBase *&imageStage);
   LT_STATUS createOverviewImageStage(MrSIDSingleImageReaderBase *&imageStage);
   
   LT_STATUS getMrSIDVersion(lt_uint8& major, lt_uint8& minor,
                             lt_uint8& tweak, char& letter) const;

   void setMaxWorkerThreads(int numThreads);
   int getMaxWorkerThreads(void) const;

   // for LizardTech internal use only
   MrSIDMemoryUsage getMemoryUsage(void) const;
   // for LizardTech internal use only
   MrSIDStreamUsage getStreamUsage(void) const;

   // for LizardTech internal use only
   bool hasMG2Tiles(void) const;
   // for LizardTech internal use only
   bool hasMG3Tiles(void) const;
   // for LizardTech internal use only
   bool hasMG4Tiles(void) const;
   // for LizardTech internal use only
   bool isOptimizable(void) const;
   // for LizardTech internal use only
   bool hasOverviewImage(void) const;
   
   // for LizardTech internal use only
   /**
    * file format type of image tile
    */
   enum CompositeImageType
   {
      COMPOSITETYPE_MG2 = 1,
      COMPOSITETYPE_MG3 = 2,
      COMPOSITETYPE_MG4 = 3,
   };

   // for LizardTech internal use only
   struct TileInfo
   {
      //lt_uint32 imageNumber;
      lt_uint32 id;
      CompositeImageType type;

      lt_uint8 numLevels;
      bool isLocked;
      bool isOptimizable;

      lt_uint32 subblockSize;
      //LTIGeoCoord geoCoord;
   };

   // for LizardTech internal use only
   const TileInfo *getTileInfo(lt_uint32 index) const;
   // for LizardTech internal use only
   const TileInfo *getOverviewInfo() const;
   // for LizardTech internal use only
   lt_uint32 findTileId(lt_uint32 id) const;

   // for LizardTech internal use only
   const MG3Container *getContainer(void) const;

   // for LizardTech internal use only
   void treatMG3asMG4(void);
protected:
   LT_STATUS init(lt_uint32 numImages,
                  MrSIDMemoryUsage memoryUsage,
                  MrSIDStreamUsage streamUsage);

   virtual LT_STATUS updateMemoryModel();
   virtual LT_STATUS createMG2Reader(lt_uint32 imageNumber,
                                     MG2ImageReader *&mg2Reader);
   virtual LT_STATUS createMG3Reader(lt_uint32 imageNumber,
                                     MG3SingleImageReader *&mg3Reader);
   virtual LT_STATUS createMG4Reader(lt_uint32 imageNumber,
                                     MG4SingleImageReader *&mg4Reader);

   LTIOStreamInf *m_stream;
   bool m_ownStream;

   MrSIDMemoryUsage m_memoryUsage;
   MrSIDStreamUsage m_streamUsage;
   lt_uint8 m_major;
   lt_uint8 m_minor;
   lt_uint8 m_tweak;
   char m_letter;
   MG3Container *m_container;
   TileInfo *m_tileInfo;
   TileInfo *m_overviewInfo;
   bool m_hasMG2Tiles;
   bool m_hasMG3Tiles;
   bool m_hasMG4Tiles;
   int m_numThreads;
};


LT_END_NAMESPACE(LizardTech)


#endif // __MRSID_IMAGE_STAGE_MANAGER_H__
