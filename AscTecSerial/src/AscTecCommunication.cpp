/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#define ENABLE_AC_PC_Commands

/* Includes ------------------------------------------------------------------*/
#include "asctecCommIntf.h"
#include <pthread.h>
#include <string.h>
#include "aci_waypoint_structs.h"
#include "asctecDefines.h"
#include "AC_PC_Commands.h"
#include <unistd.h> // UNIX standard function definitions
#include <fcntl.h>  // File control definitions
#include <sys/stat.h> // File permissions
#include <time.h>
#include "Crc16.h"
#include "AC_PC_Commands.h"

#include <list>
#include <iostream>
#include <fstream>

#include <errno.h>
#include <string.h>

#include <rc_cmd.h>

#include "AscTecCommunication.h"

using namespace std;

/* Private define ------------------------------------------------------------*/

#define DIRTY_SDC_HACK // TODO: Simple display commands aren't working for some reason?!

#define DISPLAY_SIMPLE_CMD_NONE 0x00

#define ACI_ENGINE_RATE_HZ 50
#define ACI_ENGINE_SLEEP_TIME_US (1000000 / ACI_ENGINE_RATE_HZ)

#define AUTO_TAKEOFF_LANDING_DEADMANSWITCH_TIMEOUT 1 // in seconds, see #define by same name in trinity

#define NAVIGATION_COMMAND_RESEND_TIMEOUT (ACI_ENGINE_RATE_HZ / 4)
#define LICENSES_CHECK_TIMEOUT ACI_ENGINE_RATE_HZ

/* Private typedef -----------------------------------------------------------*/

typedef enum {
	AUTO_TAKEOFF_LANDING_FLAG_TAKEOFF = (1 << 0),
	AUTO_TAKEOFF_LANDING_FLAG_LAND = (1 << 1),
} auto_takeoff_landing_flags_t;

typedef enum {
	CAM_UNKNOWN = 0x00,
	CAM_LUMIX_ZOOM = 0x02,
	CAM_SONY_NEX5 = 0x03,
	CAM_ADC_LITE = 0x04,
	CAM_SONY_NEX5N = 0x07,
	CAM_NEC_THERMOSHOT = 0x09,
	CAM_SONY_NEX7 = 0x0A,
	CAM_SONY_LANC = 0x0B,
	CAM_SONY_LANC_FLIR = 0x0C,
	CAM_SONY_FLIR_SERIAL = 0x0D,
	CAM_FLIR_LUMIX_ZOOM = 0x0E, // "FLIR/19mm"
	CAM_SONY_ALPHA = 0x0F, // "Sony Alpha 7R / 35mm"
	CAM_SONY_ALPHA6K = 0x10,
	CAM_MICASENSE = 0x11,
	CAM_RX1MK2IO = 0x12,
	CAM_RX1MK2RTK = 0x13, // Flierspoint, "RX1MK2 RTK"
	CAM_MOOSEPOINT = 0x14,
	CAM_ELKPOINT = 0x15,
} payloadId_t;

#pragma pack(1)
typedef struct
{
	uint8_t status;//VT_UINT8//0=invalid 1=valid 2=position set while flying (due to late GPS l//
	int32_t latitude;//VT_INT32////
	int32_t longitude;//VT_INT32////
	uint32_t tow;//VT_UINT32////
	uint32_t week;//VT_UINT32////
	int32_t gps_height;//VT_INT32////
} ATOS_MSG_HOME_POSITION_t ;

typedef struct {
	uint8_t atos_msg_flightmode_change_request; // see FM_CR_xxxx
	uint8_t cmd_cnt;
}
aci_flightmode_change_request_t;

typedef struct {
	uint8_t sdc; // see SDC_xxxx
	uint8_t counter; // to check that this is a new sdc
} aci_single_sdc_t;
#pragma pack()

typedef struct {
	navigationState_t state;
	uint16_t startWaypoint;
	uint16_t endWaypoint;
	uint16_t lastFinishedWaypoint;
} flightStatusHandler_t;

/* Private variables ---------------------------------------------------------*/
//global aci struct
static aciMaster_t nav1;
static ATOS_MSG_BMSHOST droneBmsHost; // BMS = Battery Management System
static aci_wind_info_t windInfo;

//float nice_param;
static int32_t lat;
static int32_t lon;
static float height;
static int gps_height;
static unsigned int flight_time;
static short cam_angle;
static vector3f euler;
static vector3f relCoords;
static uint32_t flightMode;
static vector3f angular_vel;
static unsigned int gps_speed; // mm/s

static unsigned int gps_stat;
static uint32_t gps_numSats; // Number of SVs used in Nav Solution (see ublox "NAV-SOL" message)
static unsigned int gps_quality;

//NEW accuracy
static unsigned int gps_horizontal_acc;
static unsigned int gps_speed_acc;

static int org_lat;
static int org_lon;
static float org_alt;

static aci_flightmode_change_request_t aci_flightmode_change_request;
static uint8_t aci_cmd_emergency_mode;

static bool isConnectionClosed = false;

static bool sendingTakeoffFlag = false;
static bool sendingLandingFlag = false;

static char* cachePath_;
static ATOS_WAYPOINT* wayPoints_;

static ifstream cacheRead;
static ofstream cacheWrite;

static int currentWPInd = 0;
static int totalWpCount;

static ATOS_MSG_WPLIST_CMD wpCmd;
static ATOS_WAYPOINT wp;
static ATOS_MSG_WPLIST_STATUS wpStatus;
static ATOS_MSG_NAVIGATION_STATUS navStatus;

static unsigned char wpListMsgCycleCnt_ = 0;
static unsigned char wpListStatusCnt_ = 0;

static bool uploadStarted = false;
static bool flightStarted = false;
static bool takeoffStarted = false;
static bool clearAcceptedState = false; //CMD_CNT
static bool clearAcceptedState_WP_BUFFER = false;
static bool parametersReceived = false;
static bool startFlightAccepted = false;

static int sentWpCnt = 0;

static pthread_t p_aciEngThread;
static InfoCallback infoCallback = NULL;
static TransmitCallback transmitCallback;
static StringErrorsCallback stringErrorsCallback;
static WriteCacheCallback writeCacheCallback;
static FlightCallback flightCallback;
static CmdAlarmCallback cmdAlarmCallback;
static WpCallback wpCallback;
static ParametersCallback paramCallback;
static UpdateParamsCallback updateParamsCallback;
static LicenseInformationCallback licenseInformationCallback;

static uint8_t simpleCmdList[8]; // a list of up to 8 simple commands without parameters send to the flight system. Feedback is provided through displaySimpleCmdAck
static uint8_t simpleCmdAck; // 8 bits which acknowledge commands. a set bit means that the command was accepted. Reseting the bit is possible by setting the cmd to 0
static aci_single_sdc_t single_sdc;
static uint8_t remoteSlaveState; // see REMOTE_SLAVES_STATE_FLAG_xxx
static uint16_t payloadID; // see payloadId_t

static uint8_t motorSpeeds[8];
static uint8_t motorErrorFlags; // bitcoded, motor 0, 1, ...

static uint8_t switchableCmdState;

static uint32_t errorsListCnt;
static uint8_t stringErrorsReported;
static char errorTextExpanded[43];
static uint8_t errorNoType;
static char errorMsg[TRINITY_ERROR_MSGS_BUFFER_LEN][TRINITY_ERROR_MSG_SIZEOF_STRING];
static unsigned char errorMsgTypes[TRINITY_ERROR_MSGS_BUFFER_LEN];
static char fullErrorString[(TRINITY_ERROR_MSG_SIZEOF_STRING + 1) * TRINITY_ERROR_MSGS_BUFFER_LEN]; // + 1 ... '\n'
static char idlFullErrorString[(TRINITY_ERROR_MSG_SIZEOF_STRING + 1) * TRINITY_ERROR_MSGS_BUFFER_LEN]; // + 1 ... '\n'
static ATOS_MSG_HOME_POSITION_t home_position;
static ATOS_MSG_PAYLOAD_LICENSES payload_licenses;
static uint32_t falcon_activatedFeatures;
static uint16_t takeoffLandingWaypointCrc;
static flightStatusHandler_t flightStatusHandler;
static uint16_t idlErrorCntOld;

#ifdef ENABLE_VERBOSE
static uint32_t threadTimeOkayCnt;
static uint32_t threadTimeViolationCnt;
#endif // ENABLE_VERBOSE

/* Exported variables --------------------------------------------------------*/
/* Private function prototypes -----------------------------------------------*/
static void debugOut(const char* string);
static void varListUpdateFinished();
static void cmdListUpdateFinished();
static void parListUpdateFinished();
static void versions(struct ACI_INFO);
static void transmit(void* byte, unsigned short cnt);
static void stringErrorReport(char * stringp, uint16_t string_sizeof);
static void *aciEngThread(void*);

static int writeOnDevice(void *data, int bytes);
static int readFromDevice(void *data, int bytes);
static void resetRW();
static void openDevice();
static void cmdPacketSend();
static void resetWaypointList();
static void uploadWaypoint(const ATOS_WAYPOINT& wp_);
static void requestUploadBufferState();
static void resetWplistFalcon();
static void cmdPacketInit();
static bool uploadNext();
static void debug_write(const char* line);
static void sendTheLastCommandAgain();
static bool isClearAcceptedMsg(ATOS_MSG_WPLIST_STATUS state);
static void handleUploadStatus(ATOS_MSG_WPLIST_STATUS state);
static void handleFlightStatus(void);
static void handleLicensesCallback(void);
static void stopFlightNoMutex(void);

/* Private functions ---------------------------------------------------------*/

static void cmdPacketSend() {

	aciUpdateCmdPacket(&nav1, 0);

	printf("Sent cmd: \n");
	printf("Cmd status: %u \n", wpCmd.wpListCmd);
	printf("Cmd param1: %u \n", wpCmd.wpListParam1);
	printf("Cmd param2: %u \n", wpCmd.wpListParam2);
	printf("Cmd cnt %u: \n \n", wpCmd.wpListCmdCycleCnt);
	fflush(stdout);
}

static void resetWaypointList() {
	wpCmd.wpListCmd = NAV_WPLIST_RESET_CMDCNT;
	wpCmd.wpListParam1 = 0;
	wpCmd.wpListParam2 = 0;
	wpCmd.wpListCmdCycleCnt = wpListMsgCycleCnt_ = 1;

	cmdPacketSend();
}

//  NAV_WPLIST_ADD cmd with the first waypoint in ATOS_DBG_CMD_WAYPOINT_UPLOAD
static void uploadWaypoint(const ATOS_WAYPOINT& wp_) {
	wp = wp_;
	wpCmd.wpListCmd = NAV_WPBUFFER_ADD;
	unsigned short crc = crc16(&wp, sizeof wp);
	wpCmd.wpListParam1 = crc;
	wpCmd.wpListParam2 = 0;
	wpCmd.wpListCmdCycleCnt = wpListMsgCycleCnt_++;

	cmdPacketSend();
}

static void requestUploadBufferState() {
	//qCDebug(NAVIGATION_GENERAL) << "requestUploadBufferState";
	wpCmd.wpListCmd = NAV_WPBUFFER_STATUS;
	wpCmd.wpListParam1 = 0;
	wpCmd.wpListParam2 = 0;
	wpCmd.wpListCmdCycleCnt = wpListMsgCycleCnt_++;

	cmdPacketSend();
}

static void resetWplistFalcon() {
	//qCDebug(NAVIGATION_GENERAL) << "resetWaypointList";

	wpCmd.wpListCmd = NAV_WPBUFFER_CLEAR;
	wpCmd.wpListParam1 = 0;
	wpCmd.wpListParam2 = 0;
	wpCmd.wpListCmdCycleCnt = wpListMsgCycleCnt_++;

	cmdPacketSend();
}

static void cmdPacketInit() {
	aciAddContentToCmdPacket(&nav1, 0, ATOS_DBG_CMD_WPLIST_CMD, &wpCmd);
	aciAddContentToCmdPacket(&nav1, 0, ATOS_DBG_CMD_WAYPOINT_UPLOAD, &wp);
	aciAddContentToCmdPacket(&nav1, 0, ATOS_DBG_CMD_FLIGHTMODE_CHANGEREQ, &aci_flightmode_change_request);
	aciAddContentToCmdPacket(&nav1, 0, ATOS_DBG_CMD_EMERGENCY_MODE, &aci_cmd_emergency_mode);
#ifndef DIRTY_SDC_HACK
	aciAddContentToCmdPacket(&nav1, 0, ATOS_DBG_CMD_SIMPLE_CMDS, simpleCmdList);
#endif // DIRTY_SDC_HACK
}

static bool uploadNext() {
	if (currentWPInd == totalWpCount) {

		return false;
	}
	uploadWaypoint(wayPoints_[currentWPInd]);
	currentWPInd++;
	return true;
}

static void debug_write(const char* line){

		//ofstream file_debug ("C:\\Users\\ekorotko\\Documents\\debug_ouput.txt", std::ios::app);
		//file_debug << line << endl;

}

static void transmit(void* byte, unsigned short cnt) {

	unsigned char *tbyte = (unsigned char *) byte;
	(*transmitCallback)(tbyte, cnt);

}

static void stringErrorReport(char * stringp, unsigned short string_sizeof)
{
	if(stringErrorsCallback){
		(*stringErrorsCallback)(stringp, string_sizeof);
	}
}

static void versions(struct ACI_INFO aciInfo) {

	printf("******************** AscTec Version Info *******************\n");
	printf("* Type\t\t\tDevice\t\tRemote\t*\n");
	printf("* Major version\t\t%d\t=\t\%d\t*\n", aciInfo.verMajor,
	ACI_VER_MAJOR);
	printf("* Minor version\t\t%d\t=\t\%d\t*\n", aciInfo.verMinor,
	ACI_VER_MINOR);
	printf("* MAX_DESC_LENGTH\t%d\t=\t\%d\t*\n", aciInfo.maxDescLength,
	MAX_DESC_LENGTH);
	printf("* MAX_NAME_LENGTH\t%d\t=\t\%d\t*\n", aciInfo.maxNameLength,
	MAX_NAME_LENGTH);
	printf("* MAX_UNIT_LENGTH\t%d\t=\t\%d\t*\n", aciInfo.maxUnitLength,
	MAX_UNIT_LENGTH);
	printf("* MAX_VAR_PACKETS\t%d\t=\t\%d\t*\n", aciInfo.maxVarPackets,
	MAX_VAR_PACKETS);
	printf("*************************************************\n");
	fflush(stdout);

}

static void varListUpdateFinished() //is called when all variables are received
{

	aciGetDeviceCommandsList(&nav1);

	printf("asctec variables updated\n");
	fflush(stdout);
}

static void cmdListUpdateFinished() //is called when all cmds are received
{
	printf("asctec cmds updated\n");
	fflush(stdout);
	cmdPacketInit();
	aciGetDeviceParametersList(&nav1);
}

static void parListUpdateFinished() //called when all parameters are received
{
	aciSetVarPacketTransmissionRate(&nav1, 0, 250);

	// Fast data (packet 1)
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_GPS_LAT, &lat);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_GPS_LON, &lon);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_FLIGHT_TIME, &flight_time);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_HEIGHT, &height);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_GPS_HEIGHT, &gps_height);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_CAM_PITCH, &cam_angle);
	aciAddContentToVarPacket(&nav1, 1, ACI_USER_VAR_BMS_HOST_STATE, &droneBmsHost);

	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_GPS_SPEED, &gps_speed);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_GPS_STATUS, &gps_stat);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_GPS_NUMSV, &gps_numSats);
	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_GPS_QUALITY, &gps_quality);

	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_SIMPLE_CMD_FEEDBACK, &simpleCmdAck);

	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_WIND_INFO, &windInfo);

	aciAddContentToVarPacket(&nav1, 1, ATOS_DBG_WPLIST_STATUS, &wpStatus);


	// Slow Data (Packet 0)
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_MOTOR_OUT, motorSpeeds);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_MOTOR_ERRORFLAGS, &motorErrorFlags);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_EULER_ANGLES, &euler);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_ANGULAR_VEL, &angular_vel);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_NAV_STATUS, &navStatus);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_FLIGHTMODE, &flightMode);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_SW_CMD_STATE, &switchableCmdState);

	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_CORE_ERRORS,&errorsListCnt);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_ERROR_EXPANDED_TEXT,&errorTextExpanded);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_ERROR_EXPANDED_ERROR_NO,&errorNoType);

	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_HOME_POSITION,&home_position);
	aciAddContentToVarPacket(&nav1, 0, ACI_USER_VAR_PAYLOAD_LICENSE_INFORMATION, &payload_licenses);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_CURRENT_FEATURES, &falcon_activatedFeatures);
	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_REMOTE_SLAVES_STATE, &remoteSlaveState);

	aciAddContentToVarPacket(&nav1, 0, ATOS_DBG_PAYLOAD_ID, &payloadID);

	aciSendCommandPacketConfiguration(&nav1, 0, 1);

	aciVarPacketUpdateTransmissionRates(&nav1);

	aciSendVariablePacketConfiguration(&nav1, 0);
	aciSendVariablePacketConfiguration(&nav1, 1);

	aciUpdateCmdPacket(&nav1, 0);

	printf("asctec params updated\n");
	fflush(stdout);

	parametersReceived = true;

	///////try to send a mission
	//tryToSendMission();
}

static void sendTheLastCommandAgain() {
	aciUpdateCmdPacket(&nav1, 0);

}

static bool isClearAcceptedMsg(ATOS_MSG_WPLIST_STATUS state) {
	if (state.wpListStatus == NAV_WPBUFFER_STATUS_ACK
			&& (state.wpListParam1 == NAV_WPLIST_RESET_CMDCNT
					|| state.wpListParam1 == NAV_WPBUFFER_CLEAR)) {
		return true;
	} else if (state.wpListStatus == NAV_WPLIST_CLEAR
			&& state.wpListParam1 == NAV_WPLIST_CLEAR) {
		return true;
	}

	return false;
}

static void handleUploadStatus(ATOS_MSG_WPLIST_STATUS state) {

	if (state.wpListStatusCycleCnt == wpListStatusCnt_) {
		sendTheLastCommandAgain();
		return;
	}else{
		wpListStatusCnt_ = state.wpListStatusCycleCnt;
	}

	printf("Received state: \n");
	printf("Wplist status: %u \n", state.wpListStatus);
	printf("Wplist param1: %u \n", state.wpListParam1);
	printf("Wplist param2: %u \n", state.wpListParam2);
	printf("Wplist cnt %u: \n \n", state.wpListStatusCycleCnt);
	fflush(stdout);

	if (!clearAcceptedState && !isClearAcceptedMsg(state)) {
		//resetWaypointList();
		resetWplistFalcon();

		debugOut("Error: !clearAcceptedState && !isClearAcceptedMsg");
		flightStatusHandler.state = NAVIGATION_STATE_ERROR;
		return;
	}

	//wpListStatusCnt_ = state.wpListStatusCycleCnt;

	switch (state.wpListStatus) {
	case NAV_WPLIST_CLEAR: {
		//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPLIST_CLEAR ";

		switch (state.wpListParam1) {
		case NAV_WPLIST_CLEAR: {
			clearAcceptedState = true;
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPLIST_CLEAR";
			if (!uploadNext()) {
				//uploadProgress_.setFinished(true);
				//state_ = State::Finished;
			}
			break;
		}
		case NAV_WPLIST_GOTO_SINGLE: {
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPLIST_GOTO_SINGLE IMPLEMENT!!!!";
			//startFlight(); // TODO!
			break;
		}
		case NAV_WPLIST_START_FLIGHT: {
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPLIST_START_FLIGHT";
			startFlightAccepted = true;
			if (!uploadNext()) {
				//uploadProgress_.setFinished(true);
				//state_ = State::Finished;
			}
			break;
		}
		case NAV_WPLIST_ABORT_FLIGHT: {
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPLIST_ABORT_FLIGHT IMPLEMENT!!!!";

			//          state_ = FlightState::Idle;
			//          emit nav_.flightState(FlightState::Idle);
			//          waypoint_upload_state_ = WaypointUploadState::Inactive;
			//          sendFlightAborted(); // TODO!
			break;
		}
		}
		break;
	}
	case NAV_WPBUFFER_STATUS_ACK: {
		// qCDebug(NAVIGATION_UPLOAD) << "NAV_WPBUFFER_STATUS_ACK";
		switch (state.wpListParam1) {
		//		case NAV_WPLIST_START_FLIGHT:{
		//			uploadNext();
		//			break;
		//		}
		case NAV_WPBUFFER_CLEAR:
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPBUFFER_CLEAR";
			clearAcceptedState = true;
			clearAcceptedState_WP_BUFFER = true;
			if (!uploadNext()) {
				//uploadProgress_.setFinished(true);
				//state_ = State::Finished;
			}
			break;
		case NAV_WPLIST_RESET_CMDCNT: {
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPLIST_RESET_CMDCNT";
			//try {
			clearAcceptedState = true;

			if(!clearAcceptedState_WP_BUFFER){
				resetWplistFalcon();
			}
			//} catch(const asctec::BadACIException &e) {
			//qCDebug(NAVIGATION_UPLOAD) << "Attempt to use bad or non-initialized ACI [" << e.what() << "]";
			//}

			break;
		}
		case NAV_WPBUFFER_ADD: {
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPBUFFER_ADD";
			sentWpCnt++;
			(*wpCallback)(sentWpCnt, totalWpCount);
			//uploadSuccess();

			if (state.wpListParam2 == 0) {

				if (flightStarted) {
					requestUploadBufferState();
				}
			} else {
				//uploadProgress_.increment();
				if (!uploadNext()) {
					//uploadProgress_.setFinished(true);
					//state_ = State::Finished;
				}
			}
			break;
		}
		case NAV_WPLIST_DELETE: {
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPLIST_DELETE IMPLEMENT!";
			// ...
			break;
		}
		case NAV_WPBUFFER_STATUS: {
			//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPBUFFER_STATUS";
			if (state.wpListParam2 > 0) {
				if (!uploadNext()) {
					//uploadProgress_.setFinished(true);
					//state_ = State::Finished;
				}
			} else {
				requestUploadBufferState();
			}
			break;
		}
		}
		break;
	}
	case NAV_WPBUFFER_STATUS_ERROR: {
		//qCDebug(NAVIGATION_UPLOAD) << "NAV_WPBUFFER_STATUS_ERROR";
		if (NAV_WPBUFFER_ADD == state.wpListParam1) {
			if (NAV_WPBUFFER_ERROR_CRC == state.wpListParam2) {
				uploadNext();
				debugOut("Warning: Waypoint upload CRC Error! Resending.");
			} else if ((state.wpListParam2 == NAV_WPBUFFER_ERROR_FULL)) {
				requestUploadBufferState();
				debugOut("Warning: Waypoint Buffer on Drone Full! Requesting Buffer status.");
			}
		}
		break;
	}
	case NAV_WPLIST_STATUS_ERROR: {
		if (NAV_WPLIST_START_FLIGHT == state.wpListParam1) {
			if (NAV_WPLIST_ERROR_BLOCKED == state.wpListParam2) {
				uploadNext();
				debugOut("Error start flight! Already started");
			}
		}

		switch ((navWpListError_t) state.wpListParam2) {
		case NAV_WPLIST_ERROR_BLOCKED:
			//qCDebug(NAVIGATION_GENERAL) << "Error: Blocked!";
			//state_ = State::Error;

			debugOut("Error: NAV_WPLIST_ERROR_BLOCKED");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;

		case NAV_WPLIST_ERROR_NO_LICENSE:
			debugOut("Error: NAV_WPLIST_ERROR_NO_LICENSE");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;

		case NAV_WPLIST_ERROR_WRONG_MODE:
			debugOut("Error: NAV_WPLIST_ERROR_WRONG_MODE");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;

		case NAV_WPLIST_ERROR_WRONG_WPS_CNT:
			debugOut("Error: NAV_WPLIST_ERROR_WRONG_WPS_CNT");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;

		case NAV_WPLIST_ERROR_WRONG_PARAM:
			debugOut("Error: NAV_WPLIST_ERROR_WRONG_PARAM");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;

		case NAV_WPLIST_ERROR_TOO_MANY_WPS:
			debugOut("Error: NAV_WPLIST_ERROR_TOO_MANY_WPS");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;

		case NAV_WPLIST_ERROR_CRC:
			debugOut("Error: NAV_WPLIST_ERROR_CRC");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;


		default:
			debugOut("Error: Unknown NAV_WPLIST_STATUS_ERROR");
			flightStatusHandler.state = NAVIGATION_STATE_ERROR;
			break;
		}

		break;
	}
	}
}

static void openDevice() {
	cacheRead.open(cachePath_, ios::in);
}

static int writeOnDevice(void *data, int bytes) {

	if (cacheRead.is_open()) {
		cacheRead.close();
	}
	if (!cacheWrite.is_open()) {
		cacheWrite.open(cachePath_, ios::out);
	}
	cacheWrite.write((char*) data, bytes);
	return bytes;
}

static int readFromDevice(void *data, int bytes) {

	cacheRead.read((char*) data, bytes);
	return bytes;
}

static void resetRW() {
	cacheRead.close();
	cacheRead.open(cachePath_, ios::in);
}

static void handleLicensesCallback(void)
{
	static uint32_t licenseCheckTimeout = LICENSES_CHECK_TIMEOUT;
	static ATOS_MSG_PAYLOAD_LICENSES payload_licenses_old;
	static uint32_t falcon_activatedFeatures_old;

	if(0 == licenseCheckTimeout)
	{
		licenseCheckTimeout = LICENSES_CHECK_TIMEOUT;

		if(   (0 != memcmp(&payload_licenses, &payload_licenses_old, sizeof(payload_licenses)))
				|| (falcon_activatedFeatures_old != falcon_activatedFeatures))
		{
			LicenseInformation licenseInformation;


			licenseInformation.payloadSerial = payload_licenses.serialNumber;
			memcpy(&(licenseInformation.payloadLicenses), &(payload_licenses.licenses),
					sizeof(licenseInformation.payloadLicenses));
			licenseInformation.featuresActivated = falcon_activatedFeatures;
			licenseInformation.payloadTypeId = payloadID;

			if(licenseInformationCallback)
			{

				(*licenseInformationCallback)(&licenseInformation);
			}
		}
	}
	else
	{
		licenseCheckTimeout--;
	}

	return;
}

// taken more or less from Cockpit control FlightManager::handleFlightStatus
static void handleFlightStatus(void)
{
#ifdef ENABLE_VERBOSE
	static navigationState_t navigationStateOld;
	if(navigationStateOld != flightStatusHandler.state)
	{
		printf("FlightStatusSwitch: %u -> %u", navigationStateOld, flightStatusHandler.state);
		fflush(stdout);

		navigationStateOld = flightStatusHandler.state;
	}
#endif // ENABLE_VERBOSE

	static bool idleReceived = false;
	static unsigned int resendTimeout;

	if(resendTimeout)
	{
		resendTimeout--;
	}

	// Ignore first idle
	if(NAV_STATUS_IDLE != navStatus.status)
	{
		idleReceived = false;
	}
	else
	{
		if(!idleReceived)
		{
			idleReceived = true;
			return;
		}
	}

	// we can go to flying home from any state without navigation procedures.
	if(NAV_STATUS_FLYING_HOME == navStatus.status)
	{
		flightStatusHandler.state = NAVIGATION_STATE_FLYING_HOME;
	}

	// Change states
	switch(flightStatusHandler.state)
	{
	case NAVIGATION_STATE_LANDING_STARTED:
		if(navStatus.status & NAV_STATUS_LANDING)
		{
			flightStatusHandler.state = NAVIGATION_STATE_LANDING;
		}
		else if( navStatus.status != NAV_STATUS_IDLE)
		{
			flightStatusHandler.state = NAVIGATION_STATE_BUSY;
		}
		break;

	case NAVIGATION_STATE_TAKEOFF_STARTED:
		if(navStatus.status & NAV_STATUS_TAKING_OFF)
		{
			flightStatusHandler.state = NAVIGATION_STATE_TAKING_OFF;
		}
		else if( navStatus.status != NAV_STATUS_IDLE)
		{
			flightStatusHandler.state = NAVIGATION_STATE_BUSY;
		}
		break;

	case NAVIGATION_STATE_LANDING:
		if(0 == (navStatus.status & NAV_STATUS_LANDING))
		{
			flightStatusHandler.state = NAVIGATION_STATE_BUSY;
		}
		break;

	case NAVIGATION_STATE_TAKING_OFF:
		if(0 == (navStatus.status & NAV_STATUS_TAKING_OFF))
		{
			flightStatusHandler.state = NAVIGATION_STATE_BUSY;
		}
		break;

	case NAVIGATION_STATE_FLYING_HOME_STARTED:
		// will switch to NAVIGATION_STATE_FLYING_HOME by itself (see above)
		if(0 == resendTimeout)
		{
			resendTimeout = NAVIGATION_COMMAND_RESEND_TIMEOUT;

#ifndef DIRTY_SDC_HACK
			sendSimpleCmd(SDC_COME_HOME);
#else // DIRTY_SDC_HACK
			printf("Sending simple display command \n");
			fflush(stdout);
			single_sdc.sdc = SDC_COME_HOME;
			single_sdc.counter++;
			aciTxSendSingleCommand(&nav1, ATOS_DBG_CMD_SINGLE_SDC, (uint8_t*) &single_sdc, sizeof(single_sdc));
#endif // DIRTY_SDC_HACK
		}
		break;

	case NAVIGATION_STATE_FLYING_HOME:
		if(NAV_STATUS_FLYING_HOME != navStatus.status)
		{
			flightStatusHandler.state = NAVIGATION_STATE_BUSY; // will go to idle if nothing is going on
		}
		break;

	case NAVIGATION_STATE_PAUSED: // no break intended
	case NAVIGATION_STATE_ERROR:
		break;

	case NAVIGATION_STATE_BUSY:
		if(NAV_STATUS_IDLE == navStatus.status)
		{
			flightStatusHandler.state = NAVIGATION_STATE_IDLE;
		}
		break;

	case NAVIGATION_STATE_STARTED:
		if(   (navStatus.status != NAV_STATUS_IDLE)
				&& (navStatus.waypointId == flightStatusHandler.startWaypoint)
				&& (navStatus.distanceToWaypoint != 0))
		{
			flightStatusHandler.lastFinishedWaypoint = 0;
			flightStatusHandler.state = NAVIGATION_STATE_FLYING;
		}
		break;

	case NAVIGATION_STATE_IDLE:
		if(navStatus.status != NAV_STATUS_IDLE)
		{
			flightStatusHandler.state = NAVIGATION_STATE_BUSY;
		}
		break;

	case NAVIGATION_STATE_STOPPED:
		switch((navStatus_t) navStatus.status)
		{
		case NAV_STATUS_TRAVELING: // no break intended
		case NAV_STATUS_FLYING_TO_O:
			if(0 == resendTimeout)
			{
				resendTimeout = NAVIGATION_COMMAND_RESEND_TIMEOUT;
				stopFlightNoMutex();
			}
			break;

		case NAV_STATUS_IDLE:
			flightStatusHandler.state = NAVIGATION_STATE_IDLE;
			break;

		default: // no break intended
		case NAV_STATUS_ACCELERATING: // no break intended
		case NAV_STATUS_DECELERATING: // no break intended
		case NAV_STATUS_WAITING: // no break intended
		case NAV_STATUS_EVENT1: // no break intended
		case NAV_STATUS_EVENT2: // no break intended
		case NAV_STATUS_REACHED: // no break intended
		case NAV_STATUS_PAUSE: // no break intended
		case NAV_STATUS_RESUMING: // no break intended
		case NAV_STATUS_ABORTED: // no break intended
		case NAV_STATUS_FLYING_HOME: // no break intended
		case NAV_STATUS_TAKING_OFF: // no break intended
		case NAV_STATUS_LANDING:
			break;
		}
		break;

		case NAVIGATION_STATE_FLYING:
			switch((navStatus_t) navStatus.status)
			{
			case NAV_STATUS_TRAVELING: // no break intended
			case NAV_STATUS_FLYING_TO_O:
				// falcon is flying to the first waypoint
				flightStatusHandler.lastFinishedWaypoint = navStatus.waypointId - 1;
				break;

			case NAV_STATUS_IDLE:
				startFlightAccepted = false;
				if(navStatus.waypointId == flightStatusHandler.endWaypoint)
				{
					flightStatusHandler.lastFinishedWaypoint = navStatus.waypointId;
					flightStatusHandler.state = NAVIGATION_STATE_IDLE;
				}
				else
				{
					flightStatusHandler.lastFinishedWaypoint = navStatus.waypointId - 1;
					flightStatusHandler.state = NAVIGATION_STATE_PAUSED;

					uploadStarted = false;
					flightStarted = false;
				}
				break;

			default: // no break intended
			case NAV_STATUS_ACCELERATING: // no break intended
			case NAV_STATUS_DECELERATING: // no break intended
			case NAV_STATUS_WAITING: // no break intended
			case NAV_STATUS_EVENT1: // no break intended
			case NAV_STATUS_EVENT2: // no break intended
			case NAV_STATUS_REACHED: // no break intended
			case NAV_STATUS_PAUSE: // no break intended
			case NAV_STATUS_RESUMING: // no break intended
			case NAV_STATUS_ABORTED: // no break intended
			case NAV_STATUS_FLYING_HOME: // no break intended
			case NAV_STATUS_TAKING_OFF: // no break intended
			case NAV_STATUS_LANDING:
				break;
			}
			break;

		case NAVIGATION_STATE_PRELOADING:
			if(NAV_STATUS_IDLE != navStatus.status)
			{
				if(0 == resendTimeout)
				{
					resendTimeout = NAVIGATION_COMMAND_RESEND_TIMEOUT;
					stopFlightNoMutex();
				}
			}
			break;

		case NAVIGATION_STATE_TOBEPAUSED:
			if(NAV_STATUS_IDLE == navStatus.status)
			{
				flightStatusHandler.state = NAVIGATION_STATE_PAUSED;
			}
			else if(0 == resendTimeout)
			{
				resendTimeout = NAVIGATION_COMMAND_RESEND_TIMEOUT;
				stopFlightNoMutex();
			}
			break;
	}
}

static void stopFlightNoMutex(void)
{
	wpCmd.wpListCmd = NAV_WPLIST_ABORT_FLIGHT;
	wpCmd.wpListParam1 = 0;
	wpCmd.wpListParam2 = 0;
	wpCmd.wpListCmdCycleCnt = wpListMsgCycleCnt_++;

	cmdPacketSend();

	flightStarted = false;
	uploadStarted = false;
	startFlightAccepted = false;

	flightStatusHandler.state = NAVIGATION_STATE_STOPPED;

	return;
}


/* Exported functions   ------------------------------------------------------*/

/**
 * Initializes the flight mode on aci
 */
uint8_t startFlight() {
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	if(startFlightAccepted){
		return 0;
	}

	wpCmd.wpListCmd = NAV_WPLIST_START_FLIGHT;
	wpCmd.wpListParam1 = 0;
	wpCmd.wpListParam2 = 0;
	wpCmd.wpListCmdCycleCnt = wpListMsgCycleCnt_++;

	cmdPacketSend();
	flightStarted = true;

	flightStatusHandler.state = NAVIGATION_STATE_STARTED;

	return 0;
}

/**
 * Finalizes the flight mode on aci
 */
uint8_t stopFlight() {

	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	stopFlightNoMutex();
	startFlightAccepted = false;

	return 0;
}

/**
 * passes bytes from a serial port to the aci engine
 * @param c received byte from a serial port
 */
uint8_t receive(unsigned char c) {

	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	aciReceiveHandler(&nav1, c);

	return 0;
}

/**
 * passes bytes from a serial port to the aci engine
 * @param c received byte from a serial port
 */
uint8_t receiveArray(unsigned char* c, int cnt)
{

#ifndef DISABLE_MUTEXING
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}
#endif // DISABLE_MUTEXING

	for (int i = 0; i < cnt; i++)
	{
		uint8_t blocked = ADD_TO_TX_BUFFER_RETURN_MUTEX_BLOCKED;
		while(blocked == ADD_TO_TX_BUFFER_RETURN_MUTEX_BLOCKED){
			blocked = aciReceiveHandler(&nav1, c[i]);
		}

	}

	return 0;
}

/**
 * register init callback, which is called when all the params are read
 * @param callback
 */

void registerInitCallback(ParametersCallback callback) {
	paramCallback = callback;
}

/**
 * register wp callback, which is called when a wp is sent
 * @param callback
 */
void registerWpCallback(WpCallback callback) {
	wpCallback = callback;
}

void registerCmdAlarmCallback(CmdAlarmCallback callback) {
	cmdAlarmCallback = callback;
}
/**
 * register flight callback, which is called during the flight to pass the flight status info
 * @param callback
 */
void registerFlightCallback(FlightCallback callback) {
	flightCallback = callback;
}

void registerWriteCacheCallback(WriteCacheCallback callback) {
	writeCacheCallback = callback;
}

void registerTransmitCallbac(TransmitCallback callback) {
	transmitCallback = callback;
}

void registerStringErrorsCallback(StringErrorsCallback callback) {
	stringErrorsCallback = callback;
}

void registerUpdateParamsCallback(UpdateParamsCallback callback){
	updateParamsCallback = callback;
}

void registerLicenseInformationCallback(LicenseInformationCallback callback)
{
	licenseInformationCallback = callback;
}

/**
 * start sending mission procedure to the copter
 * @param wayPoints list of the wps to send
 */
uint8_t sendMission(ATOS_WAYPOINT* wayPoints, int numPoints, int type) {
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	sentWpCnt = 0;
	currentWPInd = 0;
	totalWpCount = numPoints;
	wayPoints_ = wayPoints;

	flightStatusHandler.startWaypoint = wayPoints_[0].id;
	flightStatusHandler.endWaypoint = wayPoints_[numPoints - 1].id;
	flightStatusHandler.state = NAVIGATION_STATE_PRELOADING;

	for (int i = 0; i < numPoints; i++) {
		wayPoints[i].camAngleYaw *= 10;
		cout << "Wp num " << i << ": " << wayPoints_[i].lat << ", "
				<< wayPoints_[i].lon << ", " << wayPoints_[i].height << ", "
				<< wayPoints_[i].camAngleYaw << ", " << wayPoints_[i].flags
				<< endl;
	}
	flightStarted = false;
	uploadStarted = true;
	clearAcceptedState = false;
	clearAcceptedState_WP_BUFFER = false;
	//resetWaypointList();
	resetWplistFalcon();

	return 0;
}

/**
 * Send a single waypoint to the copter
 * @param wayPoint: a single waypoint
 */
uint8_t sendMissionPoint(ATOS_WAYPOINT* wayPoint)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	flightStatusHandler.startWaypoint = wayPoint->id;
	flightStatusHandler.endWaypoint = wayPoint->id;

	memcpy(&wp, wayPoint, sizeof(wp));

	cmdPacketSend();

	return 0;
}

void testCallbacks() {
	Parameters* params = new Parameters();
	params->cam_pitch = 90;
	params->height = 100;
	(*paramCallback)(params);

	ATOS_MSG_NAVIGATION_STATUS* status = new ATOS_MSG_NAVIGATION_STATUS();
	status->waypointId = 8;
	(*flightCallback)(status);
}

uint8_t sendInfoRequest() {
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	aciCheckVerConf(&nav1);

	return 0;
}

void registerInfoCallback(InfoCallback callback) {
	infoCallback = callback;
}

void init(char* cachePath, int pathLength) {

	debug_write( "Hello from init");



	fflush(stdout);

	isConnectionClosed = false;
	currentWPInd = 0;
	totalWpCount = 0;

	wpListMsgCycleCnt_ = 0;
	wpListStatusCnt_ = 0;

	clearAcceptedState = false;
	uploadStarted = false;
	flightStarted = false;
	parametersReceived = false;
	startFlightAccepted = false;

	//init aci
	aciInit(&nav1);

	debug_write( "Step 0: Set Debug Callback");
	aciSetDebugOutCallback(&nav1, debugOut);

	debug_write( "Step 1: Before mem create");
	fflush(stdout);
	nav1.alarmstate_cockpit = 0;
	cachePath_ = (char*) malloc(sizeof(char) * pathLength);
	debug_write( "Step before memset");
	fflush(stdout);
	memset((void*) cachePath_, 0, sizeof(char) * pathLength);
	debug_write( "Step after memset");
	fflush(stdout);
	strcpy(cachePath_, cachePath);

	debug_write(cachePath_);
	fflush(stdout);
	openDevice();

	debug_write( "Step 3: Opened device");
	fflush(stdout);
	aciSetWriteHDCallback(&nav1, writeOnDevice);
	aciSetReadHDCallback(&nav1, readFromDevice);
	aciSetResetHDCallback(&nav1, resetRW);

	debug_write( "Step 4: ");
	fflush(stdout);
	aciSetSendDataCallback(&nav1, &transmit);
	aciSetVarListUpdateFinishedCallback(&nav1, &varListUpdateFinished);
	aciSetParamListUpdateFinishedCallback(&nav1, &parListUpdateFinished);
	aciSetCmdListUpdateFinishedCallback(&nav1, &cmdListUpdateFinished);
	aciInfoPacketReceivedCallback(&nav1, &versions);

	debug_write( "Step 5: ");
	fflush(stdout);
	aciSetEngineRate(&nav1, ACI_ENGINE_RATE_HZ);
	aciCheckVerConf(&nav1);

	debug_write( "Step 6: ");
	fflush(stdout);
	//pthread_create(&p_aciEngThread, NULL, aciEngThread, NULL);

	aciGetDeviceVariablesList(&nav1);

	debug_write( "Step 7: ");
	fflush(stdout);
}


void aciEngThread(void) {
	Parameters* params = new Parameters();
	int cnt = 0;
	debug_write("Hello from the aci thread");
	while (!isConnectionClosed) {
		clock_t threadStartTime = clock();

		if(!nav1.TxDataBufferMutex_blocked)
		{
			nav1.TxDataBufferMutex_ACIEngineBlocking = 1;


			aciEngine(&nav1);
			aciSynchronizeVars(&nav1);

			handleFlightStatus();
			handleLicensesCallback();

			// TODO: Clean up this way of reporting errors!!!
			// IDL Update "free string" error messages
			if (nav1.errorMsgIdlCnt > idlErrorCntOld)
			{
				idlErrorCntOld = nav1.errorMsgIdlCnt;

				uint16_t currentOffset = 0;
				for(uint8_t current_error = 0; current_error < nav1.errorMsgIdlCnt; current_error++)
				{
					uint16_t sizeToCopy = sizeof(nav1.errorMsgIdl[current_error]);
					if(currentOffset + sizeToCopy + 1 < sizeof(idlFullErrorString)) // "+1" for '\n'
					{
						memcpy(&idlFullErrorString[currentOffset], nav1.errorMsgIdl[current_error], sizeToCopy);
						currentOffset += sizeToCopy;
						idlFullErrorString[currentOffset] = '\n';
						currentOffset++;
					}
				}

				//stringErrorReport(idlFullErrorString, currentOffset);
			}

			// Drone Update "free string" error messages if no IDL errors active
			if (errorTextExpanded[0])
			{
				debug_write("Err1");
				uint8_t errorNr = errorNoType & 0x1F;
				if (errorNr < TRINITY_ERROR_MSGS_BUFFER_LEN)
				{
					debug_write("Err2");
					strncpy(errorMsg[errorNr], errorTextExpanded, TRINITY_ERROR_MSG_SIZEOF_STRING - 1); // "-1" to ensure term. null char
					errorMsgTypes[errorNr] = errorNoType & 0xE0;

					if(   (stringErrorsReported < errorsListCnt) && (errorNr + 1u == errorsListCnt) // make sure error cycle is complete
							&& (errorsListCnt <= TRINITY_ERROR_MSGS_BUFFER_LEN)) // being careful here
					{
						// error list has grown longer and current error ist last one in last: Let's send all of it to IMC


						printf("%u", errorsListCnt);
						for(uint8_t stringError = 0; stringError < errorsListCnt; stringError++)
						{
							uint16_t currentOffset = (TRINITY_ERROR_MSG_SIZEOF_STRING + 1) * stringError;
							memcpy(&fullErrorString[currentOffset], errorMsg[stringError], TRINITY_ERROR_MSG_SIZEOF_STRING);
							fullErrorString[currentOffset + TRINITY_ERROR_MSG_SIZEOF_STRING] = '\n';

							printf(errorMsg[stringError]);
						}

						printf("Got to fflush");
						fflush(stdout);

						//stringErrorReport(fullErrorString, errorsListCnt * (TRINITY_ERROR_MSG_SIZEOF_STRING + 1));

						printf("Got over Report");



						stringErrorsReported = errorsListCnt;
					}
				}
			}

#ifndef DIRTY_SDC_HACK
			// Update simple display commands
			for (uint8_t index = 0; index < sizeof(simpleCmdList); index++)
			{
				if (((simpleCmdAck&(1<<index))) && (simpleCmdList[index]!=DISPLAY_SIMPLE_CMD_NONE))
				{
					simpleCmdList[index] = DISPLAY_SIMPLE_CMD_NONE;
				}
			}
#endif // DIRTY_SDC_HACK

			debug_write("Sync vars");

			if (parametersReceived != false) {


				//cout << "Debug: "<< lat << ", " << lon << ", " << height << ", " << gps_height << ", " << flight_time << ", " << cam_angle << ", "   << euler.z << ", " << endl;

				debug_write("Received params, try to callback");
				params->lat = lat;
				params->lon = lon;
				params->height = height;
				params->gps_height = gps_height;
				params->flight_time = flight_time;

				params->cam_roll = 0;//TODO, make asctec provide this in future when we are able to stear this as well
				params->cam_pitch = cam_angle;
				params->cam_yaw = euler.z;

				params->gps_speed = gps_speed;

				//cout << "GPS stat " << gps_stat << endl;

				params->gps_stat = gps_stat;
				params->gps_quality = gps_quality;

				//cout << "Sat cnt " << sat_count << endl;

				params->sat_count = gps_numSats;

				params->gps_horizontal_acc = gps_horizontal_acc;
				params->gps_speed_acc = gps_speed_acc;

				params->bat_voltage = droneBmsHost.system_voltage_mV;
				params->roll = euler.x;
				params->pitch = euler.y;
				params->yaw = euler.z;

				params->org_lat = org_lat;
				params->org_lon = org_lon;
				params->org_alt = org_alt;
				params->org_yaw = 0;

				params->rel_x = relCoords.x;
				params->rel_y = relCoords.y;
				params->rel_z = relCoords.z;

				params->flightMode = getFlightmode();


				params->bat_cap_used = droneBmsHost.state_of_charge_total;


				//cout << "Wind: " << params->wind_direction << ", " << params->wind_speed << endl;

				params->wind_direction = windInfo.wind_direction;
				params->wind_speed = windInfo.wind_speed;

				params->motorErrorState = getMotorErrorState();
				params->motorOnState = getMotorOnState();

				params->home_position_lat = home_position.latitude;
				params->home_position_lon = home_position.longitude;
				params->connection_quality = nav1.diversity_lock[0] + nav1.diversity_lock[1];

				params->droneLinkVersionMajor = nav1.droneLinkVersionMajor;
				params->droneLinkVersionMinor = nav1.droneLinkVersionMinor;

				//printf("Drone serial %s", std::string(nav1.droneLinkSerial));
				//fflush(stdout);
				params->droneLinkSerial = 0;//stoi(std::string(nav1.droneLinkSerial));
				//memcpy(params->droneLinkSerial, nav1.droneLinkSerial, sizeof(params->droneLinkSerial));

				fflush(stdout);

				if((cnt %= 3) == 0){
					(*paramCallback)(params);
				}
				cnt++;

				debug_write("Received params, after callback");

			}else{
				clock_t beforeUpdParamsCallback = clock();
				debug_write("Before update callback");
				(*updateParamsCallback)((short)(100 - nav1.RequestedPacketListLength * 100.0/((double)nav1.VarTableLength)),
						(short)(100 - nav1.RequestedCmdPacketListLength * 100.0/((double)nav1.CmdTableLength)),
						(short)(100 - nav1.RequestedParamPacketListLength * 100.0/((double)nav1.ParamTableLength)));
				debug_write("After update callback");

				clock_t afterUpdParamsCallback = clock();
				clock_t paramsUpdTime = afterUpdParamsCallback - beforeUpdParamsCallback;

			}


			debug_write("Before handle upload status");
			if (uploadStarted == true) {
				handleUploadStatus(wpStatus);
			}

			debug_write("Before flight callback");

			if ((takeoffStarted || flightStarted) && navStatus.waypointId != 0 && (cnt % 2 == 0)) {
				(*flightCallback)(&navStatus);
				//startFlightCommandSent = false;
				//stop sending takeoff
				sendingTakeoffFlag = false;
				//printf("Flight status %u \n", flightStatus.status);
				//printf("Flight wp %u \n", flightStatus.waypointId);
				//curFlightWp = flightStatus.waypointId;
			}

			if(nav1.TxDataBufferWritePos && nav1.SendData)
			{
				nav1.SendData(nav1.TxDataBuffer, nav1.TxDataBufferWritePos);

				nav1.TxDataBufferWritePos = 0;
			}
			else if(NULL ==  nav1.SendData)
			{
				printf("Error: Send Callback NULL!!!");
				fflush(stdout);
			}

			nav1.TxDataBufferMutex_ACIEngineBlocking = 0;


		}
		clock_t threadEndTime = clock();
		clock_t threadTime = threadEndTime - threadStartTime;
		uint64_t threadTimeUs = ((uint64_t) threadTime) * 1000; // TODO: Measure in uS !!

#ifdef ENABLE_VERBOSE
		if(threadTimeUs > ACI_ENGINE_SLEEP_TIME_US / 2) // Warn if we are close to not meeting our "real-time" requirement.
		{
			threadTimeViolationCnt++;
			printf("ACI Thread took %lu ms \n. ViolationCnt: %u, OkCnt %u",
					threadTime, threadTimeViolationCnt, threadTimeOkayCnt);
			fflush(stdout);
		}
		else
		{
			threadTimeOkayCnt++;
		}
#endif // ENABLE_VERBOSE

		if(threadTimeUs < ACI_ENGINE_SLEEP_TIME_US)
		{
			usleep(ACI_ENGINE_SLEEP_TIME_US - threadTimeUs);
		}
	}

	delete params;

}

void closeConnection() {

	sendingLandingFlag = false;
	sendingTakeoffFlag = false;
	isConnectionClosed = true;
	cacheRead.close();
	cacheWrite.close();

	free(cachePath_);

}

uint8_t sendCommand(int commandId, unsigned short defaultParam) {

	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{

		fflush(stdout);
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	rc_cmd_t cmd;
	rc_param_t param;

	cout << "Sent command " << commandId + 1 << " with param " << defaultParam
			<< endl;
	fflush(stdout);

	switch (commandId) {
	case 1:
		cmd = RC_CMD_RC_DRONE_CONNECT;
		param = defaultParam;
		break;

	default:
	case 0:
	case 2:
	case 3:
	case 4:
		cout << "Cmd" << cmd <<  " no longer supported";
		return ADD_TO_TX_BUFFER_OTHER_ERROR;
	}

	uint16_t data[2];
	data[0] = cmd;
	data[1] = param;

	return aciTxSendRawPacket(&nav1, ACIMT_PC_RC_CMD,
			ACI_ADDR_PC_USBCOCKPIT_ENDP, ACI_ADDR_PC, data, 4);
}

// Refer to https://en.wikipedia.org/wiki/Aircraft_principal_axes for definition of
// yaw, pitch, roll, thrust.
// If this is not received for 300ms (DIVERSITY_CMD_FROM_EXTERNAL_TIMEOUT), Drone
// will assume all positions are neutral.
uint8_t sendJoystickData(float yaw, float pitch, float roll, float thrust)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	static uint8_t CmdUpdateCnt;

#pragma pack(1)

	typedef struct  {
		float yaw;
		float pitch;
		float roll;
		float thrust;
		uint8_t CmdUpdateCnt;
	} pc_joystick_command_t;

#pragma pack()

	pc_joystick_command_t pc_joystick_command = {
			.yaw = yaw,
			.pitch = pitch,
			.roll = roll,
			.thrust = thrust,
			.CmdUpdateCnt = CmdUpdateCnt,
	};

	CmdUpdateCnt++;

	return aciTxSendRawPacket(&nav1, ACIMT_JOYSTICK_CMD,
	                     ACI_ADDR_DIVERSITY,
	                     ACI_ADDR_PC,
	                     (uint8_t*) &pc_joystick_command,
	                     sizeof(pc_joystick_command));
}

// Thrust stick needs to be pulled down
uint8_t TurnOnMotors(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	aci_flightmode_change_request.atos_msg_flightmode_change_request = FM_CR_CLIENT_FLYING;
	aci_flightmode_change_request.cmd_cnt++;

	cmdPacketSend();

	return 0;
}

// Thrust stick needs to be pulled down
uint8_t TurnOffMotors(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}


	aci_flightmode_change_request.atos_msg_flightmode_change_request = FM_CR_CLIENT_FLYING_OFF;
	aci_flightmode_change_request.cmd_cnt++;

	cmdPacketSend();

	return 0;
}

// On Power-Up, the falcon needs to be unblocked before e.g. motors may be started.
uint8_t UnblockFalcon(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	aci_flightmode_change_request.atos_msg_flightmode_change_request = FM_CR_CLIENT_FALCON_BLOCKED_OFF;
	aci_flightmode_change_request.cmd_cnt++;

	cmdPacketSend();

	return 0;
}

uint8_t SetEmergencyMode(emergencyMode_t emergencyMode)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	aci_cmd_emergency_mode = emergencyMode;

	cmdPacketSend();

	return 0;
}

uint8_t SetFlightMode(imc_flightmode_t flightMode)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}


	uint16_t data[2];

	switch(flightMode)
	{
	case IMC_FLIGHTMODE_MANUAL:
		data[0] = RC_CMD_DRONE_SET_FLIGHTMODE;
		data[1] = SET_FLIGHT_MODE_CMD_MANUAL;
		break;

	case IMC_FLIGHTMODE_HEIGHT:
		data[0] = RC_CMD_DRONE_SET_FLIGHTMODE;
		data[1] = SET_FLIGHT_MODE_CMD_HEIGHT;
		break;

	case IMC_FLIGHTMODE_GPS:
		data[0] = RC_CMD_DRONE_SET_FLIGHTMODE;
		data[1] = SET_FLIGHT_MODE_CMD_GPS;
		break;

	case IMC_FLIGHTMODE_NOT_AVAILABLE: // no break intended
	default: // should not happen
		return ADD_TO_TX_BUFFER_OTHER_ERROR;
		break;
	}

	return aciTxSendRawPacket(&nav1, ACIMT_PC_RC_CMD,
			ACI_ADDR_PC_USBCOCKPIT_ENDP, ACI_ADDR_PC, data, sizeof(data));
}

// See USBCOCKPITTOPC_INITINFO in aciRxHandleMessage
uint8_t RequestDroneLinkInfo(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}


	uint16_t data[2];

	data[0] = RC_CMD_RC_REQUEST_DRONELINKINFO;
	data[1] = 0;

	return aciTxSendRawPacket(&nav1, ACIMT_PC_RC_CMD,
				ACI_ADDR_PC_USBCOCKPIT_ENDP, ACI_ADDR_PC, data, sizeof(data));
}

// Normal drone link functionality will not be available --> This is for updating.
uint8_t StartDroneLinkBootloader(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	uint16_t data[2];

		data[0] = RC_CMD_RC_START_BOOTLOADER;
		data[1] = 0;

		return aciTxSendRawPacket(&nav1, ACIMT_PC_RC_CMD,
					ACI_ADDR_PC_USBCOCKPIT_ENDP, ACI_ADDR_PC, data, sizeof(data));
}

uint8_t sendRTCMData(char* data, int size){
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	//uint16_t data[2];

	//little endian on Intel processors
	return aciTxSendRawPacket(&nav1, ACIMTTx_RTCMDATA, ACI_ADDR_DIVERSITY, ACI_ADDR_NAV1_PC_ENDP, data, size);
	//printf("byte0 %x", data[0]);
	//printf("byte1 %x", data[1]);
	//printf("byte2 %x", data[2]);
	//printf("byte3 %x", data[3]);
}

// See ATOS_Messages_Defines.h ... SDC_XXX
uint8_t sendSimpleCmd(uint8_t simpleCmdId)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}


	for(uint8_t index = 0; index < sizeof(simpleCmdList); index++)
	{
		if (((simpleCmdAck&(1<<index))==0) && (simpleCmdList[index]==DISPLAY_SIMPLE_CMD_NONE))
		{
			simpleCmdList[index] = simpleCmdId;
			break;
		}
	}

	cmdPacketSend();

	return 0;
}

// If the return to home command is sent, the drone will fly to the GPS position the motors were turned on at.
// Home position is given by ATOS_DBG_HOME_POSITION
uint8_t cmdReturnToHome(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	flightStatusHandler.state = NAVIGATION_STATE_FLYING_HOME_STARTED;

	return 0;
}

// Some commands are only accepted by drone if the sender has been granted the use of
// switchable commands. With this function this may be requested.
uint8_t requestSwitchableCmdRights(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

#ifndef DIRTY_SDC_HACK
	sendSimpleCmd(SDC_WP_CTRL_BY_IDL);
	return 0
#else // DIRTY_SDC_HACK
	printf("Sending simple display command \n");
	fflush(stdout);

	single_sdc.sdc = SDC_WP_CTRL_BY_IDL;
	single_sdc.counter++;

	return aciTxSendSingleCommand(&nav1, ATOS_DBG_CMD_SINGLE_SDC, (uint8_t*) &single_sdc, sizeof(single_sdc));
#endif // DIRTY_SDC_HACK
}

// Some commands are only accepted by drone if the sender has been granted the use of
// switchable commands.
bool gotSwitchableCmdRights(void)
{
	if (SW_CMD_STATE_IDL != switchableCmdState)
	{
		return false;
	}
	else
	{
		return true;
	}
}

imc_flightmode_t getFlightmode(void)
{
	imc_flightmode_t imc_flightmode = IMC_FLIGHTMODE_NOT_AVAILABLE;

	if(flightMode & FM_POS)
	{
		imc_flightmode = IMC_FLIGHTMODE_GPS;
	}
	else if (flightMode & FM_HEIGHT)
	{
		imc_flightmode = IMC_FLIGHTMODE_HEIGHT;
	}
	else if (flightMode & FM_ACC)
	{
		imc_flightmode = IMC_FLIGHTMODE_MANUAL;
	}

	return imc_flightmode;
}

motorOnState_t getMotorOnState(void)
{
	motorOnState_t motorstate = MOTORONSTATE_UNKNOWN;

	if(flightMode & FM_FLYING)
	{
		motorstate = MOTORONSTATE_TURNED_ON;
	}
	else if (0 != flightMode) // don't want to report turned off motors when we just haven't received anything
	{
		motorstate = MOTORONSTATE_TURNED_OFF;
	}
	else
	{
		motorstate = MOTORONSTATE_UNKNOWN;
	}

	return motorstate;
}

motorErrorState_t getMotorErrorState(void)
{
	motorErrorState_t motorErrorState = MOTORERRORSTATE_OK;

	// Check motor error flags
	if(motorErrorFlags)
	{
		return MOTORERRORSTATE_ERROR;
	}

	// Check if all motors are turning if they should be
	if(MOTORONSTATE_TURNED_ON == getMotorOnState())
	{
		for(uint8_t motor = 0; motor < sizeof(motorSpeeds) / sizeof(motorSpeeds[0]); motor++)
		{
			if(0 == motorSpeeds[motor])
			{
				return MOTORERRORSTATE_ERROR;
			}
		}
	}

	return motorErrorState;
}

uint8_t idlRTCMStreamDeactivate(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	uint8_t data[2];

	data[0] = RC_CMD_RC_IDL_RTCMSTREAM_TOGGLE_ACTIVE;
	data[1] = 0;

	return aciTxSendRawPacket(&nav1, ACIMT_PC_RC_CMD,
			ACI_ADDR_PC_USBCOCKPIT_ENDP, ACI_ADDR_PC, data, sizeof(data));
}

uint8_t idlRTCMStreamActivate(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	uint8_t data[2];

	data[0] = RC_CMD_RC_IDL_RTCMSTREAM_TOGGLE_ACTIVE;
	data[1] = 1;

	return aciTxSendRawPacket(&nav1, ACIMT_PC_RC_CMD,
			ACI_ADDR_PC_USBCOCKPIT_ENDP, ACI_ADDR_PC, data, sizeof(data));
}

// see flightStatusHandler_t
uint8_t getNavigationState(void)
{
	return flightStatusHandler.state;
}

/**
 * Function to initialze auto-takeoff/landing.
 * @param latitude, longitude: Destination for for Takeoff / Land maneuver
 * @param height : In case of takeoff it is the final height, in case of landing it is the height up to which
 * 				   the drone will fly on a direct path to supplied waypoint
 * 				   (once waypoint is reached, drone will go straight down).
 * @param speed:  The speed at which the drone will fly to waypoint
 * @param acceleration: The maximal acceleration used [not implemented yet]
 */
uint8_t takeOffLandingInitialize(int32_t latitude, int32_t longitude, float height, float speed, float acceleration)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	ATOS_WAYPOINT waypoint = {
			.id = 1,
			.lat = latitude,
			.lon = longitude,
			.height = height,
			.camAnglePitch = 0,
			.camAngleRoll = 0,
			.camAngleYaw = 0,
			.speed = speed,
			.desiredAcceleration = acceleration,
			.flags = 0,
			.event1 = 0,
			.event2 = 0,
			.waitTimeEvent1 = 0,
			.waitTimeEvent2 = 0,
			.parameterEvent1 = 0,
			.parameterEvent2 = 0,
	};

	takeoffLandingWaypointCrc = crc16(&waypoint, sizeof(waypoint));

	takeoffStarted = true;
	startFlightAccepted = false;

	return sendMissionPoint(&waypoint);
}

uint8_t navigationPause(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	if(NAVIGATION_STATE_FLYING != flightStatusHandler.state)
	{
		return ADD_TO_TX_BUFFER_OTHER_ERROR;
	}

	stopFlight();
	flightStatusHandler.state = NAVIGATION_STATE_TOBEPAUSED;

	return 0;
}

uint8_t navigationResume(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

	if(NAVIGATION_STATE_PAUSED != flightStatusHandler.state)
	{
		return ADD_TO_TX_BUFFER_OTHER_ERROR;
	}

	sentWpCnt = 0;

	if(currentWPInd >= totalWpCount)
	{
		flightStatusHandler.state = NAVIGATION_STATE_IDLE;
		return ADD_TO_TX_BUFFER_OTHER_ERROR;
	}
	else if(wayPoints_[currentWPInd].id == flightStatusHandler.lastFinishedWaypoint)
	{
		totalWpCount = totalWpCount - currentWPInd;
		wayPoints_ = &wayPoints_[currentWPInd + 1];
	}
	else // last sent waypoint was not executed
	{
		totalWpCount = totalWpCount - (currentWPInd - 1);
		wayPoints_ = &wayPoints_[currentWPInd];
	}

	flightStatusHandler.startWaypoint = wayPoints_[0].id;
	flightStatusHandler.endWaypoint = wayPoints_[totalWpCount - 1].id;
	flightStatusHandler.state = NAVIGATION_STATE_PRELOADING;

	for (int i = 0; i < totalWpCount; i++) {
		cout << "Wp num " << i << ": " << wayPoints_[i].lat << ", "
				<< wayPoints_[i].lon << ", " << wayPoints_[i].height << ", "
				<< wayPoints_[i].camAngleYaw << ", " << wayPoints_[i].flags
				<< endl;
	}

	flightStarted = false;
	uploadStarted = true;
	clearAcceptedState = false;
	clearAcceptedState_WP_BUFFER = false;

	resetWplistFalcon();

	return 0;
}

/**
 * This functions starts takeoff/landing procedure and is used as a deadman's switch for
 * continued execution. It expects that takeOffLandingInitialize() has been called beforehand.
 * This functions needs to be called at least every AUTO_TAKEOFF_LANDING_DEADMANSWITCH_TIMEOUT.
 * @param takeoff_landing_execute: See takeoff_landing_execute_t
 */
uint8_t takeOffLandingExecute(uint8_t takeoff_landing_execute)
{
	static uint8_t takeOffLandingCycle;

	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}


	wpCmd.wpListCmd = NAV_WPLIST_AUTO_LAND_TAKEOFF;
	wpCmd.wpListParam1 = takeoffLandingWaypointCrc;
	wpCmd.wpListCmdCycleCnt = takeOffLandingCycle++;

	switch((takeoff_landing_execute_t) takeoff_landing_execute)
	{
	case  TAKEOFF_LANDING_EXECUTE_TAKEOFF:
		wpCmd.wpListParam2 = AUTO_TAKEOFF_LANDING_FLAG_TAKEOFF;

		if(NAVIGATION_STATE_TAKING_OFF != flightStatusHandler.state)
		{
			flightStatusHandler.state = NAVIGATION_STATE_TAKEOFF_STARTED;
		}
		break;

	case  TAKEOFF_LANDING_EXECUTE_LANDING:
		wpCmd.wpListParam2 = AUTO_TAKEOFF_LANDING_FLAG_LAND;

		if(NAVIGATION_STATE_LANDING != flightStatusHandler.state)
		{
			flightStatusHandler.state = NAVIGATION_STATE_LANDING_STARTED;
		}
		break;

	case TAKEOFF_LANDING_EXECUTE_NONE: // no break intended
	default:
		return ADD_TO_TX_BUFFER_OTHER_ERROR;
		break;
	}

	cmdPacketSend();

	return 0;
}


/**
 * This function requests joystick control. Joystick control will be granted until cockpit's joysticks are used.
 * joystick control is granted by default if no cockpit is connected to the drone.
 */
uint8_t requestJoystickControl(void)
{
	if(nav1.TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED;
	}

#ifndef DIRTY_SDC_HACK
	sendSimpleCmd(SDC_JOYSTICK_CTRL_BY_IDL);
	return 0
#else // DIRTY_SDC_HACK
	single_sdc.sdc = SDC_JOYSTICK_CTRL_BY_IDL;
	single_sdc.counter++;

	return aciTxSendSingleCommand(&nav1, ATOS_DBG_CMD_SINGLE_SDC, (uint8_t*) &single_sdc, sizeof(single_sdc));
#endif // DIRTY_SDC_HACK

	return 0;
}

bool gotJoystickControl(void)
{
	//printf("remote slave state: %u", remoteSlaveState);
	fflush(stdout);
	if(remoteSlaveState & REMOTE_SLAVES_STATE_FLAG_IDL_JOYSTICK_CTRL)
	{
		return true;
	}

	return false;
}



static void debugOut(const char* string)
{
	printf(string);
	fflush(stdout);

	return;
}

