/*

Copyright (c) 2012, Ascending Technologies GmbH
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.

 */

#ifndef ASCTECCOMMINTF_H_
#define ASCTECCOMMINTF_H_

#ifdef __cplusplus
extern "C"
{
#endif


#include <stdlib.h>
#include <stdio.h>
#include "asctecDefines.h"
#include <stdint.h>


//#define ACI_SHORT_MEM_TABLES

#pragma pack(push, 1)

typedef struct
{
	char msg[43];
	unsigned short errorId;
	unsigned char componentId;
	unsigned int parameter[2];
	unsigned int timestamp;
	unsigned char timestamp_half_hours;
	unsigned short multipleErrorCnt;
} atos_error_msg_t;

typedef struct {
	uint8_t msg_type; // see usb_cockpit_to_pc_msg_type_t
	uint8_t error_nr;
	atos_error_msg_t errorMsg;
} usb_cockpit_to_pc_errors_t;

typedef struct {
	uint8_t msg_type; // see usb_cockpit_to_pc_msg_type_t
	uint32_t version_major;
	uint32_t version_minor;
	uint8_t serial_number[16];
} usb_cockpit_to_pc_initinfo_t;

typedef struct {
	uint8_t msg_type; // see usb_cockpit_to_pc_msg_type_t
	uint8_t diversity_lock[2];/*! if sum of entries < 4 (<8) then link weak (lost) */
	uint8_t gps_quality_base;/*! 0 to 100% */
	uint8_t radiolink_region_config; /*! see cc2500_rf_config_t */
} usb_cockpit_to_pc_runtimeinfo_t;

struct ACI_MEM_TABLE_ENTRY_LONG {
  unsigned short id;
  char name[MAX_NAME_LENGTH];
  char description[MAX_DESC_LENGTH];
  char unit[MAX_UNIT_LENGTH];
  unsigned short varType;
  void* ptrToVar;
};

struct ACI_MEM_TABLE_ENTRY {
  unsigned short id;
#ifndef ACI_SHORT_MEM_TABLES
  char name[MAX_NAME_LENGTH];
  char description[MAX_DESC_LENGTH];
  char unit[MAX_UNIT_LENGTH];
#endif
  unsigned short varType;
  void* ptrToVar;
};

struct ACI_MEM_VAR_TABLE {
  struct ACI_MEM_TABLE_ENTRY tableEntry;
  struct ACI_MEM_VAR_TABLE* next;
};


///this package is fixed and should never be changed!
struct ACI_INFO {
  unsigned char verMajor;
  unsigned char verMinor;
  unsigned char maxNameLength;
  unsigned char maxDescLength;
  unsigned char maxUnitLength;
  unsigned char maxVarPackets;
  unsigned char memPacketMaxVars;
  unsigned short flags;
  unsigned short dummy[8];
};

struct ACI_MEM_VAR_ASSIGN_TABLE {
  void* ptrToVar;
  unsigned short varType;
  unsigned short id;
  struct ACI_MEM_VAR_ASSIGN_TABLE* next;
};
#pragma pack(pop)

/*enum MessageTypeTx : unsigned char {
  // both directions
  ACIMTTx_PARAMPACKET = 0x14, // send packet and ack
  ACIMTTx_PARAM = 0x49,  // request and receive single parameter
  ACIMTTx_SAVEPARAM = 0x72, // request param save / ack
  ACIMTTx_LOADPARAM = 0x73, // request param load / ack

  ACIMTTx_SINGLESEND = 0x60, // single variable reversed
  ACIMTTx_SINGLEREQ = 0x61, // request single variable

  ACIMTTx_MAGICCODES = 0x62, // request and receive magiccodes

  ACIMTTx_INFO_REQUEST = 0x70, // request version info
  ACIMTTx_INFO_REPLY = 0x71, // receive version info

  // Remote->Onboard
  ACIMTTx_REQUESTVARTABLEENTRIES = 0x01,
  ACIMTTx_GETVARTABLEINFO = 0x03,

  ACIMTTx_REQUESTCMDTABLEENTRIES = 0x04,
  ACIMTTx_GETCMDTABLEINFO = 0x06,

  ACIMTTx_REQUESTPARAMTABLEENTRIES = 0x07,
  ACIMTTx_GETPARAMTABLEINFO = 0x09,

  ACIMTTx_UPDATEVARPACKET = 0x0A,
  //0x10-0x1f are reserved for update var packets!
  ACIMTTx_UPDATEPARAMPACKET = 0x13,
  //0x60-0x6f are reserved for update param packet config!
  ACIMTTx_UPDATECMDPACKET = 0x10,
  //0x30-0x3f are reserved for update cmd packet config!
  ACIMTTx_UPDATERCVARPACKET = 0x53,
  ACIMTTx_UPDATERCCMDPACKET = 0x54,

  ACIMTTx_RCCMDPACKET = 0xB0,
  ACIMTTx_RTCMDATA = 0x87, // RTCM data packets for RTK
  ACIMTTx_CMDPACKET = 0x11, // send command packet
  //0x40-0x4f are reserved for cmd packets!

  ACIMTTx_CHANGEPACKETRATE = 0x0B,
  ACIMTTx_GETPACKETRATE = 0x0C,
  ACIMTTx_RESETREMOTE = 0x0D,

  ACIMT_DIVERSITY_EXTCMD = 0x82,
};*/

enum ExtCmdFlag {
  EXT_CMT_FLAG_YAW_AND_PITCH = 0xA0,
  EXT_CMD_FLAG_SOURCE_JOYSTICK = 0x10000000,
  EXT_CMD_FLAG_SOURCE_GAMEPAD = 0x20000000,
};

typedef enum {
	CC2500_RF_CONFIG_LOW_POWER_MODE,
	CC2500_RF_CONFIG_CE,
	CC2500_RF_CONFIG_FCC,
	CC2500_RF_CONFIG_NUMBEROF,
} cc2500_rf_config_t;

enum GamepadButton {
  GAMEPAD_DETECTED = 0x0800, //immer setzen!
  GAMEPAD_BTN_A = 0x1000,
  GAMEPAD_BTN_B = 0x2000,
  GAMEPAD_BTN_X = 0x4000,
  GAMEPAD_BTN_Y = 0x8000,
  GAMEPAD_BTN_Start = 0x0010,
  GAMEPAD_BTN_Back = 0x0020,
  GAMEPAD_BTN_LStick = 0x0040,
  GAMEPAD_BTN_RStick = 0x0080,
  GAMEPAD_BTN_LB = 0x0100,
  GAMEPAD_BTN_RB = 0x0200,
  GAMEPAD_BTN_Xbox = 0x0400,
  GAMEPAD_BTN_Up = 0x0001,
  GAMEPAD_BTN_Down = 0x0002,
  GAMEPAD_BTN_Left = 0x0004,
  GAMEPAD_BTN_Right = 0x0008,
};

typedef enum {
	ADD_TO_TX_BUFFER_RETURN_SUCCESS,
	ADD_TO_TX_BUFFER_RETURN_MUTEX_BLOCKED,
	ADD_TO_TX_BUFFER_RETURN_ACI_MUTEX_BLOCKED,
	ADD_TO_TX_BUFFER_RETURN_BUFFER_FULL,
	ADD_TO_TX_BUFFER_OTHER_ERROR,
} addToTxBufferReturn_t;

typedef struct ACI_MASTER {
  unsigned int EngineRate;

  //aci var table global variables
  unsigned short VarTableLength;
  unsigned short* RequestedPacketList;
  unsigned short RequestedPacketListLength;
  unsigned short RequestedPacketListTimeOut;

  unsigned short RequestVarListTimeout;
  unsigned short RequestCmdListTimeout;
  unsigned short RequestParListTimeout;

  unsigned char CmdWithAck[MAX_VAR_PACKETS] ;
  unsigned short* VarPacket[MAX_VAR_PACKETS + 1];
  unsigned char* VarPacketBuffer[MAX_VAR_PACKETS + 1];
  unsigned char* VarPacketContentBuffer[MAX_VAR_PACKETS + 1];

  unsigned char VarPacketContentBufferValid[MAX_VAR_PACKETS + 1];
  unsigned char VarPacketContentBufferInvalidCnt[MAX_VAR_PACKETS + 1];

  unsigned short VarPacketContentBufferLength[MAX_VAR_PACKETS + 1];
  unsigned short VarPacketLength[MAX_VAR_PACKETS + 1];
  unsigned short VarPacketTotalLength[MAX_VAR_PACKETS + 1];
  unsigned short UpdateVarPacketTimeOut[MAX_VAR_PACKETS + 1];
  unsigned char VarPacketMagicCode[MAX_VAR_PACKETS + 1];
  unsigned short VarPacketTransmissionRate[MAX_VAR_PACKETS];

  // Command
  //internal global vars
  unsigned short CmdTableLength;
  unsigned short* RequestedCmdPacketList;
  unsigned short RequestedCmdPacketListLength;
  unsigned short RequestedCmdPacketListTimeOut;

  unsigned char
  CmdPacketSendStatus[MAX_VAR_PACKETS]; // 0 nothing to send, 1 something to send, 2 waiting for ACK
  unsigned short* CmdPacket[MAX_VAR_PACKETS];
  unsigned short CmdPacketLength[MAX_VAR_PACKETS];
  unsigned short UpdateCmdPacketTimeOut[MAX_VAR_PACKETS];
  unsigned char CmdPacketMagicCode[MAX_VAR_PACKETS];
  unsigned short CmdPacketContentBufferLength[MAX_VAR_PACKETS];

  unsigned char* CmdPacketContentBuffer[MAX_VAR_PACKETS];

  // Parameter
  unsigned short ParamTableLength;
  unsigned short* RequestedParamPacketList;
  unsigned short RequestedParamPacketListLength;
  unsigned short RequestedParamPacketListTimeOut;

  unsigned char
  ParamPacketSendStatus[MAX_VAR_PACKETS]; // 0 nothing to send, 1 something to send, 2 waiting for ACK
  unsigned short* ParamPacket[MAX_VAR_PACKETS];
  unsigned short ParamPacketLength[MAX_VAR_PACKETS];
  unsigned short UpdateParamPacketTimeOut[MAX_VAR_PACKETS];
  unsigned char ParamPacketStatus[MAX_VAR_PACKETS];
  unsigned char ParamPacketMagicCode[MAX_VAR_PACKETS];
  unsigned short ParamPacketContentBufferLength[MAX_VAR_PACKETS];

  unsigned char* ParamPacketContentBuffer[MAX_VAR_PACKETS];


  unsigned short rxPacketTimeOut;
  unsigned char MyId;

  unsigned char recId;
  unsigned char DestId;
  // unsigned short VarPacketCurrentSize[MAX_VAR_PACKETS]={0,0,0};
  // unsigned short VarPacketNumberOfVars[MAX_VAR_PACKETS]={0,0,0};
  unsigned char CmdPacketUpdated[MAX_VAR_PACKETS];

  ///table to handle local ID to var connection
  struct ACI_MEM_VAR_ASSIGN_TABLE* VarAssignTableStart;
  struct ACI_MEM_VAR_ASSIGN_TABLE* VarAssignTableCurrent;

  struct ACI_MEM_VAR_ASSIGN_TABLE* CmdAssignTableStart;
  struct ACI_MEM_VAR_ASSIGN_TABLE* CmdAssignTableCurrent;

  struct ACI_MEM_VAR_ASSIGN_TABLE* ParamAssignTableStart;
  struct ACI_MEM_VAR_ASSIGN_TABLE* ParamAssignTableCurrent;

  struct ACI_INFO Info;

  // TX global variables
  uint8_t TxDataBuffer[ACI_TX_RINGBUFFER_SIZE];
  volatile uint16_t TxDataBufferWritePos;
  volatile uint8_t TxDataBufferMutex_blocked;
  volatile uint8_t TxDataBufferMutex_ACIEngineBlocking;

  //RX global variables
  unsigned char RxDataBuffer[ACI_RX_BUFFER_SIZE];
  unsigned short RxDataCnt;

  struct ACI_MEM_VAR_TABLE* MemVarTableStart;
  struct ACI_MEM_VAR_TABLE* MemVarTableCurrent;

  struct ACI_MEM_VAR_TABLE* MemCmdTableStart;
  struct ACI_MEM_VAR_TABLE* MemCmdTableCurrent;

  struct ACI_MEM_VAR_TABLE* MemParamTableStart;
  struct ACI_MEM_VAR_TABLE* MemParamTableCurrent;
  void (*SendData)(void* data, unsigned short cnt);
  void (*VarListUpdateFinished)(void);
  void (*CmdListUpdateFinished)(void);
  void (*CmdAck)(unsigned char packet);
  void (*ParamListUpdateFinished)(void);
  void (*InfoRec)(struct ACI_INFO);
  void (*VarPacketRec)(unsigned char packet);
  void (*ParaStoredC)(void);
  void (*ParaLoadedC)(void);
  void (*SingleReceivedC)(unsigned short id, void* data, unsigned short varType);
  void (*SingleReqReceivedC)(unsigned short id, void* data,
                             unsigned short varType);
  void (*diversityPacketReceived)(unsigned char src, unsigned short id,
                                  void* data, unsigned short length);
  int (*ReadHDC)(void* data, int bytes);
  int (*WriteHDC)(void* data, int bytes);
  void (*ResetHDC)(void);
  void (*DebugOut)(const char* c);
  void (*SyncStatus)(unsigned char status, unsigned char percent);
  void (*paramReceived)(unsigned short id);
  void (*ParamUpdateAck)(void);

  unsigned short MagicCodeVarLoaded;
  unsigned short MagicCodeCmdLoaded;
  unsigned short MagicCodeParLoaded;
  unsigned short paramRequestTimeout;
  unsigned short paramRequestId;

  unsigned short MagicCodeVar;
  unsigned short MagicCodeCmd;
  unsigned short MagicCodePar;

  unsigned short RequestMagicCodes;

  unsigned char MagicCodeOnHDFalse;

  /*
         * 0x01 var, 0x02 Cmd, 0x04 par
         * 0x10 var already loaded succesfully
         * 0x20 cmd already loaded succesfully
         * 0x40 par already loaded succesfully
         *
         */
  unsigned char RequestListType;

  unsigned char RxState;
  unsigned char RxMessageType;
  unsigned short RxLength;
  unsigned short RxCrc;
  unsigned short RxReceivedCrc;
  unsigned int RxHeaderCnt;
  unsigned int CrcErrorCnt;

  unsigned char reqListLen;
  char magicCodeAlreadyRequested;


  unsigned int ParPacketCnt[MAX_VAR_PACKETS];
  unsigned int CmdPacketCnt[MAX_VAR_PACKETS];

  uint16_t alarmstate_cockpit;
  uint8_t diversity_lock[2];
  uint8_t gps_quality_base; // Averaged Carrier-to-noise-density ratio of GPS satellites used by the base for positioning
  uint8_t radiolink_region_config; // see cc2500_rf_config_t

  uint32_t droneLinkVersionMajor;
  uint32_t droneLinkVersionMinor;
  char droneLinkSerial[16];

  char errorMsgIdl[TRINITY_ERROR_MSGS_BUFFER_LEN][TRINITY_ERROR_MSG_SIZEOF_STRING];
  uint16_t errorMsgIdlCnt;
} aciMaster_t;


uint8_t aciTxSendRawPacket(struct ACI_MASTER * aci,
                        unsigned char aciMessageType,
                        unsigned char dest,
                        unsigned char src,
                        void * data,
                        unsigned short cnt);

uint8_t aciTxSendSingleCommand(struct ACI_MASTER * aci, uint16_t cmdId, uint8_t * data, uint16_t len);

extern void aciSetDebugOutCallback(struct ACI_MASTER* aci,
                                   void (*debugOutCB)(const char* c));

/** Has to be called ones during initialisation
 *  It setup all necessary varibales and struct.
 * **/
extern void aciInit(struct ACI_MASTER* aci);

/**
 * If you are not sure, if the version and configurations of the device is the same like the version of this SDK, you can check this with this function <br>
 *
 */
extern void aciCheckVerConf(struct ACI_MASTER* aci);


/** The aciReceiveHandler is fed by the uart rx function and decodes all necessary packets
*   @param receivedByte received Byte from uart.
*   @see aciSetSendDataCallback
**/
extern uint8_t aciReceiveHandler(struct ACI_MASTER* aci,
                              unsigned char receivedByte);

/**
 *  Has to be called a specified number of times per second. You have to set the rate for the Engine and the heartbeat in aciSetEngineRate().<br>
 *  It handles the transmission to the device and send also a signal, that inform the device, that the remote is still alive and able to send data. If too much data would come from the device and would use the whole bandwidth, it may happen, that the remote cannot send any data. With the signal of a heartbeat, the host is informed, that it could get data from the remote.<br>
 *  After no getting any heartbeat from the remote for a while, the host stops to send data and will send again, if it receive a heartbeat.
 **/
extern void aciEngine(struct ACI_MASTER* aci);


/** Set's the number of time aciEngine is called per second and the heartbeat rate. It's important to make sure that the number of calls and this setting are fitting
  * **/
extern void aciSetEngineRate(struct ACI_MASTER* aci,
                             const unsigned short callsPerSecond);

/** resets remote interface to a zero variable packet configuration **/
extern void aciResetRemote(struct ACI_MASTER* aci);

/** Polls the device variable list. The variable list update finished function aciVarListUpdateFinished() is called on completion.<br>
 * Depends on your update rate and the number of available variables, this could take some time. Be sure, that you don't read the variable list before the variable list update finished function was executed<br>
 * You have to request the variable list one time only.
 *
 **/
extern void aciGetDeviceVariablesList(struct ACI_MASTER* aci);

/** Polls the device command list. The command list update finished function aciCmdListUpdateFinished() is called on completion.<br>
 * Depends on your update rate and the number of available commands, this could take some time. Be sure, that you don't read the command list before the command list update finished function was executed<br>
 * You have to request the command list one time only.
 **/
extern void aciGetDeviceCommandsList(struct ACI_MASTER* aci);

/** Polls the device parameter list. The parameter list update finished function aciParListUpdateFinished() is called on completion.<br>
 * Depends on your update rate and the number of available parameter, this could take some time. Be sure, that you don't read the parameter list before the parameter list update finished function was executed<br>
 * You have to request the parameter list one time only.
 **/
extern void aciGetDeviceParametersList(struct ACI_MASTER* aci);

/**
 * Send a signal to the device, that it shall save all parameters on the EEPROM.
 */
extern void aciSendParamStore(struct ACI_MASTER* aci);

/**
 * Send a signal to the device, that it shall load all parameters from the EEPROM.
 */
extern void aciSendParamLoad(struct ACI_MASTER* aci);

/**
 * Request the value of a parameter from the device. The content will be written in the assign variable for that parameter.
 * @param id The id of the parameter
 * @return none
 */
extern void aciGetParamFromDevice(struct ACI_MASTER* aci, unsigned short id);

/**
 * Return the information, if a parameter packet was updated
 * @param packetid The id of the packet
 * @return Return 1, if the packet with its parameters are already received, otherwise 0
 */
extern unsigned char aciGetParamPacketStatus(struct ACI_MASTER* aci,
    unsigned short packetid);
/** Get the ACI info packet.
 * @return If no packet was received, all variables in the ACI_INFO struct are 0, else it returns the values.
 * **/
extern struct ACI_INFO aciGetInfo(struct ACI_MASTER* aci);


/** Return the number of variables, you get after calling @See aciGetRemoteVariablesList(). <br>
 * @return It returns 0, if no variables are available (i.e. if @See aciGetRemoteVariablesList() were not called)
 * **/

extern unsigned short aciGetVarTableLength(struct ACI_MASTER* aci);

/** Find a variable by id <br>
 * Normally, you request a variable of the device by declaring a pointer on your own created variable. In this case, you can use @See aciSynchronizeVars() to copy the received buffer into your variable. <br>
 * Otherwise you can also access to the variable and all the information about it by using this function.
 * @param id The id of the variable you are looking for.
 * @return It returns a pointer on a struct (#ACI_MEM_TABLE_ENTRY), which contains all information about the variable. If the id of the variable doesn't exist, it returns NULL.
 **/
extern struct ACI_MEM_TABLE_ENTRY* aciGetVariableItemById(
  struct ACI_MASTER* aci, unsigned short id);

/** Find a command by id <br>
 * Normally, you create a command of the device by declaring a pointer on your own created variable. After setting your variable by the command, you want to send, you call aciUpdateCmdPacket() with its packet id to send it.<br>
 * Otherwise you can also access to the command and all the information about it by using this function.
 * @param id The id of the command you are looking for.
 * @return It returns a pointer on a struct, which contains all information about the command. If the id of the command doesn't exist, it returns NULL.
 **/
extern struct ACI_MEM_TABLE_ENTRY* aciGetCommandItemById(struct ACI_MASTER* aci,
    unsigned short id);

/** Find a parameter by id <br>
 * Normally, you create a parameter of the device by declaring a pointer on your own created variable. After setting your variable by the parameter, you can synchronize it with the device.<br>
 * Otherwise you can also access to the parameter and all the information about it by using this function.
 * @param id The id of the parameter you are looking for.
 * @return It returns a pointer on a struct, which contains all information about the parameter. If the id of the parameter doesn't exist, it returns NULL.
 **/
extern struct ACI_MEM_TABLE_ENTRY* aciGetParameterItemById(
  struct ACI_MASTER* aci, unsigned short id);

/** Get a variable by index. The index of the variable is defined, when the parameter was received after calling aciGetRemoteParamtersList().
 * @param index The index of the variable in the list
 * @return It returns a pointer on a struct,, which contains all information about the variable. If the id of the variable doesn't exist, it returns NULL.
 * **/
extern struct ACI_MEM_TABLE_ENTRY* aciGetVariableItemByIndex(
  struct ACI_MASTER* aci, unsigned short index);
extern struct ACI_MEM_TABLE_ENTRY* aciGetParameterItemByIndex(
  struct ACI_MASTER* aci, unsigned short index);

/** Reset variable packet content. Call @See aciSendVariablePacketConfiguration for changes to get effective
 * @param packetId The id of the packet you want to reset.
 * @return none
 * **/
extern void aciResetVarPacketContent(struct ACI_MASTER* aci,
                                     unsigned char packetId);

/** Reset command packet content. Call @See aciSendCommandPacketConfiguration for changes to get effective
 * @param packetId The id of the packet you want to reset.
 * @return none
 **/

extern void aciResetCmdPacketContent(struct ACI_MASTER* aci,
                                     unsigned char packetId);

/** Reset parameter packet content. Call @See aciSendParameterPacketConfiguration for changes to get effective
 * @param packetId The id of the packet you want to reset.
 * @return none
 **/
extern void aciResetParPacketContent(struct ACI_MASTER* aci,
                                     unsigned char packetId);

/** Get the length of a variable package
 * @param packetId The id of the package
 * @return The length of the package
 * **/
extern unsigned short aciGetVarPacketLength(struct ACI_MASTER* aci,
    unsigned char packetId);

/** Get the length of a command package
 * @param packetId The id of the package
 * @return The length of the package
 * **/
extern unsigned short aciGetCmdPacketLength(struct ACI_MASTER* aci,
    unsigned char packetId);

/** Get the length of a parameter package
 * @param packetId The id of the package
 * @return The length of the package
 * **/
extern unsigned short aciGetParPacketLength(struct ACI_MASTER* aci,
    unsigned char packetId);

/** Get a variable packet item by index.
 * @param packetId The id of the packet
 * @param index The index of the variable in the packet
 * @return Return the id of the item if exist, otherwise 0
 *  **/
extern unsigned short aciGetVarPacketItem(struct ACI_MASTER* aci,
    unsigned char packetId, unsigned short index);

/** Get a command packet item by index.
 * @param packetId The id of the packet
 * @param index The index of the coammand in the packet
 * @return Return the id of the item if exist, otherwise 0
 *  **/
extern unsigned short aciGetCmdPacketItem(struct ACI_MASTER* aci,
    unsigned char packetId, unsigned short index);

/** Get a parameter packet item by index.
 * @param packetId The id of the packet
 * @param index The index of the parameter in the packet
 * @return Return the id of the item if exist, otherwise 0
 *  **/
extern unsigned short aciGetParPacketItem(struct ACI_MASTER* aci,
    unsigned char packetId, unsigned short index);

/** Get the rate of a variable package. (Useful for aciGetVarPacketRateFromRemote() to check, which transmission rate is set on the device )
 * @param packetId The id of the package
 * @return the transmission rate of the packet
 * **/
unsigned short aciGetVarPacketRate(struct ACI_MASTER* aci,
                                   unsigned char packetId);

/** Request the transmission rate of every variable package on the device. After receiving the data, you get it over aciGetVarPacketRate().**/
void aciGetVarPacketRateFromDevice(struct ACI_MASTER* aci);

/** Adds content to packet. <br>
 * Call aciSendVariablePacketConfiguration() for changes to get effective
 * @param packetId Define the id of the packet, where the variable should be send. The first id is 0 and the numbers of packets is defined in #MAX_VAR_PACKETS (by default: 3)
 * @param id The id of the variable, which should be included in the packet
 * @param var_ptr a pointer to the variable, where the content shall be written after calling @See aciSynchronizeVars()
 * @return 0 if packet is too long
 **/
extern unsigned char aciAddContentToVarPacket(struct ACI_MASTER* aci,
    unsigned char packetId, unsigned short id, void* var_ptr);

/**
 * Send variables packet configuration to the device which shall be send to the remote.
 * @param packetId The id of the packet, which shall be received from the device.
 * @return none
 **/
extern void aciSendVariablePacketConfiguration(struct ACI_MASTER* aci,
    unsigned char packetId);

/**
 * Send command packet configuration to the device which shall be send to the device.
 * @param packetId The id of the packet, which includes the list of commands for sending.
 * @param with_ack If you set this not zero, it will send the last command until it gets an acknowledge.
 * @return none
 **/
extern void aciSendCommandPacketConfiguration(struct ACI_MASTER* aci,
    unsigned char packetId, unsigned char with_ack);

/**
 * Send parameter packet configuration to the device which shall be send/set to/on the device.
 * @param packetId The id of the packet, which includes the list of parameter for sending.
 * @return none
 **/
extern void aciSendParameterPacketConfiguration(struct ACI_MASTER* aci,
    unsigned char packetId);

/** Change transmission rate of the individual packages. Call aciVarPacketUpdateTransmissionRates() for changes to get effective.
 *  @param packetId The id of the packet you want to set the transmission rate
 *  @param rate The rate depends on the engine rate of the device (default 1000 calls per Second) and is calculated through (Engine rate of the device)/rate.
 *  @return none
 *
 * **/
extern void aciSetVarPacketTransmissionRate(struct ACI_MASTER* aci,
    unsigned char packetId, unsigned short rate);

/** updates the transmission data rates for all variable packets at once. Change the individual rates with aciSetVarPacketTransmissionRate() **/
extern void aciVarPacketUpdateTransmissionRates(struct ACI_MASTER* aci);

/* assign local variable to ID. By calling aciSynchronizeVars() the most recent content get's copied to all assigned variables **/
//extern unsigned char aciAssignVariableToId(void * ptrToVar, unsigned char varType, unsigned short id);

/** By calling aciSynchronizeVars() the content of all requested variables in every package will be updated from the content in the receiving buffer. */
extern void aciSynchronizeVars(struct ACI_MASTER* aci);


/** Adds content to command packet. <br>
 * @param packetId Define the id of the packet, where the command should be received by the device. The first id is 0 and the numbers of packets is defined in #MAX_VAR_PACKETS (by default: 3).
 * @param id The id of the command, which should be included in the packet.
 * @param ptr a pointer to the command, where the content to send is in.
 * @return none
 **/
extern void aciAddContentToCmdPacket(struct ACI_MASTER* aci,
                                     const unsigned char packetId, const unsigned short id, void* ptr);

/**
 * Send the content of the commands to the device
 * @param packetId The id of the packet
 * @return none
 */

extern void aciUpdateCmdPacket(struct ACI_MASTER* aci,
                               const unsigned short packetId);
/**
 * Return the send status of a command package.
 * @param packetId The id of the packet
 * @return 0 for no command to send or command sended, 1 for a pending command to send, 2 for waiting acknowledge (if acknowledge for the package is set on).
 */

extern unsigned char aciGetCmdSendStatus(struct ACI_MASTER* aci,
    const unsigned short packetId);


/** Adds content to parameter packet. <br>
 * @param packetId Define the id of the packet, where the parameter shall be in. The first id is 0 and the numbers of packets is defined in #MAX_VAR_PACKETS (by default: 3)
 * @param id The id of the parameter, which should be included in the packet
 * @param ptr a pointer to the variable, where the content shall be written for sending and receiving.
 * @return none
 **/
extern int aciAddContentToParamPacket(struct ACI_MASTER* aci,
                                      unsigned char packetId, unsigned short id, void* ptr);

/**
 * Send the content of the parameters to the device
 * @param packetId The id of the packet
 * @return none
 */
extern void aciUpdateParamPacket(struct ACI_MASTER* aci,
                                 const unsigned short packetId);

/**
 * \ingroup callbacks
 * Set send data callback.<br>
 * The callback is called when the ACI want's to send a data packet. That happens i.e. in the @See aciEngine() function, where everytime a heartbeat will send to the host.
 **/
extern void aciSetSendDataCallback(struct ACI_MASTER* aci,
                                   void (*aciSendDataCallback_func)(void* data, unsigned short cnt));

/**
 * \ingroup callbacks
 * Set variable list update finished callback. <br>
 * The callback is called after the variable list was successfully received.
 **/
extern void aciSetVarListUpdateFinishedCallback(struct ACI_MASTER* aci,
    void (*aciVarListUpdateFinished_func)(void));

/**
 * \ingroup callbacks
 * Set command list update finished callback.<br>
 * The callback is called after the command list was successfully received.
 **/
extern void aciSetCmdListUpdateFinishedCallback(struct ACI_MASTER* aci,
    void (*aciCmdListUpdateFinished_func)(void));

/**
 * \ingroup callbacks
 * Set parameter list update finished callback.<br>
 * The callback is called after the parameters list was successfully received.
 **/
extern void aciSetParamListUpdateFinishedCallback(struct ACI_MASTER* aci,
    void (*aciParamListUpdateFinished_func)(void));

/**
 * \ingroup callbacks
 * Set parameter list update finished callback.<br>
 * The callback is called after the parameters list was successfully received.
 **/
extern void aciSetCmdAckCallback(struct ACI_MASTER* aci,
                                 void (*aciCmdAck_func)(unsigned char));

/**
 * \ingroup callbacks
 * Set version information received callback. <br>
 * The callback is called if you request the ACI info package and  It was received. It includes the version information of the device.  <br>
 **/
extern void aciInfoPacketReceivedCallback(struct ACI_MASTER* aci,
    void (*aciInfoRec_func)(struct ACI_INFO));

/**
 * \ingroup callbacks
 * Set version information received callback. <br>
 * The callback is called after a variable packet was received. The parameter is the packet number of the packet.
 **/
extern void aciVarPacketReceivedCallback(struct ACI_MASTER* aci,
    void (*aciVarPacketRec_func)(unsigned char));

/**
 * \ingroup callbacks
 * Set parameter saved callback. <br>
 * The callback is called after storing the parameters on the device.
 **/
extern void aciParPacketStoredCallback(struct ACI_MASTER* aci,
                                       void (*aciParPacketStored_func)(void));
/**
 * \ingroup callbacks
 * Set parameter saved callback. <br>
 * The callback is called after loading the parameters from the device.
 **/
extern void aciParPacketLoadedCallback(struct ACI_MASTER* aci,
                                       void (*aciParPacketLoaded_func)(void));

/**
 * \ingroup callbacks
 * Set the callback, that will be executed after receiving a single variable from the device. A single variable is a variable, that will be send instantly from the device to the remote. It is useful, if you want to commit a status update. The sending id depends on none of the ids in any list and is individual set by the user.
 * The AscTec SDK 3.0 doesn't include any single variable to send.
 *
 **/
extern void aciSetSingleReceivedCallback(struct ACI_MASTER* aci,
    void (*aciSingleReceived)(unsigned short id, void* data,
                              unsigned short varType));

/**
 * \ingroup callbacks
 * Set the callback for a requested variable. <br>
 * After calling aciRequestSingleVariable, this function will be executed, when the requested variable was received.
 *
 **/
extern void aciSetSingleRequestReceivedCallback(struct ACI_MASTER* aci,
    void (*aciSingleReqReceived)(unsigned short id, void* data,
                                 unsigned short varType));

/**
 * \ingroup callbacks
 * Set the reading stored data callback. <br>
 * If you set this callback, ACI will try to read the lists from any storage device, you defined in the callback function.
 *
 **/
extern void aciSetReadHDCallback(struct ACI_MASTER* aci,
                                 int (*aciReadHD)(void* data, int bytes));

/**
 * \ingroup callbacks
 * Set the writing stored data callback. <br>
 * If you set this callback, ACI will store the lists on any storage device, you defined in the callback function.
 *
 **/
extern void aciSetWriteHDCallback(struct ACI_MASTER* aci,
                                  int (*aciWriteHD)(void* data, int bytes));

/**
 * \ingroup callbacks
 * Set the reset stored data callback. <br>
 * You maybe need this callback, if you set the reading and writing callback on the same device. It will be called, when it starts to write the data on the device.
 *
 **/
extern void aciSetResetHDCallback(struct ACI_MASTER* aci,
                                  void (*aciResetHD)(void));
/**
 * If you want to know the current value of a variable without putting it in a packet, you can use to function. It is useful for getting any status of a variable for one time. If you want all the time the current value, it is recommended to put the variable in a packet. After receiving the variable, the callback defined with aciSetSingleRequestReceivedCallback() will be executed.
 * @param id The id of the variable, you want to request
 * @return none
 *
 **/
extern void aciRequestSingleVariable(struct ACI_MASTER* aci, unsigned short id);

extern uint8_t aciTxSendPacket(struct ACI_MASTER* aci,
                            unsigned char aciMessageType, void* data,
                            unsigned short cnt);

/**
 * If you call this function, the stored list of all variables, commands and parameters will be not loaded. You can also use it to request a list again.
 * Anyway, this function is useful, if you changed the descriptions of the variables, command or parameters. Otherwise, the lists will be updated by itself.
 *
 **/
extern void aciForceListRequestFromDevice(struct ACI_MASTER* aci);
extern unsigned short aciGetParamTableLength(struct ACI_MASTER* aci);
extern struct ACI_MEM_TABLE_ENTRY* aciGetCommandItemByIndex(
  struct ACI_MASTER* aci, unsigned short index);
extern unsigned short aciGetCmdTableLenth(struct ACI_MASTER* aci);
extern struct ACI_MEM_VAR_ASSIGN_TABLE* aciVarGetAssignmentById(
  struct ACI_MASTER* aci, unsigned short id);

extern void aciSetParamReceivedCallback(struct ACI_MASTER* aci,
                                        void (*aciParamRec_func)(unsigned short id));

extern void aciSetParamUpdateAckCallback(struct ACI_MASTER* aci,
    void (*aciParamUpdAck_func)(void));

extern void aciSetSyncStatusCallback(struct ACI_MASTER* aci,
                                     void (*aciSyncStatus_func)(unsigned char status, unsigned char percent));

extern void aciSetDiversityPacketReceivedCallback(struct ACI_MASTER* aci,
    void (*aciDiversityPacketReceived)(unsigned char src, unsigned short id,
                                       void* data, unsigned short length));

#ifdef __cplusplus
}
#endif

#endif /* ASCTECCOMMINTF_H_ */
