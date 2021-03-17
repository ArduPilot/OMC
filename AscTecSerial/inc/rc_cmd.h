/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#include <stdint.h>

#pragma pack(1)

typedef struct
{
       uint16_t alarmstate_cockpit;//VT_UINT16// see lcd_data_alarmstates_t//
       uint8_t diversity_lock[2];//VT_UINT8(2)// if sum of entries < 4 (<8) then link weak (lost)//

} ATOS_MSG_MBS_TO_TABLET ;

#pragma pack()

typedef enum{
        LCD_DATA_ALARMSTATE_NONE    = 0,
        LCD_DATA_ALARMSTATE_SYS_BAT     = (1 << 0), // Battery is empty, land now
        LCD_DATA_ALARMSTATE_GPS     = (1 << 1), // GPS signal lost
        LCD_DATA_ALARMSTATE_MOT     = (1 << 2), // Some Motorcontroller problem
        LCD_DATA_ALARMSTATE_LNKBAD  = (1 << 3), // Link weak, but everything should be working
        LCD_DATA_ALARMSTATE_SYS_LOBAT   = (1 << 4), // Battery is low, land soon
        LCD_DATA_ALARMSTATE_LNKLOST = (1 << 5), // Link lost: F8+ will go into emergency mode
        LCD_DATA_ALARMSTATE_WIND    = (1 << 6), // Wind-warning
        LCD_DATA_ALARMSTATE_HIWIND  = (1 << 7), // Strong wind-warning
        LCD_DATA_ALARMSTATE_JOYSTICK = (1 << 8), // Some error with Joysticks
        LCD_DATA_ALARMSTATE_COC_BAT = (1 << 9), // Battery empty in cockpit
		LCD_DATA_ALARMSTATE_COC_LOBAT = (1 << 10), // Battery low in cockpit
		LCD_DATA_ALARMSTATE_COC_TEMP = (1 << 11), // Cockpit Coreboard temperature too high
		LCD_DATA_ALARMSTATE_SYS_BATTERY_LOST   = (1 << 12), // In FM_FLIGHT the number of identified batteries decreased
		LCD_DATA_ALARMSTATE_SYS_BATTERY_UPDATE = (1 << 13), // Battery Update in progress, see ACI_USER_VAR_BMS_HOST_STATE->batteries_numberof for percent progress
		LCD_DATA_ALARMSTATE_SYS_BATTERY_HEAT   = (1 << 14), // Cell Pack oder Battery PCB is in danger of overheating
		LCD_DATA_ALARMSTATE_VIDEOCONVERTER_FIRMWARE_UPDATE = (1 << 15), // updating video converter firmware (rx or tx)

} lcd_data_alarmstates_t;


/*
 *
I am sending you ACI Raw packets with
-        Source: ACI_ADDR_DIVERSITY_LCD = 0x40
-        Destination: ACI_ADDR_PC = 0x80
-        Type / id : ACIMT_IDL_TO_PC = 0xD2

Please send ACI Raw packets to me with
-        Source: ACI_ADDR_PC = 0x80
-        Destination: ACI_ADDR_PC_USBCOCKPIT_ENDP = 0x02
-        Type / id: ACIMT_IDL_TO_PC = 0xD2

 */
