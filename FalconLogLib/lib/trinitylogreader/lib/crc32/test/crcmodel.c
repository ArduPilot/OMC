
/******************************************************************************/
/*                             Start of crcmodel.c                            */
/******************************************************************************/
/*                                                                            */
/* Author : Ross Williams (ross@guest.adelaide.edu.au.).                      */
/* Date   : 3 June 1993.                                                      */
/* Status : Public domain.                                                    */
/*                                                                            */
/* Description : This is the implementation (.c) file for the reference       */
/* implementation of the Rocksoft^tm Model CRC Algorithm. For more            */
/* information on the Rocksoft^tm Model CRC Algorithm, see the document       */
/* titled "A Painless Guide to CRC Error Detection Algorithms" by Ross        */
/* Williams (ross@guest.adelaide.edu.au.). This document is likely to be in   */
/* "ftp.adelaide.edu.au/pub/rocksoft".                                        */
/*                                                                            */
/* Note: Rocksoft is a trademark of Rocksoft Pty Ltd, Adelaide, Australia.    */
/*                                                                            */
/******************************************************************************/
/*                                                                            */
/* Implementation Notes                                                       */
/* --------------------                                                       */
/* To avoid inconsistencies, the specification of each function is not echoed */
/* here. See the header file for a description of these functions.            */
/* This package is light on checking because I want to keep it short and      */
/* simple and portable (i.e. it would be too messy to distribute my entire    */
/* C culture (e.g. assertions package) with this package.                     */
/*                                                                            */
/******************************************************************************/

#include "crcmodel.h"

/******************************************************************************/

/* The following definitions make the code more readable. */

#define BITMASK(X) (1L << (X))
#define MASK32 0xFFFFFFFFL
#define LOCAL static

/******************************************************************************/

LOCAL ulong reflect(ulong v, int b)
/* Returns the value v with the bottom b [0,32] bits reflected. */
/* Example: reflect(0x3e23L,3) == 0x3e26                        */
{
 int   i;
 ulong t = v;
 for (i=0; i<b; i++)
   {
    if (t & 1L)
       v|=  BITMASK((b-1)-i);
    else
       v&= ~BITMASK((b-1)-i);
    t>>=1;
   }
 return v;
}

/******************************************************************************/

LOCAL ulong widmask(p_cm_t p_cm)
/* Returns a longword whose value is (2^p_cm->cm_width)-1.     */
/* The trick is to do this portably (e.g. without doing <<32). */
{
 return (((1L<<(p_cm->cm_width-1))-1L)<<1)|1L;
}

/******************************************************************************/

void cm_ini(p_cm_t p_cm)
{
 p_cm->cm_reg = p_cm->cm_init;
}

/******************************************************************************/

void cm_nxt(p_cm_t p_cm, int ch)
{
 int   i;
 ulong uch  = (ulong) ch;
 ulong topbit = BITMASK(p_cm->cm_width-1);

 if (p_cm->cm_refin) uch = reflect(uch,8);
 p_cm->cm_reg ^= (uch << (p_cm->cm_width-8));
 for (i=0; i<8; i++)
   {
    if (p_cm->cm_reg & topbit)
       p_cm->cm_reg = (p_cm->cm_reg << 1) ^ p_cm->cm_poly;
    else
       p_cm->cm_reg <<= 1;
    p_cm->cm_reg &= widmask(p_cm);
   }
}

/******************************************************************************/

void cm_blk(p_cm_t   p_cm, p_ubyte_ blk_adr, ulong    blk_len)
{
 while (blk_len--) cm_nxt(p_cm,*blk_adr++);
}

/******************************************************************************/

ulong cm_crc(p_cm_t p_cm)
{
 if (p_cm->cm_refot)
    return p_cm->cm_xorot ^ reflect(p_cm->cm_reg,p_cm->cm_width);
 else
    return p_cm->cm_xorot ^ p_cm->cm_reg;
}

/******************************************************************************/

ulong cm_tab(p_cm_t p_cm, int    index)
{
 int   i;
 ulong r;
 ulong topbit = BITMASK(p_cm->cm_width-1);
 ulong inbyte = (ulong) index;

 if (p_cm->cm_refin) inbyte = reflect(inbyte,8);
 r = inbyte << (p_cm->cm_width-8);
 for (i=0; i<8; i++)
    if (r & topbit)
       r = (r << 1) ^ p_cm->cm_poly;
    else
       r<<=1;
 if (p_cm->cm_refin) r = reflect(r,p_cm->cm_width);
 return r & widmask(p_cm);
}

/******************************************************************************/
/*                             End of crcmodel.c                              */
/******************************************************************************/


unsigned int CRC_CalcBlockCRC(ulword *buffer, ulword bytes)
{
cm_t        crc_model;
ulword      word_to_do;

    // Values for the STM32F generator.

    crc_model.cm_width = 32;            // 32-bit CRC
    crc_model.cm_poly  = 0x04C11DB7;    // CRC-32 polynomial
    crc_model.cm_init  = 0xFFFFFFFF;    // CRC initialized to 1's
    crc_model.cm_refin = FALSE;         // CRC calculated MSB first
    crc_model.cm_refot = FALSE;         // Final result is not bit-reversed
    crc_model.cm_xorot = 0x00000000;    // Final result XOR'ed with this

    cm_ini(&crc_model);

    if(bytes == 0)
        return 0;

    int words  = (bytes + 3) / 4;

    while (--words)
    {
        // The STM32F10x hardware does 32-bit words at a time!!!

        wordCrc(&crc_model, *buffer++);
    }

    word_to_do = *buffer++;
    switch(bytes % 4)
    {
    case 1:
        word_to_do &= 0x000000FF;
        break;
    case 2:
        word_to_do &= 0x0000FFFF;
        break;
    case 3:
        word_to_do &= 0x00FFFFFF;
        break;
    }
    wordCrc(&crc_model, word_to_do);

    // Return the final result.
    return (cm_crc(&crc_model));
}

void wordCrc(p_cm_t crc_model, ulword word_to_do)
{
    ubyte       byte_to_do;
    // Do all bytes in the 32-bit word.

    for (unsigned int i = 0; i < sizeof(word_to_do); i++)
    {
        // We calculate a *byte* at a time. If the CRC is MSB first we
        // do the next MS byte and vica-versa.

        if (crc_model->cm_refin == FALSE)
        {
            // MSB first. Do the next MS byte.

            byte_to_do = (ubyte) ((word_to_do & 0xFF000000) >> 24);
            word_to_do <<= 8;
        }
        else
        {
            // LSB first. Do the next LS byte.

            byte_to_do = (ubyte) (word_to_do & 0x000000FF);
            word_to_do >>= 8;
        }

        cm_nxt(crc_model, byte_to_do);
    }
}
