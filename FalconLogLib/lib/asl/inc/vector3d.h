/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef VECTOR3D_H
#define VECTOR3D_H

#include "atostypes.h"

namespace asl {

/*!
 * \brief container vector or vertex in 3D space
 *
 * it consists of three coordinates, traditionally called x, y, z.
 */
class Vector3D {
public:
  /*!
   * \brief constructs a vector with coordinates ( \a x, \a y, \a z).
   * \param x
   * \param y
   * \param z
   */
  Vector3D(double x = 0, double y = 0, double z = 0);
  /*!
   * \brief constructs a vector from atos::vector3f
   * \param v vector to construct from
   */
  Vector3D(const atos::vector3f& v);
  /*!
   * \brief constructs a vector from atos::vector3i
   * \param v vector to construct from
   */
  Vector3D(const atos::vector3i& v);

  /*!
   * \brief Equality Operator.
   *
   * Returns true if \a other is equal to this Vector3D; otherwise returns false.
   *
   * Two quaternions are considered equal if they contain the same values.
   *
   * \sa operator!=().
   */
  bool operator==(const Vector3D& other) const;
  /*!
   * \brief Inequality Operator.
   *
   * Returns true if \a other is not equal to this Vector3D; otherwise returns false.
   *
   * Two quaternions are considered equal if they contain the same values.
   *
   * \sa operator==().
   */
  bool operator!=(const Vector3D& other) const;

  /*!
   * \return x
   */
  double x() const;
  /*!
   * \return y
   */
  double y() const;
  /*!
   * \return z
   */
  double z() const;

private:
  double x_, y_, z_;
};

// implementation

inline Vector3D::Vector3D(double x, double y, double z) :
  x_(x),
  y_(y),
  z_(z)
{
}

inline Vector3D::Vector3D(const atos::vector3f& v) :
  x_(v.elem[0]),
  y_(v.elem[1]),
  z_(v.elem[2])
{
}

inline Vector3D::Vector3D(const atos::vector3i& v) :
  x_(v.elem[0]),
  y_(v.elem[1]),
  z_(v.elem[2])
{
}

inline bool Vector3D::operator==(const Vector3D& other) const {
  return x_ == other.x_ &&
      y_ == other.y_ &&
      z_ == other.z_;
}

inline bool Vector3D::operator!=(const Vector3D& other) const {
  return ! (*this == other);
}

inline double Vector3D::x() const {
  return x_;
}

inline double Vector3D::y() const {
  return y_;
}

inline double Vector3D::z() const {
  return z_;
}

}

#endif // VECTOR3D_H
