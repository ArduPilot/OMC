/*
 * Crc16.h
 *
 *  Created on: Jan 27, 2017
 *      Author: mavinci
 */

unsigned short crc_update(unsigned short crc, unsigned char data);

unsigned short crc16(void* data, unsigned short cnt);
