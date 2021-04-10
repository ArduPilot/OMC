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

#ifndef __LIDAR_POINT_READER_H__
#define __LIDAR_POINT_READER_H__

#include "lidar/PointSource.h"
#include "lidar/IO.h"

LT_BEGIN_LIDAR_NAMESPACE


/**
 *  PointReader is the base class for reading LiDAR file formats.
 *
 * The PointReader class is the base class for reading LiDAR file formats.
 * It add one method to the PointSource interface, getFileFormatString().
 */
class PointReader : public PointSource
{
   ABSTRACT_OBJECT(PointReader);
public:
   const PointInfo &getPointInfo(void) const;

   count_type getNumPoints(void) const;
   const char *getWKT(void) const;

   /*
    * Override the WKT stored in the file.
    *
    * This method alows the caller to override the WKT store in the file.  This
    * useful when the file does not have a WKT.
    */
   void overrideWKT(const char *wkt);

   /**
    * Get the file type and version.
    *
    * This method returns a string the contains the File Type and
    * version of the PointReader.  For example for a MrSID file this will
    * will return "MG4 4.0.0.1".
    */
   virtual const char *getFileFormatString(void) const = 0;

   const Bounds &getBounds(void) const;
   const double *getScale(void) const;
   const double *getOffset(void) const;

   char const * const *getClassIdNames(void) const;
   size_t getNumClassIdNames(void) const;

   double getTotalWork(const Bounds &bounds, double fraction) const;

protected:
   /**
    * Set the point information.
    *
    * This method sets the channel information.
    * All subclasses must call this function in thier init() function.
    *
    *  \param pointInfo the channel information
    *
    *  \see getPointInfo()
    */
   void setPointInfo(const PointInfo &pointInfo);
   /**
    * Set the number of points in the point cloud.
    *
    * This method sets the number of points in the point cloud.
    * All subclasses must call this function in thier init() function.
    *
    *  \param numPoints the number of points in the point cloud
    *
    *  \see getNumPoints()
    */
   void setNumPoints(count_type numPoints);
   /**
    * Set the bounding box of the point cloud.
    *
    * This method sets the bounding box of the point cloud.
    * All subclasses must call this function in thier init() function.
    *
    *  \param bounds the bounding box of the point cloud.
    *
    *  \see getBounds()
    */
   void setBounds(const Bounds &bounds);
   /**
    * Set the spatial reference system.
    *
    * This method uses a Well Known Test (WKT) string to set the spatial
    * reference system.
    *
    *  \param wkt the WKT representation of the spatial reference system
    *
    *  \see getWKT()
    */
   void setWKT(const char *wkt);
   /**
    * Set the quantization scale and offset.
    *
    * This method sets the quantization scale and offset for the point cloud.
    * If the data is not quantized do not call this function or call it with
    * scale = NULL and offset = NULL.
    *
    *  \param scale the quantization scale for the X, Y, and Z channels
    *  \param offset the offset for the X, Y, and Z channels
    *
    *  \see getScale()
    *  \see getOffset()
    */
   void setQuantization(const double scale[3], const double offset[3]);

private:
   PointInfo m_pointInfo;
   count_type m_numPoints;
   char *m_wkt;
   Bounds m_bounds;
   double m_scale[3];
   double m_offset[3];
protected:
   bool m_ignoreNativeWKTMetadata;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_POINT_READER_H__
