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

#ifndef __LIDAR_POINT_DATA_H__
#define __LIDAR_POINT_DATA_H__

#include "lidar/Base.h"

LT_BEGIN_LIDAR_NAMESPACE
class ChannelData;
class PointData;

/**
 * Channel data types
 *
 * This enum is used to repersent the data type of channel samples.
 */
enum DataType
{
   // don't change the values they are serialized
   // all values must be less then 1 << 16

   DATATYPE_INVALID = 0x0000,

   DATATYPE_UINT8  = 0x0100,
   DATATYPE_SINT8  = 0x0101,
   DATATYPE_UINT16 = 0x0200,
   DATATYPE_SINT16 = 0x0201,
   DATATYPE_UINT32 = 0x0400,
   DATATYPE_SINT32 = 0x0401,
   DATATYPE_UINT64 = 0x0800,
   DATATYPE_SINT64 = 0x0801,

   DATATYPE_FLOAT32 = 0x0403,
   DATATYPE_FLOAT64 = 0x0803
};

/**
 * Helper functions for interaction with the DataType enum.
 */
struct DataTypeUtils
{
   /**
    * Determine if the datatype is signed.
    *
    * This function returns true if the data type is signed.
    *
    * \param dt the datatype value
    */
   static inline bool isSigned(DataType dt)    { return (dt & 0x1) == 0x1; }
   
   /**
    * Determine if the datatype is a floating point type.
    *
    * This function returns true if the data type is a floating point type.
    *
    * \param dt the datatype value
    */
   static inline bool isFloat(DataType dt)     { return (dt & 0x2) == 0x2; }
   
   /**
    * Get the data type size.
    *
    * This function returns the size of the data type in bytes.
    *
    * \param dt the datatype value
    * \return size of the data type in bytes
    */
   static inline int byteWidth(DataType dt)    { return (dt >> 8) & 0xFF; }
   
   /**
    * Get the string representation.
    *
    * This function returns the human readable representation of the data type.
    *
    * \param dt the datatype value
    * \return human readable string
    */
   static const char *toString(DataType dt);
   static DataType toDataType(const char *str);
};

/**
 *  The canonical names of channels.
 */
#define CHANNEL_NAME_X               "X"
#define CHANNEL_NAME_Y               "Y"
#define CHANNEL_NAME_Z               "Z"
#define CHANNEL_NAME_Intensity       "Intensity"
#define CHANNEL_NAME_ReturnNum       "ReturnNum"
#define CHANNEL_NAME_NumReturns      "NumReturns"
#define CHANNEL_NAME_ScanDir         "ScanDir"
#define CHANNEL_NAME_EdgeFlightLine  "EdgeFlightLine"
#define CHANNEL_NAME_ScannerChannel  "ScannerChannel"
#define CHANNEL_NAME_ClassId         "ClassId"
#define CHANNEL_NAME_ClassFlags      "ClassFlags"
#define CHANNEL_NAME_ScanAngle       "ScanAngle"
#define CHANNEL_NAME_UserData        "UserData"
#define CHANNEL_NAME_SourceId        "SourceId"
#define CHANNEL_NAME_GPSTime_Week    "GPSTime"
#define CHANNEL_NAME_GPSTime_Adjusted "GPSTime_Adjusted"
#define CHANNEL_NAME_Red             "Red"
#define CHANNEL_NAME_Green           "Green"
#define CHANNEL_NAME_Blue            "Blue"
#define CHANNEL_NAME_NearInfrared    "NearInfrared"

// Used with the TXTPointReader to skip a field
#define CHANNEL_NAME_Skip            "@Skip"



/**
 * ChannelInfo stores the basic properties of a channel.
 *
 * ChannelInfo stores the basic properties of a channel; its name, datatype and
 * bits of precision.
 */
class ChannelInfo
{
   SIMPLE_OBJECT(ChannelInfo);
public:
   ~ChannelInfo(void);
   ChannelInfo(void);

   void init(const char *name, DataType datatype, int bits, double quantization = 0);
   void init(const ChannelInfo &info);

   /**
    * Get the name
    *
    * This method returns the channels name.
    */
   const char *getName(void) const;

   /**
    * Get the data type
    *
    * This method returns the channels data type.
    */
   DataType getDataType(void) const;
   
   /**
    * Get the bit precision
    *
    * This functon returns the number of bits used in the data type.  For
    * floating point data types this value it the number of bits needed after
    * its been quantized.
    */
   size_t getBits(void) const;

   /**
    * Get the quantization scale
    *
    * This functon returns the quantization scale factor for floating data.
    */
   double getQuantization(void) const;
   void setQuantization(double value);

   bool operator==(const ChannelInfo &rhs) const;
   bool operator!=(const ChannelInfo &rhs) const { return !operator==(rhs); }

protected:
   char *m_name;
   DataType m_datatype;
   int m_bits;
   double m_quantization;
};

/**
 * PointInfo is a group of ChannelInfo objects.
 *
 * PointInfo is a group of ChannelInfo objects for specifying channels.
 *
 * \see See examples/src/UserTutorial.cpp for examples on setting up and using
 *  PointInfo.
 */
class PointInfo
{
   SIMPLE_OBJECT(PointInfo);
public:
   ~PointInfo(void);
   PointInfo(void);
   
   void init(size_t numChannels);
   void init(const PointInfo &pointInfo);
   void init(const PointData &pointData);

   void init(const PointInfo &pointInfo, const char * const *channels, size_t numChannels);
   void init(const PointData &pointData, const char * const *channels, size_t numChannels);

   void init(const PointInfo &pointInfo, const size_t *channels, size_t numChannels);
   void init(const PointData &pointData, const size_t *channels, size_t numChannels);


   /**
    * Get the number of channels.
    *
    * This method returns the number of channels.
    */
   size_t getNumChannels(void) const;
   
   /**
    * Access the channel info.
    *
    * This method returns the ChannelInfo for a given index.
    *
    * \param idx the index of the wanted channel
    * \return the channel info for the given channel
    */
   const ChannelInfo &getChannel(size_t idx) const;

   /**
    * Access the channel info.
    *
    * This method returns the ChannelInfo for a given index.
    *
    * \param idx the index of the wanted channel
    * \return the channel info for the given channel
    */
   ChannelInfo &getChannel(size_t idx);
   
   /**
    * Determine if there is a channel with a given name.
    *
    * The method determines if this object has a channel with the given name.
    *
    * \param name the channel name
    * \return true if the channel was found
    */
   bool hasChannel(const char *name) const;
   
   /**
    * Access the channel data.
    *
    * This method returns the ChannelInfo for a given name.
    *
    * \param name the name of the wanted channel
    * \return the channel data for the given channel
    */
   const ChannelInfo *getChannel(const char *name) const;

   /**
    * Access the channel data.
    *
    * This method returns the ChannelInfo for a given name.
    *
    * \param name the name of the wanted channel
    * \return the channel data for the given channel
    */
   ChannelInfo *getChannel(const char *name);
   
   /**
    * Get the index for a given channel.
    *
    * This method returns index of the given channel name.
    *
    * \param name the name of the wanted channel
    * \return the index for the given channel
    */
   size_t getChannelIndex(const char *name) const;
   
   /**
    * Make sure the PointInfo has X, Y, and Z channels.
    *
    * This function checks the PointInfo object for X, Y, and Z channels
    * and that their data type is DATATYPE_FLOAT64
    */
   bool hasValidXYZ(void) const;


   bool operator==(const PointInfo &rhs) const;
   bool operator!=(const PointInfo &rhs) const { return !operator==(rhs); }

protected:
   size_t m_numChannels;
   ChannelInfo *m_channel;
};

/**
 * ChannelData adds sample values to the ChannelInfo class.
 */
class ChannelData : public ChannelInfo
{
   DISABLE_COPY(ChannelData);
public:
   ~ChannelData(void);
   ChannelData(void);

   void init(const ChannelInfo &info, size_t numSamples);

   /**
    * Get the number of samples.
    *
    * This method returns the maximum number of samples that can be stored
    * in the object.
    */
   size_t getNumSamples(void) const;

   /**
    * Get the data buffer.
    *
    * This method returns the data buffer.  The caller must cast it to the
    * appropriate datatype.
    *
    * \see getDataType()
    */
   const void *getData(void) const;
   
   /**
    * Get the data buffer.
    *
    * This method returns the data buffer.  The caller must cast it to the
    * appropriate datatype.
    *
    * \see getDataType()
    */
   void *getData(void);

   /**
    * Copy samples between channels.
    *
    * \param dst the destination channel
    * \param dstOffset the first sample to overwrite
    * \param src the source channel
    * \param srcOffset the fisrt sample to copy
    * \param length the number of samples to copy
    *
    * \note The source and destination data types must be the same.  The
    *       source and destination may be the same object and sample
    *       ranges may overlap.
    */
   static void copy(ChannelData &dst, size_t dstOffset,
                    const ChannelData &src, size_t srcOffset,
                    size_t length);
   
   /**
    * Copy and convert the data type of samples between channel.
    *
    * This function does a datatype convertion as it copies the source samples
    * into the destination channel.  This function does not support changing
    * between integral and floating point types.
    *
    * \param dst the destination channel
    * \param dstOffset the first sample to overwrite
    * \param src the source channel
    * \param srcOffset the fisrt sample to copy
    * \param length the number of samples to copy
    *
    * \note The source and destination may not be the same object.
    */
   static void convert(ChannelData &dst, size_t dstOffset,
                       const ChannelData &src, size_t srcOffset,
                       size_t length);

   /**
    * Copy and convert the data type of samples between channel.
    *
    * This function does a datatype convertion as it copies the source samples
    * into the destination channel.  This function does support changing
    * between integral and floating point types.
    *
    * \param dst the destination channel
    * \param dstOffset the first sample to overwrite
    * \param src the source channel
    * \param srcOffset the fisrt sample to copy
    * \param offset the quantization offset
    * \param scale the quantization scale
    * \param length the number of samples to copy
    *
    * \note The source and destination may not be the same object.
    * \note When converting integer to float: dst = scale * src + offset.
    * \note When converting float to integer: dst = floor((src - offset) / scale + 0.5)
    */
   static void convert(ChannelData &dst, size_t dstOffset,
                       const ChannelData &src, size_t srcOffset,
                       double offset, double scale,
                       size_t length);

   /**
    * Channge the data type on the fly.
    *
    * This method is used to change the data type on the fly.  Use with
    * care and it should only be used went the new data type is smaller than
    * the old data type.
    *
    * \note internal LizardTech use only.
    */
   void setDataType(DataType datatype);

   /**
    * Resize the data buffer.
    *
    * This method change the size of the data buffer.
    * \param newNumSamples the new size of the buffer
    */
   void resize(size_t newNumSamples);

   /** dirty hack -- only use this if you're the buffer onwer */
   void setOffset(size_t offset);

   /** change the buffer */
   void setData(void *data, bool deleteData);

protected:
   size_t m_numSamples;
   size_t m_offset;
   void *m_data;
   bool m_deleteData;
};

/**
 * PointData is a group of ChannelData objects.
 *
 * PointData is a group of ChannelData objects for extracting the point cloud.
 *
 * \see See examples/src/UserTutorial.cpp for examples on setting up and using
 *  PointData.
 */
class PointData
{
   SIMPLE_OBJECT(PointData);
public:
   ~PointData(void);
   PointData(void);

   void init(const PointInfo &pointInfo, size_t numSamples);

   /**
    * Get the number of channels.
    *
    * This method returns the number of channels.
    */
   size_t getNumChannels(void) const;

   /**
    * Get the number of samples.
    *
    * This method returns the maximum number of samples that can be stored
    * in the object.
    */
   size_t getNumSamples(void) const;

   /**
    * Access the channel data.
    *
    * This method returns the ChannelData for a given index.
    *
    * \param idx the index of the channel wanted
    * \return the channel data for the given channel
    */
   const ChannelData &getChannel(size_t idx) const;
   
   /**
    * Access the channel data.
    *
    * This method returns the ChannelData for a given index.
    *
    * \param idx the index of the channel wanted
    * \return the channel data for the given channel
    */
   ChannelData &getChannel(size_t idx);

   /**
    * Determine if there is a channel with a given name.
    *
    * The method determines if this object has a channel with the given name.
    *
    * \param name the channel name
    * \return true if the channel was found
    */
   bool hasChannel(const char *name) const;

   /**
    * Access the channel data.
    *
    * This method returns the ChannelData for a given name.
    *
    * \param name the name of the channel wanted
    * \return the channel data for the given channel
    */
   const ChannelData *getChannel(const char *name) const;
   
   /**
    * Access the channel data.
    *
    * This method returns the ChannelData for a given name.
    *
    * \param name the name of the channel wanted
    * \return the channel data for the given channel
    */
   ChannelData *getChannel(const char *name);

   /**
    * Get the X values.
    *
    * This method returns a pointer to the X channel samples.
    */
   const double *getX(void) const;
   
   /**
    * Get the X values.
    *
    * This method returns a pointer to the X channel samples.
    */
   double *getX(void);
   
   /**
    * Get the Y values.
    *
    * This method returns a pointer to the Y channel samples.
    */
   const double *getY(void) const;
   
   /**
    * Get the Y values.
    *
    * This method returns a pointer to the Y channel samples.
    */
   double *getY(void);
   
   /**
    * Get the Z values.
    *
    * This method returns a pointer to the Z channel samples.
    */
   const double *getZ(void) const;
   
   /**
    * Get the Z values.
    *
    * This method returns a pointer to the Z channel samples.
    */
   double *getZ(void);

   /**
    * Copy samples between buffers.
    *
    * \param dst the destination buffer
    * \param dstOffset the first sample to overwrite
    * \param src the source buffer
    * \param srcOffset the fisrt sample to copy
    * \param length the number of samples to copy
    *
    * \note The source and destination data types must be the same.  The
    *       source and destination may be the same object and sample
    *       ranges may overlap.
    */
   static void copy(PointData &dst, size_t dstOffset,
                    const PointData &src, size_t srcOffset,
                    size_t length);

   /**
    * Merge points.
    *
    * This function merges the the source buffer into the destination buffer
    * in a manner the destination becomes a uniform sampling of both buffers.
    *
    * \param dst the destination buffer
    * \param dstNumPoint the number of point the destination buffer represents
    *        (it may be large than the buffer size)
    * \param src the source buffer
    * \param srcNumPoint the number of point the source buffer represents
    *        (it may be large than the buffer size)
    */
   static void merge(PointData &dst, size_t dstNumPoints,
                     const PointData &src, size_t srcNumPoints);

   /**
    * Resize the data buffer.
    *
    * This method change the size of the data buffer.
    * \param newNumSamples the new size of the buffer
    */
   void resize(size_t newNumSamples);

   /** dirty hack -- only use this if you're the buffer onwer */
   void setOffset(size_t offset);
protected:
   size_t m_numChannels;
   ChannelData *m_channel;
   size_t m_numSamples;
   size_t m_offset;

   // just pointers into m_channel[]
   double *m_x;
   double *m_y;
   double *m_z;
};

LT_END_LIDAR_NAMESPACE
#endif /* __LIDAR_POINT_DATA_H__ */
