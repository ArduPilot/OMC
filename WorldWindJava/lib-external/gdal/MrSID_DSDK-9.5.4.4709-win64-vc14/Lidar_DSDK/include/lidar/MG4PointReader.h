/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008-2010 LizardTech, Inc, 1008 Western      //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef __LIDAR_MG4_POINT_READER_H__
#define __LIDAR_MG4_POINT_READER_H__

#include "lidar/PointReader.h"
#include "lidar/Stream.h"

LT_BEGIN_LIDAR_NAMESPACE

class WaveletDecoderInfo;

/**
 * MG4PointReader reads LiDAR-based MrSID files.
 *
 * The MG4PointReader class reads MrSID files that contain LiDAR data.
 * 
 * \see See examples/src/DecodeMG4ToTXT.cpp and examples/src/UserTutorial.cpp
 *  for examples on reading MG4 files.
 */
class MG4PointReader : public PointReader
{
   CONCRETE_OBJECT(MG4PointReader);
public:
   /**
    * Initalize with a filename
    *
    * This method initalizes the reader with a filename.
    *
    * \param path the filename
    */
   void init(const char *path);
   /**
    * Initalize with a IO object
    *
    * This method initalizes the reader with a IO object.
    *
    * \param io the data source
    */
   void init(IO *io);
   
   void loadMetadata(Metadata &metadata, bool sanitize) const;
   
   char const * const *getClassIdNames(void) const;
   size_t getNumClassIdNames(void) const;
   
   const char *getFileFormatString(void) const;

   double getTotalWork(const Bounds &bounds, double fraction) const;
   PointIterator *createIterator(const Bounds &bounds,
                                 double fraction,
                                 const PointInfo &pointInfo,
                                 ProgressDelegate *delegate) const;

protected:
   char m_version[32];
   
   WaveletDecoderInfo *m_waveletInfo;
   IO::Location m_metadata;
   char **m_classId;
   size_t m_numClasses;
   bool m_mergingFloats;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_MG4_POINT_READER_H__
