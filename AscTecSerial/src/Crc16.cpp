/*
 * Crc16.cpp
 *
 *  Created on: Jan 27, 2017
 *      Author: mavinci
 */

#include "Crc16.h"

unsigned short crc_update(unsigned short crc, unsigned char data)
{
  data ^= (crc & 0xff);
  data ^= data << 4;

  return ((((unsigned short)data << 8) | ((crc >> 8) & 0xff)) ^ (unsigned char)(
            data >> 4)
          ^ ((unsigned short)data << 3));
}

unsigned short crc16(void* data, unsigned short cnt)
{
  unsigned short crcNew = 0xff;
  unsigned char* chrData = (unsigned char*)data;
  int i;

  for (i = 0; i < cnt; i++) {
    crcNew = crc_update(crcNew, chrData[i]);
  }

  return crcNew;
}


