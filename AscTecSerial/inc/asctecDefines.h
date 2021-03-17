/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#ifndef ASCTECDEFINES_H_
#define ASCTECDEFINES_H_


///version defines
#define ACI_VER_MAJOR   2
#define ACI_VER_MINOR	0

#define TRINITY_ERROR_MSGS_BUFFER_LEN 20
#define TRINITY_ERROR_MSG_SIZEOF_STRING 43

/// Defines the maximum number of different variables packets. Both (onboard and offboard) have to be compiled with
/// the setting. 3 is standard due to limited memory of the onboard code.<br>
/// It is recommended, that you don't change the value for that.
#define MAX_VAR_PACKETS 1



/**
 * \defgroup vartype Variable Types
 * \brief A List of all Variable Types.
 *
 *	The varible types are needed for describing the content of a variable. The problem is, that only the size are not only describe the type of a variable fully (mostly because of signed and unsigned). Every time, if you want to get a variable, you get the type and size of the variable for finding it from the buffer.
 */

/**
 * \defgroup callbacks Callback Functions
 * \brief This is a list of all callback functions, you have to call at the beginning of your code.
 *
 * This module describes all important functions, that should be declared at the beginning of the program. If you don't do that, the fully functionality of ACI cannot be guaranteed.
 *
 */

 // Upper three bits define size of type in memory. Lower 5 bits are IDs from 0..31 per size <br>
 // Upper six bits define size of type in memory (0..63). Lower 2 bits are IDs from 0..3 per size
#define MAX_NAME_LENGTH 32
#define MAX_DESC_LENGTH 64
#define MAX_UNIT_LENGTH 32

#define MEMPACKET_MAX_VARS 64

#define ACI_RX_BUFFER_SIZE		512
#define ACI_TX_RINGBUFFER_SIZE 	512

#define MAX_RC_VAR_LENGTH 72 // see aciGenerateRcPacketWithRtcmAndIdlFeedback()
/// ID defines
#define ID_NONE 0

///type definitions

#define LENGTH_CHAR 	1
#define LENGTH_SHORT	2
#define LENGTH_INT		4
#define LENGTH_LONGINT	8
#define LENGTH_FLOAT 	4
#define LENGTH_DOUBLE 	8


#define ACI_ADDR_NAV1_PC_ENDP 0x00
#define ACI_ADDR_NAV1_DISPLAY_ENDP 0x01
#define ACI_ADDR_NAV2_PC_ENDP 0x10
#define ACI_ADDR_NAV2_DISPLAY_ENDP 0x11
#define ACI_ADDR_DIVERSITY 0x30
#define ACI_ADDR_DIVERSITY_LCD 0x40
#define ACI_ADDR_PC 0x80
#define ACI_ADDR_PC_ALTERNATE_ROUTE 0x81
#define ACI_ADDR_NAV1_ALTERNATE_ROUTE 0x82
#define ACI_ADDR_NAV2_ALTERNATE_ROUTE 0x83
#define ACIMT_DIVERSITY_EXTCMD   0x82
#define ACIMTTx_RTCMDATA   0x87 // RTCM data packets for RTK
#define ACIMT_JOYSTICK_CMD 0x89
#define ACIMT_PC_RC_CMD	   0x90
#define ACI_ADDR_DIVERSITY   0x30
#define ACI_ADDR_NAV1_PC_ENDP   0x00


//var


// Variable type defines. upper six bits define size of type in memory (0..63). Lower 2 bits are IDs from 0..3 per size
#define VARCLASS_SIGNED   0x00
#define VARCLASS_UNSIGNED 0x01
#define VARCLASS_FLOAT	 0x02
#define VARCLASS_STRUCT   0x03
#define VARCLASS_VECTOR_FLOAT	 0x04
#define VARCLASS_VECTOR_SIGNED	 0x05
#define VARCLASS_VECTOR_UNSIGNED	 0x06


union VECT3I{
	struct { int x,y,z; };
	int elem[3];
};
typedef union VECT3I vector3i;

union VECT2I{
	struct { int x,y; };
	int elem[2];
};
typedef union VECT2I vector2i;

union QUATERNION
{
	struct { float q1, q2, q3, q4; };
	float elem[4];
};
typedef union QUATERNION quaternion;

union VECT3F{
	struct { float x,y,z; };
	float elem[3];
};
typedef union VECT3F vector3f;


union VECT2F{
	struct { float x,y; };
	float elem[2];
};
typedef union VECT2F vector2f;

/**
 * \ingroup vartype
 * \brief 1 byte types signed
 */
#define VARTYPE_INT8	((1<<3)|VARCLASS_SIGNED)

/**
 * \ingroup vartype
 * \brief 1 byte types unsigned
 */
#define VARTYPE_UINT8	((1<<3)|VARCLASS_UNSIGNED)

/**
 * \ingroup vartype
 * \brief 2 byte types signed
 */
#define VARTYPE_INT16	((2<<3)|VARCLASS_SIGNED)

/**
 * \ingroup vartype
 * \brief 2 byte types signed
 */
#define VARTYPE_UINT16	((2<<3)|VARCLASS_UNSIGNED)

/**
 * \ingroup vartype
 * \brief 4 byte types signed
 */
#define VARTYPE_INT32	((4<<3)|VARCLASS_SIGNED)
/**
 * \ingroup vartype
 * \brief 4 byte types unsigned
 */
#define VARTYPE_UINT32	((4<<3)|VARCLASS_UNSIGNED)
/**
 * \ingroup vartype
 * \brief 4 byte types signed float
 */
#define VARTYPE_SINGLE	((4<<3)|VARCLASS_FLOAT)

/**
 * \ingroup vartype
 * \brief 8 byte types signed
 */
#define VARTYPE_INT64	((8<<3)|VARCLASS_SIGNED)

/**
 * \ingroup vartype
 * \brief 8 byte types unsigned
 */
#define VARTYPE_UINT64	((8<<3)|VARCLASS_UNSIGNED)

/**
 * \ingroup vartype
 * \brief 8 byte types signed double
 */
#define VARTYPE_DOUBLE	((8<<3)|VARCLASS_FLOAT)

/**
 * \ingroup vartype
 * \brief struct with size of a
 */
#define VARTYPE_STRUCT_WITH_SIZE(a) ((a<<3)|VARCLASS_STRUCT)

/**
 * \ingroup vartype
 * \brief 3 float vector
 */
#define VARTYPE_VECTOR_3F ((12<<3)|VARCLASS_VECTOR_FLOAT)
/**
 * \ingroup vartype
 * \brief 3 int vector
 */

#define VARTYPE_VECTOR_3I ((12<<3)|VARCLASS_VECTOR_SIGNED)

/**
 * \ingroup vartype
 * \brief quaternion (4 float) vector
 */
#define VARTYPE_QUAT	 ((16<<3)|VARCLASS_VECTOR_FLOAT)

/**
 * \ingroup vartype
 * \brief 3 float vector
 */
#define VARTYPE_VECTOR_2F ((8<<3)|VARCLASS_VECTOR_FLOAT)
/**
 * \ingroup vartype
 * \brief 3 int vector
 */

#define VARTYPE_VECTOR_2I ((8<<3)|VARCLASS_VECTOR_SIGNED)




//RX states
#define ARS_IDLE 			0x00
#define ARS_STARTBYTE1 		0x01
#define ARS_STARTBYTE2 		0x02
#define ARS_MESSAGETYPE 	0x03
#define ARS_LENGTH1 		0x04
#define ARS_LENGTH2 		0x05
#define ARS_DATA 			0x06
#define ARS_CRC1 			0x07
#define ARS_CRC2 			0x08

#define ARS_DEST                        0x09
#define ARS_SOURCE                         0x0A
// ACI Packet types
//0x80 marks pakets through diversity rc packet link
//Remote->Onboard
#define ACIMT_REQUESTVARTABLEENTRIES 		0x01
#define ACIMT_GETVARTABLEINFO				0x03

#define ACIMT_REQUESTCMDTABLEENTRIES 		0x04
#define ACIMT_GETCMDTABLEINFO				0x06

#define ACIMT_REQUESTPARAMTABLEENTRIES 		0x07
#define ACIMT_GETPARAMTABLEINFO				0x09

#define ACIMT_UPDATEVARPACKET           	0x0A
//0x10-0x1f are reserved for update var packets!
#define ACIMT_CHANGEPACKETRATE				0x0B
#define ACIMT_GETPACKETRATE					0x0C
#define ACIMT_RESETREMOTE					0x0D

#define ACIMT_UPDATECMDPACKET           	0x10
//0x30-0x3f are reserved for update cmd packet config!

#define ACIMT_CMDPACKET                 	0x11
//0x40-0x4f are reserved for cmd packets!

#define ACIMT_CMDACK            	    	0x12
//0x50-0x5f are reserved for cmd packets acks!

#define ACIMT_UPDATEPARAMPACKET           	0x13
//0x60-0x6f are reserved for update param packet config!

#define ACIMT_PARAMPACKET					0x14

#define ACIMT_CMDSINGLE						0x15

//Onboard->Remote
#define ACIMT_SENDVARTABLEINFO				0x40
#define ACIMT_SENDVARTABLEENTRY				0x41
#define ACIMT_SENDVARTABLEENTRYINVALID		0x42

#define ACIMT_SENDCMDTABLEINFO				0x43
#define ACIMT_SENDCMDTABLEENTRY				0x44
#define ACIMT_SENDCMDTABLEENTRYINVALID		0x45

#define ACIMT_SENDPARAMTABLEINFO			0x46
#define ACIMT_SENDPARAMTABLEENTRY			0x47
#define ACIMT_SENDPARAMTABLEENTRYINVALID	0x48
#define ACIMT_PARAM						0x49

//was 0x50
#define ACIMT_VARPACKET                 	0x50
//0x90-0x9f are reserved for var packets!

#define ACIMT_ACK                       	0x51
#define ACIMT_PACKETRATEINFO				0x52
#define ACIMT_SINGLESEND					0x60
#define ACIMT_SINGLEREQ						0x61
#define ACIMT_MAGICCODES					0x62


#define ACIMT_UPDATERCVARPACKET				0x53
#define ACIMT_UPDATERCCMDPACKET				0x54
#define ACIMT_RCCMDPACKET					0xB0
#define ACIMT_RCVARPACKET					0xB1


//GENERAL
#define ACIMT_INFO_REQUEST					0x70
#define ACIMT_INFO_REPLY					0x71
#define ACIMT_SAVEPARAM						0x72
#define ACIMT_LOADPARAM						0x73

#define ACIMT_COCKPIT_TO_TABLET				0xD0
#define ACIMT_IDL_TO_PC  					0xD2

#define ACI_ADDR_PC_USBCOCKPIT_ENDP 		0x02

//was 0x51
#define ACI_ACK_UPDATEVARPACKET         	0x53
//0x10-0x1f are reserved for ack var packets!

#define ACI_ACK_OK                          0x01
#define ACI_ACK_CRC_ERROR                   0xF0
#define ACI_ACK_PACKET_TOO_LONG				0xF1
#define ACI_ACK_RESEND_CONFIG				0xF2

#define ACI_DBG								0x7F

//internal structures


#define ACI_REQUEST_LIST_TIMEOUT ((2 * 500 * aci->EngineRate) / 1000)
#define ACI_REQUEST_LIST_PENDING_TIMEOUT ((2 * 20 * aci->EngineRate) / 1000)
#define ACI_UPDATE_PACKET_TIMEOUT ((2 * 200 * aci->EngineRate) / 1000)
#define TIMEOUT_INVALID_PACKET 5


#endif /* ASCTECDEFINES_H_ */

// Documentation Doxygen

/**
 * \mainpage AscTec Communication Interface
 * \htmlinclude start.html
 *
 *
 */

// Irgendwas mit ATOS

#define ATOS_DBG_CORE_ERRORS 0x23
#define ATOS_DBG_CORE_ERROR_EXPANDED 0x24
#define ATOS_DBG_ERROR_EXPANDED_TEXT 0x25
#define ATOS_DBG_ERROR_EXPANDED_ERROR_NO 0x26

#define ATOS_DBG_MOTOR_ERRORFLAGS 0x7F
#define ATOS_DBG_MOTOR_OUT 0x80
#define ATOS_DBG_FLIGHTMODE 0x81
#define ATOS_DBG_BATVOLT 0x82
#define ATOS_DBG_GPS_LAT 0x83
#define ATOS_DBG_GPS_LON 0x84
#define ATOS_DBG_GPS_NUMSV 0x85
#define ATOS_DBG_GPS_HORACC 0x86
#define ATOS_DBG_GPS_SPEEDACC 0x87
#define ATOS_DBG_GPS_SPEED 0x88
#define ATOS_DBG_HEIGHT 0x89
#define ATOS_DBG_EULER_ANGLES 0x8A
#define ATOS_DBG_FLIGHT_TIME 0x8B
#define ATOS_DBG_GPS_STATUS 0x8C
#define ATOS_DBG_GPS_NUMSV 0x85
#define ATOS_DBG_GPS_QUALITY 0x8D
#define ATOS_DBG_SW_CMD_STATE 0xD4
#define ATOS_DBG_WIND_INFO  0xD5

#define ATOS_DBG_SIMPLE_CMD_FEEDBACK 0x8E
#define ATOS_DBG_SINGLE_WAYPOINT_STATUS 0x8F
#define ATOS_DBG_SIMPLE_CMD_TEST 0x90
#define ATOS_DBG_WPLIST_STATUS 0x91
#define ATOS_DBG_GPS_HEIGHT 0x92

#define ATOS_DBG_VERSION_MAJOR 0x93
#define ATOS_DBG_VERSION_MINOR 0x94
#define ATOS_DBG_VERSION_SERIAL 0x95
#define ATOS_DBG_PAYLOAD_ID 0x96
#define ATOS_DBG_PAYLOAD_OPT 0x97
#define ATOS_DBG_CAM_ROLL_CALIB 0x98
#define ATOS_DBG_CAM_PITCH 0x99
#define ATOS_DBG_NAV_STATUS 0x9A
#define ATOS_DBG_HOME_POSITION 0x9B
#define ACI_USER_VAR_BMS_HOST_STATE 0x715

#define ACI_USER_VAR_PAYLOAD_LICENSE_INFORMATION 0x750 //struct
#define ATOS_DBG_CURRENT_FEATURES			0xB6
#define ATOS_DBG_REMOTE_SLAVES_STATE        0xD3

//Mehr ATOS

#define ATOS_DBG_ANGULAR_VEL 0xa3
#define ATOS_DBG_ORG_LAT 0xa4
#define ATOS_DBG_ORG_LON 0xa5
#define ATOS_DBG_ORG_ALT 0xa6

#define ATOS_DBG_BAT_CAP_USED 0xab
#define ATOS_DBG_BAT_CAP_TOTAL 0xac

#define ATOS_DBG_PAYLOAD_ID 0x96

//ATOS commands
#define ATOS_DBG_CMD_EXPAND_ERROR 0x01
#define ATOS_DBG_CMD_SIMPLE_CMDS 0x02
#define ATOS_DBG_CMD_SINGLE_SDC  0x26
#define ATOS_DBG_CMD_SINGLE_WAYPOINT 0x03
#define ATOS_DBG_CMD_WAYPOINT_UPLOAD 0x04
#define ATOS_DBG_CMD_WPLIST_CMD 0x05
#define ATOS_DBG_CAM_ROLL_CALIB_UPDATE 0x06
#define ATOS_DBG_CMD_EXT_CONTROL 0x07

#define ATOS_DBG_CMD_EXT_CONTROL_MODE 0x10
#define ATOS_DBG_CMD_EXT_CONTROL_UPDATE 0x10
#define ATOS_DBG_CMD_EXT_CONTROL_UPDATE 0x10

#define ATOS_DBG_CMD_FLIGHTMODE_CHANGEREQ   0x25
#define ATOS_DBG_CMD_EMERGENCY_MODE 0x23

#define ATOS_DBG_CMD_WAYPOINT_UPLOAD 0x04

#define SW_CMD_STATE_TABLET 0
#define SW_CMD_STATE_IDL 1

#define REMOTE_SLAVES_STATE_FLAG_COCKPIT_CONNECTED 1
#define REMOTE_SLAVES_STATE_FLAG_IDL_CONNECTED 2
#define REMOTE_SLAVES_STATE_FLAG_IDL_JOYSTICK_CTRL 4

#define FM_CR_CLIENT_FLYING				0x14
#define FM_CR_CLIENT_FLYING_OFF         0x15
#define FM_CR_CLIENT_FALCON_BLOCKED_OFF 0x16

#define FM_ACC 0x01
#define FM_POS 0x02
#define FM_FLYING 0x04
#define FM_EMERGENCY 0x08
#define FM_TRAJECTORY 0x10
#define FM_HEIGHT 0x20
#define FM_MOTOR_CURRENT_CALIB 0x40
#define FM_AUTO_COMPASS_CALIB 0x80
#define FM_HOVER_CALIB 0x100
#define FM_HELI_OFF 0x200
#define FM_HELI_IDLE 0x400
#define FM_HELI_STARTUP 0x800
#define FM_HELI_FLYING 0x1000
#define FM_HELI_BACK2IDLE 0x2000
#define FM_OBSTACLE_AVOIDANCE 0x4000
#define FM_PAYLOAD_CALIB 0x8000
#define FM_INVERTED_HH 0x10000
#define FM_POI 0x20000
#define FM_CABLECAM 0x40000
#define FM_VIDEOMODE 0x80000
#define FM_FALCON_BLOCKED 0x100000
#define FM_AIRBORNE 0x200000
#define FM_TCF_ACTIVE 0x400000
#define FM_CEE_ACTIVE 0x00800000
#define FM_VIDEOMODE_HEIGHTSTICK_CONTROLS_CAM 0x01000000
#define FM_MAG_STRENGTH_WARNING 0x02000000
#define FM_FOLLOW_ME 0x04000000

//simple display commands
#define SCD_NONE						0x00
#define SDC_MOTOR_CURRENT_CALIB			0x01
#define SDC_AUTO_COMPASS_CALIB 			0x02
#define SDC_HOVER_CALIB 				0x03
#define SDC_PAYLOAD_CALIB 				0x04
#define SDC_START_PANO_19MM				0x05
#define SDC_COME_HOME					0x06
#define SDC_RESTORE_DEFAULT_PARAMETERS 	0x07
#define SDC_TEST_OBSTACLE_AVOIDANCE		0x08
#define SDC_FAST_STICKS					0x09
#define SDC_START_PANO_30MM				0x0A
#define SDC_START_LINE_PANO				0x0B
#define SDC_ENABLE_CABLE_CAM_MODE		0x0C
#define SDC_DISABLE_CABLE_CAM_MODE		0x0D
#define SDC_ENABLE_POI					0x0E
#define SDC_CAM_MODE_CHANGE				0x0F
#define SDC_CAM_VIDEO_SWITCH			0x10
#define SDC_DISABLE_POI					0x11
#define SDC_CAM_RECORD					0x12
#define SDC_CAM_PREVIEW					0x13
#define SDC_CAM_DAIL2_PLUS				0x14
#define SDC_CAM_DAIL2_MINUS				0x15
#define SDC_ENABLE_BOUNDING_BOX			0x16
#define SDC_DISABLE_BOUNDING_BOX		0x17
#define SDC_MOVE_RIGHT					0x18 //HELI_MONSTER functionality
#define SDC_MOVE_LEFT					0x19 //HELI_MONSTER functionality
#define SDC_CAM_PWROFF					0x1A
#define SDC_ROLLCOMP_ONOFF				0x1B
#define SDC_CAM_TRIGGER					0x1C
#define SDC_TEST_COI_WP					0x1D
#define SDC_CANCEL_NAVIGATION			0x1E
#define SDC_SET_HEADING_TO_0			0x1F

#define SDC_DELETE_ACC_CALIB			0x20
#define SDC_DELETE_MAG_CALIB			0x21
#define SDC_DELETE_TEMP_CALIB			0x22
#define SDC_DELETE_MOTOR_CURRENT_CALIB	0x23
#define SDC_DELETE_AUTO_COMPASS_CALIB	0x24
#define SDC_DELETE_HOVER_CALIB			0x25
#define SDC_DELETE_PAYLOAD_CALIB		0x26
#define SDC_DELETE_AUTO_COMPASS_CALIB_0 0x27
#define SDC_DELETE_AUTO_COMPASS_CALIB_1 0x28
#define SDC_DELETE_AUTO_COMPASS_CALIB_2 0x29
#define SDC_DELETE_AUTO_COMPASS_CALIB_3 0x2A
#define SDC_DELETE_AUTO_COMPASS_CALIB_4 0x2B
#define SDC_DELETE_AUTO_COMPASS_CALIB_5 0x2C
#define SDC_DELETE_AUTO_COMPASS_CALIB_6 0x2D
#define SDC_DELETE_AUTO_COMPASS_CALIB_7 0x2E
#define SDC_DELETE_AUTO_COMPASS_CALIB_8 0x2F

#define SDC_SETMOTORTYPE_SCORPION		0x30

#define SDC_DISTURB_GPS_FOR_TESTING_SHORT		0x31
#define SDC_DISTURB_GPS_FOR_TESTING_LONG		0x32

#define SDC_EMODE_DIRECT_LANDING		0x40
#define SDC_EMODE_COME_HOME_DIRECT		0x41
#define SDC_EMODE_COME_HOME_HIGH		0x42
#define SDC_BATSELECT_PP6250			0x43
#define SDC_BATSELECT_PP6100			0x44
#define SDC_BATSELECT_PP8300			0x45
#define SDC_VIDEOMODE_ON				0x46
#define SDC_VIDEOMODE_OFF				0x47
#define SDC_SET_HOME					0x4E
#define SDC_UNBLOCK_FALCON				0x4F

#define SDC_DELETE_ALL_FEATURES					0x50
#define SDC_SET_PHOTO_PACKAGE_FEATURE   		0x51
#define SDC_SET_VIDEO_PACKAGE_FEATURE   		0x52
#define SDC_SET_ADVANCED_VIDEO_PACKAGE_FEATURE  0x53
#define SDC_SET_SURVEY_PACKAGE_FEATURE   		0x54
#define SDC_SET_INSPECTION_PACKAGE_FEATURE 		0x55
#define SDC_SET_ICC_FEATURE				   		0x56
#define SDC_SET_RTK_FEATURE				   		0x57
#define SDC_SET_DEFAULT_FEATURES				0x58
#define SDC_UPDATE_FEATURES_FROM_CHIP			0x59

#define SDC_COPYLOG2USB							0x60
#define SDC_FORMATSD							0x61

#define SDC_SET_TESTLICENSE_FEATURE				0x62
#define SDC_BIND_YUNEEC							0x63
#define SDC_SHOOTINGSTAR_UPDATE_BL				0x64
#define SDC_SHOOTINGSTAR_UPDATE_LED				0x65
#define SDC_AMIMON_BIND							0x66

#define SDC_WP_CTRL_BY_IDL						0x67
#define SDC_WP_CTRL_BY_TABLET					0x68
#define SDC_JOYSTICK_CTRL_BY_IDL				0x69
