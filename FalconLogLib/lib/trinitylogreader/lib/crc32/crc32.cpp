/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include "crc32.h"

Crc32::Crc32()
{
  cfg_.init = 0xFFFFFFFF;
  cfg_.polynomial = 0x04C11DB7;
  crc_ = cfg_.init;
  generateLookup();
}

Crc32::Crc32(uint32_t init, uint32_t polynomial)
{
  crc_ = init;
  cfg_.init = init;
  cfg_.polynomial = polynomial;
  generateLookup();
}

void Crc32::addData(const void* data, size_t length)
{
  size_t llwords = length      / 16;
  size_t lwords  = length % 16 /  8;
  size_t words   = length %  8 /  4;
  size_t bytes   = length %  4;

  const uint32_t* d = (const uint32_t*) data;

  while(llwords--)
  {
    uint32_t one   = *d++ ^ crc_;
    uint32_t two   = *d++;
    uint32_t three = *d++;
    uint32_t four  = *d++;
    crc_  = lookup_[ 0][(four       ) & 0xFF] ^
            lookup_[ 1][(four  >>  8) & 0xFF] ^
            lookup_[ 2][(four  >> 16) & 0xFF] ^
            lookup_[ 3][(four  >> 24) & 0xFF] ^
            lookup_[ 4][(three      ) & 0xFF] ^
            lookup_[ 5][(three >>  8) & 0xFF] ^
            lookup_[ 6][(three >> 16) & 0xFF] ^
            lookup_[ 7][(three >> 24) & 0xFF] ^
            lookup_[ 8][(two        ) & 0xFF] ^
            lookup_[ 9][(two   >>  8) & 0xFF] ^
            lookup_[10][(two   >> 16) & 0xFF] ^
            lookup_[11][(two   >> 24) & 0xFF] ^
            lookup_[12][(one        ) & 0xFF] ^
            lookup_[13][(one   >>  8) & 0xFF] ^
            lookup_[14][(one   >> 16) & 0xFF] ^
            lookup_[15][(one   >> 24) & 0xFF];
  }

  while(lwords--)
  {
    uint32_t one = *d++ ^ crc_;
    uint32_t two = *d++;
    crc_ =  lookup_[7][ one>>24        ] ^
            lookup_[6][(one>>16) & 0xFF] ^
            lookup_[5][(one>> 8) & 0xFF] ^
            lookup_[4][ one      & 0xFF] ^
            lookup_[3][ two>>24        ] ^
            lookup_[2][(two>>16) & 0xFF] ^
            lookup_[1][(two>> 8) & 0xFF] ^
            lookup_[0][ two      & 0xFF];
  }

  while(words--)
  {
    uint32_t one = *d++ ^ crc_;

    crc_ = lookup_[3][(one>>24) & 0xFF] ^
           lookup_[2][(one>>16) & 0xFF] ^
           lookup_[1][(one>> 8) & 0xFF] ^
           lookup_[0][(one)     & 0xFF];
  }

  if(bytes)
  {
    uint32_t lastWord = 0x0;
    switch(bytes) {
      case 1:
        lastWord |= *d & 0xFF;
        break;
      case 2:
        lastWord |= *d & 0xFFFF;
        break;
      case 3:
        lastWord |= *d & 0xFFFFFF;
        break;
      }

    uint32_t one = lastWord ^ crc_;
    crc_ = lookup_[3][(one>>24) & 0xFF] ^
           lookup_[2][(one>>16) & 0xFF] ^
           lookup_[1][(one>> 8) & 0xFF] ^
           lookup_[0][(one)     & 0xFF];
  }
}



void Crc32::generateLookup()
{
  for(uint32_t i = 0; i <= 0xFF; i++)
  {
    uint32_t r = i<<24;
    for (size_t j=0; j<8; j++)
      r = (r << 1) ^ ((r>>31 & 1) * cfg_.polynomial);
    lookup_[0][i] = r;

  }
  for(size_t i = 0; i <= 0xFF; i++)
  {
    for (size_t slice = 1; slice < 16; slice++)
    {
      lookup_[slice][i] = (lookup_[slice-1][i] << 8) ^
          lookup_[0][(lookup_[slice-1][i] >> 24) & 0xFF];
    }
  }
}
