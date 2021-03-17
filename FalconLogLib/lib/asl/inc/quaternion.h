/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef QUATERNION_H
#define QUATERNION_H

#include "atostypes.h"
#include <math.h>
#include <vector3d.h>

#ifndef M_PI
  #define M_PI 3.14159265358979323846
#endif

namespace asl {

/*!
 * \brief container for a quaternion
 *
 * provides basic mathematical functionality of quaternion interactions
 */
class Quaternion {
public:
  /*!
   * \brief constructs Quaternion with provided parameters.
   *        if none provided a unit quaternion will be constructed
   * \param w
   * \param x
   * \param y
   * \param z
   */
  Quaternion(double w = 1, double x = 0, double y = 0, double z = 0);

  /*!
   * \brief constructs a Quaternion with data from atos::quaternion
   * \param q source atos::quaternion
   */
  Quaternion(atos::quaternion q);

  /*!
   * \brief constructs Quaternion from euler angles
   * \param roll
   * \param pitch
   * \param yaw
   * \return newly constructed Quaternion
   */
  static Quaternion fromRPY(double roll, double pitch, double yaw);

  /*!
   * \brief constructs Quaternion from euler angles
   * \param v source Vector3D with euler angles
   * \return newly constructed Quaternion
   * \sa fromRPY(double roll, double pitch, double yaw);
   */
  static Quaternion fromRPY(const Vector3D& v);

  /*!
   * \brief Equality Operator.
   *
   * Returns true if \a other is equal to this Quaternion; otherwise returns false.
   *
   * Two quaternions are considered equal if they contain the same values.
   *
   * \sa operator!=().
   */
  bool operator==(const Quaternion& other) const;
  /*!
   * \brief Inequality Operator.
   *
   * Returns true if \a other is not equal to this Quaternion; otherwise returns false.
   *
   * Two quaternions are considered equal if they contain the same values.
   *
   * \sa operator==().
   */
  bool operator!=(const Quaternion& other) const;

  /*!
   * \return w
   */
  double w() const;

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

  // Vector3D rpy() const;

private:
  double w_, x_, y_, z_;
};

// implementation

inline Quaternion::Quaternion(double w, double x, double y, double z) :
  w_(w),
  x_(x),
  y_(y),
  z_(z)
{
}

inline Quaternion::Quaternion(atos::quaternion q) :
  w_(q.elem[0]),
  x_(q.elem[1]),
  y_(q.elem[2]),
  z_(q.elem[3])
{
}

inline Quaternion Quaternion::fromRPY(double roll, double pitch, double yaw) {
  roll = roll * M_PI / 180.0;
  pitch = pitch * M_PI / 180.0;
  yaw = yaw * M_PI / 180.0;
  const double fSinPitch(sin(pitch * 0.5));
  const double fCosPitch(cos(pitch * 0.5));
  const double fSinYaw(sin(yaw * 0.5));
  const double fCosYaw(cos(yaw * 0.5));
  const double fSinRoll(sin(roll * 0.5));
  const double fCosRoll(cos(roll * 0.5));
  const double fCosPitchCosYaw(fCosPitch * fCosYaw);
  const double fSinPitchSinYaw(fSinPitch * fSinYaw);
  Quaternion quat;
  quat.w_ = fCosRoll * fCosPitchCosYaw     + fSinRoll * fSinPitchSinYaw;
  quat.x_ = fSinRoll * fCosPitchCosYaw     - fCosRoll * fSinPitchSinYaw;
  quat.y_ = fCosRoll * fSinPitch * fCosYaw + fSinRoll * fCosPitch * fSinYaw;
  quat.z_ = fCosRoll * fCosPitch * fSinYaw - fSinRoll * fSinPitch * fCosYaw;
  return quat;
}

inline Quaternion Quaternion::fromRPY(const Vector3D& v)
{
  return fromRPY(v.x(), v.y(), v.z());
}

inline bool Quaternion::operator==(const Quaternion& other) const {
  return w_ == other.w_ &&
      x_ == other.x_ &&
      y_ == other.y_ &&
      z_ == other.z_;
}

inline bool Quaternion::operator!=(const Quaternion& other) const {
  return ! (*this == other);
}

inline double Quaternion::w() const {
  return w_;
}

inline double Quaternion::x() const {
  return x_;
}

inline double Quaternion::y() const {
  return y_;
}

inline double Quaternion::z() const {
  return z_;
}

//inline Vector3D Quaternion::rpy() const
//{
//  const double sqw = w_ * w_;
//  const double sqx = x_ * x_;
//  const double sqy = y_ * y_;
//  const double sqz = z_ * z_;

//  const double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
//  const double test = x_ * y_ + z_ * w_;

//  double heading, attitude, bank;

//  if (test > 0.499 * unit) { // singularity at north pole
//    heading = 2 * atan2(x_, w_) * 180 / M_PI;
//    attitude = 90;
//    bank = 0;
//  } else if (test < - 0.499 * unit) { // singularity at south pole
//    heading = - 2 * atan2(x_, w_) * 180 / M_PI;
//    attitude = - 90;
//    bank = 0;
//  } else {
//    heading = atan2(2 * y_ * w_ - 2 * x_ * z_, sqx - sqy - sqz + sqw) * 180 / M_PI;
//    attitude = asin(2 * test / unit) * 180 / M_PI;
//    bank = atan2(2 * x_ * w_ - 2 * y_ * z_, - sqx + sqy - sqz + sqw) * 180 / M_PI;
//  }
//  return Vector3D(attitude, heading, bank);
//}

}


#endif // QUATERNION_H
