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

#ifndef __LIDAR_TXT_POINT_READER_H__
#define __LIDAR_TXT_POINT_READER_H__

#include "lidar/PointReader.h"
#include "lidar/IO.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * TXTPointReader reads LiDAR-based Text files.
 *
 * The TXTPointReader class reads point clouds from text files.  Each line of
 * the text file is point with the fields delimited by white space.
 */
class TXTPointReader : public PointReader
{
   CONCRETE_OBJECT(TXTPointReader);
public:

   /**
    * Initialize with filename and format string.
    *
    * This method initializes the reader with a filename and format string.
    *
    * \param path the filename
    * \param format the channel layout format string,
    *      see TXTPointReader::parseString().
    * \param header the number of header lines to skip
    * \param doFullInit if true call initBoundsAndNumPoints()
    */
   void init(const char *path, const char *format,
             size_t header, bool doFullInit);
   /**
    * Initialize with IO object and format string.
    *
    * This method initializes the reader with a IO object and format string.
    *
    * \param io the data source
    * \param format the channel layout format string,
    *      see TXTPointReader::parseString().
    * \param header the number of header lines to skip
    * \param doFullInit if true call initBoundsAndNumPoints()
    */
   void init(IO *io, const char *format,
             size_t header, bool doFullInit);

   /**
    * Initialize with filename and field list.
    *
    * This method initializes the reader with a filename and field list.
    *
    * \param path the filename
    * \param fieldInfo a ChannelInfo array describing the column layout
    * \param header the number of header lines to skip
    * \param doFullInit if true call initBoundsAndNumPoints()
    *
    * \note Use CHANNEL_NAME_Skip to skip a column.
    */
   void init(const char *path, const PointInfo &fieldInfo,
             size_t header, bool doFullInit);
   /**
    * Initialize with IO object and field list.
    *
    * This method initializes the reader with IO object and field list.
    *
    * \param io the data source
    * \param fieldInfo a ChannelInfo array describing the column layout
    * \param header the number of header lines to skip
    * \param doFullInit if true call initBoundsAndNumPoints()
    *
    * \note Use CHANNEL_NAME_Skip to skip a column.
    */
   void init(IO *io, const PointInfo &fieldInfo,
             size_t header, bool doFullInit);

   /**
    * Setup the bounds and number of points.
    *
    * This method reads the input text file to figures out the number of points
    * and the bounding box.  This method can take some time.  If the method
    * has not be called getBounds() will return Bounds::Huge() and
    * getNumPoints() will return 2**64 - 1.
    */
   void initBoundsAndNumPoints(void);

   const char *getFileFormatString(void) const;

   void loadMetadata(Metadata &metadata, bool sanitize) const;

   double getTotalWork(const Bounds &bounds, double fraction) const;

   PointIterator *createIterator(const Bounds &bounds,
                                 double fraction,
                                 const PointInfo &pointInfo,
                                 ProgressDelegate *delegate) const;

   /**
    * Build a ChannelInfo array form a format string.
    *
    * The function builds a ChannelInfo array based on the format string.
    *
    * Format syntax:
    * - 'x' => CHANNEL_NAME_X, DATATYPE_FLOAT64
    * - 'y' => CHANNEL_NAME_Y, DATATYPE_FLOAT64
    * - 'z' => CHANNEL_NAME_Z, DATATYPE_FLOAT64
    * - 'i' => CHANNEL_NAME_Intensity, DATATYPE_UINT16
    * - 'r' => CHANNEL_NAME_ReturnNum, DATATYPE_UINT8
    * - 'n' => CHANNEL_NAME_NumReturns, DATATYPE_UINT8
    * - 'd' => CHANNEL_NAME_ScanDir, DATATYPE_UINT8
    * - 'e' => CHANNEL_NAME_EdgeFlightLine, DATATYPE_UINT8
    * - 'c' => CHANNEL_NAME_ClassId, DATATYPE_UINT8
    * - 'a' => CHANNEL_NAME_ScanAngle, DATATYPE_SINT8
    * - 'u' => CHANNEL_NAME_UserData, DATATYPE_UINT8
    * - 'p' => CHANNEL_NAME_SourceId, DATATYPE_UINT16
    * - 't' => CHANNEL_NAME_GPSTime_Week, DATATYPE_FLOAT64
    * - 'T' => CHANNEL_NAME_GPSTime_Adjusted, DATATYPE_FLOAT64
    * - 'R' => CHANNEL_NAME_Red, DATATYPE_UINT16
    * - 'G' => CHANNEL_NAME_Green, DATATYPE_UINT16
    * - 'B' => CHANNEL_NAME_Blue, DATATYPE_UINT16
    * - 's' => CHANNEL_NAME_Skip, Skip this field          
    *
    * \param format the format string
    * \param fieldInfo the ouptut ChannelInfo array
    */
   static void parseFormat(const char *format,
                           PointInfo &fieldInfo);
   /**
    * Build a ChannelInfo array form a format string.
    *
    * The function builds a ChannelInfo array based on the format string and
    * the use the given ChannelInfo array's data types.
    *
    * \param format the format string
    * \param fieldInfo the ouptut ChannelInfo array
    * \param pointInfo the input ChannelInfo array
    */
    static void parseFormat(const char *format,
                            PointInfo &fieldInfo,
                            const PointInfo &pointInfo);

   /**
    * Update the quantization values of a PointInfo.
    *
    * The function updates the quantization values of the PointInfo based
    * on the format string.
    *
    * Format syntax:
    *   <channel ids>@<quantization scale>[,<channel ids>@<quantization scale>]*
    *
    *  Where:
    *    <channel ids> is one or more of the following values:
    *       - 'x' => CHANNEL_NAME_X
    *       - 'y' => CHANNEL_NAME_Y
    *       - 'z' => CHANNEL_NAME_Z
    *       - 't' => CHANNEL_NAME_GPSTime_Week | CHANNEL_NAME_GPSTime_Adjusted
    *    <quantization scale> is a non-zero number
    *
    * Example:
    *    xyz@0.01,t@0.0000001
    *    x@0.01,y@0.01,z@0.01,t@0.0000001
    *    (both have the same effect)
    *
    * \param format the format string
    * \param fieldInfo the PointInfo for updating
    */

   static void parseQuantization(const char *format,
                                 PointInfo &fieldInfo);


protected:
   void setBoundsAndNumPoints(bool doFullInit);

   IO *m_io;
   size_t m_header;
   PointInfo m_fieldInfo;
   double m_totalWork;

   class Iterator;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_TXT_POINT_READER_H__
