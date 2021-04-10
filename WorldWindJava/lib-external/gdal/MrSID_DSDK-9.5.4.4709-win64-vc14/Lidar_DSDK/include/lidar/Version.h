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

#ifndef __LIDAR_VERSION_H__
#define __LIDAR_VERSION_H__

#include "lidar/IO.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * Functions for geting the SDK version and MrSID file version.
 */
struct Version
{
   /**
    * Get the SDK Version.
    *
    * This function gets the SDK version.
    *
    * \param major the Major version number
    * \param minor the Minor version number
    * \param age the Age version value
    * \param build the build number
    */
   static void getSDKVersion(int &major, int &minor, int &age, int &build)
   {
      major = getSDKMajorVersion();
      minor = getSDKMinorVersion();
      age = getSDKVersionAge();
      build = getSDKBuildNumber();
   }

   /**
    * Get the SDK's Major Version.
    */
   static int getSDKMajorVersion(void);
   /**
    * Get the SDK's Minor Version.
    */
   static int getSDKMinorVersion(void);
   /**
    * Get the SDK's Version Age.
    */
   static int getSDKVersionAge(void);
   /**
    * Get the SDK's Build Number.
    */
   static int getSDKBuildNumber(void);

   /**
    * Get the SDK Version String.
    *
    * This function returns the SDK version as a printable string.
    *
    * \return the version string
    */
   static const char * const getSDKVersionString(void);
   /**
    * Get when the SDK was built.
    *
    * This function returns when the SDK was built.
    *
    * \return a date string
    */
   static const char * const getSDKBuildDate(void);

   /**
    * Get the MrSID file version.
    *
    * This function gets the MrSID version of the given file.
    *
    * \param path the input filename
    * \param version gets set to the file's version number
    * \param raster gets set to true for Raster-based MrSID files and false for
    *     LiDAR-based MrSID files
    * \return true when the file is a MrSID file
    */
   static bool getMrSIDFileVersion(const char *path, int &version, bool &raster);
#ifdef _WIN32
   /**
    * Get the MrSID file version.
    *
    * This function gets the MrSID version of the given file.
    *
    * \param path the input filename
    * \param version gets set to the file's version number
    * \param raster gets set to true for Raster-based MrSID files and false for
    *     LiDAR-based MrSID files
    * \return true when the file is a MrSID file
    */
#ifdef SWIG
   static bool getMrSIDFileVersion(const wchar_t *path, int &version, bool &raster);
#else
   static bool getMrSIDFileVersion(const unsigned short *path, int &version, bool &raster);
   static bool getMrSIDFileVersion(const __wchar_t *path, int &version, bool &raster);
#endif
#endif
   /**
    * Get the MrSID file version.
    *
    * This function gets the MrSID version of the given IO object.
    *
    * \param io the source IO object
    * \param version gets set to the file's version number
    * \param raster gets set to true for Raster-based MrSID files and false for
    *     LiDAR-based MrSID files
    * \return true when the file is a MrSID file
    */
   static bool getMrSIDFileVersion(IO *io, int &version, bool &raster);
   /**
    * Get the MrSID file version.
    *
    * This function gets the MrSID version of the given file header.
    *
    * \param header the file's first 8 bytes
    * \param version gets set to the file's version number
    * \param raster gets set to true for Raster-based MrSID files and false for
    *     LiDAR-based MrSID files
    * \return true when the file is a MrSID file
    */
   static bool getMrSIDFileVersion(lt_uint8 header[8], int &version, bool &raster);
};

LT_END_LIDAR_NAMESPACE

#endif // __LIDAR_VERSION_H__
