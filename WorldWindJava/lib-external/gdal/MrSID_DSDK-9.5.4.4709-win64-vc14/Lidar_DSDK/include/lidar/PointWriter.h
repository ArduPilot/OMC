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

#ifndef __LIDAR_POINT_WRITER_H__
#define __LIDAR_POINT_WRITER_H__

#include "lidar/PointSource.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * PointWriter is the base for writing LiDAR files
 *
 * The PointWriter is the base class for writing LiDAR point clouds.
 */
class PointWriter : public Object
{
   ABSTRACT_OBJECT(PointWriter);
public:
   typedef PointSource::count_type count_type;
   
   
   /**
    * Set the metadata.
    *
    * This method sets the metadata that will be written to the output 
    * file.  By default the metadata from the PointSource is not copied.
    *
    * \param metadata the source metadata
    */
   void setMetadata(const Metadata &metadata);
   
   /**
    * Get the Metadata
    *
    * This method returns the metadata that will be writen to the output
    * file.
    */
   Metadata &getMetadata(void);

   /**
    * Set the quantization.
    *
    * This method set the output files quantization values.
    *
    * \param scale the quantization scale factor (may be NULL)
    * \param offset the quantization offset (may be NULL)
    *
    * \note Some file format requier quantization, so make sure you call this
    *  if needed.
    * \note The default values are the input PointSource values.
    */
   virtual void setQuantization(const double scale[3], const double offset[3]) = 0;

   /**
    * Get the quantization scale.
    *
    * This method returns the quantization scale factors for X, Y, and
    * Z channels.  It returns NULL then the point cloud is not quantized or
    * quantization is unknown.
    *
    * \note By default this will be the same as the PointSources' scale.
    */
   virtual const double *getScale(void) const = 0;
   /**
    * Get the quantization offset.
    *
    * This method returns the quantization offset for X, Y, and
    * Z channels.  It returns NULL then the point cloud is not quantized or
    * quantization is unknown.
    *
    * \note By default this will be the same as the PointSources' offset.
    */
   virtual const double *getOffset(void) const = 0;

   /**
    * Write out the point cloud.
    *
    * This method writes the output file.
    *
    *  \param bounds the region of interest (HUGE_VAL are handled)
    *  \param fraction the fraction of the points you want
    *                  (use 1.0 for all the points and
    *                   use 0.1 to keep every tenth point)
    *  \param pointInfo the list of channels to be extracted
    *  \param delegate a ProgressDelegate for feedback (can be NULL)
    *  \return returns the number of points writen
    */
   virtual count_type write(const Bounds &bounds,
                            double fraction,
                            const PointInfo &pointInfo,
                            ProgressDelegate *delegate) = 0;

   void setChunckSize(size_t size);


   /**
    * Remove the unsupported channels.
    *
    * This method take a list of channels and returns the supported channels.
    *
    *  \param inputPointInfo a list of channels
    *  \param supportedPointInfo a filtered version of inputPointInfo
    *  \return returns true if channels were removed
    */
   virtual bool supportedChannels(const PointInfo &inputPointInfo,
                                  PointInfo &supportedPointInfo) const = 0;

protected:
   /**
    * Initalize the object.
    *
    * \param src the input PointSource
    */
   void init(const PointSource *src);

   /**
    * Get the input PointSource
    *
    * This method returns the input PointSource.
    */
   const PointSource *getSrc(void) const;
   
   /**
    * Remove metadata that is inappropriate for the given bounds and fraction.
    *
    * \param bounds the region of interest
    * \param fraction the fraction of the points you want
    */
   void groomMetadata(const Bounds &bounds, double fraction);

   size_t getChunckSize(void) const;

private:
   const PointSource *m_src;
   Metadata m_metadata;
   size_t m_chunckSize;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_POINT_WRITER_H__
