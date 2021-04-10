/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2009 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


// This is the code from the User Tutorial/Manual
// (error are not handle, see the other examples for error handling)

#include "main.h"

#include "lidar/MG4PointReader.h"
#include "lidar/FileIO.h"

LT_USE_LIDAR_NAMESPACE

void UserTutorial_iterator()
{
   // open a MG4 File
   MG4PointReader *pointSource = NULL;
   {
      FileIO *file = FileIO::create();
      // On Windows you should use the wide char * version to handle 
      // paths that can not be represented in the native code page.
      file->init(INPUT_PATH("Tetons_200k.las.sid"), "r");
      pointSource = MG4PointReader::create();
      pointSource->init(file);
      file->release();
      file = NULL;
   }

   // get some the bulk properties
   PointSource::count_type numPoints = pointSource->getNumPoints();
   size_t numChannels = pointSource->getNumChannels();
   const PointInfo &pointInfo = pointSource->getPointInfo();

   ::fprintf(stdout, "Number of points: %lld\n", numPoints);
   ::fprintf(stdout, "Number of channels: %lu\n", numChannels);
   for(size_t i = 0; i < numChannels; i += 1)
      ::fprintf(stdout, "Channel %lu: %s\n",  i,  pointInfo.getChannel(i).getName());

   // access the point cloud with the PointIterator
   PointData buffer;
   // create buffers for all the channels 1000 long
   buffer.init(pointInfo, 1000);
   // create an iterator of the whole point cloud with all the channels
   PointIterator *iter = pointSource->createIterator(pointSource->getBounds(),
                                                     1.0,
                                                     pointInfo,
                                                     NULL);

   size_t count;
   // walk the iterator
   while((count = iter->getNextPoints(buffer)) != 0)
   {
      // do some thing with this chunk of the point cloud.
   }
   iter->release();
   iter = NULL;

   // clean up
   pointSource->release();
   pointSource = NULL;
}

void UserTutorial_read()
{
   // open a MG4 File
   MG4PointReader *pointSource = NULL;
   {
      FileIO *file = FileIO::create();
      // On Windows you should use the wide char * version to handle 
      // paths that can not be represented in the native code page.
      file->init(INPUT_PATH("Tetons_200k.las.sid"), "r");
      pointSource = MG4PointReader::create();
      pointSource->init(file);
      file->release();
      file = NULL;
   }

   // get some the bulk properties
   PointSource::count_type numPoints = pointSource->getNumPoints();
   size_t numChannels = pointSource->getNumChannels();
   const PointInfo &pointInfo = pointSource->getPointInfo();

   ::fprintf(stdout, "Number of points: %lld\n", numPoints);
   ::fprintf(stdout, "Number of channels: %lu\n", numChannels);
   for(size_t i = 0; i < numChannels; i += 1)
      ::fprintf(stdout, "Channel %lu: %s\n",  i,  pointInfo.getChannel(i).getName());

   // access the point cloud with PointSource::read()
   PointData buffer;
   {
      // only decode X, Y, Z
      PointInfo pointInfo;
      pointInfo.init(3);
      size_t i = 0;
      pointInfo.getChannel(i++).init(*pointSource->getChannel(CHANNEL_NAME_X));
      pointInfo.getChannel(i++).init(*pointSource->getChannel(CHANNEL_NAME_Y));
      pointInfo.getChannel(i++).init(*pointSource->getChannel(CHANNEL_NAME_Z));
      buffer.init(pointInfo, 10000);
   }

   pointSource->read(Bounds::Huge(), buffer, NULL);
   // do some thing with the points

   // clean up
   pointSource->release();
   pointSource = NULL;
}



