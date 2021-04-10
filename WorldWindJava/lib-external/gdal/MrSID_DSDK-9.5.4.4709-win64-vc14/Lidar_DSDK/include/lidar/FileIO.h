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

#ifndef __LIDAR_FILE_IO_H__
#define __LIDAR_FILE_IO_H__

#include "lidar/IO.h"
#include "lidar/Mutex.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * FileIO implements file-based IO.
 *
 * The FileIO class implements a file-based IO object.  It uses the pread() and
 * pwrite() on POSIX systems and ReadFile() and WriteFile() with overlaps on
 * Windows systems.
 *
 * \see See examples/src/support.cpp compareFiles() for an example of reading
 * a file.
 */
class FileIO : public IO
{
   CONCRETE_OBJECT(FileIO);
public:

   /**
    * Initialize with the file path and access mode.
    *
    * This method initializes the FileIO object with a filename path and
    * access mode.
    *
    * Modes:
    * - "r": Open a file for reading.
    * - "r+": Open a file for reading and writing.
    * - "w": Truncate or create a file for writing.
    * - "w+": Truncate or create a file for reading and writing.
    *
    * \param path the filename path (UTF-8 encoded)
    * \param mode the file access mode
    *
    */
   void init(const char *path, const char *mode);
#ifdef _WIN32
   /**
    * Initialize with the file path and access mode.
    *
    * This method initializes the FileIO object with a filename path and
    * access mode.
    *
    * Modes:
    * - "r": Open a file for reading.
    * - "r+": Open a file for reading and writing.
    * - "w": Truncate or create a file for writing.
    * - "w+": Truncate or create a file for reading and writing.
    *
    * \param path the filename path
    * \param mode the file access mode
    *
    */
#ifdef SWIG
   void init(const wchar_t *path, const char *mode);
#else
   void init(const unsigned short *path, const char *mode);
   void init(const __wchar_t *path, const char *mode);
#endif
#endif

   /**
    * Create a temporary file for readeing and writing.
    *
    * This method creates temporary file for reading and writing.  When the 
    * FileIO object is deallocted the temporary file is deleted.  If tempdir
    * is NULL the default temporary directory is used.
    *
    * \param tempdir the location of the temporary directory (UTF-8 encode)
    */
   void init(const char *tempdir);
#ifdef _WIN32
   /**
    * Create a temporary file for readeing and writing.
    *
    * This method creates temporary file for reading and writing.  When the 
    * FileIO object is deallocted the temporary file is deleted.  If tempdir
    * is NULL the default temporary directory is used.
    *
    * \param tempdir the location of the temporary directory
    */
#ifdef SWIG
   void init(const wchar_t *tempdir);
#else
   void init(const unsigned short *tempdir);
   void init(const __wchar_t *tempdir);
#endif
#endif

   void unlink(void);

   /**
    * Delete a file
    *
    * This is utility function for deleting a file.
    *
    * \param path path to the file
    */
   static void deleteFile(const char *path);
   /**
    * Test if the file exists
    *
    * This is utility function for seeing if the file exists.
    *
    * \param path path to the file
    */
   static bool fileExists(const char *path);

protected:
   class Imp;
   Mutex m_openLock;
   int m_openCount;
   bool m_unlinkFile;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_FILE_IO_H__
