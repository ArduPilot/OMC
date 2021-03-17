/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef POSITION_H
#define POSITION_H

#include <vector3d.h>
#include <string>
#include <math.h>

namespace asl {

/*!
 * \brief container for GPS positions
 */
class Position {
public:
  /*!
   * \brief constructs Position Object with provided parameters, (0,0,0) if none are provided
   * \param lat latitude in decimal degrees
   * \param lon longitude in decimal degrees
   * \param height height in meters (WGS84)
   */
  Position(double lat = 0, double lon = 0, double height = 0);

  /*!
   * \brief constructs Position with data from Vector3D
   * \param vec source Vector3D
   */
  Position(Vector3D& vec);

  /*!
   * \return latitude in decimal degrees
   */
  double latitude() const;
  /*!
   * \return longitude in decimal degrees
   */
  double longitude() const;
  /*!
   * \return height in meters (WGS84)
   */
  double height() const;

  /*!
   * \brief copies the Coordinates of Position into a Vector3D
   * \return newly constructed Vector3D
   */
  Vector3D toVector3D() const;

  std::string toString() const;

  /*!
   * \return distance 2D to other Position in meters
   */
  double distance2D(const Position& other) const;

private:
  double latitude_;
  double longitude_;
  double height_;
};

// implementation

inline Position::Position(double lat, double lon, double height) :
  latitude_(lat),
  longitude_(lon),
  height_(height)
{
  if(abs(latitude_) > 85.0 || abs(longitude_) > 180.0)
  {
    latitude_ = 0;
    longitude_ = 0;
  }
}

inline Position::Position(Vector3D& v) :
  latitude_(v.x()),
  longitude_(v.y()),
  height_(v.z())
{
}

inline double Position::latitude() const {
  return latitude_;
}

inline double Position::longitude() const {
  return longitude_;
}

inline double Position::height() const {
  return height_;
}

inline Vector3D Position::toVector3D() const {
  return Vector3D(latitude_, longitude_, height_);
}

inline std::string Position::toString() const
{
  return "(" + std::to_string(latitude_) + ", " +
      std::to_string(longitude_) + ", " +
      std::to_string(height_) + ")";
}

inline double Position::distance2D(const Position& other) const
{
  // http://en.wikipedia.org/wiki/Haversine_formula
  auto deg2rad = [](double deg) -> double {
    return deg * 3.14159265358979323846 / 180.0;
  };

  double lat1r, lon1r, lat2r, lon2r, u, v;
  lat1r = deg2rad(latitude_);
  lon1r = deg2rad(longitude_);
  lat2r = deg2rad(other.latitude_);
  lon2r = deg2rad(other.longitude_);
  u = sin((lat2r - lat1r)/2);
  v = sin((lon2r - lon1r)/2);
  return 1000.0 * 2.0 * 6371.0 * asin(sqrt(u * u + cos(lat1r) * cos(lat2r) * v * v));
}
}

#endif // POSITION_H
