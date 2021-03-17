/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#ifndef ACI_WAYPOINT_STRUCTS_H
#define ACI_WAYPOINT_STRUCTS_H

#pragma pack(1)

#define NAV_WPLIST_IDLE 0x00
#define NAV_WPLIST_CLEAR 0x01
#define NAV_WPLIST_ADD 0x02
#define NAV_WPLIST_INSERT 0x03
#define NAV_WPLIST_DELETE 0x04
#define NAV_WPLIST_START_FLIGHT 0x05
#define NAV_WPLIST_GOTO_SINGLE 0x06
#define NAV_WPLIST_ABORT_FLIGHT 0x07
#define NAV_WPLIST_STORE_TO_FLASH 0x08
#define NAV_WPLIST_LOAD_FROM_FLASH 0x09
#define NAV_WPLIST_GET_ENTRY 0x0A
#define NAV_WPLIST_PREPARE_MULTIPLE_WP_UPLOAD 0x0B
#define NAV_WPLIST_GET_MULTIPLE_STATUS 0x0C
#define NAV_WPLIST_AUTO_GEN_MATRIX 0x0D
#define NAV_WPLIST_RESET_CMDCNT 0x12
#define NAV_WPLIST_AUTO_LAND_TAKEOFF 0x13

#define NAV_WPBUFFER_ERROR_CRC 0x01
#define NAV_WPBUFFER_ERROR_BLOCKED 0x02
#define NAV_WPBUFFER_ERROR_FULL 0x03
#define NAV_WPBUFFER_STATUS_ACK 0x04
#define NAV_WPBUFFER_STATUS_ERROR 0x05
#define NAV_WPBUFFER_CLEAR 0x0E
#define NAV_WPBUFFER_ADD 0x0F
#define NAV_WPBUFFER_STATUS 0x10

#define NAV_WPBUFFER_MAX_WAYPOINTS 32

#define NAV_WPLIST_STATUS_BLOCKED 0xFF
#define NAV_WPLIST_STATUS_ACK 0x01
#define NAV_WPLIST_STATUS_ERROR 0x02
#define NAV_WPLIST_STATUS_MULTIMODE_ACTIVE 0x03

typedef enum {
 NAV_WPLIST_ERROR_CRC = 0x01,
 NAV_WPLIST_ERROR_TOO_MANY_WPS = 0x02,
 NAV_WPLIST_ERROR_BLOCKED = 0x03,
 NAV_WPLIST_ERROR_WRONG_PARAM = 0x04,
 NAV_WPLIST_ERROR_WRONG_WPS_CNT = 0x05,
 NAV_WPLIST_ERROR_WRONG_MODE = 0x06,
 NAV_WPLIST_ERROR_NO_LICENSE = 0x07,
} navWpListError_t;

#define NAV_EVENT_NONE 0x00
#define NAV_EVENT_TRIGGER 0x01
#define NAV_WPLIST_MAX_WAYPOINTS 30

#define WP_FLAG_PREPARED_FOR_UPLOAD 0x80000000
#define WP_FLAG_CAM_PITCH_ACTIVE 0x00000001
#define WP_FLAG_CAM_ROLL_ACTIVE 0x00000002
#define WP_FLAG_CAM_YAW_ACTIVE 0x00000004
#define WP_FLAG_START_MOTORS 0x00000008
#define WP_FLAG_STOP_MOTORS 0x00000010
#define WP_FLAG_YAW_ACTIVE 0x00000020
#define WP_FLAG_HEIGHT_ACTIVE 0x00000040
#define WP_FLAG_RELATIVE_COORDS 0x00000080
#define WP_FLAG_STOP 0x00000100
#define NAV_FLAG_COI 0x20000000
#define NAV_FLAG_ABSOLUTE_ORIGIN 0x00010000
#define NAV_FLAG_HEADING_FIXED 0x00020000
#define NAV_FLAG_HEADING_BY_SEPERATE_PATH 0x00040000
#define NAV_FLAG_CAMERA_ANGLE_BY_PATH 0x00080000
#define NAV_FLAG_ROTATE_PATH_TO_START_HEADING 0x00100000
#define NAV_FLAG_SINGLE_WAYPOINTS 0x00200000
#define NAV_FLAG_MATRIX_SPLINE 0x00400000
#define NAV_FLAG_CUBIC_SPLINE 0x00800000
#define NAV_FLAG_SINGLE_WP_SPLINE                        0x40000000
#define NAV_FLAG_PANO 0x01000000
#define NAV_FLAG_RING_BUFFER 0x02000000

typedef enum {
	NAV_STATUS_IDLE = 0x0001,
	NAV_STATUS_ACCELERATING = 0x0002,
	NAV_STATUS_TRAVELING = 0x0004,
	NAV_STATUS_DECELERATING = 0x0008,
	NAV_STATUS_WAITING = 0x0010,
	NAV_STATUS_EVENT1 = 0x0020,
	NAV_STATUS_EVENT2 = 0x0040,
	NAV_STATUS_REACHED = 0x0080,
	NAV_STATUS_FLYING_TO_O = 0x0100,
	NAV_STATUS_PAUSE = 0x0200,
	NAV_STATUS_RESUMING = 0x0400,
	NAV_STATUS_ABORTED = 0x0800,
	NAV_STATUS_FLYING_HOME = 0x1000,
	NAV_STATUS_TAKING_OFF = 0x2000,
	NAV_STATUS_LANDING = 0x4000,
} navStatus_t;

#define SDC_MOTOR_CURRENT_CALIB              0x01
#define SDC_AUTO_COMPASS_CALIB               0x02
#define SDC_HOVER_CALIB                      0x03
#define SDC_PAYLOAD_CALIB                           0x04
#define SDC_START_PANO_19MM                         0x05
#define SDC_COME_HOME                               0x06
#define SDC_START_PANO_30MM                         0x0A
#define SDC_START_LINE_PANO                         0x0B
#define SDC_ENABLE_CABLE_CAM_MODE            0x0C
#define SDC_DISABLE_CABLE_CAM_MODE           0x0D
#define SDC_ENABLE_POI                              0x0E
#define SDC_CAM_MODE_CHANGE                         0x0F
#define SDC_CAM_VIDEO_SWITCH                 0x10
#define SDC_DISABLE_POI                             0x11

#define SDC_EMODE_DIRECT_LANDING       0x40
#define SDC_EMODE_COME_HOME_DIRECT           0x41
#define SDC_EMODE_COME_HOME_HIGH       0x42
#define SDC_BATSELECT_PP6250                 0x43
#define SDC_BATSELECT_PP6100                 0x44
#define SDC_BATSELECT_PP8300                 0x45

struct ATOS_MSG_WPLIST_CMD {
  unsigned char wpListCmd;//VT_UINT8////
  unsigned short wpListParam1;//VT_UINT16////
  unsigned short wpListParam2;//VT_UINT16////
  unsigned char wpListCmdCycleCnt;//VT_UINT8////
};

struct ATOS_MSG_WPLIST_STATUS {
  unsigned char wpListStatus;//VT_UINT8////
  unsigned short wpListParam1;//VT_UINT16////
  unsigned short wpListParam2;//VT_UINT16////
  unsigned char wpListStatusCycleCnt;//VT_UINT8////
};

struct ATOS_MSG_NAVIGATION_STATUS {
  unsigned int status;//VT_UINT32////
  unsigned short waypointId;//VT_UINT16//id of the currently active waypoint//
  float distanceToWaypoint;//VT_SINGLE////
  float distanceToGoal;//VT_SINGLE////
  unsigned char cycleCnt;//VT_UINT8////
};


struct ATOS_WAYPOINT {
  unsigned short id;//VT_UINT16//waypoint id//
  int lat;//VT_SINGLE//latitude//decimal degrees * 10000000
  int lon;//VT_SINGLE//longtiude//decimal degrees * 10000000
  float height;//VT_SINGLE//height above starting point//m
  short camAnglePitch;//VT_INT16//cam pitch angle//*100th deg
  short camAngleRoll;//VT_INT16//cam roll angle//100th deg
  unsigned short camAngleYaw;//VT_UINT16//cam yaw angle//100th deg
  float speed;//VT_SINGLE//max speed//m/s
  float desiredAcceleration;//VT_SINGLE//acceleration to reach speed and 0//m/s
  unsigned int flags;//VT_UINT32//flags//
  unsigned char event1;//VT_UINT8//event. 0x01=trigger//
  unsigned char event2;//VT_UINT8////
  unsigned short waitTimeEvent1;//VT_UINT16//time in ms//
  unsigned short waitTimeEvent2;//VT_UINT16//time in ms//
  unsigned short parameterEvent1;//VT_UINT16////
  unsigned short parameterEvent2;//VT_UINT16////

};

struct ATOS_MSG_HOME_POSITION {
  unsigned char
  status;//VT_UINT8//0=invalid 1=valid 2=position set while flying (due to late GPS l//
  int latitude;//VT_INT32////
  int longitude;//VT_INT32////
  unsigned int tow;//VT_UINT32////
  unsigned int week;//VT_UINT32////
  int gps_height;//VT_INT32////
};

struct ATOS_RF_API_LINKINFO_REDUCED {
  unsigned char local0_snr[12];
  unsigned char local1_snr[12];
  unsigned char peer0_snr[12];
  unsigned char peer1_snr[12];
  unsigned char local_received_packets[2];
  unsigned char peer_received_packets[2];
  unsigned char versionMajor;
  unsigned char versionMinor;
  unsigned int serial;

};

struct ATOS_MSG_EXT_COMMAND {
  unsigned int flags;//VT_UINT32//see EXT_CMD_FLAG_//
  short pitch;//VT_INT16//0..4095. Mid=2048//
  short roll;//VT_INT16//0..4095//
  short yaw;//VT_INT16//0..4095//
  short thrust;//VT_INT16//0..4095//
  short mode;//VT_INT16////0..4095. 2048=he
  short cam_pitch;//VT_INT16////0..4095
  short poweronoff;//VT_INT16//0..4095//
  short cam_yaw;//VT_INT16////0..4095
  short trigger;//VT_INT16//0..4095//
  short aux1;//VT_INT16//0..4095//
  short aux2;//VT_INT16//0..4095//
  unsigned short buttons;//VT_UINT16//16 buttons//
  unsigned char updateCnt;//VT_UINT8//inc always by 1//
};
#pragma pack()



#endif // ACI_WAYPOINT_STRUCTS_H
