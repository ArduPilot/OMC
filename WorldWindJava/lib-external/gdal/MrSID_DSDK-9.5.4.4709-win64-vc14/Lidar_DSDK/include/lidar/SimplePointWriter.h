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

#ifndef __LIDAR_SIMPLE_POINT_WRITER_H__
#define __LIDAR_SIMPLE_POINT_WRITER_H__

#include "lidar/PointWriter.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * SimplePointWriter handles some of the bookkeeping of writing a file.
 */
class SimplePointWriter : public PointWriter
{
   ABSTRACT_OBJECT(SimplePointWriter);
public:
   void setQuantization(const double scale[3], const double offset[3]);

   const double *getScale(void) const;
   const double *getOffset(void) const;

   /**
    * \note this write() implements creating the PointIterator and reading
    * the point cloud.  Calls writeBegin(), writePoints(), and writeEnd()
    * as needed.
    */
   count_type write(const Bounds &bounds,
                    double fraction,
                    const PointInfo &pointInfo,
                    ProgressDelegate *delegate);

   /**
    * Begin writing the file
    *
    * This method is called to start writing file.
    *
    * \param channelInfo an array describing which channels are being saved
    * \param numChannels the number of channels
    */
   virtual void writeBegin(const PointInfo &pointInfo) = 0;
   /**
    * Write a set of points.
    *
    * This method is called when points need to be written.
    *
    * \param points the points to be written
    * \param numPoints the number of points to write
    * \param delegete the progress delegete to be updated (can be NULL)
    */
   virtual void writePoints(const PointData &points,
                            size_t numPoints,
                            ProgressDelegate *delegate) = 0;

   /**
    * Finish writing the file
    *
    * This method is called when no more points are left.
    *
    * \param numPoints the total number of points written
    * \param bounds the tight boundsing box the points
    *
    * \note On faliure writeEnd() is called with numPoints = 0 and
    *    bounds = Bounds::Huge()
    */
   virtual void writeEnd(PointSource::count_type numPoints,
                         const Bounds &bounds) = 0;

protected:
   /**
    * Get the amount of work needed to write the file
    *
    * This method returns the work needed to wirte the file.
    *
    *  \param bounds the region of interest (HUGE_VAL are handled)
    *  \param fraction the fraction of the points you want
    *                  (use 1.0 for all the points and
    *                   use 0.1 to keep every tenth point)
    *
    * \note This value does not include the work needed to read the source
    *  points.
    */
   virtual double getTotalWork(const Bounds &bounds,
                               double fraction) const;

   double m_offsets[3];
   double m_scale[3];
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_SIMPLE_POINT_WRITER_H__

