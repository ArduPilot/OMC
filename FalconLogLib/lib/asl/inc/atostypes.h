/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef MESSAGEDEFINES_H
#define MESSAGEDEFINES_H

#include <stdint.h>

namespace asl {
namespace atos {

#define ATOS_MSG_MAX_DESC_LENGTH 64
#define ATOS_MSG_MAX_UNIT_LENGTH 16
#define ATOS_MSG_MAX_MSG_NAME_LENGTH 64

struct ATOS_MSG_STRUCT_ENTRY
{
    char name[ATOS_MSG_MAX_DESC_LENGTH];
    char desc[ATOS_MSG_MAX_DESC_LENGTH];
    char unit[ATOS_MSG_MAX_UNIT_LENGTH];
    unsigned char varType;
    unsigned short arrayCnt;
};

struct ATOS_MSG_INFO
{
    char name[ATOS_MSG_MAX_MSG_NAME_LENGTH];
    char desc[ATOS_MSG_MAX_DESC_LENGTH];
    unsigned short msgId;
    unsigned short noOfStructEntries;
    // const struct ATOS_MSG_STRUCT_ENTRY * structEntries;
};

#pragma pack(push, 1)
struct MESSAGE_HEAD
{
    uint32_t timeStampUs;
    unsigned short timeStampHHours, messageID, flags, size;
};

enum class TimeUnit {
  HalfMicrosecond,
  Microsecond,
  Millisecond,
  Second,
};

struct MESSAGE_HEAD_2
{
    TimeUnit timeUnit : 8;
    uint64_t timestamp : 56;
    uint16_t messageID, flags;
};
#pragma pack(pop)


union VECT3I {
  struct {
    int x, y, z;
  };
  int elem[3];
};
typedef union VECT3I vector3i;

union VECT2I {
  struct {
    int x, y;
  };
  int elem[2];
};
typedef union VECT2I vector2i;

union QUATERNION {
  struct {
    float q1, q2, q3, q4;
  };
  struct {
    float w, x, y, z;
  };
  float elem[4];
};
typedef union QUATERNION quaternion;

union VECT3F {
  struct {
    float x, y, z;
  };
  float elem[3];
};
typedef union VECT3F vector3f;

union VECT2F {
  struct {
    float x, y;
  };
  float elem[2];
};
typedef union VECT2F vector2f;

enum varTypes {
 VARTYPE_UNKNOWN = 0x00,
 VARTYPE_INT8 = 0x01,
 VARTYPE_UINT8 = 0x02,
 VARTYPE_INT16 = 0x03,
 VARTYPE_UINT16 = 0x04,
 VARTYPE_INT32 = 0x05,
 VARTYPE_UINT32 = 0x06,
 VARTYPE_INT64 = 0x07,
 VARTYPE_UINT64 = 0x08,
 VARTYPE_VECTOR3I = 0x09,
 VARTYPE_VECTOR3F = 0xA,
 VARTYPE_QUAT = 0x0B,
 VARTYPE_SINGLE = 0x0C,
 VARTYPE_DOUBLE = 0x0D
};

inline unsigned int varSize(varTypes type)
{
    switch(type)
    {
    case VARTYPE_INT8:
    case VARTYPE_UINT8:
        return 8;
        break;
    case VARTYPE_INT16:
    case VARTYPE_UINT16:
        return 16;
        break;
    case VARTYPE_INT32:
    case VARTYPE_UINT32:
    case VARTYPE_SINGLE:
        return 32;
        break;
    case VARTYPE_INT64:
    case VARTYPE_UINT64:
    case VARTYPE_DOUBLE:
        return 64;
        break;
    case VARTYPE_VECTOR3F:
    case VARTYPE_VECTOR3I:
        return 96;
        break;
    case VARTYPE_QUAT:
        return 128;
        break;
    case VARTYPE_UNKNOWN:
    default:
        return 0;
        break;
    }
}

template<typename T>
struct Type2VarType {
    static const varTypes value = VARTYPE_UNKNOWN;
};

#define SpecializeType2VarTypeTemplate(aciType, storageType) \
    template<> \
    struct Type2VarType<storageType>{ \
    static const varTypes value = aciType; \
};

SpecializeType2VarTypeTemplate(VARTYPE_DOUBLE, double)
SpecializeType2VarTypeTemplate(VARTYPE_SINGLE, float)
SpecializeType2VarTypeTemplate(VARTYPE_INT16, int16_t)
SpecializeType2VarTypeTemplate(VARTYPE_INT32, int32_t)
SpecializeType2VarTypeTemplate(VARTYPE_INT64, int64_t)
SpecializeType2VarTypeTemplate(VARTYPE_INT8, int8_t)
SpecializeType2VarTypeTemplate(VARTYPE_UINT8, uint8_t)
SpecializeType2VarTypeTemplate(VARTYPE_UINT16, uint16_t)
SpecializeType2VarTypeTemplate(VARTYPE_UINT32, uint32_t)
SpecializeType2VarTypeTemplate(VARTYPE_UINT64, uint64_t)
SpecializeType2VarTypeTemplate(VARTYPE_QUAT, quaternion)
SpecializeType2VarTypeTemplate(VARTYPE_VECTOR3F, vector3f)
SpecializeType2VarTypeTemplate(VARTYPE_VECTOR3I, vector3i)

enum class FlightmodeFlags {
  ACC = 0x01,
  POS  =  0x02,
  FLYING = 0x04,
  EMERGENCY = 0x08,
  TRAJECTORY = 0x10,
  HEIGHT = 0x20,
  MOTOR_CURRENT_CALIB = 0x40,
  AUTO_COMPASS_CALIB =  0x80,
  HOVER_CALIB = 0x100,
  HELI_OFF = 0x200,
  HELI_IDLE = 0x400,
  HELI_STARTUP = 0x800,
  HELI_FLYING = 0x1000,
  HELI_BACK2IDLE = 0x2000,
  OBSTACLE_AVOIDANCE = 0x4000,
  PAYLOAD_CALIB = 0x8000,
  INVERTED_HH = 0x10000,
  POI = 0x20000,
  CABLECAM = 0x40000,
  VIDEOMODE = 0x80000,
  FALCON_BLOCKED = 0x100000,
  AIRBORNE = 0x200000,
  TCF_ACTIVE = 0x400000,
  CEE_ACTIVE = 0x00800000,
  VIDEOMODE_HEIGHTSTICK_CONTROLS_CAM = 0x01000000,
  MAG_STRENGTH_WARNING = 0x02000000
};

}
}

#endif // MESSAGEDEFINES_H
