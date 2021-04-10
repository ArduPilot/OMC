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

#ifndef __LIDAR_POINT_SOURCE_H__
#define __LIDAR_POINT_SOURCE_H__

#include "lidar/Metadata.h"
#include "lidar/PointIterator.h"
#include "lidar/ProgressDelegate.h"

LT_BEGIN_LIDAR_NAMESPACE

/**
 * PointSource is the base class of LiDAR point cloud extraction pipeline.
 *
 * The PointSource class the base class for accessing LiDAR data.  PointSource
 * has two types of methods.  The first class are methods that report bulk
 * properties of the point cloud, such as number of points and number of
 * channels.  The second class of methods give access to the point cloud itself,
 * see createIterator() and read().
 *
 * \note All methods are const, so you should be able to use this Object on
 * multiple threads at a time.  The decode state lives in the PointIterator so
 * only one thread can use a PointIterator.
 *
 * \see See examples/src/DumpMG4Info.cpp dumpHeader() for an example of
 * accessing bulk properties of the point cloud.
 * \see See examples/src/UserTutorial.cpp for examples of accessing the 
 * point cloud.
 */

class PointSource : public Object
{
   ABSTRACT_OBJECT(PointSource);
public:
   typedef lt_int64 count_type;

   /**
    * Get the point information.
    *
    * This method returns a PointInfo object.
    */
   virtual const PointInfo &getPointInfo(void) const = 0;

   /**
    * Get the number of channels.
    *
    * This method returns the channels.  This method is equivalent to calling
    * getPointInfo().getNumChannels().
    */
   size_t getNumChannels(void) const;

   /**
    * Determine if there is a channel with a given name.
    *
    * The method determines if this source has a channel with the given name.
    *
    * \param name the channel name
    * \return true if the channel was found
    */
   bool hasChannel(const char *name) const;

   /**
    * Access the channel info.
    *
    * This method returns the ChannelInfo for a given index.
    *
    * \param idx the index of the wanted channel
    * \return the channel info for the given channel
    * \note This method is equivalent to calling getPointInfo().getChannel(idx).
    */
   const ChannelInfo &getChannel(size_t idx) const;
   
   /**
    * Access the channel data.
    *
    * This method returns the ChannelInfo for a given name.
    *
    * \param name the name of the wanted channel
    * \return the channel data for the given channel
    * \note This method is equivalent to calling getPointInfo().getChannel(name).
    */
   const ChannelInfo *getChannel(const char *name) const;

   /**
    * Get the number of points.
    *
    * This method returns the number of points in the point cloud.
    */
   virtual count_type getNumPoints(void) const = 0;

   /**
    * Get the spatial reference system.
    *
    * This method returns the spatial reference system as a Well Known Text
    * (WKT) string.  If the PointSource does not have a spatial reference
    * system it will return NULL.
    */
   virtual const char *getWKT(void) const = 0;

   /**
    * Load the point clouds metadata.
    *
    * This method loads the metadata for the point cloud. PointSource
    * only load metadata on demand to reduce memory usage.
    *
    *  \param   metadata The metadata object to fill.
    *  \param   sanitize If true remove vendor-specific metadata that we
    *                    don't understand for example,
    *                    Merrick::102 ("Index to page of point records")
    */
   virtual void loadMetadata(Metadata &metadata,
                             bool sanitize) const = 0;

   /**
    * Get the bounding box.
    *
    * This method returns the geo bounding box of the point cloud.
    */
   virtual const Bounds &getBounds(void) const = 0;
   
   /**
    * Get the quantization scale.
    *
    * This method returns the quantization scale factors for X, Y, and
    * Z channels.  It returns NULL then the point cloud is not quantized or
    * quantization is unknown.
    */
   virtual const double *getScale(void) const = 0;
   
   /**
    * Get the quantization offset.
    *
    * This method returns the quantization offset for X, Y, and
    * Z channels.  It returns NULL then the point cloud is not quantized or
    * quantization is unknown.
    */
   virtual const double *getOffset(void) const = 0;

   /**
    * Get the number of classification names.
    *
    * This methods returns the number of classification names.
    */
   virtual size_t getNumClassIdNames(void) const = 0;
   
   /**
    * Get the classification names.
    *
    * This methods returns an array of classification names with length
    * getNumClassIdNames().
    */
   virtual char const * const *getClassIdNames(void) const = 0;

   /**
    * Get the amount of work needed to decode bounds.
    *
    * This method returns the amount of work needed to decode the points in
    * bounds. getTotalWork() is used with ProgressDelegate to track the
    * progress of a decode or encode.
    *
    *  \param bounds the region of interest
    *  \param fraction the fraction of the points you want
    *                  (use 1.0 for all the points and
    *                   use 0.1 to keep every tenth point)
    *  \return the work needed to decode bounds
    */
   virtual double getTotalWork(const Bounds &bounds,
                               double fraction) const = 0;

   /**
    * Get a PointIterator for a given bounds.
    *
    * This methods returns a PointIterator for the given bounds.
    *
    *  \param bounds the region of interest (HUGE_VAL are handled)
    *  \param fraction the fraction of the points you want
    *                  (use 1.0 for all the points and
    *                   use 0.1 to keep every tenth point)
    *  \param channelInfo the list of channels to be extracted
    *  \param numChannels the number of channels to be extracted
    *  \param delegate a ProgressDelegate for feedback (can be NULL)
    */
   virtual PointIterator *createIterator(const Bounds &bounds,
                                         double fraction,
                                         const PointInfo &pointInfo,
                                         ProgressDelegate *delegate) const = 0;

   /**
    * Fill the point buffer with the best sample of the point cloud in bounds.
    *
    *  \param bounds the region of interest
    *  \param points the destination point buffer
    *  \param delegate a ProgressDelegate for feedback (can be NULL)
    *  \return the number of points written into the destination point buffer
    */
   size_t read(const Bounds &bounds,
               PointData &points,
               ProgressDelegate *delegate) const;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_POINT_SOURCE_H__
