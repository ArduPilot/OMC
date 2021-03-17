/*

Copyright (c) 2013, Ascending Technologies GmbH
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
#ifdef __cplusplus

extern "C"
{
#endif
#include "asctecCommIntf.h"
#include "asctecDefines.h"
#include <stdint.h>

#include <string.h>
//#include "main.h"
//internal global vars

#define DEBUGOUT(a) {if (aci->DebugOut) aci->DebugOut(a);}
//aci helper prototypes
void aciFreeMemVarTable(struct ACI_MEM_VAR_TABLE* ptr);

///update CRC with 1 byte
unsigned short aciCrcUpdate(unsigned short crc, unsigned char data);
///update crc with multiple bytes
unsigned short aciUpdateCrc16(unsigned short crc, void* data,
                              unsigned short cnt);

uint8_t addToTxBuffer(struct ACI_MASTER* aci, uint8_t* data, uint16_t len);

//aciRxHandler prototypes
void aciRxHandleMessage(struct ACI_MASTER* aci, unsigned char messagetype,
                        unsigned short length);

// aci
void aciParamAck(struct ACI_MASTER* aci, unsigned char packet);

int aciStoreList(struct ACI_MASTER* aci);
void aciLoadHeaderList(struct ACI_MASTER* aci);
int aciLoadList(struct ACI_MASTER* aci);

//aci TX prototypes

/** Search for a variable in any created packet and returns its success.
 * @param ptrToVar Pointer to the variable, where the value will be stored. If there is no memory allocated for the pointer, there will be allocated with the size of varType. Make sure, that the size of allocated memory is the same as varType.
 * @param varType The type of the consigning pointer. This should be one of the defined \link #vartype variables type \endlink
 * @param id The id of the variable, which should be set in \a ptrToVar
 * @return If the value could be set, it returns 1 otherwise 0.
**/
char aciGetVarById(struct ACI_MASTER* aci, void* ptrToVar,
                   const unsigned short varType, const unsigned short id);


//aci assignment table prototypes

void aciFreeMemVarTable(struct ACI_MEM_VAR_TABLE* ptr)
{
  if (ptr->next) {
    aciFreeMemVarTable(ptr->next);
  }

  free(ptr);
}

void aciCheckVerConf(struct ACI_MASTER* aci)
{
  aciTxSendPacket(aci, ACIMT_INFO_REQUEST, NULL, 0);
}

void aciParamAck(struct ACI_MASTER* aci, unsigned char packet)
{
  aci->ParamPacketSendStatus[packet] = 0;

  if (aci->ParamUpdateAck) {
    aci->ParamUpdateAck();
  }
}

/** has to be called ones during initialisation **/
void aciInit(struct ACI_MASTER* aci)
{
  int i;

  if (aci->MemVarTableStart) {
    aciFreeMemVarTable(aci->MemVarTableStart);
  }

  aci->MyId = ACI_ADDR_PC;

  aci->DestId = ACI_ADDR_NAV1_PC_ENDP;
  aci->RequestVarListTimeout = 60000;
  aci->RequestCmdListTimeout = 60000;
  aci->RequestParListTimeout = 60000;


  aci->MagicCodeVar = 0x00FF;
  aci->MagicCodeCmd = 0x00FF;
  aci->MagicCodePar = 0x00FF;
  aci->RxState = ARS_IDLE;



  for (i = 0; i < MAX_VAR_PACKETS; i++) {
    aciResetVarPacketContent(aci, i);
    aci->VarPacketContentBufferValid[i] = 0;
    aci->VarPacketContentBufferInvalidCnt[i] = 0;
  }


  aci->VarAssignTableStart = (struct ACI_MEM_VAR_ASSIGN_TABLE*) malloc(sizeof(
                                                                         struct ACI_MEM_VAR_ASSIGN_TABLE));
  aci->VarAssignTableStart->next = NULL;

  aci->MemVarTableStart = (struct ACI_MEM_VAR_TABLE*) malloc(sizeof(
                                                               struct ACI_MEM_VAR_TABLE));

  aci->MemVarTableStart->next = NULL;

  aci->CmdAssignTableStart = (struct ACI_MEM_VAR_ASSIGN_TABLE*) malloc(sizeof(
                                                                         struct ACI_MEM_VAR_ASSIGN_TABLE));
  aci->CmdAssignTableStart->next = NULL;

  aci->MemCmdTableStart = (struct ACI_MEM_VAR_TABLE*) malloc(sizeof(
                                                               struct ACI_MEM_VAR_TABLE));

  aci->MemCmdTableStart->next = NULL;

  aci->ParamAssignTableStart = (struct ACI_MEM_VAR_ASSIGN_TABLE*) malloc(sizeof(
                                                                           struct ACI_MEM_VAR_ASSIGN_TABLE));
  aci->ParamAssignTableStart->next = NULL;

  aci->MemParamTableStart = (struct ACI_MEM_VAR_TABLE*) malloc(sizeof(
                                                                 struct ACI_MEM_VAR_TABLE));

  aci->MemParamTableStart->next = NULL;

  aci->RequestedPacketList = NULL;
  aci->RequestListType = 0;

  aci->Info.verMajor = 0;
  aci->Info.verMinor = 0;
  aci->Info.maxNameLength = 0;
  aci->Info.maxDescLength = 0;
  aci->Info.maxUnitLength = 0;
  aci->Info.maxVarPackets = 0;
  aci->Info.memPacketMaxVars = 0;
}

void aciResetRemote(struct ACI_MASTER* aci)
{
  //reset remote link
  aciTxSendPacket(aci, ACIMT_RESETREMOTE, NULL, 0);
}

void aciEngine(struct ACI_MASTER* aci)
{
	int i;
	unsigned short crc = 0xff;
	// unsigned char heartbeat_to_send = 1;

	//printf( "ACI MAGICCODE: %d \n", aci->RequestMagicCodes );
	//fflush(stdout);

	if (aci->RequestMagicCodes) {
		if (aci->RequestMagicCodes == 1) {
			DEBUGOUT("requesting magiccodes");
			aciTxSendPacket(aci, ACIMT_MAGICCODES, NULL, 0);
			aci->RequestMagicCodes++;
		} else if (aci->RequestMagicCodes > aci->EngineRate) {
			aci->RequestMagicCodes = 1;
		} else {
			aci->RequestMagicCodes++;
		}
	}

	if (aci->paramRequestTimeout) {
		aci->paramRequestTimeout--;

		if (!aci->paramRequestTimeout) {
			aciGetParamFromDevice(aci, aci->paramRequestId);
		}
	}

	if (aci->RequestVarListTimeout != 60000) {
		if (aci->RequestVarListTimeout) {
			aci->RequestVarListTimeout--;
		} else {
			aciTxSendPacket(aci, ACIMT_GETVARTABLEINFO, NULL, 0);
			aci->RequestVarListTimeout = ACI_REQUEST_LIST_TIMEOUT;
		}
	}

	if (aci->RequestCmdListTimeout != 60000) {
		if (aci->RequestCmdListTimeout) {
			aci->RequestCmdListTimeout--;
		} else {
			aciTxSendPacket(aci, ACIMT_GETCMDTABLEINFO, NULL, 0);
			aci->RequestCmdListTimeout = ACI_REQUEST_LIST_TIMEOUT;
		}
	}

	if (aci->RequestParListTimeout != 60000) {
		if (aci->RequestParListTimeout) {
			aci->RequestParListTimeout--;
		} else {
			aciTxSendPacket(aci, ACIMT_GETPARAMTABLEINFO, NULL, 0);
			aci->RequestParListTimeout = ACI_REQUEST_LIST_TIMEOUT;
		}
	}

	if (aci->rxPacketTimeOut < 2 * aci->EngineRate) {
		aci->rxPacketTimeOut++;

		if (aci->rxPacketTimeOut == 2 * aci->EngineRate) {
			//more than 1 second without data. Resend packet configurations
			aciSendVariablePacketConfiguration(aci, 0);
			aciSendVariablePacketConfiguration(aci, 1);
			aciSendCommandPacketConfiguration(aci, 0, 1);
		}
	}

	if (aci->RequestedPacketListLength) {
		if (aci->RequestedPacketListTimeOut) {
			aci->RequestedPacketListTimeOut--;
		} else {
			unsigned short reqlen;
			reqlen = aci->RequestedPacketListLength;

			if (reqlen > 4) {
				reqlen = 4;
			}

			aci->reqListLen = (unsigned char)reqlen;
			//request next entry
			aciTxSendPacket(aci, ACIMT_REQUESTVARTABLEENTRIES, aci->RequestedPacketList,
					reqlen * 2);
			aci->RequestedPacketListTimeOut = ACI_REQUEST_LIST_TIMEOUT;
		}

	}

	if (aci->RequestedCmdPacketListLength) {
		if (aci->RequestedCmdPacketListTimeOut) {
			aci->RequestedCmdPacketListTimeOut--;
		} else {
			unsigned short reqlen;
			reqlen = aci->RequestedCmdPacketListLength;

			if (reqlen > 4) {
				reqlen = 4;
			}

			aci->reqListLen = (unsigned char)reqlen;
			//request next entry
			aciTxSendPacket(aci, ACIMT_REQUESTCMDTABLEENTRIES, aci->RequestedCmdPacketList,
					reqlen * 2);
			aci->RequestedCmdPacketListTimeOut = ACI_REQUEST_LIST_TIMEOUT;
		}

	}

	if (aci->RequestedParamPacketListLength) {
		if (aci->RequestedParamPacketListTimeOut) {
			aci->RequestedParamPacketListTimeOut--;
		} else {
			unsigned short reqlen;
			reqlen = aci->RequestedParamPacketListLength;

			if (reqlen > 4) {
				reqlen = 4;
			}

			aci->reqListLen = (unsigned char)reqlen;
			//request next entry
			aciTxSendPacket(aci, ACIMT_REQUESTPARAMTABLEENTRIES,
					aci->RequestedParamPacketList, reqlen * 2);
			aci->RequestedParamPacketListTimeOut = ACI_REQUEST_LIST_TIMEOUT;
		}

	}



	for (i = 0; i < MAX_VAR_PACKETS + 1; i++) {
		if (aci->UpdateVarPacketTimeOut[i]) {
			aci->UpdateVarPacketTimeOut[i]--;

			if (!aci->UpdateVarPacketTimeOut[i]) {
				//packet was not acknoledged -> send again

				unsigned char* temp;
				crc = 0xff;

				crc = aciUpdateCrc16(crc, aci->VarPacket[i], aci->VarPacketLength[i] * 2);

				temp = (unsigned char*) malloc(aci->VarPacketLength[i] * 2 + 1);
				memcpy(&temp[1], aci->VarPacket[i], aci->VarPacketLength[i] * 2);
				temp[0] = (unsigned char)crc;
				aci->VarPacketMagicCode[i] = (unsigned char)crc;

				if (i == 0) {
					aciTxSendPacket(aci, ACIMT_UPDATEVARPACKET, temp,
							aci->VarPacketLength[i] * 2 + 1);
				} else {
					aciTxSendPacket(aci, ACIMT_UPDATERCVARPACKET, temp,
							aci->VarPacketLength[i] * 2 + 1);
				}

				aci->UpdateVarPacketTimeOut[i] = ACI_UPDATE_PACKET_TIMEOUT;
				free(temp);
			}
		}
	}

	for (i = 0; i < MAX_VAR_PACKETS; i++) {

		if (aci->UpdateCmdPacketTimeOut[i]) {

			aci->UpdateCmdPacketTimeOut[i]--;

			if (!aci->UpdateCmdPacketTimeOut[i]) {
				// packet was not acknoledged -> send again
				unsigned char* temp;

				crc = 0xff;
				crc = aciUpdateCrc16(crc, aci->CmdPacket[i],
						aci->CmdPacketLength[i] * 2);

				temp = malloc(aci->CmdPacketLength[i] * 2 + 2);
				memcpy(&temp[2], aci->CmdPacket[i], aci->CmdPacketLength[i] * 2);
				temp[0] = (unsigned char)crc;
				temp[1] = aci->CmdWithAck[i];
				aci->CmdPacketMagicCode[i] = (unsigned char)crc;

				aciTxSendPacket(aci, ACIMT_UPDATECMDPACKET + i, temp,
						aci->CmdPacketLength[i] * 2 + 2);
				aci->UpdateCmdPacketTimeOut[i] = ACI_UPDATE_PACKET_TIMEOUT;
				free(temp);
			}
		}

		if (aci->UpdateParamPacketTimeOut[i]) {

			aci->UpdateParamPacketTimeOut[i]--;

			if (!aci->UpdateParamPacketTimeOut[i]) {
				//packet was not acknoledged -> send again
				unsigned char* temp;
				crc = 0xff;

				crc = aciUpdateCrc16(crc, aci->ParamPacket[i], aci->ParamPacketLength[i] * 2);

				temp = (unsigned char*)malloc(aci->ParamPacketLength[i] * 2 + 1);
				memcpy(&temp[1], aci->ParamPacket[i], aci->ParamPacketLength[i] * 2);
				temp[0] = (unsigned char)crc;
				aci->ParamPacketMagicCode[i] = (unsigned char)crc;

				aciTxSendPacket(aci, ACIMT_UPDATEPARAMPACKET + i, temp,
						aci->ParamPacketLength[i] * 2 + 1);
				aci->UpdateParamPacketTimeOut[i] = ACI_UPDATE_PACKET_TIMEOUT;
				free(temp);
			}
		}
	}

	for (i = 0; i < MAX_VAR_PACKETS; i++) {

		// Check Status, if packet for send is avaible.
		if ((aci->CmdPacketSendStatus[i] == 1)) {
			// Send Commando
			unsigned char* temp;
			unsigned char cnt = 0;
			int z;

			temp = (unsigned char*)malloc(aci->CmdPacketContentBufferLength[i] + 1);
			temp[cnt++] = aci->CmdPacketMagicCode[i];

			//add data to ringbuffer and calculate CRC
			for (z = 0; z < aci->CmdPacketLength[i]; z++) {
				memcpy(&temp[cnt], aciGetCommandItemById(aci, aci->CmdPacket[i][z])->ptrToVar,
						aciGetCommandItemById(aci, aci->CmdPacket[i][z])->varType >> 3);
				cnt += (aciGetCommandItemById(aci, aci->CmdPacket[i][z])->varType >> 3);
			}

			aciTxSendPacket(aci, ACIMT_CMDPACKET + i, &temp[0],
					aci->CmdPacketContentBufferLength[i] + 1);

			if (!aci->CmdWithAck[i]) {
				aci->CmdPacketSendStatus[i] = 0;  // Commando sended, do not send it again
			} else {
				aci->CmdPacketCnt[i] = 0;
				aci->CmdPacketSendStatus[i] = 2;
			}

			free(temp);
		} else if (aci->CmdPacketSendStatus[i] == 2) {
			aci->CmdPacketCnt[i]++;

			if (aci->CmdPacketCnt[i] == (aci->EngineRate / 4)) { //resend after 250ms
				aci->CmdPacketCnt[i] = 0;
				aci->CmdPacketSendStatus[i] = 1;
			}
		}

		// Check Status, if packet for send is available.
		if (!aci->ParamPacketSendStatus[i]) {
			continue;
		} else if (aci->ParamPacketSendStatus[i] == 1) {

			// Send Parameter
			unsigned char* temp;
			unsigned char cnt = 0;
			int z;
			temp = (unsigned char*)malloc(aci->ParamPacketContentBufferLength[i] + 1);
			temp[cnt++] = aci->ParamPacketMagicCode[i];

			for (z = 0; z < aci->ParamPacketLength[i]; z++) {
				memcpy(&temp[cnt], aciGetParameterItemById(aci,
						aci->ParamPacket[i][z])->ptrToVar, aciGetParameterItemById(aci,
								aci->ParamPacket[i][z])->varType >> 3);
				cnt += (aciGetParameterItemById(aci, aci->ParamPacket[i][z])->varType >> 3);
			}

			aciTxSendPacket(aci, ACIMT_PARAMPACKET + i, &temp[0],
					aci->ParamPacketContentBufferLength[i] + 1);
			aci->ParamPacketSendStatus[i] = 2;
			free(temp);
		} else if (aci->ParamPacketSendStatus[i] == 2) {
			aci->ParPacketCnt[i]++;

			if (aci->ParPacketCnt[i] % (aci->EngineRate / 2) == 0) {
				aci->ParPacketCnt[i] = 0;
				aci->ParamPacketSendStatus[i] = 1;
			}
		}

	}
}

void aciSetEngineRate(struct ACI_MASTER* aci,
                      const unsigned short callsPerSecond)
{
  aci->EngineRate = callsPerSecond;
}

#ifdef __cplusplus
void aciSetSendDataCallback(void (*aciSendDataCallback_func)(
                              unsigned char* data, unsigned short cnt))
#else
void aciSetSendDataCallback(struct ACI_MASTER* aci,
                            void (*aciSendDataCallback_func)(void* data, unsigned short cnt))
#endif
{
  aci->SendData = aciSendDataCallback_func;
}


void aciResetVarPacketContent(struct ACI_MASTER* aci, unsigned char packetId)
{
  if (aci->VarPacket[packetId]) {
    free(aci->VarPacket[packetId]);
  }

  aci->VarPacket[packetId] = NULL;
  aci->VarPacketLength[packetId] = 0;
}

void aciResetCmdPacketContent(struct ACI_MASTER* aci, unsigned char packetId)
{
  if (aci->CmdPacket[packetId]) {
    free(aci->CmdPacket[packetId]);
  }

  aci->CmdPacket[packetId] = NULL;
  aci->CmdPacketLength[packetId] = 0;
}

void aciResetParPacketContent(struct ACI_MASTER* aci, unsigned char packetId)
{
  if (aci->ParamPacket[packetId]) {
    free(aci->ParamPacket[packetId]);
  }

  aci->ParamPacket[packetId] = NULL;
  aci->ParamPacketLength[packetId] = 0;
}

/** get length of ID list of Packet **/
unsigned short aciGetVarPacketLength(struct ACI_MASTER* aci,
                                     unsigned char packetId)
{
  return aci->VarPacketLength[packetId];
}

/* get length of ID list of Packet */
unsigned short aciGetCmdPacketLength(struct ACI_MASTER* aci,
                                     unsigned char packetId)
{
  return aci->CmdPacketLength[packetId];
}

unsigned short aciGetParPacketLength(struct ACI_MASTER* aci,
                                     unsigned char packetId)
{
  return aci->ParamPacketLength[packetId];
}


/**get variable packet item by index **/
unsigned short aciGetVarPacketItem(struct ACI_MASTER* aci,
                                   unsigned char packetId, unsigned short index)
{
  if (index < aciGetVarPacketLength(aci, packetId)) {
    return aci->VarPacket[packetId][index];
  } else {
    return 0;
  }
}

unsigned short aciGetCmdPacketItem(struct ACI_MASTER* aci,
                                   unsigned char packetId, unsigned short index)
{
  if (index < aciGetCmdPacketLength(aci, packetId)) {
    return aci->CmdPacket[packetId][index];
  } else {
    return 0;
  }
}

unsigned short aciGetParPacketItem(struct ACI_MASTER* aci,
                                   unsigned char packetId, unsigned short index)
{
  if (index < aciGetParPacketLength(aci, packetId)) {
    return aci->ParamPacket[packetId][index];
  } else {
    return 0;
  }
}

unsigned short aciGetVarPacketRate(struct ACI_MASTER* aci,
                                   unsigned char packetId)
{
  return aci->VarPacketTransmissionRate[packetId];
}

void aciGetVarPacketRateFromDevice(struct ACI_MASTER* aci)
{
  aciTxSendPacket(aci, ACIMT_GETPACKETRATE, NULL, 0);
}

unsigned char aciAddContentToVarPacket(struct ACI_MASTER* aci,
                                       unsigned char packetId, unsigned short id,  void* var_ptr)
{
	if (var_ptr == NULL) {
		printf("ERROR: var_ptr == NULL\n, id %u", id);
		fflush(stdout);

		return 0;
	}

	if (aciGetVariableItemById(aci, id) == NULL) {
		printf("ERROR: Unknown Var Id:%u\n", id);
		fflush(stdout);

		return 0;
	}

	if (packetId > MAX_VAR_PACKETS) {
		printf("ERROR: Max Var Packet Id:%u, Requested :%u \n", MAX_VAR_PACKETS, packetId);
		fflush(stdout);

		return 0;
	}

	if (aci->VarPacket[packetId] == NULL) {
		aci->VarPacket[packetId] = malloc(2);
		aci->VarPacket[packetId][0] = id;
		aci->VarPacketLength[packetId] = 1;
		aciGetVariableItemById(aci, id)->ptrToVar = var_ptr;
		aci->VarPacketTotalLength[packetId] = aciGetVariableItemById(aci,
				id)->varType >> 3;
	} else {
		unsigned short* ptr;
		int i;

		//check for double entries
		for (i = 0; i < aci->VarPacketLength[packetId]; i++)
		{
			if (aci->VarPacket[packetId][i] == id) {
				printf("ERROR: Var Id already registered:%u \n", id);
				fflush(stdout);

				return 0;
			}
		}


		if (((aci->VarPacketTotalLength[packetId] + (aciGetVariableItemById(aci, id)->varType >> 3)) > MAX_RC_VAR_LENGTH)
				&& (packetId == 1))
		{
			printf("ERROR: RC Packet (Packet 1, the fast one) is full.");
			fflush(stdout);

			return 0;
		}


		aci->VarPacketTotalLength[packetId] += aciGetVariableItemById(aci, id)->varType >> 3;
		aci->VarPacketLength[packetId]++;
		ptr = malloc(2 * aci->VarPacketLength[packetId]);
		memcpy(ptr, aci->VarPacket[packetId], (aci->VarPacketLength[packetId] - 1) * 2);
		free(aci->VarPacket[packetId]);
		aci->VarPacket[packetId] = ptr;
		aci->VarPacket[packetId][aci->VarPacketLength[packetId] - 1] = id;
		aciGetVariableItemById(aci, id)->ptrToVar = var_ptr;
	}

	return 1;
}

void aciAddContentToCmdPacket(struct ACI_MASTER* aci,
                              const unsigned char packetId, const unsigned short id, void* var_ptr)
{
	if (var_ptr == NULL) {
		printf("ERROR: cmd_ptr == NULL\n, id %u", id);
		fflush(stdout);

		return;
	}

	if (aciGetCommandItemById(aci, id) == NULL) {
		printf("ERROR: Unknown Cmd Id:%u\n", id);
		fflush(stdout);

		return;
	}

	if (packetId >= MAX_VAR_PACKETS) {
		printf("ERROR: Max Cmd Packet Id:%u, Requested :%u \n", MAX_VAR_PACKETS, packetId);
		fflush(stdout);

		return;
	}

	if (packetId < MAX_VAR_PACKETS) {
		aci->CmdPacketUpdated[packetId] = 1;

		if (aci->CmdPacket[packetId] == NULL) {
			aci->CmdPacket[packetId] = malloc(2);
			aci->CmdPacket[packetId][0] = id;
			aci->CmdPacketLength[packetId] = 1;

			if (aciGetCommandItemById(aci, id) == NULL) {
				printf("ERROR: Unknown Cmd Id:%u\n", id);
				fflush(stdout);

				return;
			}

			aciGetCommandItemById(aci, id)->ptrToVar = var_ptr;
		} else {
			unsigned short* ptr;
			int i;

			//check for double entries
			for (i = 0; i < aci->CmdPacketLength[packetId]; i++)
			{
				if (aci->CmdPacket[packetId][i] == id) {
					printf("ERROR: Cmd Id already registered:%u \n", id);
					fflush(stdout);

					return;
				}
			}

			aci->CmdPacketLength[packetId]++;
			ptr = malloc(2 * aci->CmdPacketLength[packetId]);
			memcpy(ptr, aci->CmdPacket[packetId], (aci->CmdPacketLength[packetId] - 1) * 2);
			free(aci->CmdPacket[packetId]);
			aci->CmdPacket[packetId] = ptr;
			aci->CmdPacket[packetId][aci->CmdPacketLength[packetId] - 1] = id;
			aciGetCommandItemById(aci, id)->ptrToVar = var_ptr;
		}
	}
}

int aciAddContentToParamPacket(struct ACI_MASTER* aci, unsigned char packetId,
                               unsigned short id, void* var_ptr)
{
  if (var_ptr == NULL) {
    return -1;
  }

  if (aciGetParameterItemById(aci, id) == NULL) {
    return -2;
  }

  if (packetId >= MAX_VAR_PACKETS) {
    return -3;
  }

  if (packetId < MAX_VAR_PACKETS) {

    if (aci->ParamPacket[packetId] == NULL) {
      aci->ParamPacket[packetId] = malloc(2);
      aci->ParamPacket[packetId][0] = id;
      aci->ParamPacketLength[packetId] = 1;
      aciGetParameterItemById(aci, id)->ptrToVar = var_ptr;
    } else {
      unsigned short* ptr;
      int i;

      //check for double entries
      for (i = 0; i < aci->ParamPacketLength[packetId]; i++)
        if (aci->ParamPacket[packetId][i] == id) {
          return -4;
        }

      aci->ParamPacketLength[packetId]++;
      ptr = malloc(2 * aci->ParamPacketLength[packetId]);
      memcpy(ptr, aci->ParamPacket[packetId],
             (aci->ParamPacketLength[packetId] - 1) * 2);
      free(aci->ParamPacket[packetId]);
      aci->ParamPacket[packetId] = ptr;
      aci->ParamPacket[packetId][aci->ParamPacketLength[packetId] - 1] = id;
      aciGetParameterItemById(aci, id)->ptrToVar = var_ptr;
    }

    aciGetParamFromDevice(aci, id);
  }

  return 0;
}
#ifndef ACI_SHORT_MEM_TABLES

struct ACI_MEM_VAR_ASSIGN_TABLE* aciVarGetAssignmentById(struct ACI_MASTER* aci,
                                                         unsigned short id)
{
  aci->VarAssignTableCurrent = aci->VarAssignTableStart;

  //check for existing variable assignments
  while (aci->VarAssignTableCurrent->next) {
    aci->VarAssignTableCurrent = aci->VarAssignTableCurrent->next;

    if (aci->VarAssignTableCurrent->id == id) {
      return aci->VarAssignTableCurrent;
    }
  }

  return NULL;
}
#endif
void aciSetVarPacketTransmissionRate(struct ACI_MASTER* aci,
                                     unsigned char packetId, unsigned short callsPerSecond)
{
  if (packetId < MAX_VAR_PACKETS) {
    aci->VarPacketTransmissionRate[packetId] = callsPerSecond;
  }
}

void aciVarPacketUpdateTransmissionRates(struct ACI_MASTER* aci)
{
  aciTxSendPacket(aci, ACIMT_CHANGEPACKETRATE, &aci->VarPacketTransmissionRate[0],
      sizeof(aci->VarPacketTransmissionRate));
}



#ifndef ACI_SHORT_MEM_TABLES

char aciGetVarById(struct ACI_MASTER* aci, void* ptrToVar,
                   const unsigned short varType, const unsigned short id)
{
  int i;

  for (i = 0; i < MAX_VAR_PACKETS; i++) {
    int z;
    unsigned char* ptr;

    if (!aci->VarPacketContentBufferValid[i]) {
      continue;
    }

    ptr = aci->VarPacketContentBuffer[i];

    for (z = 0; z < aci->VarPacketLength[i]; z++) {
      struct ACI_MEM_TABLE_ENTRY* entry;

      entry = aciGetVariableItemById(aci, aci->VarPacket[i][z]);

      if ((aci->VarPacket[i][z] == id) && (entry)) {

        if (entry->varType == varType) {
          if (ptrToVar) {
            memcpy(ptrToVar, ptr, varType >> 3);
          } else {
            ptrToVar = malloc(varType >> 3);
            memcpy(ptrToVar, ptr, varType >> 3);
          }

          return 1;
        }
      }

      //entry should always exist!
      if (entry) {
        ptr += entry->varType >> 3;
      } else {
        return 0;  //otherwise stop sync!
      }
    }
  }

  return 0;

}
#endif
void aciSynchronizeVars(struct ACI_MASTER* aci)
{
  int i;

  for (i = 0; i < MAX_VAR_PACKETS + 1; i++) {
    int z;
    unsigned char* ptr;

    if (!aci->VarPacketContentBufferValid[i]) {
      continue;
    }

    ptr = aci->VarPacketContentBuffer[i];

    if (ptr == NULL) {
      continue;
    }

    for (z = 0; z < aci->VarPacketLength[i]; z++) {
      struct ACI_MEM_TABLE_ENTRY* entry;
      entry = aciGetVariableItemById(aci, aci->VarPacket[i][z]);

      if ((entry)) {
        memcpy(entry->ptrToVar, ptr, entry->varType >> 3);
      }

      //entry should always exist!
      if (entry) {
        ptr += entry->varType >> 3;
      } else {
        return;  //otherwise stop sync!
      }
    }
  }
}

/**send variables packet configuration onboard**/
void aciSendVariablePacketConfiguration(struct ACI_MASTER* aci,
                                        unsigned char packetId)
{

  unsigned char* temp;
  unsigned short crc = 0xff;
  int i;
  unsigned short packetDataLength = 0;

  if (packetId >= MAX_VAR_PACKETS + 1) {
    return;
  }

  crc = 0xff;
  crc = aciUpdateCrc16(crc, aci->VarPacket[packetId],
                       aci->VarPacketLength[packetId] * 2);

  temp = malloc(aci->VarPacketLength[packetId] * 2 + 1);
  memcpy(&temp[1], aci->VarPacket[packetId], aci->VarPacketLength[packetId] * 2);
  temp[0] = (unsigned char)crc;

  for (i = 0; i < aci->VarPacketLength[packetId]; i++) {
    struct ACI_MEM_TABLE_ENTRY* entry;

    entry = aciGetVariableItemById(aci, aci->VarPacket[packetId][i]);
    packetDataLength += entry->varType >> 3;
  }

  aci->VarPacketMagicCode[packetId] = (unsigned char)crc;

  //reallocate temporary buffer if neccessary
  if (aci->VarPacketContentBufferLength[packetId] != packetDataLength) {
    aci->VarPacketContentBufferLength[packetId] = 0;
    free(aci->VarPacketContentBuffer[packetId]);
    aci->VarPacketContentBuffer[packetId] = malloc(packetDataLength);
    aci->VarPacketContentBufferLength[packetId] = packetDataLength;
  }

  if (packetId == 0) {
    aciTxSendPacket(aci, ACIMT_UPDATEVARPACKET,   temp,
                    aci->VarPacketLength[packetId] * 2 + 1);
  } else {
    aciTxSendPacket(aci, ACIMT_UPDATERCVARPACKET, temp,
                    aci->VarPacketLength[packetId] * 2 + 1);
  }

  aci->UpdateVarPacketTimeOut[packetId] = ACI_UPDATE_PACKET_TIMEOUT;
  free(temp);

}

/**send command packet configuration onboard**/
void aciSendCommandPacketConfiguration(struct ACI_MASTER* aci,
                                       unsigned char packetId, unsigned char with_ack)
{
  unsigned char* temp;
  unsigned short crc = 0xff;
  int i;
  unsigned short packetDataLength = 0;

  if (packetId >= MAX_VAR_PACKETS) {
    return;
  }

  crc = 0xff;
  crc = aciUpdateCrc16(crc, aci->CmdPacket[packetId],
                       aci->CmdPacketLength[packetId] * 2);

  temp = malloc(aci->CmdPacketLength[packetId] * 2 + 2);
  memcpy(&temp[2], aci->CmdPacket[packetId], aci->CmdPacketLength[packetId] * 2);
  temp[0] = (unsigned char)crc;
  temp[1] = with_ack;

  for (i = 0; i < aci->CmdPacketLength[packetId]; i++) {
    struct ACI_MEM_TABLE_ENTRY* entry;

    entry = aciGetCommandItemById(aci, aci->CmdPacket[packetId][i]);
    packetDataLength += entry->varType >> 3;
  }

  aci->CmdPacketMagicCode[packetId] = (unsigned char)crc;

  aci->CmdWithAck[packetId] = with_ack;

  //reallocate temporary buffer if neccessary
  if (aci->CmdPacketContentBufferLength[packetId] != packetDataLength) {

    aci->CmdPacketContentBufferLength[packetId] = 0;
    free(aci->CmdPacketContentBuffer[packetId]);
    aci->CmdPacketContentBuffer[packetId] = malloc(packetDataLength);
    aci->CmdPacketContentBufferLength[packetId] = packetDataLength;

  }

  aciTxSendPacket(aci, ACIMT_UPDATECMDPACKET + packetId, temp,
                  aci->CmdPacketLength[packetId] * 2 + 2);
  aci->UpdateCmdPacketTimeOut[packetId] = ACI_UPDATE_PACKET_TIMEOUT;
  free(temp);
}

/**send command packet configuration onboard**/
void aciSendParameterPacketConfiguration(struct ACI_MASTER* aci,
                                         unsigned char packetId)
{
  unsigned char* temp;
  unsigned short crc = 0xff;
  int i;
  unsigned short packetDataLength = 0;
  crc = 0xff;
  crc = aciUpdateCrc16(crc, aci->ParamPacket[packetId],
                       aci->ParamPacketLength[packetId] * 2);

  temp = malloc(aci->ParamPacketLength[packetId] * 2 + 1);
  memcpy(&temp[1], aci->ParamPacket[packetId],
      aci->ParamPacketLength[packetId] * 2);
  temp[0] = (unsigned char)crc;

  for (i = 0; i < aci->ParamPacketLength[packetId]; i++) {
    struct ACI_MEM_TABLE_ENTRY* entry;

    entry = aciGetParameterItemById(aci, aci->ParamPacket[packetId][i]);
    packetDataLength += entry->varType >> 3;
  }

  aci->ParamPacketMagicCode[packetId] = (unsigned char)crc;

  //reallocate temporary buffer if neccessary

  if (aci->ParamPacketContentBufferLength[packetId] != packetDataLength) {
    aci->ParamPacketContentBufferLength[packetId] = 0;
    free(aci->ParamPacketContentBuffer[packetId]);
    aci->ParamPacketContentBuffer[packetId] = malloc(packetDataLength);
    aci->ParamPacketContentBufferLength[packetId] = packetDataLength;
  }

  aciTxSendPacket(aci, ACIMT_UPDATEPARAMPACKET + packetId, temp,
                  aci->ParamPacketLength[packetId] * 2 + 1);
  aci->UpdateParamPacketTimeOut[packetId] = ACI_UPDATE_PACKET_TIMEOUT;
  free(temp);
}


void aciSetVarListUpdateFinishedCallback(struct ACI_MASTER* aci,
                                         void(*aciVarListUpdateFinished_func)(void))
{
  aci->VarListUpdateFinished = aciVarListUpdateFinished_func;
}

void aciSetCmdListUpdateFinishedCallback(struct ACI_MASTER* aci,
                                         void(*aciCmdListUpdateFinished_func)(void))
{
  aci->CmdListUpdateFinished = aciCmdListUpdateFinished_func;
}

void aciSetParamListUpdateFinishedCallback(struct ACI_MASTER* aci,
                                           void(*aciParamListUpdateFinished_func)(void))
{
  aci->ParamListUpdateFinished = aciParamListUpdateFinished_func;
}
void aciSetCmdAckCallback(struct ACI_MASTER* aci,
                          void (*aciCmdAck_func)(unsigned char))
{
  aci->CmdAck = aciCmdAck_func;
}

void aciInfoPacketReceivedCallback(struct ACI_MASTER* aci,
                                   void (*aciInfoRec_func)(struct ACI_INFO))
{
  aci->InfoRec = aciInfoRec_func;
}

void aciVarPacketReceivedCallback(struct ACI_MASTER* aci,
                                  void (*aciVarPacketRec_func)(unsigned char))
{
  aci->VarPacketRec = aciVarPacketRec_func;
}

void aciParPacketStoredCallback(struct ACI_MASTER* aci,
                                void (*aciParPacketStored_func)(void))
{
  aci->ParaStoredC = aciParPacketStored_func;
}

void aciParPacketLoadedCallback(struct ACI_MASTER* aci,
                                void (*aciParPacketLoaded_func)(void))
{
  aci->ParaLoadedC = aciParPacketLoaded_func;
}

uint8_t aciTxSendPacket(struct ACI_MASTER* aci, unsigned char aciMessageType,
                     void* data,
                     unsigned short cnt)
{
  unsigned char startstring[3] = { '!', '#', '!' };
  unsigned short crc = 0xFF;
  unsigned char packetTxBuffer[ACI_TX_RINGBUFFER_SIZE];
//  char s[200];
  int pos = 0;

  if (cnt + 12 >= ACI_TX_RINGBUFFER_SIZE) {
    return ADD_TO_TX_BUFFER_RETURN_BUFFER_FULL;
  }


  //add header to ringbuffer
  memcpy(&packetTxBuffer[pos], &startstring, 3);
  pos += 3;


  //add dest to ringbuffer
  memcpy(&packetTxBuffer[pos], &aci->DestId, 1);
  pos += 1;
  crc = aciUpdateCrc16(crc, &aci->DestId, 1);

  //add message type to ringbuffer
  memcpy(&packetTxBuffer[pos], &aci->MyId, 1);
  pos += 1;
  crc = aciUpdateCrc16(crc, &aci->MyId, 1);


  //add message type to ringbuffer
  memcpy(&packetTxBuffer[pos], &aciMessageType, 1);
  pos += 1;
  crc = aciUpdateCrc16(crc, &aciMessageType, 1);

  //add data size to ringbuffer
  memcpy(&packetTxBuffer[pos], &cnt, 2);
  pos += 2;
  crc = aciUpdateCrc16(crc, &cnt, 2);

  memcpy(&packetTxBuffer[pos], data, cnt);
  pos += cnt;
  crc = aciUpdateCrc16(crc, data, cnt);

  //add CRC to ringbuffer
  memcpy(&packetTxBuffer[pos], &crc, 2);
  pos += 2;


  return addToTxBuffer(aci, &packetTxBuffer[0], pos);
}

uint8_t aciTxSendRawPacket(struct ACI_MASTER * aci, unsigned char aciMessageType, unsigned char dest, unsigned char src, void * data,
                        unsigned short cnt) {
  unsigned char startstring[3] = { '!', '#', '!' };
  unsigned short crc = 0xFF;
  unsigned char packetTxBuffer[ACI_TX_RINGBUFFER_SIZE];
//  char s[200];
  int pos = 0;

  if (cnt + 12 >= ACI_TX_RINGBUFFER_SIZE)
    return ADD_TO_TX_BUFFER_RETURN_BUFFER_FULL;


//  sprintf(&s,"TX-P: MT: 0x%02X, L: %i",aciMessageType,cnt);
//  DEBUGOUT(s);


  //add header to ringbuffer
  memcpy(&packetTxBuffer[pos], &startstring, 3);
  pos += 3;


  //add dest to ringbuffer
  memcpy(&packetTxBuffer[pos], &dest, 1);
  pos += 1;
  crc = aciUpdateCrc16(crc, &dest, 1);

  //add message type to ringbuffer
  memcpy(&packetTxBuffer[pos], &src, 1);
  pos += 1;
  crc = aciUpdateCrc16(crc, &src, 1);


  //add message type to ringbuffer
  memcpy(&packetTxBuffer[pos], &aciMessageType, 1);
  pos += 1;
  crc = aciUpdateCrc16(crc, &aciMessageType, 1);

  //add data size to ringbuffer
  memcpy(&packetTxBuffer[pos], &cnt, 2);
  pos += 2;
  crc = aciUpdateCrc16(crc, &cnt, 2);

  memcpy(&packetTxBuffer[pos], data, cnt);
  pos += cnt;
  crc = aciUpdateCrc16(crc, data, cnt);

  //add CRC to ringbuffer
  memcpy(&packetTxBuffer[pos], &crc, 2);
  pos += 2;

  return addToTxBuffer(aci, &packetTxBuffer[0], pos);

}

uint8_t aciTxSendSingleCommand(struct ACI_MASTER * aci, uint16_t cmdId, uint8_t * data, uint16_t len)
{
#define SINGLE_CMD_LEN_MAX 70 // TODO: chosen nearly arbitrarily for now

#pragma pack(1)
	typedef struct {
		uint16_t cmdId;
		uint8_t cmdCnt;
	} singleCmdHeader_t;
#pragma pack()
	static uint8_t cmdCnt;
	cmdCnt++;

	if(SINGLE_CMD_LEN_MAX < len + sizeof(singleCmdHeader_t))
	{
		DEBUGOUT("ERROR: Single Cmd too long");
		return ADD_TO_TX_BUFFER_RETURN_BUFFER_FULL;
	}

	uint8_t outData[SINGLE_CMD_LEN_MAX];

	singleCmdHeader_t singleCmdHeader = {
			.cmdId = cmdId,
			.cmdCnt = cmdCnt,
	};

	memcpy(outData, &singleCmdHeader, sizeof(singleCmdHeader));
	memcpy(&(outData[sizeof(singleCmdHeader)]), data, len);

	return aciTxSendRawPacket(aci, ACIMT_CMDSINGLE, ACI_ADDR_NAV1_PC_ENDP , ACI_ADDR_PC, outData, len + sizeof(singleCmdHeader_t));
}

void aciGetDeviceVariablesList(struct ACI_MASTER* aci)
{

  if (!(aci->RequestListType & 0x10))
  {
    if (aci->ReadHDC && !aci->MagicCodeOnHDFalse)
    {
      aci->RequestListType |= 0x01;
      aci->RequestMagicCodes = 1;
    }
    else
    {
      aci->RequestVarListTimeout = 0;
    }
  }
}

void aciGetDeviceCommandsList(struct ACI_MASTER* aci)
{

  if (!(aci->RequestListType & 0x20))
  {
    if (aci->ReadHDC && !aci->MagicCodeOnHDFalse)
    {
      aci->RequestListType |= 0x02;
      aci->RequestMagicCodes = 1;
    }
    else
    {
      aci->RequestCmdListTimeout = 0;
    }
  }
}

void aciGetDeviceParametersList(struct ACI_MASTER* aci)
{
  if (!(aci->RequestListType & 0x40)) {
    if (aci->ReadHDC && !aci->MagicCodeOnHDFalse) {
      aci->RequestListType |= 0x04;
      aci->RequestMagicCodes = 1;
    } else {
      aci->RequestParListTimeout = 0;
    }
  }
}

void aciForceListRequestFromDevice(struct ACI_MASTER* aci)
{
  aci->MagicCodeOnHDFalse = 1;
  aci->RequestListType = 0;
}

void aciGetParamFromDevice(struct ACI_MASTER* aci, unsigned short id)
{
  aciTxSendPacket(aci, ACIMT_PARAM, &id, 2);
  aci->paramRequestTimeout = 10;
  aci->paramRequestId = id;

}


/** get list item by index **/
struct ACI_MEM_TABLE_ENTRY* aciGetVariableItemByIndex(struct ACI_MASTER* aci,
                                                      unsigned short index)
{
  unsigned short i = 0;
  aci->MemVarTableCurrent = aci->MemVarTableStart;

  while (aci->MemVarTableCurrent->next) {
    aci->MemVarTableCurrent = aci->MemVarTableCurrent->next;

    if (i == index) {
      return (&aci->MemVarTableCurrent->tableEntry);
    }

    i++;
  }

  return NULL;
}


/** try to find a list item by id **/
struct ACI_MEM_TABLE_ENTRY* aciGetVariableItemById(struct ACI_MASTER* aci,
                                                   unsigned short id)
{
  aci->MemVarTableCurrent = aci->MemVarTableStart;

  while (aci->MemVarTableCurrent->next) {
    aci->MemVarTableCurrent = aci->MemVarTableCurrent->next;

    if (aci->MemVarTableCurrent->tableEntry.id == id) {
      return (&aci->MemVarTableCurrent->tableEntry);
    }
  }

  return NULL;
}


/**get length of var table**/
unsigned short aciGetVarTableLength(struct ACI_MASTER* aci)
{
  unsigned short length = 0;

  aci->MemVarTableCurrent = aci->MemVarTableStart;

  while (aci->MemVarTableCurrent->next) {
    length++;
    aci->MemVarTableCurrent = aci->MemVarTableCurrent->next;
  }

  return length;
}


struct ACI_INFO aciGetInfo(struct ACI_MASTER* aci)
{
  return aci->Info;
}
#ifndef ACI_SHORT_MEM_TABLES

/** get list item by index **/
struct ACI_MEM_TABLE_ENTRY* aciGetParameterItemByIndex(struct ACI_MASTER* aci,
                                                       unsigned short index)
{
  unsigned short i = 0;
  aci->MemParamTableCurrent = aci->MemParamTableStart;

  while (aci->MemParamTableCurrent->next) {
    aci->MemParamTableCurrent = aci->MemParamTableCurrent->next;

    if (i == index) {
      return (&aci->MemParamTableCurrent->tableEntry);
    }

    i++;
  }

  return NULL;
}
#endif

/** try to find a list item by id **/
struct ACI_MEM_TABLE_ENTRY* aciGetParameterItemById(struct ACI_MASTER* aci,
                                                    unsigned short id)
{
  aci->MemParamTableCurrent = aci->MemParamTableStart;

  while (aci->MemParamTableCurrent->next) {
    aci->MemParamTableCurrent = aci->MemParamTableCurrent->next;

    if (aci->MemParamTableCurrent->tableEntry.id == id) {
      return (&aci->MemParamTableCurrent->tableEntry);
    }
  }

  return NULL;
}

#ifndef ACI_SHORT_MEM_TABLES

/**get length of var table**/
unsigned short aciGetParamTableLength(struct ACI_MASTER* aci)
{
  unsigned short length = 0;

  aci->MemParamTableCurrent = aci->MemParamTableStart;

  while (aci->MemParamTableCurrent->next) {
    length++;
    aci->MemParamTableCurrent = aci->MemParamTableCurrent->next;
  }

  return length;
}

/** get list item by index **/
struct ACI_MEM_TABLE_ENTRY* aciGetCommandItemByIndex(struct ACI_MASTER* aci,
                                                     unsigned short index)
{
  unsigned short i = 0;
  aci->MemCmdTableCurrent = aci->MemCmdTableStart;

  while (aci->MemCmdTableCurrent->next) {
    aci->MemCmdTableCurrent = aci->MemCmdTableCurrent->next;

    if (i == index) {
      return (&aci->MemCmdTableCurrent->tableEntry);
    }

    i++;
  }

  return NULL;
}
#endif

/** try to find a list item by id **/
struct ACI_MEM_TABLE_ENTRY* aciGetCommandItemById(struct ACI_MASTER* aci,
                                                  unsigned short id)
{
  aci->MemCmdTableCurrent = aci->MemCmdTableStart;

  while (aci->MemCmdTableCurrent->next) {
    aci->MemCmdTableCurrent = aci->MemCmdTableCurrent->next;

    if (aci->MemCmdTableCurrent->tableEntry.id == id) {
      return (&aci->MemCmdTableCurrent->tableEntry);
    }
  }

  return NULL;
}

#ifndef ACI_SHORT_MEM_TABLES

/**get length of var table**/
unsigned short aciGetCmdTableLenth(struct ACI_MASTER* aci)
{
  unsigned short length = 0;

  aci->MemCmdTableCurrent = aci->MemCmdTableStart;

  while (aci->MemCmdTableCurrent->next) {
    length++;
    aci->MemCmdTableCurrent = aci->MemCmdTableCurrent->next;
  }

  return length;
}
#endif
void aciUpdateCmdPacket(struct ACI_MASTER* aci, const unsigned short packetId)
{
  aci->CmdPacketSendStatus[packetId] = 1;
  //  aci->CmdWithAck[packetId] = 1;
}

void aciUpdateParamPacket(struct ACI_MASTER* aci, const unsigned short packetId)
{
  aci->ParamPacketSendStatus[packetId] = 1;
}

unsigned char aciGetCmdSendStatus(struct ACI_MASTER* aci,
                                  const unsigned short packetId)
{
  return aci->CmdPacketSendStatus[packetId];
}


void aciRxHandleMessage(struct ACI_MASTER* aci, unsigned char messagetype,
                        unsigned short length)
{
	int i;
	unsigned char packetSelect;
	unsigned char temp_ack;
	unsigned short temp_id;

	typedef enum {
		USBCOCKPITTOPC_RUNTIMEINFO,
		USBCOCKPITTOPC_INITINFO,
		USBCOCKPITTOPC_DEBUGINFO,
		USBCOCKPITTOPC_ERRORS,
	} usb_cockpit_to_pc_msg_type_t;

	//printf( "messagetype: %d \n", messagetype );
	//fflush(stdout);
	switch (messagetype) {

	case ACIMT_IDL_TO_PC:
	{
		/*
		 * uint16_t alarmstate_cockpit;//VT_UINT16// see lcd_data_alarmstates_t//
       uint8_t diversity_lock[2];//VT_UINT8(2)// if sum of entries < 4 (<8) then link weak (lost)//
		 */

		usb_cockpit_to_pc_msg_type_t msg_type = aci->RxDataBuffer[0];

		switch(msg_type)
		{
		case USBCOCKPITTOPC_RUNTIMEINFO:
		{
			usb_cockpit_to_pc_runtimeinfo_t* runtime_pt = (usb_cockpit_to_pc_runtimeinfo_t*) aci->RxDataBuffer;
			aci->diversity_lock[0] = runtime_pt->diversity_lock[0];
			aci->diversity_lock[1] = runtime_pt->diversity_lock[1];
			aci->gps_quality_base = runtime_pt->gps_quality_base;
			aci->radiolink_region_config = runtime_pt->radiolink_region_config;
		}
			break;

		case USBCOCKPITTOPC_INITINFO:
		{
			usb_cockpit_to_pc_initinfo_t* info_pt = (usb_cockpit_to_pc_initinfo_t*) aci->RxDataBuffer;
			aci->droneLinkVersionMajor = info_pt->version_major;
			aci->droneLinkVersionMinor = info_pt->version_minor;
			memcpy(aci->droneLinkSerial, info_pt->serial_number, sizeof(aci->droneLinkSerial));
		}
			break;

		case USBCOCKPITTOPC_ERRORS:
		{
			usb_cockpit_to_pc_errors_t* error_pt = (usb_cockpit_to_pc_errors_t*) aci->RxDataBuffer;
			if(error_pt->error_nr < sizeof(aci->errorMsgIdl) / sizeof(aci->errorMsgIdl[0]))
			{
				memcpy(aci->errorMsgIdl[error_pt->error_nr], error_pt->errorMsg.msg, sizeof(aci->errorMsgIdl[error_pt->error_nr]));

				if(error_pt->error_nr + 1 > aci->errorMsgIdlCnt)
				{
					aci->errorMsgIdlCnt = error_pt->error_nr + 1;
				}
			}
		}
			break;

		default:
			break;
		}
	}
	break;

	case ACIMT_INFO_REQUEST:
		aci->Info.verMajor = ACI_VER_MAJOR;
		aci->Info.verMinor = ACI_VER_MINOR;
		aci->Info.maxDescLength = MAX_DESC_LENGTH;
		aci->Info.maxNameLength = MAX_NAME_LENGTH;
		aci->Info.maxUnitLength = MAX_UNIT_LENGTH;
		aci->Info.maxVarPackets = MAX_VAR_PACKETS;
		aci->Info.flags = 0;

		for (i = 0; i < 8; i++) {
			aci->Info.dummy[i] = 0;
		}

		aciTxSendPacket(aci, ACIMT_INFO_REPLY, &aci->Info, sizeof(aci->Info));

		break;

	case ACIMT_INFO_REPLY:
		if (length == sizeof(struct ACI_INFO)) {
			memcpy(&aci->Info, &aci->RxDataBuffer[0], length);

			if (aci->InfoRec) {
				aci->InfoRec(aci->Info);
			}
		}

		break;

	case ACIMT_SENDVARTABLEINFO:
		if (length >= 2) {
			aci->VarTableLength = (aci->RxDataBuffer[1] << 8) | aci->RxDataBuffer[0];

			if (length == aci->VarTableLength * 2 + 2) {
				aci->RequestedPacketList = malloc(sizeof(unsigned short) * aci->VarTableLength);
				aci->RequestedPacketListLength = aci->VarTableLength;

				for (i = 0; i < aci->VarTableLength; i++) {
					aci->RequestedPacketList[i] = (aci->RxDataBuffer[3 + i * 2] << 8) |
							aci->RxDataBuffer[2 + i * 2];
				}

				//request first entry
				aciTxSendPacket(aci, ACIMT_REQUESTVARTABLEENTRIES, aci->RequestedPacketList, 2);
				aci->RequestedPacketListTimeOut = ACI_REQUEST_LIST_TIMEOUT;
				aci->RequestVarListTimeout = 60000;
			}

		}

		break;

	case ACIMT_SENDVARTABLEENTRY:
		aci->RequestedPacketListTimeOut = ACI_REQUEST_LIST_PENDING_TIMEOUT;

		if ((length == (sizeof(struct ACI_MEM_TABLE_ENTRY_LONG)) - sizeof(void*))
				&& (aci->RequestedPacketListLength)) {
			unsigned short id = (aci->RxDataBuffer[1] << 8) | (aci->RxDataBuffer[0]);
			unsigned char idAlreadyExists = 0;

			if (aci->RequestedPacketList[0] != id) {
				break;
			}

			aci->MemVarTableCurrent = aci->MemVarTableStart;
			aci->reqListLen--;

			while (aci->MemVarTableCurrent->next) {
				aci->MemVarTableCurrent = aci->MemVarTableCurrent->next;

				if (aci->MemVarTableCurrent->tableEntry.id == id) {
					idAlreadyExists = 1;
				}
			}

			if (!idAlreadyExists) {
#ifdef ACI_SHORT_MEM_TABLES
struct ACI_MEM_TABLE_ENTRY_LONG* entry;
#endif
aci->MemVarTableCurrent->next = malloc(sizeof(struct ACI_MEM_VAR_TABLE));
aci->MemVarTableCurrent = aci->MemVarTableCurrent->next;
#ifdef ACI_SHORT_MEM_TABLES

entry = (struct ACI_MEM_TABLE_ENTRY_LONG*)&aci->RxDataBuffer[0];
aci->MemVarTableCurrent->tableEntry.id = entry->id;
aci->MemVarTableCurrent->tableEntry.varType = entry->varType;
#else
memcpy(&(aci->MemVarTableCurrent->tableEntry), &aci->RxDataBuffer[0],
		(sizeof(struct ACI_MEM_TABLE_ENTRY)) - sizeof(void*));
#endif
aci->MagicCodeVarLoaded++;
aci->MagicCodeVar = aciUpdateCrc16(aci->MagicCodeVar,
		&aci->MemVarTableCurrent->tableEntry.id, 2);
aci->MagicCodeVar = aciUpdateCrc16(aci->MagicCodeVar,
		&aci->MemVarTableCurrent->tableEntry.varType, 2);
aci->MemVarTableCurrent->next = NULL;
			}

			for (i = 0; i < aci->RequestedPacketListLength; i++)
				if (aci->RequestedPacketList[i] == id) {
					int z;

					for (z = i; z < aci->RequestedPacketListLength - 1; z++) {
						aci->RequestedPacketList[z] = aci->RequestedPacketList[z + 1];
					}

					aci->RequestedPacketListLength--;
					///////////////////////////
					//printf("Received var, var left %i: \n",   aci->RequestedPacketListLength);
					//fflush(stdout);
					////////////////////////////

					if (!aci->RequestedPacketListLength) {
						free(aci->RequestedPacketList);
						aci->RequestListType |= 0x10;
#ifndef ACI_SHORT_MEM_TABLES

						if ((aci->RequestListType & 0x10) && (aci->RequestListType & 0x20)
								&& (aci->RequestListType & 0x40) && (aci->WriteHDC) && (aci->ResetHDC)) {
							aciStoreList(aci);
						}

#endif

						if (aci->VarListUpdateFinished) {
							aci->VarListUpdateFinished();
						}
					}

					break;
				}

			if (aci->SyncStatus) {
				aci->SyncStatus(1, (aci->VarTableLength - aci->RequestedPacketListLength) * 100
						/ aci->VarTableLength);
			}

			if (!aci->reqListLen) {
				aci->RequestedPacketListTimeOut = 1;
			} else if (aci->RequestedPacketListLength) {
				aci->RequestedPacketListTimeOut = ACI_REQUEST_LIST_PENDING_TIMEOUT;
			}
		}

		break;

	case ACIMT_SENDVARTABLEENTRYINVALID:
	case ACIMT_SENDCMDTABLEENTRYINVALID:
	case ACIMT_SENDPARAMTABLEENTRYINVALID:
		// printf("Invalid Table Entry\n");

		break;

	case ACIMT_SENDCMDTABLEINFO:
		if (length >= 2) {
			aci->CmdTableLength = (aci->RxDataBuffer[1] << 8) | aci->RxDataBuffer[0];

			if (!aci->CmdTableLength) {
				aci->RequestCmdListTimeout = 60000;
				aci->RequestListType |= 0x20;
#ifndef ACI_SHORT_MEM_TABLES

				if ((aci->RequestListType & 0x10) && (aci->RequestListType & 0x20)
						&& (aci->RequestListType & 0x40) && (aci->WriteHDC) && (aci->ResetHDC)) {
					aciStoreList(aci);
				}

#endif

				if (aci->CmdListUpdateFinished) {
					aci->CmdListUpdateFinished();
				}
			} else

				if (length == aci->CmdTableLength * 2 + 2) {
					aci->RequestCmdListTimeout = 60000;

					if (aci->RequestedCmdPacketList) {
						free(aci->RequestedCmdPacketList);
					}

					aci->RequestedCmdPacketList = (unsigned short*) malloc(aci->CmdTableLength * 2);
					aci->RequestedCmdPacketListLength = aci->CmdTableLength;

					for (i = 0; i < aci->CmdTableLength; i++) {
						aci->RequestedCmdPacketList[i] = (aci->RxDataBuffer[3 + i * 2] << 8) |
								aci->RxDataBuffer[2 + i * 2];
					}

					//request first entry
					aciTxSendPacket(aci, ACIMT_REQUESTCMDTABLEENTRIES, aci->RequestedCmdPacketList,
							2);
					aci->RequestedCmdPacketListTimeOut = ACI_REQUEST_LIST_TIMEOUT;
				}
		}

		break;

	case ACIMT_SENDCMDTABLEENTRY:

		//printf("Cmd table length: %i \n", aci->RequestedCmdPacketListLength);
		//fflush(stdout);

		if ((length == (sizeof(struct ACI_MEM_TABLE_ENTRY_LONG)) - sizeof(void*))
				&& (aci->RequestedCmdPacketListLength)) {
			unsigned short id = (aci->RxDataBuffer[1] << 8) | (aci->RxDataBuffer[0]);
			unsigned char idAlreadyExists = 0;

			if (aci->RequestedCmdPacketList[0] != id) {
				break;
			}

			aci->MemCmdTableCurrent = aci->MemCmdTableStart;

			while (aci->MemCmdTableCurrent->next) {
				aci->MemCmdTableCurrent = aci->MemCmdTableCurrent->next;

				if (aci->MemCmdTableCurrent->tableEntry.id == id) {
					idAlreadyExists = 1;
				}
			}

			if (!idAlreadyExists) {
#ifdef ACI_SHORT_MEM_TABLES
struct ACI_MEM_TABLE_ENTRY_LONG* entry;
#endif
aci->MemCmdTableCurrent->next = malloc(sizeof(struct ACI_MEM_VAR_TABLE));
aci->MemCmdTableCurrent = aci->MemCmdTableCurrent->next;
#ifdef ACI_SHORT_MEM_TABLES
entry = (struct ACI_MEM_TABLE_ENTRY_LONG*)&aci->RxDataBuffer[0];
aci->MemCmdTableCurrent->tableEntry.id = entry->id;
aci->MemCmdTableCurrent->tableEntry.varType = entry->varType;
#else
memcpy(&(aci->MemCmdTableCurrent->tableEntry), &aci->RxDataBuffer[0],
		sizeof(struct ACI_MEM_TABLE_ENTRY) - sizeof(void*));
#endif
aci->MagicCodeCmdLoaded++;
aci->MagicCodeCmd  = aciUpdateCrc16(aci->MagicCodeCmd,
		&aci->MemCmdTableCurrent->tableEntry.id, 2);
aci->MagicCodeCmd = aciUpdateCrc16(aci->MagicCodeCmd,
		&aci->MemCmdTableCurrent->tableEntry.varType, 2);
aci->MemCmdTableCurrent->next = NULL;
			}

			//remove entry from requestedPacketList
			for (i = 0; i < aci->RequestedCmdPacketListLength; i++)
				if (aci->RequestedCmdPacketList[i] == id) {
					//remove from list
					int z;

					for (z = i; z < aci->RequestedCmdPacketListLength - 1; z++) {
						aci->RequestedCmdPacketList[z] = aci->RequestedCmdPacketList[z + 1];
					}

					aci->RequestedCmdPacketListLength--;

					if (!aci->RequestedCmdPacketListLength) {
						free(aci->RequestedCmdPacketList);
						aci->RequestListType |= 0x20;
#ifndef ACI_SHORT_MEM_TABLES

						if ((aci->RequestListType & 0x10) && (aci->RequestListType & 0x20)
								&& (aci->RequestListType & 0x40) && (aci->WriteHDC) && (aci->ResetHDC)) {
							aciStoreList(aci);
						}

#endif

						if (aci->CmdListUpdateFinished) {
							aci->CmdListUpdateFinished();
						}
					}

					break;
				}

			if (aci->SyncStatus) {
				aci->SyncStatus(2, (aci->CmdTableLength - aci->RequestedCmdPacketListLength) *
						100 / aci->CmdTableLength);
			}

			if (aci->RequestedCmdPacketListLength) {
				//request next entry
				aci->RequestedCmdPacketListTimeOut = ACI_REQUEST_LIST_PENDING_TIMEOUT;
			}
		}

		break;

	case ACIMT_SENDPARAMTABLEINFO:
		if (length >= 2) {

			aci->ParamTableLength = (aci->RxDataBuffer[1] << 8) | aci->RxDataBuffer[0];

			if (length == aci->ParamTableLength * 2 + 2) {
				aci->RequestParListTimeout = 60000;
				aci->RequestedParamPacketList = (unsigned short*)  malloc(
						aci->ParamTableLength * 2);
				aci->RequestedParamPacketListLength = aci->ParamTableLength;

				for (i = 0; i < aci->ParamTableLength; i++) {
					aci->RequestedParamPacketList[i] = (aci->RxDataBuffer[3 + i * 2] << 8) |
							aci->RxDataBuffer[2 + i * 2];
				}

				//request first entry
				if (aci->ParamTableLength == 0) {
					aci->RequestListType |= 0x40;
#ifndef ACI_SHORT_MEM_TABLES

					if ((aci->RequestListType & 0x10) && (aci->RequestListType & 0x20)
							&& (aci->RequestListType & 0x40) && (aci->WriteHDC) && (aci->ResetHDC)) {
						aciStoreList(aci);
					}

#endif

					if (aci->ParamListUpdateFinished) {
						aci->ParamListUpdateFinished();
					}
				} else {
					aciTxSendPacket(aci, ACIMT_REQUESTPARAMTABLEENTRIES,
							aci->RequestedParamPacketList, 2);
					aci->RequestedParamPacketListTimeOut = ACI_REQUEST_LIST_TIMEOUT;
				}
			}
		}

		break;

	case ACIMT_SENDPARAMTABLEENTRY:

		//printf("Param table length: %i \n", aci->RequestedParamPacketListLength);
		//fflush(stdout);

		if ((length == ((sizeof(struct ACI_MEM_TABLE_ENTRY_LONG)) - sizeof(void*)))
				&& (aci->RequestedParamPacketListLength)) {
			unsigned short id = (aci->RxDataBuffer[1] << 8) | (aci->RxDataBuffer[0]);
			unsigned char idAlreadyExists = 0;

			if (aci->RequestedParamPacketList[0] != id) {
				break;
			}

			aci->MemParamTableCurrent = aci->MemParamTableStart;

			while (aci->MemParamTableCurrent->next) {
				aci->MemParamTableCurrent = aci->MemParamTableCurrent->next;

				if (aci->MemParamTableCurrent->tableEntry.id == id) {
					idAlreadyExists = 1;
				}
			}

			if (!idAlreadyExists) {

#ifdef ACI_SHORT_MEM_TABLES
				struct ACI_MEM_TABLE_ENTRY_LONG* entry;
#endif
				aci->MemParamTableCurrent->next = malloc(sizeof(struct ACI_MEM_VAR_TABLE));
				aci->MemParamTableCurrent = aci->MemParamTableCurrent->next;
#ifdef ACI_SHORT_MEM_TABLES
				entry = (struct ACI_MEM_TABLE_ENTRY_LONG*)&aci->RxDataBuffer[0];
				aci->MemParamTableCurrent->tableEntry.id = entry->id;
				aci->MemParamTableCurrent->tableEntry.varType = entry->varType;
#else
				memcpy(&(aci->MemParamTableCurrent->tableEntry), &aci->RxDataBuffer[0],
						(sizeof(struct ACI_MEM_TABLE_ENTRY)) - sizeof(void*));
#endif
				aci->MagicCodeParLoaded++;
				aci->MagicCodePar  = aciUpdateCrc16(aci->MagicCodePar,
						&aci->MemParamTableCurrent->tableEntry.id, 2);
				aci->MagicCodePar = aciUpdateCrc16(aci->MagicCodePar,
						&aci->MemParamTableCurrent->tableEntry.varType, 2);
				aci->MemParamTableCurrent->next = NULL;
			}

			//remove entry from requestedPacketList
			for (i = 0; i < aci->RequestedParamPacketListLength; i++)
				if (aci->RequestedParamPacketList[i] == id) {
					//remove from list
					int z;

					for (z = i; z < aci->RequestedParamPacketListLength - 1; z++) {
						aci->RequestedParamPacketList[z] = aci->RequestedParamPacketList[z + 1];
					}

					aci->RequestedParamPacketListLength--;

					if (!aci->RequestedParamPacketListLength) {
						free(aci->RequestedParamPacketList);
						aci->RequestListType |= 0x40;
#ifndef ACI_SHORT_MEM_TABLES

						if ((aci->RequestListType & 0x10) && (aci->RequestListType & 0x20)
								&& (aci->RequestListType & 0x40) && (aci->WriteHDC) && (aci->ResetHDC)) {
							aciStoreList(aci);
						}

#endif

						if (aci->ParamListUpdateFinished) {
							aci->ParamListUpdateFinished();
						}
					}

					break;
				}

			if (aci->SyncStatus) {
				aci->SyncStatus(3, (aci->ParamTableLength - aci->RequestedParamPacketListLength)
						* 100 / aci->ParamTableLength);
			}

			if (aci->RequestedParamPacketListLength) {
				//request next entry
				aci->RequestedParamPacketListTimeOut = ACI_REQUEST_LIST_PENDING_TIMEOUT;
			}
		}

		break;

	case ACIMT_RCVARPACKET:
		//check magic code to see that the packet fits the desired configuration
		aci->rxPacketTimeOut = 0;

		packetSelect = 1;

		if ((aci->VarPacketMagicCode[packetSelect] == aci->RxDataBuffer[0])
				&& (aci->VarPacketContentBufferLength[packetSelect] == length - 1)) {
			//copy packet data to temporary buffer
			memcpy(aci->VarPacketContentBuffer[packetSelect], &aci->RxDataBuffer[1],
					length - 1);
			aci->VarPacketContentBufferValid[packetSelect] = 1;
			aci->VarPacketContentBufferInvalidCnt[packetSelect] = 0;

			if (aci->VarPacketRec) {
				aci->VarPacketRec(packetSelect);
			}

		} else {
			aci->VarPacketContentBufferValid[packetSelect] = 0;
			aci->VarPacketContentBufferInvalidCnt[packetSelect]++;

			if (aci->VarPacketContentBufferInvalidCnt[packetSelect] ==
					TIMEOUT_INVALID_PACKET) {
				aci->VarPacketContentBufferInvalidCnt[packetSelect]--;
				aci->UpdateVarPacketTimeOut[packetSelect] =
						1; //trigger resend packet configuration
			}
		}

		break;

	case ACIMT_VARPACKET:
		//check magic code to see that the packet fits the desired configuration
		packetSelect = messagetype - ACIMT_VARPACKET;

		if (packetSelect >= MAX_VAR_PACKETS) {
			break;
		}

		if ((aci->VarPacketMagicCode[packetSelect] == aci->RxDataBuffer[0])
				&& (aci->VarPacketContentBufferLength[packetSelect] == length - 1)) {
			//copy packet data to temporary buffer
			memcpy(aci->VarPacketContentBuffer[packetSelect], &aci->RxDataBuffer[1],
					length - 1);
			aci->VarPacketContentBufferValid[packetSelect] = 1;
			aci->VarPacketContentBufferInvalidCnt[packetSelect] = 0;

			if (aci->VarPacketRec) {
				aci->VarPacketRec(packetSelect);
			}

		} else {
			aci->VarPacketContentBufferValid[packetSelect] = 0;
			aci->VarPacketContentBufferInvalidCnt[packetSelect]++;

			if (aci->VarPacketContentBufferInvalidCnt[packetSelect] ==
					TIMEOUT_INVALID_PACKET) {
				aci->VarPacketContentBufferInvalidCnt[packetSelect]--;
				aci->UpdateVarPacketTimeOut[packetSelect] =
						1; //trigger resend packet configuration
			}
		}

		break;

	case ACIMT_PARAM:
		temp_id = (aci->RxDataBuffer[1] << 8) | aci->RxDataBuffer[0];

		if (temp_id == aci->paramRequestId) {
			aci->paramRequestTimeout = 0;
		}

		if (((short)(aciGetParameterItemById(aci,
				temp_id)->varType >> 3)) == (length - 2)) {
			memcpy(aciGetParameterItemById(aci, temp_id)->ptrToVar, &aci->RxDataBuffer[2],
					length - 2);
		}

		if (aci->paramReceived) {
			aci->paramReceived(temp_id);
		}

		break;

	case ACIMT_PACKETRATEINFO:
		memcpy(&aci->VarPacketTransmissionRate[0], &aci->RxDataBuffer[0],
				MAX_VAR_PACKETS * 2);
		break;

	case ACIMT_SAVEPARAM:
		if (length == 2) {
			if (aci->ParaStoredC) {
				aci->ParaStoredC();
			}
		}

		break;

	case ACIMT_SINGLESEND:
		if (length > 4) {
			temp_id = (aci->RxDataBuffer[1] << 8) | aci->RxDataBuffer[0];

			if (temp_id != 0) {
				aciTxSendPacket(aci, ACIMT_SINGLESEND, &temp_id, 2);
			}

			temp_id = (aci->RxDataBuffer[3] << 8) | aci->RxDataBuffer[2];
			temp_ack = aci->RxDataBuffer[4];

			if (aci->SingleReceivedC) {
				aci->SingleReceivedC(temp_id, &aci->RxDataBuffer[5], temp_ack);
			}
		}

		break;

	case ACIMT_SINGLEREQ:
		if (length > 3) {
			temp_id = (aci->RxDataBuffer[1] << 8) | aci->RxDataBuffer[0];
			temp_ack = aci->RxDataBuffer[2];

			if (aci->SingleReqReceivedC) {
				aci->SingleReqReceivedC(temp_id, &aci->RxDataBuffer[3], temp_ack);
			}

		}

		break;

	case ACIMT_MAGICCODES:
		if ((length == 12) && (aci->magicCodeAlreadyRequested == 0)) {

			unsigned short tempMagicVar, tempMagicCmd, tempMagicPar;
			unsigned short tempVarCount, tempCmdCount, tempParCount;
			aci->magicCodeAlreadyRequested = 1;
			aci->RequestMagicCodes = 0;

			tempMagicVar = (aci->RxDataBuffer[1] << 8) | aci->RxDataBuffer[0];
			tempMagicCmd = (aci->RxDataBuffer[3] << 8) | aci->RxDataBuffer[2];
			tempMagicPar = (aci->RxDataBuffer[5] << 8) | aci->RxDataBuffer[4];

			tempVarCount = (aci->RxDataBuffer[7] << 8) | aci->RxDataBuffer[6];
			tempCmdCount = (aci->RxDataBuffer[9] << 8) | aci->RxDataBuffer[8];
			tempParCount = (aci->RxDataBuffer[11] << 8) | aci->RxDataBuffer[10];

			aciLoadHeaderList(aci);

			if ((tempMagicVar == aci->MagicCodeVar) && (tempMagicCmd == aci->MagicCodeCmd)
					&& (tempMagicPar == aci->MagicCodePar)
					&& (tempVarCount == aci->MagicCodeVarLoaded)
					&& (tempCmdCount == aci->MagicCodeCmdLoaded)
					&& (tempParCount == aci->MagicCodeParLoaded)) {
				if (aciLoadList(aci) == 0) {

					aci->RequestListType = 0x70;

					if (aci->VarListUpdateFinished) {
						aci->VarListUpdateFinished();
					}

					if (aci->CmdListUpdateFinished) {
						aci->CmdListUpdateFinished();
					}

					if (aci->ParamListUpdateFinished) {
						aci->ParamListUpdateFinished();
					}

					break;
				}
			}

			DEBUGOUT("magic codes dont mach or file invalid or not present");

			aci->MagicCodeVarLoaded = 0;
			aci->MagicCodeCmdLoaded = 0;
			aci->MagicCodeParLoaded = 0;

			aci->MagicCodeVar = 0x00FF;
			aci->MagicCodeCmd = 0x00FF;
			aci->MagicCodePar = 0x00FF;

			aci->MagicCodeOnHDFalse = 1;

			if (aci->RequestListType & 0x01) {
				aci->RequestVarListTimeout = 0;
				aci->RequestListType &= ~0x01;
			} else  if (aci->RequestListType & 0x02) {
				aci->RequestCmdListTimeout = 0;
				aci->RequestListType &= ~0x02;
			} else  if (aci->RequestListType & 0x04) {
				aci->RequestParListTimeout = 0;
				aci->RequestListType &= ~0x04;
			}
		}

		break;

	case ACIMT_LOADPARAM:
		if (length == 2) {
			if (aci->ParaLoadedC) {
				aci->ParaLoadedC();
			}
		}

		break;

	case ACI_DBG:
		break;

	case ACIMT_ACK:
		switch (aci->RxDataBuffer[0]) {
		case ACIMT_UPDATERCVARPACKET:
			packetSelect = 1;

			if (packetSelect > MAX_VAR_PACKETS) {
				break;
			}

			if (aci->RxDataBuffer[1] == ACI_ACK_OK) {
				aci->UpdateVarPacketTimeOut[packetSelect] = 0;
			} else if (aci->RxDataBuffer[1] == ACI_ACK_PACKET_TOO_LONG) {
				// Variable packet too long
				DEBUGOUT("Variable packet too long\n");
			} else {
				aci->UpdateVarPacketTimeOut[packetSelect] = 1;  //resend with next engine cycle
			}

			break;

		case ACIMT_UPDATEVARPACKET:
			packetSelect = 0;

			if (packetSelect > MAX_VAR_PACKETS) {
				break;
			}

			if (aci->RxDataBuffer[1] == ACI_ACK_OK) {
				aci->UpdateVarPacketTimeOut[packetSelect] = 0;
			} else if (aci->RxDataBuffer[1] == ACI_ACK_PACKET_TOO_LONG) {
				// Variable packet too long
				DEBUGOUT("Variable packet too long\n");
			} else {
				aci->UpdateVarPacketTimeOut[packetSelect] = 1;  //resend with next engine cycle
			}

			break;

		case ACIMT_UPDATECMDPACKET:
			packetSelect = 0;

			if (packetSelect > MAX_VAR_PACKETS) {
				break;
			}

			if (aci->RxDataBuffer[1] == ACI_ACK_OK) {
				aci->UpdateCmdPacketTimeOut[packetSelect] = 0;
			} else if (aci->RxDataBuffer[1] == ACI_ACK_PACKET_TOO_LONG) {
				// Command packet too long
				DEBUGOUT("Command packet too long\n");
			} else {
				aci->UpdateCmdPacketTimeOut[packetSelect] = 1;  //resend with next engine cycle
			}

			break;

		case ACIMT_CMDACK:
			packetSelect = 0;

			if (aci->CmdPacketSendStatus[packetSelect] == 2) {
				aci->CmdPacketSendStatus[packetSelect] = 0;
			}

			if (aci->CmdAck) {
				aci->CmdAck(packetSelect);
			}

			break;



		case ACIMT_PARAMPACKET:
			packetSelect = 0;
			aciParamAck(aci, packetSelect);
			break;

		case ACIMT_UPDATEPARAMPACKET:
			packetSelect = 0;

			if (packetSelect > MAX_VAR_PACKETS) {
				break;
			}

			if (aci->RxDataBuffer[1] == ACI_ACK_OK) {
				aci->UpdateParamPacketTimeOut[packetSelect] = 0;
				aci->ParamPacketStatus[packetSelect] = 1;
			} else if (aci->RxDataBuffer[1] == ACI_ACK_PACKET_TOO_LONG) {
				DEBUGOUT("Parameter packet too long\n");
			} else {
				aci->UpdateParamPacketTimeOut[packetSelect] =
						1;  //resend with next engine cycle
			}

			break;
		}

		break;
	}

}

void aciSetSingleReceivedCallback(struct ACI_MASTER* aci,
                                  void (*aciSingleReceived)(unsigned short id, void* data,
                                                            unsigned short varType))
{
  aci->SingleReceivedC = aciSingleReceived;
}

void aciSetSingleRequestReceivedCallback(struct ACI_MASTER* aci,
                                         void (*aciSingleReqReceived)(unsigned short id, void* data,
                                                                      unsigned short varType))
{
  aci->SingleReqReceivedC = aciSingleReqReceived;
}

void aciSetDiversityPacketReceivedCallback(struct ACI_MASTER* aci,
                                           void (*aciDiversityPacketReceived)(unsigned char src, unsigned short id,
                                                                              void* data, unsigned short length))
{
  aci->diversityPacketReceived = aciDiversityPacketReceived;
}


void aciSetReadHDCallback(struct ACI_MASTER* aci, int (*aciReadHD)(void* data, int bytes))
{
  aci->ReadHDC = aciReadHD;
}

void aciSetWriteHDCallback(struct ACI_MASTER* aci, int (*aciWriteHD)(void* data,
                                                                     int bytes))
{
  aci->WriteHDC = aciWriteHD;
}

void aciSetResetHDCallback(struct ACI_MASTER* aci, void (*aciResetHD)(void))
{
  aci->ResetHDC = aciResetHD;
}

void aciRequestSingleVariable(struct ACI_MASTER* aci, unsigned short id)
{
  if (aci->RequestListType & 0x10) {
    aciTxSendPacket(aci, ACIMT_SINGLEREQ, &id, 2);
  }
}

void aciSendParamStore(struct ACI_MASTER* aci)
{
  aciTxSendPacket(aci, ACIMT_SAVEPARAM, NULL, 0);
}

void aciSendParamLoad(struct ACI_MASTER* aci)
{
  aciTxSendPacket(aci, ACIMT_LOADPARAM, NULL, 0);
}


void aciSetDebugOutCallback(struct ACI_MASTER* aci,
                            void (*debugOutCB)(const char* c))
{
  aci->DebugOut = debugOutCB;
}

void aciSetSyncStatusCallback(struct ACI_MASTER* aci,
                              void (*aciSyncStatus_func)(unsigned char status, unsigned char percent))
{
  aci->SyncStatus = aciSyncStatus_func;

}

unsigned char aciGetParamPacketStatus(struct ACI_MASTER* aci,
                                      unsigned short packetid)
{
  if (packetid < MAX_VAR_PACKETS) {
    return aci->ParamPacketStatus[packetid];
  } else {
    return 0;
  }
}


/** the aciReceiveHandler is fed by the uart rx function and decodes all neccessary packets  **/
uint8_t aciReceiveHandler(struct ACI_MASTER* aci, unsigned char rxByte)
{
	if(aci->TxDataBufferMutex_ACIEngineBlocking)
	{
		return ADD_TO_TX_BUFFER_RETURN_MUTEX_BLOCKED;
	}

	static int firstDataPacketReceived = 0;

	//printf( "RxState: %d \n", aci->RxState );
	//fflush(stdout);

	switch (aci->RxState) {
	case ARS_IDLE:
		if (rxByte == '!') {
			aci->RxState = ARS_STARTBYTE1;
		}

		break;

	case ARS_STARTBYTE1:
		if (rxByte == '#') {
			aci->RxState = ARS_STARTBYTE2;
		} else {
			aci->RxState = ARS_IDLE;
		}

		break;

	case ARS_STARTBYTE2:
		if (rxByte == '!') {
			aci->RxHeaderCnt++;
			aci->RxState = ARS_DEST;
		} else {
			aci->RxState = ARS_IDLE;
		}

		break;

	case ARS_DEST:
		if (rxByte == aci->MyId) {
			aci->RxCrc = 0xff;
			aci->RxCrc = aciCrcUpdate(aci->RxCrc, rxByte);
			aci->RxState = ARS_SOURCE;
		} else {
			aci->RxState = ARS_IDLE;
		}

		break;

	case ARS_SOURCE:
		aci->recId = rxByte;
		aci->RxState = ARS_MESSAGETYPE;
		aci->RxCrc = aciCrcUpdate(aci->RxCrc, rxByte);

		break;


	case ARS_MESSAGETYPE:
		aci->RxMessageType = rxByte;
		aci->RxCrc = aciCrcUpdate(aci->RxCrc, rxByte);
		aci->RxState = ARS_LENGTH1;
		break;

	case ARS_LENGTH1:
		aci->RxLength = rxByte;
		aci->RxCrc = aciCrcUpdate(aci->RxCrc, rxByte);
		aci->RxState = ARS_LENGTH2;
		break;

	case ARS_LENGTH2:
		aci->RxLength |= rxByte << 8;

		if (aci->RxLength > ACI_RX_BUFFER_SIZE) {
			aci->RxState = ARS_IDLE;
		} else {
			aci->RxCrc = aciCrcUpdate(aci->RxCrc, rxByte);
			aci->RxDataCnt = 0;

			if (aci->RxLength) {
				aci->RxState = ARS_DATA;
			} else {
				aci->RxState = ARS_CRC1;
			}
		}

		break;

	case ARS_DATA:
		aci->RxCrc = aciCrcUpdate(aci->RxCrc, rxByte);
		aci->RxDataBuffer[aci->RxDataCnt++] = rxByte;

		if ((aci->RxDataCnt) == aci->RxLength) {
			aci->RxState = ARS_CRC1;
		}

		break;

	case ARS_CRC1:
		aci->RxReceivedCrc = rxByte;
		aci->RxState = ARS_CRC2;
		break;

	case ARS_CRC2:
		aci->RxReceivedCrc |= rxByte << 8;

		if (aci->RxReceivedCrc == aci->RxCrc)
		{
			if (!firstDataPacketReceived)
			{
				firstDataPacketReceived = 1;
			}

			if (aci->recId == ACI_ADDR_DIVERSITY)
			{
				if (aci->diversityPacketReceived)
				{
					aci->diversityPacketReceived(aci->recId, aci->RxMessageType,
							&aci->RxDataBuffer[0], aci->RxLength);
				}
			}
			else
			{
				aciRxHandleMessage(aci, aci->RxMessageType, aci->RxLength);
			}
		}
		else // CRC Error
		{
			aci->CrcErrorCnt++;
			printf("ERROR: CRC Errors:%u, Headers:%u \n", aci->CrcErrorCnt, aci->RxHeaderCnt);
			fflush(stdout);
		}

		aci->RxState = ARS_IDLE;

		break;
	}

	return 0;
}
#ifndef ACI_SHORT_MEM_TABLES
int aciStoreList(struct ACI_MASTER* aci)
{
	printf("To Cache: MagicCodeVarLoaded =  %u\n", aci->MagicCodeVarLoaded);
	printf("To Cache: MagicCodeCmdLoaded =  %u\n", aci->MagicCodeCmdLoaded);
	printf("To Cache: MagicCodeParLoaded =  %u\n", aci->MagicCodeParLoaded);
	fflush(stdout);

	unsigned char buffer[12];
	int i;
	aci->ResetHDC();

	memcpy(&buffer[0], &aci->MagicCodeVar, 2);
	memcpy(&buffer[2], &aci->MagicCodeCmd, 2);
	memcpy(&buffer[4], &aci->MagicCodePar, 2);

	memcpy(&buffer[6],  &aci->MagicCodeVarLoaded, 2);
	memcpy(&buffer[8],  &aci->MagicCodeCmdLoaded, 2);
	memcpy(&buffer[10], &aci->MagicCodeParLoaded, 2);

	aci->WriteHDC(buffer, 12);

	int x64bugfix = 0; // where the files are different between 32 and 64 bit
	if(sizeof(void*) != 4) {
		x64bugfix = 4;
	}

	for (i = 0; i < aci->MagicCodeVarLoaded; i++)
	{
		if (aciGetVariableItemByIndex(aci, i) == NULL)
		{
			printf("index not found: %d", i);
			fflush(stdout);

			return 1;
		}

		int len = aci->WriteHDC(aciGetVariableItemByIndex(aci, i), sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);

		if (len != sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix)
		{
			DEBUGOUT("ERROR: Writing ACI Var Cache failed!");

			return 1;
		}
	}

	for (i = 0; i < aci->MagicCodeCmdLoaded; i++)
	{
		if (aciGetCommandItemByIndex(aci, i) == NULL)
		{
			printf("index not found: %d", i);
			fflush(stdout);
			return 1;
		}

		int len = aci->WriteHDC(aciGetCommandItemByIndex(aci, i),
				sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);

		if (len != sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix)
		{
			DEBUGOUT("ERROR: Writing ACI Cmd Cache failed!");

			return 1;
		}
	}

	for (i = 0; i < aci->MagicCodeParLoaded; i++)
	{
		if (aciGetParameterItemByIndex(aci, i) == NULL)
		{
			printf("index not found: %d", i);
			fflush(stdout);
			return 1;
		}

		int len = aci->WriteHDC(aciGetParameterItemByIndex(aci, i),
				sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);

		if (len != sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix)
		{
			DEBUGOUT("ERROR: Writing ACI Param Cache failed!");

			return 1;
		}
	}

	DEBUGOUT("Successfully saved ACI Table to Cache!");
	return 0;
}

#endif

void aciSetParamReceivedCallback(struct ACI_MASTER* aci,
                                 void (*aciParamRec_func)(unsigned short id))
{
  aci->paramReceived = aciParamRec_func;
}
#ifndef ACI_SHORT_MEM_TABLES

void aciLoadHeaderList(struct ACI_MASTER* aci)
{
	unsigned char buffer[12];

	if (aci->ReadHDC(buffer, 12) <= 0) {
		DEBUGOUT("Error: Could not read ACI Table Cache (failed reading header)!");

		return;
	}

	memcpy(&aci->MagicCodeVar, &buffer[0], 2);
	memcpy(&aci->MagicCodeCmd, &buffer[2], 2);
	memcpy(&aci->MagicCodePar, &buffer[4], 2);

	memcpy(&aci->MagicCodeVarLoaded,  &buffer[6], 2);
	memcpy(&aci->MagicCodeCmdLoaded,  &buffer[8], 2);
	memcpy(&aci->MagicCodeParLoaded, &buffer[10], 2);

	printf("From Cache: MagicCodeVarLoaded =  %u\n", aci->MagicCodeVarLoaded);
	printf("From Cache: MagicCodeCmdLoaded =  %u\n", aci->MagicCodeCmdLoaded);
	printf("From Cache: MagicCodeParLoaded =  %u\n", aci->MagicCodeParLoaded);
	fflush(stdout);

	return;
}

void aciSetParamUpdateAckCallback(struct ACI_MASTER* aci,
                                  void (*aciParamUpdAck_func)(void))
{
  aci->ParamUpdateAck = aciParamUpdAck_func;
}

int aciLoadList(struct ACI_MASTER* aci)
{
	unsigned char buffer[sizeof(struct ACI_MEM_TABLE_ENTRY)];

	int i;

	int x64bugfix = 0; // where the files are different between 32 and 64 bit
	if(sizeof(void*) != 4) {
		x64bugfix = 4;
	}

	aci->MemVarTableCurrent = aci->MemVarTableStart;
	aci->MemCmdTableCurrent = aci->MemCmdTableStart;
	aci->MemParamTableCurrent = aci->MemParamTableStart;

	for (i = 0; i < aci->MagicCodeVarLoaded; i++)
	{
		aci->MemVarTableCurrent->next = malloc(sizeof(struct ACI_MEM_VAR_TABLE));
		aci->MemVarTableCurrent = aci->MemVarTableCurrent->next;
		int read = aci->ReadHDC(buffer, sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);

		if (read != sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix)
		{
			DEBUGOUT("Error: ACI Var Mem Table Read from Cache failed: Could not retrieve a full table entry!");

			return 1;
		}

		memcpy(&(aci->MemVarTableCurrent->tableEntry), &buffer[0],
				sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);
		aci->MemVarTableCurrent->next = NULL;
	}

	for (i = 0; i < aci->MagicCodeCmdLoaded; i++)
	{
		aci->MemCmdTableCurrent->next = malloc(sizeof(struct ACI_MEM_VAR_TABLE));
		aci->MemCmdTableCurrent = aci->MemCmdTableCurrent->next;
		int read = aci->ReadHDC(buffer, sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);

		if (read != sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix)
		{
			DEBUGOUT("Error: ACI Cmd Mem Table Read from Cache failed: Could not retrieve a full table entry!");

			return 1;
		}

		memcpy(&(aci->MemCmdTableCurrent->tableEntry), &buffer[0],
				sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);
		aci->MemCmdTableCurrent->next = NULL;
	}

	for (i = 0; i < aci->MagicCodeParLoaded; i++) {
		aci->MemParamTableCurrent->next = malloc(sizeof(struct ACI_MEM_VAR_TABLE));
		aci->MemParamTableCurrent = aci->MemParamTableCurrent->next;
		int read = aci->ReadHDC(buffer, sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);

		if (read != sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix) {
			DEBUGOUT("Error: ACI Param Mem Table Read from Cache failed: Could not retrieve a full table entry!");
			return 1;
		}

		memcpy(&(aci->MemParamTableCurrent->tableEntry), &buffer[0],
				sizeof(struct ACI_MEM_TABLE_ENTRY) - x64bugfix);
		aci->MemParamTableCurrent->next = NULL;

	}

	return 0;
}
#endif
/*
 *
 * ACI Helper functions
 *
 *
 */

unsigned short aciCrcUpdate(unsigned short crc, unsigned char data)
{
  data ^= (crc & 0xff);
  data ^= data << 4;

  return ((((unsigned short)data << 8) | ((crc >> 8) & 0xff)) ^ (unsigned char)(
            data >> 4)
          ^ ((unsigned short)data << 3));
}

unsigned short aciUpdateCrc16(unsigned short crc, void* data,
                              unsigned short cnt)
{
  unsigned short crcNew = crc;
  unsigned char* chrData = (unsigned char*)data;
  int i;

  for (i = 0; i < cnt; i++) {
    crcNew = aciCrcUpdate(crcNew, chrData[i]);
  }

  return crcNew;
}

// Returns nonzero upon fail, see addToTxBufferReturn_t
uint8_t addToTxBuffer(struct ACI_MASTER* aci, uint8_t* data, uint16_t len)
{
	if(aci->TxDataBufferMutex_blocked)
	{
		printf("BLOCKED BEFORE COMMAND \n" );
		fflush(stdout);
		return ADD_TO_TX_BUFFER_RETURN_MUTEX_BLOCKED;
	}

#ifndef DISABLE_MUTEXING
	aci->TxDataBufferMutex_blocked = 1;
#endif // DISABLE_MUTEXING

	if(aci->TxDataBufferWritePos + len > sizeof(aci->TxDataBuffer))
	{


		return ADD_TO_TX_BUFFER_RETURN_BUFFER_FULL;
	}

	memcpy(&(aci->TxDataBuffer[aci->TxDataBufferWritePos]), data, len);
	aci->TxDataBufferWritePos += len;

	aci->TxDataBufferMutex_blocked = 0;

	return ADD_TO_TX_BUFFER_RETURN_SUCCESS;
}

#ifdef __cplusplus

}
#endif

