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

#ifndef __LIDAR_TXT_POINT_WRITER_H__
#define __LIDAR_TXT_POINT_WRITER_H__

#include "lidar/SimplePointWriter.h"
#include "lidar/Stream.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * TXTPointWriter writes LiDAR-based Text files.
 *
 * The TXTPointWriter class writes point clouds to text files.  Each line of
 * the text file is point with the fields delimited by white spaces.
 */
class TXTPointWriter : public SimplePointWriter
{
   CONCRETE_OBJECT(TXTPointWriter);
public:
   /**
    * Initialize with input PointSource and output filename and format string.
    *
    * This method initializes the writer with a intput PointSource and output 
    * filename and format string.  The format string defines the order of the
    * text columns.
    *
    * \param src the input PointSource
    * \param path the output filename
    * \param format the channel layout format string,
    *      see TXTPointReader::parseString().
    */
   void init(const PointSource *src, const char *path, const char *format);
   /**
    * Initialize with input PointSource and output IO object and format string.
    *
    * This method initializes the writer with a intput PointSource and output 
    * IO object and format string.  The format string defines the order of the
    * text columns.
    *
    * \param src the input PointSource
    * \param path the output filename
    * \param format the channel layout format string,
    *      see TXTPointReader::parseString().
    */
   void init(const PointSource *src, IO *io, const char *format);

   /**
    * Initialize with input PointSource and output filename and field list.
    *
    * This method initializes the writer with a intput PointSource and output 
    * filename and field list.  The field list defines the order of the text
    * columns.
    *
    * \param src the input PointSource
    * \param path the output filename
    * \param fieldInfo a ChannelInfo array describing the column layout
    * \param numFields the number of fields
    */
   void init(const PointSource *src, const char *path, const PointInfo &fieldInfo);
   /**
    * Initialize with input PointSource and output IO object and field list.
    *
    * This method initializes the writer with a intput PointSource and output 
    * IO object and field list.  The field list defines the order of the text
    * columns.
    *
    * \param src the input PointSource
    * \param path the output filename
    * \param fieldInfo a ChannelInfo array describing the column layout
    * \param numFields the number of fields
    */
   void init(const PointSource *src, IO *io, const PointInfo &fieldInfo);

   void writeBegin(const PointInfo &pointInfo);
   void writePoints(const PointData &points,
                    size_t numPoints,
                    ProgressDelegate *delegate);
   void writeEnd(PointSource::count_type numPoints,
                 const Bounds &bounds);

   bool supportedChannels(const PointInfo &inputPointInfo,
                          PointInfo &supportedPointInfo) const;

protected:
   struct Handler;

   StreamWriter m_stream;
   size_t m_numHandlers;
   Handler *m_handler;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_TXT_POINT_WRITER_H__
