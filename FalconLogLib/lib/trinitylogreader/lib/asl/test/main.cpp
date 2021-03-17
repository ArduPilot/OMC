/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "assert.h"
#include <iostream>

#include "quaternion.h"
#include "vector3d.h"
#include "position.h"

int main() {
  using namespace asl;
  Quaternion q1(1,0,0,0);
  auto q2 = Quaternion::fromRPY(0,0,0);
  assert(q1 == q2);
  Quaternion q3(1,5,0,0);
  assert(q1 != q3);

  auto q4 = q1;
  assert(q4 == q1);

  Vector3D v1(0,0,1);
  Vector3D v2(0,0,1);
  assert(v1 == v2);
  Vector3D v3(1,0,0);
  assert(v1 != v3);

  auto v4 = v1;
  assert(v4 == v1);


  std::cout << "all tests passed!" << std::endl;
  return 0;
}
