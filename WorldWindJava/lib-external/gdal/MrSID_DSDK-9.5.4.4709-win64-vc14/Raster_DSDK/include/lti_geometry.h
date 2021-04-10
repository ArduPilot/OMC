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

#ifndef LTI_GEOMETRY_H
#define LTI_GEOMETRY_H

// lt_lib_mrsid_core
#include "lti_types.h"

LT_BEGIN_NAMESPACE(LizardTech)


template<class T>
class LTIGeomPoint
{
public:
   LTIGeomPoint(T inX = 0, T inY = 0) :
      x(inX),
      y(inY)
   {
   }

   //   ------------------------------
   //   *   copy constructor
   //   ------------------------------
   LTIGeomPoint(const LTIGeomPoint<T>& copy) :
      x(copy.x),
      y(copy.y)

   {
   }

   //   ------------------------------
   //   *   assignment operator
   //   ------------------------------
   LTIGeomPoint<T>& operator=(const LTIGeomPoint<T>& copy)
   {
      x = copy.x;
      y = copy.y;
      return *this;
   }


   //   ----------------------------
   //   *   addition
   //   ----------------------------
   template<class T2>
   LTIGeomPoint<T>& operator +=(const LTIGeomPoint<T2>& offset)
   {
      x += offset.x;
      y += offset.y;
      return *this;
   }

   //   ----------------------------
   //   *   subtraction
   //   ----------------------------
   template<class T2>
   LTIGeomPoint<T>& operator -=(const LTIGeomPoint<T2>& offset)
   {
      x -= offset.x;
      y -= offset.y;
      return *this;
   }

   //   --------------------------------------------------------
   //   *   equality operator
   //   --------------------------------------------------------
   inline bool operator ==(const LTIGeomPoint<T>& other) const
   {
      return x == other.x && y == other.y;
   }

   //   --------------------------------------------------------
   //   *   not equals operator
   //   --------------------------------------------------------
   inline bool operator !=(const LTIGeomPoint<T>& other) const
   {
      return !operator==(other);
   }

   inline bool equal(const LTIGeomPoint<T>& other, T eps) const
   {
      return (x < other.x ? other.x - x : x - other.x) <= eps &&
             (y < other.y ? other.y - y : y - other.y) <= eps;
   }

public:
   //   --------------------------------------------------------
   //   *   Data
   //   --------------------------------------------------------
   T x;
   T y;
};



//   --------------------------------------------------------------
//   *   global point operators
//   --------------------------------------------------------------
template<class T>
LTIGeomPoint<T> operator+(const LTIGeomPoint<T>& p1, const  LTIGeomPoint<T>& p2)
{
   LTIGeomPoint<T> p3 = p1;
   p3 += p2;
   return p3;
}

template<class T>
LTIGeomPoint<T> operator-(const LTIGeomPoint<T>& p1, const  LTIGeomPoint<T>& p2)
{
   LTIGeomPoint<T> p3 = p1;
   p3 -= p2;
   return p3;
}




//   ----------------------------------------
//
//            Dimensions
//
//   ----------------------------------------

template<class T>
class LTIGeomDim
{
public:
   LTIGeomDim(T w = 0, T h = 0) :
     width(w),
     height(h)
   {}

   LTIGeomDim(const LTIGeomDim<T>& other) :
     width(other.width),
     height(other.height)
   {}

   LTIGeomDim<T>& operator=(const LTIGeomDim<T>& other)
   {
      width = other.width;
      height = other.height;
      return *this;
   }

   bool operator==(const LTIGeomDim<T>& other) const
   {
      return width == other.width && height == other.height;
   }

   bool operator!=(const LTIGeomDim<T>& other) const
   {
      return !operator==(other);
   }

public:
   T width;
   T height;
};



//   ------------------------------------------------------------
//
//            Rectangles
//
//   The LTIGeomRect class represents rectangles in 2 dimensional space.
//   A rectangle is defined as having an upper left and a lower right corner.
//   +X goes to the right and +Y goes down. (It is a left handed world.)
//
//   ------------------------------------------------------------

template<class T, bool inclusive = true>
class LTIGeomRect
{
public:
   //   ------------------------------------------------------------
   //   *   Construct from 2 points
   //   ------------------------------------------------------------
   LTIGeomRect(const LTIGeomPoint<T>& inUL, const LTIGeomPoint<T>& inLR) :
      uLeft(inUL),
      lRight(inLR)
   {}

   //   ------------------------------------------------------------
   //   *   Construct from 4 values - the first pair is upper left,
   //    second pair is lower right
   //   ------------------------------------------------------------
   LTIGeomRect(T ulx=0, T uly=1, T lrx=-1, T lry=0) :
      uLeft(ulx, uly),
      lRight(lrx, lry)
   {}


   //   ------------------------------------------------------------
   //   *   Construct from a dimension
   // Resulting rect has uLeft = (0,0) and dimensions d
   //   ------------------------------------------------------------
   LTIGeomRect(const LTIGeomDim<T> d) :
      uLeft(0,0),
      lRight(d.width - (inclusive ? 1 : 0), d.height - (inclusive ? 1 : 0))
   {}

   //   ------------------------------------------------------------
   //   *   Copy constructor
   //   ------------------------------------------------------------
      LTIGeomRect(const LTIGeomRect<T, inclusive>& copy) :
      uLeft(copy.uLeft),
      lRight(copy.lRight)
   {}

   //   ------------------------------------------------------------
   //   *   Assignment operator
   //   ------------------------------------------------------------
   LTIGeomRect<T, inclusive>& operator=(const LTIGeomRect<T, inclusive>& copy)
   {
      uLeft = copy.uLeft;
      lRight = copy.lRight;
      return *this;
   }

   //   ------------------------------------------------------------
   //   *   width
   //   ------------------------------------------------------------
   T getWidth() const
   {
      return (lRight.x - uLeft.x) + (T)(inclusive ? 1 : 0);
   }
   T width() const { return getWidth(); }    // BUG: api change

   //   ------------------------------------------------------------
   //   *   height
   //   ------------------------------------------------------------
   T getHeight() const
   {
      return (lRight.y - uLeft.y) + (T)(inclusive ? 1 : 0);
   }
   T height() const { return getHeight(); }  // BUG: api change

   //   ------------------------------------------------------------
   //   *   isEmpty
   //   ------------------------------------------------------------
   bool isEmpty() const
   {
      return getWidth() <= 0 || getHeight() <= 0;
   }

   //   ------------------------------------------------------------
   //   *   dimensions
   //   ------------------------------------------------------------
   LTIGeomDim<T> getDimensions() const
   {
      return LTIGeomDim<T>(getWidth(), getHeight());
   }

   //   ------------------------------------------------------------
   //   *   returns center point of the rectangle
   //   ------------------------------------------------------------
   LTIGeomPoint<T> getCenter() const
   {
      T cx = uLeft.x + getWidth()/2;
      T cy = uLeft.y + getHeight()/2;
      return LTIGeomPoint<T>(cx, cy);
   }


    //   ------------------------------------------------------------
   //   *   location
   // returns the location of the specified reference point
   //   ------------------------------------------------------------
   LTIGeomPoint<T> location(LTIPosition referencePoint)
   {
      LTIGeomPoint<T> ref = uLeft;
      switch (referencePoint)
      {
         case LTI_POSITION_UPPER_LEFT:
            break;
         case LTI_POSITION_UPPER_CENTER:
            ref.x += static_cast<T>(getWidth()/2.0);
            break;
         case LTI_POSITION_UPPER_RIGHT:
            ref.x = static_cast<T>(lRight.x);
            break;

         case LTI_POSITION_CENTER_LEFT:
            ref.y -= static_cast<T>(-getHeight()/2.0);
            break;
         case LTI_POSITION_CENTER:
            ref.x += static_cast<T>(getWidth()/2.0);
            ref.y -= static_cast<T>(-getHeight()/2.0);
            break;
         case LTI_POSITION_CENTER_RIGHT:
            ref.x = static_cast<T>(lRight.x);
            ref.y -= static_cast<T>(-getHeight()/2.0);
            break;

         case LTI_POSITION_LOWER_LEFT:
            ref.y = static_cast<T>(lRight.y);
            break;
         case LTI_POSITION_LOWER_CENTER:
            ref.y = static_cast<T>(lRight.y);
            ref.x += static_cast<T>(getWidth()/2.0);
            break;
         case LTI_POSITION_LOWER_RIGHT:
            ref = lRight;
            break;
      }
      return ref;
   }


   //   ------------------------------------------------------------
   //   *   addition
   //   moves the restangle by an offset
   //   ------------------------------------------------------------
   template<class T2>
   LTIGeomRect<T, inclusive>& operator+=(const LTIGeomPoint<T2>& offset)
   {
      uLeft += offset;
      lRight += offset;
      return *this;
   }

   //   ------------------------------------------------------------
   //   *   subtraction
   //   moves the restangle by an offset
   //   ------------------------------------------------------------
   template<class T2>
   LTIGeomRect<T, inclusive>& operator-=(const LTIGeomPoint<T2>& offset)
   {
      uLeft -= offset;
      lRight -= offset;
      return *this;
   }

   //   ------------------------------------------------------------
   //   *   intersection operator
   //   returns the intersection between two rectangles
   //   ------------------------------------------------------------
   LTIGeomRect<T, inclusive>& operator &=(const LTIGeomRect<T, inclusive>& other)
   {
      if(!intersect(other))
         *this = LTIGeomRect();  // invalid rectangle
      return *this;
   }

   //   ------------------------------------------------------------
   //   *   union operator
   //   returns the union of two rectangles
   //   ------------------------------------------------------------
   LTIGeomRect<T, inclusive>& operator |=(const LTIGeomRect<T, inclusive>& other)
   {
      uLeft.x = LT_MIN(uLeft.x, other.uLeft.x);
      uLeft.y = LT_MIN(uLeft.y, other.uLeft.y);
      lRight.x = LT_MAX(lRight.x, other.lRight.x);
      lRight.y = LT_MAX(lRight.y, other.lRight.y);
      return *this;
   }

   bool operator==(const LTIGeomRect<T, inclusive>& other) const
   {
      return uLeft == other.uLeft && lRight == other.lRight;
   }

   //   --------------------------------------------------------
   //   *   not equals operator
   //   --------------------------------------------------------
   inline bool operator !=(const LTIGeomRect<T, inclusive>& other) const
   {
      return !operator==(other);
   };

   // clip our rectangle to the given size, i.e. do an intersection
   // return true iff we remain a valid rectangle
   bool intersect(const LTIGeomRect<T, inclusive>& other)
   {
      uLeft.x = LT_MAX(uLeft.x, other.uLeft.x);
      uLeft.y = LT_MAX(uLeft.y, other.uLeft.y);
      lRight.x = LT_MIN(lRight.x, other.lRight.x);
      lRight.y = LT_MIN(lRight.y, other.lRight.y);

      return uLeft.x <= lRight.x && uLeft.y <= lRight.y;
   }

public:
   //   ------------------------------------------------
   //   *   Data
   //   ------------------------------------------------
   LTIGeomPoint<T> uLeft;   //   upper left corner
   LTIGeomPoint<T> lRight;   //   lower right corner
};


//   --------------------------------------------------------------
//   *   global rect operators
//   --------------------------------------------------------------
template<class T, bool inclusive>
LTIGeomRect<T, inclusive> operator+(const LTIGeomRect<T, inclusive>& r1,
                                     const LTIGeomPoint<T>& offset)
{
   LTIGeomRect<T, inclusive> r3 = r1;
   r3 += offset;
   return r3;
}

template<class T, bool inclusive>
LTIGeomRect<T, inclusive> operator-(const LTIGeomRect<T, inclusive>& r1,
                                     const LTIGeomPoint<T>& offset)
{
   LTIGeomRect<T, inclusive> r3 = r1;
   r3 -= offset;
   return r3;
}

template<class T, bool inclusive>
LTIGeomRect<T, inclusive> operator&(const LTIGeomRect<T, inclusive>& r1,
                                     const LTIGeomRect<T, inclusive>& r2)
{
   LTIGeomRect<T, inclusive> r3 = r1;
   r3 &= r2;
   return r3;
}

template<class T, bool inclusive>
LTIGeomRect<T, inclusive> operator|(const LTIGeomRect<T, inclusive>& r1,
                                     const LTIGeomRect<T, inclusive>& r2)
{
   LTIGeomRect<T, inclusive> r3 = r1;
   r3 |= r2;
   return r3;
}


//   ------------------------------------------------------------
//
//            Bound Box
//
//   The LTIGeomBBox class represents rectangles in 2 dimensional space.
//   A rectangle is defined as having a min point and a max point.
//   +X goes to the right and +Y goes up.  (It is a right handed world.)
//
//   ------------------------------------------------------------

template<class T>
class LTIGeomBBox
{
public:
   LTIGeomBBox(T minX = 0, T minY = 0, T maxX = 0, T maxY = 0) :
      xMin(minX),
      yMin(minY),
      xMax(maxX),
      yMax(maxY)
   {}

   LTIGeomBBox(const LTIGeomBBox<T>& copy) :
      xMin(copy.xMin),
      yMin(copy.yMin),
      xMax(copy.xMax),
      yMax(copy.yMax)
   {}

   LTIGeomBBox<T>& operator=(const LTIGeomBBox<T>& copy)
   {
      xMin = copy.xMin;
      yMin = copy.yMin;
      xMax = copy.xMax;
      yMax = copy.yMax;
      return *this;
   }

   T getWidth() const  { return xMax - xMin; }
   T getHeight() const { return yMax - yMin; }
   bool isEmpty() const { return xMax <= xMin || yMax <= yMin; }
   LTIGeomPoint<T> getCenter() const
   {
      return LTIGeomPoint<T>((xMin + xMax) / 2, (yMin + yMax) / 2);
   }

   // intersection operator
   LTIGeomBBox<T>& operator &=(const LTIGeomBBox<T>& other)
   {
      xMin = LT_MAX(xMin, other.xMin);
      yMin = LT_MAX(yMin, other.yMin);
      xMax = LT_MIN(xMax, other.xMax);
      yMax = LT_MIN(yMax, other.yMax);
      return *this;
   }

   // union operator
   LTIGeomBBox<T>& operator |=(const LTIGeomBBox<T>& other)
   {
      xMin = LT_MIN(xMin, other.xMin);
      yMin = LT_MIN(yMin, other.yMin);
      xMax = LT_MAX(xMax, other.xMax);
      yMax = LT_MAX(yMax, other.yMax);
      return *this;
   }

   bool overlap(const LTIGeomBBox<T>& other) const
   {
      if(xMin > other.xMax || other.xMin > xMax)
         return false;
      if(yMin > other.yMax || other.yMin > yMax)
         return false;
      return true;
   }

   bool containsPoint(const LTIGeomPoint<T> &pt) const
   {
      return (xMin <= pt.x && pt.x <= xMax) &&
             (yMin <= pt.y && pt.y <= yMax);
   }

   // equalty operators
   bool operator ==(const LTIGeomBBox<T>& other) const
   {
      return xMin == other.xMin &&
             yMin == other.yMin &&
             xMax == other.xMax &&
             yMax == other.yMax;
   }
   bool operator !=(const LTIGeomBBox<T>& other) const
   {
      return !operator==(other);
   }

public:
   T xMin;
   T yMin;
   T xMax;
   T yMax;
};

typedef LTIGeomRect<lt_int32, true>  LTIGeomIntRect;
typedef LTIGeomDim<lt_int32>         LTIGeomIntDim;
typedef LTIGeomPoint<lt_int32>       LTIGeomIntPoint;

typedef LTIGeomRect<double, false>   LTIGeomDblRect;
typedef LTIGeomDim<double>           LTIGeomDblDim;
typedef LTIGeomPoint<double>         LTIGeomDblPoint;

// BUG: these just for backwards compatability (mg3)
typedef LTIGeomRect<lt_int32, true>  MG3Rect;
typedef LTIGeomDim<lt_int32>         MG3Dim;
typedef LTIGeomPoint<lt_int32>       MG3Point;

// BUG: these just for backwards compatability (mg2)
typedef LTIGeomRect<lt_int32, true>  IntRect;
typedef LTIGeomDim<lt_int32>         IntDimension;
typedef LTIGeomPoint<lt_int32>       IntPoint;

LT_END_NAMESPACE(LizardTech)


#endif // LTI_GEOMETRY_H
