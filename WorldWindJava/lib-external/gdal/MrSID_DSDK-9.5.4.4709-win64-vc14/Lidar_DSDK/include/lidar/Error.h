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

#ifndef __LIDAR_ERROR_H__
#define __LIDAR_ERROR_H__

#include "lidar/Base.h"
#include <exception>

LT_BEGIN_LIDAR_NAMESPACE

/** Use this to throw runtime-based errors */
#define THROW_LIBRARY_ERROR(code) \
   throw Error(__FILE__, __FUNCTION__, __LINE__, code)

/** Use this to throw errors in response to OS errors */
#define THROW_OS_ERROR() \
   throw OSError(__FILE__, __FUNCTION__, __LINE__)

/**
 * Error is the base class for all LiDAR SDK errors.
 */
class Error : public std::exception
{
public:
   ~Error(void) throw();
   Error(const Error &copy) throw();

   /**
    * Construct an error exception.
    *
    * \param file the file name where the error occurred
    * \param func the function name where the error occurred
    * \param line the line number where the error occurred
    * \param code the error code
    *
    */
   Error(const char *file, const char *func, int line, int code) throw();

   /**
    * Get the file name.
    *
    * This method returns the file name where the error occurred.
    */
   const char *filename(void) const throw();
   /**
    * Get the function name.
    *
    * This method returns the function name where the error occurred.
    */
   const char *function(void) const throw();
   /**
    * Get the line number.
    *
    * This method returns the line number where the error occurred.
    */
   int line(void) const throw();
   /**
    * Get the error code.
    *
    * This method returns the error code for the error.
    */
   int error(void) const throw();

   /**
    * Get the text description.
    *
    * This method returns a text discription of the error.
    */
   const char *what(void) const throw();

   /**
    * Append more text the error discription.
    *
    * This method append text to the error discription.  It uses the printf()
    * format syntax.
    *
    */
   Error &operator()(const char *fmt, ...) throw();

protected:
   enum { BUFFERSIZE = 1024 };
   char m_desc[BUFFERSIZE];
   const char *m_file;
   const char *m_func;
   int m_line;
   int m_error;
};

/**
 * OSError extends Error by using errno or GetLastError() to build the text
 * description of the error.
 */
class OSError : public Error
{
public:
   /**
    * Construct an OS error exception.
    *
    * This constructor looks at errno or GetLastError() to build the text
    * description.
    *
    * \param file the file name where the error occurred
    * \param func the function name where the error occurred
    * \param line the line number where the error occurred
    *
    */
   OSError(const char *file, const char *func, int line) throw();
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_ERROR_H__
