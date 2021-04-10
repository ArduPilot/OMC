/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008-2010 LizardTech, Inc, 1008 Western      //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef __LIDAR_ENDIAN_H__
#define __LIDAR_ENDIAN_H__

#include "lidar/Base.h"

LT_BEGIN_LIDAR_NAMESPACE

/** Use this macro when you need to know the host is big endian */
#define HOST_IS_BIG_ENDIAN (Endian().isHostBigEndian())
/** Use this macro when you need to know the host is little endian */
#define HOST_IS_LITTLE_ENDIAN (Endian().isHostLittleEndian())

/**
 * Endian is a helper class that figures out the host's byte order.
 *
 * The Endian class is used to figure out if the host is big or little endian
 * and swap between byte orders.
 */
class Endian
{
public:
   Endian(void) : word(1) {}
   
   /**
    * Determine if the host is big endian.
    *
    * This method returns true when the host is big endian.  The compiler
    * should be able the optimize this function call away.
    */
   bool isHostBigEndian(void) const { return byte[sizeof(int) - 1] == 1; }
   /**
    * Determine if the host is little endian.
    *
    * This method returns true when the host is little endian.  The compiler
    * should be able the optimize this function call away.
    */
   bool isHostLittleEndian(void) const { return byte[0] == 1; }

   
   /**
    * Swap byte order.
    *
    * This method swaps the byte order of the buffer.
    *
    * \param size the length of the buffer (in most cases it is a power of 2)
    * \param buffer the data to be swapped
    */
   template<size_t size> static void swap(void *buffer)
   {
      unsigned char *head = static_cast<unsigned char *>(buffer);
      unsigned char *tail = head + size - 1;
      while(head < tail)
      {
         unsigned char temp = *head;
         *head++ = *tail;
         *tail-- = temp;
      }
   }

   /**
    * Swap byte order.
    *
    * This method swaps the byte order of the buffer.
    *
    * \param buffer the data to be swapped
    * \param size the length of the buffer (in most cases it is a power of 2)
    */
   static void swap(void *buffer, size_t size)
   {
      unsigned char *head = static_cast<unsigned char *>(buffer);
      unsigned char *tail = head + size - 1;
      while(head < tail)
      {
         unsigned char temp = *head;
         *head++ = *tail;
         *tail-- = temp;
      }
   }

protected:
   const union { int word; char byte[sizeof(int)]; };
};


LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_ENDIAN_H__
