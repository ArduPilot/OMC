/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include <iostream>
#include <random>

#include "crc32.h"
#include "crcmodel.h"
#include "crcmodel.c"

unsigned int
reverse(register unsigned int x)
{
  x = (((x & 0xaaaaaaaa) >> 1) | ((x & 0x55555555) << 1));
  x = (((x & 0xcccccccc) >> 2) | ((x & 0x33333333) << 2));
  x = (((x & 0xf0f0f0f0) >> 4) | ((x & 0x0f0f0f0f) << 4));
  x = (((x & 0xff00ff00) >> 8) | ((x & 0x00ff00ff) << 8));
  return((x >> 16) | (x << 16));

}

uint32_t CRC32WideFast(uint32_t Crc, uint32_t Size, char *Buffer)
{
  Size = Size >> 2; // /4

  while(Size--)
  {
    static const uint32_t CrcTable[16] = { // Nibble lookup table for 0x04C11DB7 polynomial
      0x00000000,0x04C11DB7,0x09823B6E,0x0D4326D9,0x130476DC,0x17C56B6B,0x1A864DB2,0x1E475005,
      0x2608EDB8,0x22C9F00F,0x2F8AD6D6,0x2B4BCB61,0x350C9B64,0x31CD86D3,0x3C8EA00A,0x384FBDBD };

    Crc = Crc ^ *((uint32_t *)Buffer); // Apply all 32-bits

    Buffer += 4;

    // Process 32-bits, 4 at a time, or 8 rounds

    Crc = (Crc << 4) ^ CrcTable[Crc >> 28]; // Assumes 32-bit reg, masking index to 4-bits
    Crc = (Crc << 4) ^ CrcTable[Crc >> 28]; //  0x04C11DB7 Polynomial used in STM32
    Crc = (Crc << 4) ^ CrcTable[Crc >> 28];
    Crc = (Crc << 4) ^ CrcTable[Crc >> 28];
    Crc = (Crc << 4) ^ CrcTable[Crc >> 28];
    Crc = (Crc << 4) ^ CrcTable[Crc >> 28];
    Crc = (Crc << 4) ^ CrcTable[Crc >> 28];
    Crc = (Crc << 4) ^ CrcTable[Crc >> 28];
  }

  return(Crc);
}

int main(int argc, char *argv[])
{
  using namespace std;

  char endl = '\n';

  size_t numbytes = 100019;
  uint32_t data[100024];

  for(size_t i = 0; i < 100024; i++)
  {
      data[i] = rand() | rand()<<16;
  }

  uint32_t verifiedCrc = CRC_CalcBlockCRC((uint32_t*)data, numbytes);
  cout << "verified: " << std::hex << verifiedCrc << endl;
  cout << "testedde: " << std::hex << CRC32WideFast(0xFFFFFFFF, numbytes, (char*) data) << endl;

  Crc32 crc(0xFFFFFFFF, 0x04C11DB7);
  crc.addData(data, numbytes);

  cout << "calculat: " << std::hex << crc.getHash() << endl;


  cout << endl;
  return 0;
}
