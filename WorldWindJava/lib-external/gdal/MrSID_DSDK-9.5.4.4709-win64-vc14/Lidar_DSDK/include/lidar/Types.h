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

#ifndef __LIDAR_TYPES_H__
#define __LIDAR_TYPES_H__

#include "lidar/Base.h"
#include <math.h>
/* this should be a vaild C header file */

LT_BEGIN_LIDAR_NAMESPACE

#ifdef _WIN32
/* it looks like windows.h defined min() and max() */
#undef min
#undef max
#endif

/**
 * Range is a 1-dimensional interval
 *
 * The Range structure stores a 1-dimensional interval and defines some basic
 * operation on intervals.
 */
struct Range
{
   double min;  /* -HUGE_VAL is valid */
   double max;  /* +HUGE_VAL is valid */

#ifdef __cplusplus
   Range(double amin = +HUGE_VAL, double amax = -HUGE_VAL) :
      min(amin), max(amax) {}

   bool operator==(const Range &r) const
   {
      return min == r.min && max == r.max;
   }

   bool operator!=(const Range &r) const
   {
      return min != r.min || max != r.max;
   }
   
   /** Determine if the value is in the interval. */
   bool contains(double v) const
   {
      return min <= v && v <= max;
   }

   /** Determine if two Ranges intersect. */
   bool overlaps(const Range &r) const
   {
      // assumes both are not empty()
      return min <= r.max && max >= r.min;
   }

   /** Determine if the interval is empty. */
   bool empty(void) const
   {
      return min >= max;
   }

   /** Get the length of the interval. */
   double length(void) const
   {
      return max - min;
   }

   /** Translate the interval. */
   void shift(double v)
   {
      min += v;
      max += v;
   }

   /** Scale the interval. */
   void scale(double v)
   {
      min *= v;
      max *= v;
   }

   /** Take the intersection of two intervals */
   void clip(const Range &r)
   {
      if(r.min > min)
         min = r.min;
      if(r.max < max)
         max = r.max;
   }

   /** Take the union of two intervals */
   void grow(const Range &r)
   {
      if(r.min < min)
         min = r.min;
      if(r.max > max)
         max = r.max;
   }

   /** Expand the interval to include this value */
   void grow(double v)
   {
      if(v < min)
         min = v;
      if(v > max)
         max = v;
   }
#endif
};
typedef struct Range Range;

/**
 * Bounds is a 3-dimensional bounding box
 *
 * The Bounds structure stores a 3-dimensional bounding box and defines some
 * basic operation on bounding boxes.
 */
struct Bounds
{
   Range x;
   Range y;
   Range z;

#ifdef __cplusplus

   static const Bounds &Huge(void);

   Bounds(double xmin = +HUGE_VAL, double xmax = -HUGE_VAL,
          double ymin = +HUGE_VAL, double ymax = -HUGE_VAL,
          double zmin = +HUGE_VAL, double zmax = -HUGE_VAL) :
      x(xmin, xmax), y(ymin, ymax), z(zmin, zmax) {}

   Bounds(const Range &ax, const Range &ay, const Range &az) :
      x(ax), y(ay), z(az) {}

   bool operator==(const Bounds &b) const
   {
      return x == b.x && y == b.y && z == b.z;
   }

   bool operator!=(const Bounds &b) const
   {
      return x != b.x || y != b.y || z != b.z;
   }

   /** Determine if the a 3D points is in the bounding box. */
   bool contains(double ax, double ay, double az) const
   {
      return x.contains(ax) && y.contains(ay) && z.contains(az);
   }

   /** Determine if two Bounds intersect. */
   bool overlaps(const Bounds &b) const
   {
      return x.overlaps(b.x) && y.overlaps(b.y) && z.overlaps(b.z);
   }

   /** Determine if the bounding box is empty. */
   bool empty(void) const
   {
      return x.empty() || y.empty() || z.empty();
   }

   /** Get the volume of the bounding box. */
   double volume(void) const
   {
      return x.length() * y.length() * z.length();
   }

   /** Translate the bounding box. */
   void shift(double dx, double dy, double dz)
   {
      x.shift(dx);
      y.shift(dy);
      z.shift(dz);
   }

   /** Scale the bounding box. */
   void scale(double dx, double dy, double dz)
   {
      x.scale(dx);
      y.scale(dy);
      z.scale(dz);
   }

   /** Take the intersection of two bounding boxes */
   void clip(const Bounds &r)
   {
      x.clip(r.x);
      y.clip(r.y);
      z.clip(r.z);
   }

   /** Take the union of two bounding boxes */
   void grow(const Bounds &r)
   {
      x.grow(r.x);
      y.grow(r.y);
      z.grow(r.z);
   }

   /** Expand the bounding box to include this point */
   void grow(double ax, double ay, double az)
   {
      x.grow(ax);
      y.grow(ay);
      z.grow(az);
   }
   
   /**
    * Get how much to Bounds overlap.
    *
    * \param r1 the first Bounds
    * \param r2 the second Bounds
    * \return volume(clip(r1,r2)) / volume(r1)
    * \note This function handles +/-HUGE_VAL "right"
    */
   static double overlapFraction(const Bounds &r1, const Bounds &r2);
#endif
};
typedef struct Bounds Bounds;

LT_END_LIDAR_NAMESPACE
#endif /* __LIDAR_TYPES_H__ */
