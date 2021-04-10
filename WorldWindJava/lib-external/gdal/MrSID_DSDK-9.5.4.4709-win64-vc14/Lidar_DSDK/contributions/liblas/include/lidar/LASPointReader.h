/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2009-2009 LizardTech, Inc, 1008 Western      //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */     
/* PUBLIC */

#ifndef __LIDAR_LAS_POINT_READER_H__
#define __LIDAR_LAS_POINT_READER_H__

#include "lidar/PointReader.h"

class LASreader;

LT_BEGIN_LIDAR_NAMESPACE

class LASPointReader : public PointReader
{
   CONCRETE_OBJECT(LASPointReader);
public:
   void init(const char *path);
   void init(const char *path, bool tolerateUnsupportedWaveFormData);

   const char *getFileFormatString() const;
   
   void loadMetadata(Metadata &metadata, bool sanitize) const;
   
   char const * const *getClassIdNames(void) const;
   size_t getNumClassIdNames(void) const;
   
   PointIterator *createIterator(const Bounds &bounds,
                                 double fraction,
                                 const PointInfo &pointInfo,
                                 ProgressDelegate *delegate) const;

   int getRecordFormat(void) const;
protected:
   char *m_path;
   char m_fileFormatString[32];
   char **m_classId;
   size_t m_numClasses;
   int m_recordFormat;

   bool m_tolerateUnsupportedWaveFormData;
   bool m_useExtended;


   class Iterator;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_LAS_POINT_READER_H__
