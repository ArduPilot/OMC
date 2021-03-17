/**
  ******************************************************************************
  * @file    AC_PC_Commands.h
  * @author  pomland
  * @version V1.0.0
  * @date    Mar 8, 2017
  * @brief   TODO: Add a description here
  *          (multiline)
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2013 Ascending Technologies GmbH</center></h2>
  *
  * Do not distribute.
  *
  ******************************************************************************
  */

/* do not include if not enabled ---------------------------------------------*/
#ifdef ENABLE_AC_PC_Commands

/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef AC_PC_COMMANDS_H_
#define AC_PC_COMMANDS_H_

/* Ensure C++ Header compatability -------------------------------------------*/

#ifdef __cplusplus
 extern "C" {
#endif

/* Exported types ------------------------------------------------------------*/

 /*!
  * The USB-Cockpit executes ((uint16_t) rc_cmd_t Command, rc_param_t Parameter) command pairs
  * rc_cmd_t specifies the Command to be executed with optional Parameter as described below
  */
 typedef enum {
	 RC_CMD_NONE = 0,
	 RC_CMD_RC_DRONE_CONNECT = 2,       /*! Param: Serial of Drone, Connects to drone */
	 RC_CMD_DRONE_SET_FLIGHTMODE = 6,     /*! Param: See set_flight_mode_cmd_t */
	 RC_CMD_RC_REQUEST_DRONELINKINFO = 7, /*! Param: non, drone will send out USBCOCKPITTOPC_INITINFO */
	 RC_CMD_RC_START_BOOTLOADER = 8, /*! Param: none, drone link will start bootloader (note: normal functionality will not be available) */
	 RC_CMD_RC_DEBUG_OUTPUT_ENABLE = 9, /*! Param: none, hell knows what this will do */
	 RC_CMD_RC_SET_REGION = 10, /*! Param: New Region, see ATOS_REGION_xxx */
	 RC_CMD_RC_IDL_RTCMSTREAM_TOGGLE_ACTIVE = 11, /*! Param: 0 for inactive, else active */
 } rc_cmd_t;

 typedef enum {
	 SET_FLIGHT_MODE_CMD_NONE,
	 SET_FLIGHT_MODE_CMD_MANUAL,
	 SET_FLIGHT_MODE_CMD_HEIGHT,
	 SET_FLIGHT_MODE_CMD_GPS,
 } set_flight_mode_cmd_t;

 typedef uint16_t rc_param_t; /*! Parameter Type. Send with with every rc_cmd_t Command.*/

/* Exported constants --------------------------------------------------------*/

/*!
 * The PC may send up to RC_CMD_CMDPARAM_PAIRS_MAX (Command, Parameter) pairs
 * (refer to rc_param_t) in one ACI-package.
 */
#define RC_CMD_CMDPARAM_PAIRS_MAX 10u // WARNING: Do not change without checking ATOS_MSG_PC_COMMAND


/* Exported macro ------------------------------------------------------------*/

/* Exported functions --------------------------------------------------------*/ 

/* ATOS functions    ---------------------------------------------------------*/

/**
  * @brief  ATOS run  
  * @param  none
  * @retval None
  */
extern void AC_PC_Commands_Run(void);


/**
  * @brief  Message handler for ATOS messages 
  * @param  msg_id
  * @param  flags 
  * @param  msg_src
  * @param  size
  * @retval None
  */
extern void AC_PC_Commands_ReceiveMessage(unsigned short msg_id, unsigned short flags, void * msg_src, unsigned short size);

/**
  * @brief  Initializes the Component 
  *         init Hardware, register messages 
  * @param  None
  * @retval None
  */
extern void AC_PC_Commands_Init(void);

/**
  * @brief  Deinitializes the Component 
  *          (optional)
  * @param  None
  * @retval None
  */
extern void AC_PC_Commands_DeInit(void);




#ifdef __cplusplus
 }
#endif // _cplusplus

#endif // AC_PC_COMMANDS_H_ 
#endif // ATOS_ENABLE_AC_PC_Commands
