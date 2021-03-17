/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#include "aci_waypoint_structs.h"
/* Header for class eu_mavinci_desktop_rs232_AscTecCommunication */

#ifndef _Included_eu_mavinci_desktop_rs232_AscTecCommunication
#define _Included_eu_mavinci_desktop_rs232_AscTecCommunication
#ifdef __cplusplus
extern "C" {
#endif

#define ENABLE_VERBOSE
//#define DISABLE_MUTEXING

/* Exported types ------------------------------------------------------------*/
typedef  void (*AscTecCallback)(char *, char*);

typedef enum {
	EMERGENCYMODE_DIRECT_LANDING  = SDC_EMODE_DIRECT_LANDING,
	EMERGENCYMODE_HOME_DIRECT     = SDC_EMODE_COME_HOME_DIRECT,
	EMERGENCYMODE_COME_HOME_HIGHT = SDC_EMODE_COME_HOME_HIGH,
} emergencyMode_t;

typedef enum {
	IMC_FLIGHTMODE_NOT_AVAILABLE,
	IMC_FLIGHTMODE_GPS,
	IMC_FLIGHTMODE_HEIGHT,
	IMC_FLIGHTMODE_MANUAL,
} imc_flightmode_t;

typedef enum {
	MOTORONSTATE_UNKNOWN,
	MOTORONSTATE_TURNED_ON,
	MOTORONSTATE_TURNED_OFF,
} motorOnState_t;

typedef enum {
	MOTORERRORSTATE_UNKNOWN,
	MOTORERRORSTATE_OK,
	MOTORERRORSTATE_ERROR,
} motorErrorState_t;

#pragma pack(1)
typedef struct
{
	uint32_t error_state;//VT_UINT32//Refer to bmshost_host_error_t//
	uint32_t remaining_runtime_s;//VT_UINT32// In Seconds: System remaining runtime//
	uint16_t system_voltage_mV;//VT_UINT16//System Voltage in mV//
	uint8_t  shutdown_status;//VT_UINT8//Refer to bmshost_shutdown_status_t;//
	uint8_t  state_of_charge_total;//VT_INT8//System SoC in %, i.e.100*(Sum of Charge)/(Sum of total Capacity)//
	uint8_t batteries_numberof;//VT_UINT8// number of identified batteries//
} ATOS_MSG_BMSHOST ;

typedef struct {
	float wind_direction;//VT_SINGLE//wind direction//0..360
	float wind_speed;//VT_SINGLE//absolute wind speed//m/s
} aci_wind_info_t;

typedef struct
{
	uint8_t feature;//VT_UINT8// select values from SDC_SET_* such as SDC_SET_INSPECTION_PACKAGE//
	uint8_t lastValidYear;//VT_UINT8// add 2000 to make year (example: 17 means 2017)//
	uint8_t lastValidMonth;//VT_UINT8// 1 to 12. Upper nibble is RESERVED//
	uint8_t lastValidDay;//VT_UINT8// 1 to 31//
} ATOS_MSG_PAYLOAD_LICENSE_DATA ;

typedef struct
{
	uint32_t serialNumber;//VT_UINT32////
	ATOS_MSG_PAYLOAD_LICENSE_DATA licenses[13];//VT_UNKNOWN// check PAYLOAD_LICENSE_COUNT_MAX before changing size//
} ATOS_MSG_PAYLOAD_LICENSES ;

#pragma pack()

typedef enum {
	GPS_STATUS_RTK_INVALID = 0,
	GPS_STATUS_RTK_AUTONOMOUS = 1,
	GPS_STATUS_RTK_DGPS = 2,
	GPS_STATUS_RTK_RTK_FLOAT = 3,
	GPS_STATUS_RTK_RTK_FIX = 4,
} gps_status_rtk_t;

typedef enum {
	TAKEOFF_LANDING_EXECUTE_NONE,
	TAKEOFF_LANDING_EXECUTE_TAKEOFF,
	TAKEOFF_LANDING_EXECUTE_LANDING,
} takeoff_landing_execute_t;

typedef enum {
	NAVIGATION_STATE_IDLE = 0,
	NAVIGATION_STATE_STOPPED = 1,
	NAVIGATION_STATE_BUSY = 2,
	NAVIGATION_STATE_STARTED = 3,
	NAVIGATION_STATE_PAUSED = 4,
	NAVIGATION_STATE_FLYING = 5,
	NAVIGATION_STATE_PRELOADING = 6,
	NAVIGATION_STATE_TOBEPAUSED = 7,
	NAVIGATION_STATE_ERROR = 8,
	NAVIGATION_STATE_FLYING_HOME = 9,
	NAVIGATION_STATE_FLYING_HOME_STARTED = 10,
	NAVIGATION_STATE_TAKEOFF_STARTED = 11,
	NAVIGATION_STATE_TAKING_OFF = 12,
	NAVIGATION_STATE_LANDING_STARTED = 13,
	NAVIGATION_STATE_LANDING = 14
} navigationState_t;


#pragma pack(1)
typedef struct _Parameters {

	int lat;
	int lon;
	float height;
	int gps_height;

	unsigned int flight_time;

	float cam_roll;
	int cam_pitch;
	float cam_yaw;

	unsigned int gps_speed;
	unsigned int gps_stat;
	unsigned int gps_quality;

	unsigned int sat_count;
	unsigned int gps_horizontal_acc;
	unsigned int gps_speed_acc;

	int bat_voltage;
	int bat_cap_used;

	float roll;
	float pitch;
	float yaw;

	int org_lat;
	int org_lon;
	float org_alt;
	float org_yaw;

	float rel_x;
	float rel_y;
	float rel_z;

	int flightMode;

	float wind_direction;
	float wind_speed;

	int motorOnState; // see motorOnState_t
	int motorErrorState; // see motorErrorState_t

	int home_position_lat;
	int home_position_lon;
	uint8_t connection_quality;

	uint32_t droneLinkVersionMajor;
	uint32_t droneLinkVersionMinor;
	int droneLinkSerial;
}Parameters;

typedef struct _LicenseInformation{
	uint32_t payloadSerial;
	uint32_t payloadTypeId;
	ATOS_MSG_PAYLOAD_LICENSE_DATA payloadLicenses[13];
	uint32_t featuresActivated;
}LicenseInformation;

#pragma pack()

typedef void (*TransmitCallback)(unsigned char* byte, unsigned short cnt);

typedef  void (*ParametersCallback)(Parameters* parameters);

typedef void (*LicenseInformationCallback)(LicenseInformation* licenseInformation);

typedef  void (*WpCallback)(int idx, int maxCnt);

typedef void (*CmdAlarmCallback)(uint16_t alarmstate_cockpit, uint8_t link1, uint8_t link2);

typedef  void (*FlightCallback)(ATOS_MSG_NAVIGATION_STATUS* navStatus);

typedef  void (*UpdateParamsCallback)(short varUpd, short cmdUpd, short paramsUpd);

typedef  void (*WriteCacheCallback)(char* cacheData);

typedef  void (*InfoCallback)(unsigned char verNum);

typedef void (*StringErrorsCallback)(char* stringp, unsigned short cnt);

/* Exported constants --------------------------------------------------------*/

#define GPS_STATUS_RTK_BITMASK 0xF0000000

/* Exported functions --------------------------------------------------------*/

/**
 * init aci engine, sets listeners, requests var update
 * @param cacheFile the name of the file to store aci cache
 */
extern void init(char* cachePath, int pathLength);

/**
 * passes bytes from a serial port to the aci engine
 * @param c received byte from a serial port
 */
extern uint8_t receive(unsigned char c);

/**
 * passes bytes from a serial port to the aci engine
 * @param c received byte from a serial port
 */
extern uint8_t receiveArray(unsigned char* c, int cnt);

/**
 * register init callback, which is called when all the params are read
 * @param callback
 */
extern void registerInitCallback(ParametersCallback callback);

/**
 * start sending mission procedure to the copter
 * @param wayPoints list of the wps to send
 */
extern uint8_t sendMission(ATOS_WAYPOINT* wayPoints, int num, int type);

/**
 * start sending mission procedure to the copter point by point
 * @param wayPoints list of the wps to send
 */
extern uint8_t sendMissionPoint(ATOS_WAYPOINT* wayPoint);

/*
 * TODO what if there are more than 32 wps
 */
/**
 * register wp callback, which is called when a wp is sent
 * @param callback
 */
extern void registerWpCallback(WpCallback callback);
extern void registerTransmitCallbac(TransmitCallback callback);
extern void registerCmdAlarmCallback(CmdAlarmCallback callback);
/**
 * Initializes the flight mode on aci
 */
extern uint8_t startFlight();

/**
 * Finalizes the flight mode on aci
 */
extern uint8_t stopFlight();

/**
 * register flight callback, which is called during the flight to pass the flight status info
 * @param callback
 */
extern void registerFlightCallback(FlightCallback callback);
extern void registerUpdateParamsCallback(UpdateParamsCallback callback);
extern void registerLicenseInformationCallback(LicenseInformationCallback callback);
extern void registerWriteCacheCallback(WriteCacheCallback callback);

/**
 * register string errors callback: It is called everytime a new string error is transmitted from falcon
 * @param callback
 */
extern void registerStringErrorsCallback(StringErrorsCallback stringErrorCallback);


extern void testCallbacks();

extern void closeConnection();

extern uint8_t sendCommand(int commandId, unsigned short param);
extern uint8_t sendJoystickData(float yaw, float pitch, float roll, float thrust);
extern uint8_t TurnOnMotors(void);
extern uint8_t TurnOffMotors(void);
extern uint8_t UnblockFalcon(void);
extern uint8_t SetEmergencyMode(emergencyMode_t emergencyMode);
extern uint8_t SetFlightMode(imc_flightmode_t flightMode);
extern uint8_t RequestDroneLinkInfo(void);
extern uint8_t StartDroneLinkBootloader(void);
extern uint8_t sendInfoRequest();
extern void registerInfoCallback(InfoCallback callback);

extern uint8_t sendRTCMData(char* data, int size);
extern uint8_t sendSimpleCmd(uint8_t simpleCmdId);
extern imc_flightmode_t getFlightmode(void);
extern motorOnState_t getMotorOnState(void);
extern motorErrorState_t getMotorErrorState(void);
extern uint8_t requestSwitchableCmdRights(void);
extern uint8_t requestJoystickControl(void);
extern bool gotJoystickControl(void);
extern uint8_t cmdReturnToHome(void);
extern bool gotSwitchableCmdRights(void);
extern uint8_t idlRTCMStreamDeactivate(void);
extern uint8_t idlRTCMStreamActivate(void);
extern uint8_t takeOffLandingInitialize(int32_t latitude, int32_t longitude, float height, float speed, float acceleration);
extern uint8_t takeOffLandingExecute(uint8_t takeoff_landing_execute);
extern uint8_t navigationResume(void);
extern uint8_t navigationPause(void);
extern uint8_t getNavigationState(void);
extern void aciEngThread(void);

#ifdef __cplusplus
}
#endif
#endif
