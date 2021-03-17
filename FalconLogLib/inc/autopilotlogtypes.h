/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#ifndef AUOTPILOTLOGTYPES
#define AUOTPILOTLOGTYPES

namespace autopilot {

// TODO 64-Bit Problem!!!!!!!
struct LOGFILE_GPS_TIME {
  unsigned int time_of_week = 0;   //[ms]
  unsigned short week = 0;         //[1..52]
};

struct LOGFILE_GPS_DATA {
  //latitude/longitude in deg * 10^7
  int latitude = 0;
  int longitude = 0;
  //GPS height in mm
  int height = 0;
  //speed in x (E/W) and y(N/S) in mm/s
  int speed_x = 0;
  int speed_y = 0;
  //GPS heading in deg * 1000
  int heading = 0;

  //accuracy estimates in mm and mm/s
  unsigned int horizontal_accuracy = 0;
  unsigned int vertical_accuracy = 0;
  unsigned int speed_accuracy = 0;

  //number of satellite vehicles used in NAV solution
  unsigned int numSV = 0;

  // GPS status information; Bit7...Bit3: 0 Bit 2: longitude direction Bit1: latitude direction Bit 0: GPS lock
  int status = 0;
};

enum FlightModes {
  FM_ACC = 0x01,
  FM_HEIGHT = 0x02,
  FM_POS = 0x04,
  FM_SPEED = 0x08,
  FM_COMPASS_FAILURE = 0x10,
  FM_SCIENTIFIC = 0x20,
  FM_SCIENTIFIC_ACTIVE = 0x40,
  FM_EMERGENCY = 0x80,
  FM_CALIBRATION_ERROR = 0x100,
  FM_CALIBRATION_ERROR_GYROS =0x200,
  FM_CALIBRATION_ERROR_ACC = 0x400,
  FM_MOTORTYPE_MISMATCH = 0x800,
  FM_FLYING_BLOCKED = 0x1000,
  FM_MOTORSTART_WAS_BLOCKED = 0x2000,
  FM_MAG_FIELD_STRENGTH_ERROR =0x4000,
  FM_MAG_INCLINATION_ERROR = 0x8000,
};


#pragma pack(push, 1)
struct LOGFILE_LL_ATTITUDE_DATA {
  unsigned short system_flags; //GPS data acknowledge, etc.

  short angle_pitch;      //angles [deg*100]
  short angle_roll;
  unsigned short angle_yaw;

  short angvel_pitch;     //angular velocities; bias-free [0.015°/s]
  short angvel_roll;
  short angvel_yaw;

  //<-- 14 bytes @ 1kHz
  //--> 3x 26 bytes @ 333 Hz
  //=> total = 40 bytes @ 1 kHz
  //-----------------------------PAGE0
  unsigned char RC_data[10];   //8 channels @ 10 bit

  int latitude_best_estimate;  //GPS data fused with all other sensors
  int longitude_best_estimate;
  short acc_x;            //accelerations [mg]
  short acc_y;
  short acc_z;

  unsigned short temp_gyro;
  //-----------------------------PAGE1
  unsigned char motor_data[16]; //speed 0..7, PWM 0..7

  short speed_x_best_estimate;
  short speed_y_best_estimate;
  int height;       //height [mm]
  short dheight;          //differentiated height[mm/s]
  //------------------------------PAGE2
  short mag_x;
  short mag_y;
  short mag_z;

  short cam_angle_pitch;
  short cam_angle_roll;
  short cam_status;

  short battery_voltage1;
  short battery_voltage2;
  short flightMode;
  short slowDataUpChannelDataShort; //former flight_time
  short cpu_load;
  short status;
  short status2; //Bits 7..1: slowDataUpChannelSelect (7bit) Bit0:flying Bit15..8:active Motors
};

struct GPS_TIME {
  unsigned int time_of_week;   //[ms]
  unsigned short week;         //[1..52]
};

struct SVINFO_HEADER {
  unsigned char numCh;
  unsigned char globalFlags;
};

struct SVINFO {
  unsigned char channel;
  unsigned char svId;
  unsigned char flags;
  unsigned char quality;
  unsigned char cNo;
  char elev; //in deg
  short azim; // in deg
  int prRes; //pseudo range residual
};

struct GPS_HW_STATUS {
  unsigned char antennaStatus;
  unsigned char antennaPower;
  unsigned short agcMonitor;
  unsigned short noiseLevel;

  unsigned short stackUsed;
  unsigned short stackAv;
  unsigned short cpuLoad;
  unsigned char fullSlots;
  unsigned char partSlots;
};

struct ADV_LOG_DATA { //more data to log, mostly filled in ll_hl_comm.c via SPI
  unsigned char startbytes[3];
  struct GPS_TIME gpsTime;
  short hlCpuLoad; //HL cpu load
  short hlUpTime; //HL up time
  unsigned char videoSwitch; //status of video switch
  unsigned char camCommands; //camera commands
  struct SVINFO_HEADER satInfoHeader;
  struct GPS_HW_STATUS gpsHwStatus;
  struct SVINFO svInfo[16];

  //everything below here is filled in ll_hl_comm.c via SPI
  //up to three batterys are stored. For F8 only the first battery has valid data
  unsigned short voltage; // 1/100th V
  unsigned short current; // 1/100th A
  unsigned short usedCapacity;
  unsigned short estimatedTotalCapacity;

  unsigned char gamePadPitch;
  unsigned char gamePadYaw;
  unsigned char gamePadZoom;
  unsigned short gamePadButtons;

  unsigned char joystickPitch;
  unsigned char joystickYaw; // 1/100th V

  unsigned char dummy0;
  unsigned short dummy[3]; // 1/100th A

  unsigned char total_logs_per_second;
  //---1st subpage 25 bytes

  unsigned char
  batteryFlags; // bits 0..3: state variable bits 4:battery was not full at startup ..7:unused
  unsigned char estimatedZeroLoadVoltage; // in 0.1V

  unsigned short nav_status;
  unsigned short dist_to_wp;
  unsigned char xbee_rssi[2];
  unsigned char xbee_tx_noack_per_sec[2];
  unsigned char xbee_tx_success_per_sec[2];
  unsigned char xbee_rx_ok_per_sec[2];

  unsigned char motorCtrlStatus[8];
  unsigned short i2cTransmitErrors;
  unsigned char i2cBusRestarts;
  //---2nd subpage 25 bytes

  unsigned char motorPwmIntegral[8];
  short calc_mag_x;
  short calc_mag_y;
  short calc_mag_z;
  unsigned short waypointTimeDiff; //time between two waypoint commands in 10 ms

  unsigned char i2cReceiveErrors[8];

  //---3rd subpage 25 bytes
  unsigned short chkSum; //crc16
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

#pragma pack(pop)
}

#endif // AUOTPILOTLOGTYPES

