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

#ifndef __LIDAR_POINT_ITERATOR_H__
#define __LIDAR_POINT_ITERATOR_H__

#include "lidar/Types.h"
#include "lidar/Object.h"
#include "lidar/PointData.h"
#include "lidar/ProgressDelegate.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * PointIterator is the base class for accessing the point cloud.
 *
 * The PointIterator class is the base class for accessing the point data in
 * a PointSource.  Use PointSource::createIterator() to create a iterator.
 *
 * \see See examples/src/UserTutorial.cpp for an example of using
 *  a PointIterator.
 */
class PointIterator : public Object
{
   ABSTRACT_OBJECT(PointIterator);
public:
   /**
    * Get the next set of points.
    *
    * This method gets the next group of points.
    *
    * \param points the destination buffer
    * \return the number points written in the buffer
    */
   virtual size_t getNextPoints(PointData &points) = 0;

protected:
   /**
    * Initialize the iterator.
    *
    * This method initializes iterator.
    *
    *  \param bounds the region of interest (HUGE_VAL are handled)
    *  \param fraction the fraction of the points you want
    *                  (use 1.0 for all the points and
    *                   use 0.1 to keep every tenth point)
    *  \param pointInfo the list of channels to be extracted
    *  \param delegate a ProgressDelegate for feedback (can be NULL)
    *
    * \note init() should have the same prototype a PointSource::createIterator()
    */
   void init(const Bounds &bounds,
             double fraction,
             const PointInfo &pointInfo,
             ProgressDelegate *delegate);

   /**
    * This method does the bounds and subsample tests.
    */
   inline bool useSample(double x, double y, double z)
   {
      if(!m_bounds.contains(x, y, z))
         return false;
      else
      {
         m_accumulator += m_fraction;
         if(m_accumulator > m_cutoff)
         {
            m_accumulator -= 1;
            return true;
         }
         else
            return false;
      }
   }

   Bounds m_bounds;
   double m_fraction;
   double m_accumulator;
   double m_cutoff;
   ProgressDelegate *m_delegate;
};


#define CONCRETE_ITERATOR(classname) \
   DISABLE_COPY(classname); \
   public: \
      static classname *create(void) \
      { \
         return new classname(); \
      }

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_POINT_ITERATOR_H__
