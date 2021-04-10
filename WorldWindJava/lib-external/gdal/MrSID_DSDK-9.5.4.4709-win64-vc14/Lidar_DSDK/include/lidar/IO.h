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

#ifndef __LIDAR_IO_H__
#define __LIDAR_IO_H__

#include "lidar/Object.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * IO is the base class for binary input and output.
 *
 * The IO class was designed to thread safe for reading and writing.  The
 * reading and writing functions do not have a internal file position, the 
 * offset is passed into pread() and pwrite().  This is based off the POSIX 
 * pread() and pwrite() interfaces.  Unlike the libc's FILE structure the
 * buffering is not part of the IO object.  Buffering is handled by the 
 * StreamReader and the StreamWriter classes.
 */
class IO : public Object
{
   ABSTRACT_OBJECT(IO);
public:
   /**
    * Integer data type for file offsets and sizes.
    */
   typedef lt_int64 offset_type;

   /**
    * Open the IO object.
    *
    * This method opens the IO object.  open() and close() are reference
    * counted so you must call close() the some number of times as open().
    *
    * When subclassing IO, open() and close() must implement the reference
    * counting and be thread safe because they can be called from multiple
    * threads.  open() should look something like the following:
    * \code{.cpp}
      void open(void)
      {
         MutexMonitor mon(m_openLock);
         if (m_openCount == 0)
         {
            // open the resource
         }
         m_openCount += 1;
      }
      \endcode
    */
   virtual void open(void) = 0;
   /**
    * Close the IO object.
    *
    * This method closes the IO object.  open() and close() are reference
    * counted so you must call close() the some number of times as open().
    *
    * When subclassing IO, open() and close() must implement the reference
    * counting and be thread safe because they can be called from multiple
    * threads.  close() should look something like the following:
    * \code{.cpp}
      void close(void)
      {
         MutexMonitor mon(m_openLock);
         m_openCount -= 1;
         if (m_openCount == 0)
         {
            // close the resource
         }
      }
      \endcode
    */
   virtual void close(void) = 0;

   /**
    * Read data.
    *
    * This method tries to read nbytes bytes at the given offset.
    *
    * \param offset the file offset to read from
    * \param buffer the destination memory location
    * \param nbytes the number of bytes to read
    * \return the number of bytes read
    */
   virtual size_t pread(offset_type offset,
                        void *buffer,
                        size_t nbytes) const = 0;
   /**
    * Write data.
    *
    * This method tries to write nbytes bytes at the given offset.
    *
    * \param offset the file offset to write to
    * \param buffer the source memory location
    * \param nbytes the number of bytes to write
    * \return the number of bytes written
    */
   virtual size_t pwrite(offset_type offset,
                         const void *buffer,
                         size_t nbytes) const = 0;

   /**
    * Get the size of the resource.
    *
    * This method returns the size of the resource.
    */
   virtual offset_type size(void) const = 0;

   /**
    * Set the size of the resource.
    *
    * This method sets the size of the resource.  If the new size is smaller
    * the data is lost.  If the new size is larger the resource padded with
    * zeros.
    *
    * \note The object must be open.
    */
   virtual void truncate(offset_type length) = 0;

   /**
    * Delete the resource when the IO object is deleted.
    *
    * This method marks the resource for deletion when the object goes away.
    */
   virtual void unlink(void) = 0;


   /**
    * Location is a helper structure for holding the location of data in a IO
    * object.
    */
   struct Location
   {
      IO *io;
      offset_type offset;
      offset_type length;

      ~Location(void);

      Location(IO *io = NULL, offset_type offset = 0, offset_type length = 0);
      Location(const Location &rhs);
      Location &operator=(const Location &rhs);

      void set(IO *io, offset_type offset, offset_type length);
   };
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_IO_H__
