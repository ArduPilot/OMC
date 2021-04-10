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

#ifndef __LIDAR_STREAM_H__
#define __LIDAR_STREAM_H__

#include "lidar/IO.h"
#include "lidar/Endian.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * Stream is the base class for buffered input and output for IO objects.
 */
class Stream
{
   SIMPLE_OBJECT(Stream);
public:
   /**
    * Default Buffer Size (4096).
    */
   enum { DefaultBufferSize = 1 << 12 };

   /**
    * Seek offset origin.
    */
   enum Mode
   {
      /** seek from the begining of the file */
      MODE_SET = 0,
      /** seek from the current location in the file */
      MODE_CUR = 1,
      /** seek from the end of the file */
      MODE_END = 2
   };

   /**
    * Integer data type for seek() and tell() offsets.
    */
   typedef IO::offset_type offset_type;

   /**
    * Get the underlying IO object 
    */
   IO *getIO();

protected:
   ~Stream(void);
   Stream(void);

   typedef unsigned char byte_t;

   IO *m_io;
   size_t m_size;
   offset_type m_pos;
   byte_t *m_head;
   byte_t *m_cur;
   byte_t *m_tail;
};

/**
 * StreamReader implements buffered reads from IO objects.
 *
 * \see See examples/src/support.cpp compareTXTFiles() for an example of
 * reading a file.
 */
class StreamReader : public Stream
{
   SIMPLE_OBJECT(StreamReader);
public:
   ~StreamReader(void);
   StreamReader(void);

   /**
    * Initalize the reader.
    *
    * This method binds the stream to a IO object.
    *
    * \param io the source IO object
    * \param open if true init() will call open()
    * \param bufferSize the buffer size that is passed to open()
    */
   void init(IO *io, bool open, size_t bufferSize = DefaultBufferSize);
   /**
    * Initalize the reader.
    *
    * This method binds the stream to a IO object and seeks to the given offset.
    *
    * \param location the source IO object and the begining offset
    * \param bufferSize the buffer size that is passed to open()
    */
   void init(const IO::Location &location,
             size_t bufferSize = DefaultBufferSize);

   /**
    * Open the IO object.
    *
    * This method opens the underlying IO object.
    *
    * \param bufferSize the size of buffered reads
    */
   void open(size_t bufferSize = DefaultBufferSize);

   /**
    * Close the IO object.
    *
    * This method closes the underlying IO object.
    */
   void close(void);

   /**
    * Flush the memory buffer.
    *
    * This method disregard any data in the buffer and causes the next read()
    * to get dat from the underlying IO object.
    */
   void flush(void);
   
   /**
    * Set the file offset.
    *
    * This method sets the file offset.  See Mode for offset origin.
    *
    * \param offset number bytes to move
    * \param whence the origin of movement
    */
   void seek(offset_type offset, Mode whence);
   
   /**
    * Get the current file offset.
    *
    * This method get the current file offset.
    */
   offset_type tell(void);

   /**
    * Read data.
    *
    * This method reads nbytes into the given buffer.
    *
    * \param buf the destination buffer
    * \param nbytes the number of bytes to read
    * \return the number bytes actually read
    */
   size_t read(void *buf, size_t nbytes);

   /**
    * Read little endian data.
    *
    * This method reads sizeof(TYPE) bytes and endian swaps as needed.
    *
    * \param value the destination
    * \return true for success
    */
   template<typename TYPE> bool get_le(TYPE &value)
   {
      size_t nbytes = read(&value, sizeof(TYPE));
      if(HOST_IS_BIG_ENDIAN)
         Endian::swap<sizeof(TYPE)>(&value);
      return nbytes == sizeof(TYPE);
   }

   /**
    * Read big endian data.
    *
    * This method reads sizeof(TYPE) bytes and endian swaps as needed.
    *
    * \param value the destination
    * \return true for success
    */
   template<typename TYPE> bool get_be(TYPE &value)
   {
      size_t nbytes = read(&value, sizeof(TYPE));
      if(HOST_IS_LITTLE_ENDIAN)
         Endian::swap<sizeof(TYPE)>(&value);
      return nbytes == sizeof(TYPE);
   }

   /**
    * Read the next line of text.
    *
    * This method reads the next line of the file. It uses '\n' as the line
    * terminator.  This method also allocates memory as needed and it is the
    * responsible of the caller to DEALLOC() the returned line buffer.
    *
    * \param line pointer to the line buffer
    * \param length the length of the line buffer
    * \return true for success
    */
   bool get_str(char *&line, size_t &length);
};

/**
 * StreamWriter implements buffered writes to IO objects.
 */
class StreamWriter : public Stream
{
   SIMPLE_OBJECT(StreamWriter);
public:
   ~StreamWriter(void);
   StreamWriter(void);

   /**
    * Initalize the writer.
    *
    * This method binds the stream to a IO object.
    *
    * \param io the destination IO object
    * \param open if true init() will call open()
    * \param bufferSize the buffer size that is passed to open()
    */
   void init(IO *io, bool open, size_t bufferSize = DefaultBufferSize);

   /**
    * Open the IO object.
    *
    * This method opens the underlying IO object,
    *
    * \param bufferSize the size of buffered writes
    */
   void open(size_t bufferSize = DefaultBufferSize);
   
   /**
    * Close the IO object.
    *
    * This method flushs any buffered data if doFlush is true and then closes
    * the underlying IO object.
    */
   void close(bool doFlush = true);

   /**
    * Flush the memory buffer.
    *
    * This method wirtes any buffered data to the IO object.
    */
   void flush(void);
   
   /**
    * Set the file offset.
    *
    * This method flushs any beffered data then sets the file offset.  See
    * Mode for offset origin.
    *
    * \param offset number bytes to move
    * \param whence the origin of movement
    */
   void seek(offset_type offset, Mode whence);
   
   /**
    * Get the current file offset.
    *
    * This method get the current file offset.
    */
   offset_type tell(void);

   /**
    * Write data.
    *
    * This method writes nbytes from the given buffer.
    *
    * \param buf the source buffer
    * \param nbytes the number of bytes to write
    * \return the number bytes actually written
    */
   size_t write(const void *buf, size_t nbytes);

   /**
    * Write little endian data.
    *
    * This method writes sizeof(TYPE) bytes in little endian format.
    *
    * \param value the source value
    * \return true for success
    */
   template<typename TYPE> bool put_le(TYPE value)
   {
      if(HOST_IS_BIG_ENDIAN)
         Endian::swap<sizeof(TYPE)>(&value);
      return write(&value, sizeof(TYPE)) == sizeof(TYPE);
   }

   /**
    * Write little endian data.
    *
    * This method writes sizeof(TYPE) bytes in big endian format.
    *
    * \param value the source value
    * \return true for success
    */
   template<typename TYPE> bool put_be(TYPE value)
   {
      if(HOST_IS_LITTLE_ENDIAN)
         Endian::swap<sizeof(TYPE)>(&value);
      return write(&value, sizeof(TYPE)) == sizeof(TYPE);
   }

   /**
    * Write a string.
    *
    * This method writes the string with given length.
    *
    * \param str the source string
    * \param length the number of bytes to write if length is -1 put_str() uses
    *    the null terminator length.
    * \return true for success
    */
   bool put_str(const char *str, size_t length = static_cast<size_t>(-1));

   /**
    * Copy data from an IO object
    *
    * This method copies length bytes for io at offset.
    *
    * \param io the source IO object
    * \param offset the byte offset to source data
    * \param length the number of bytes to copy
    * \return true for success
    */
   bool copy(IO *io, offset_type offset, offset_type length);
   
   /**
    * Copy data from an IO object
    *
    * This method copies the data pointed to by location.
    *
    * \param location the location of the data
    * \return true for success
    */
   bool copy(IO::Location &location);

   /**
    * Copy data from an ReaderStream
    *
    * This method copies the next length bytes from stream
    *
    * \param stream the source stream
    * \param length the number of bytes to copy
    * \return true for success
    */
   bool copy(StreamReader &stream, offset_type length);
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_STREAM_H__
