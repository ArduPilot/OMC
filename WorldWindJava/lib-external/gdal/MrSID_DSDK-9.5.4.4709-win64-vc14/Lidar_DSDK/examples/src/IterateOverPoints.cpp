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


// This demonstrates how to get the properties of a PointSource

#include "main.h"

#include "lidar/MG4PointReader.h"

LT_USE_LIDAR_NAMESPACE

void IterateOverPoints()
{
   MG4PointReader *reader = NULL;
   PointIterator *iter = NULL;
   try
   {
      // open the MG4 file
      reader = MG4PointReader::create();
      reader->init(INPUT_PATH("Tetons_200k.xyz.sid"));

      // choosing subset so we don't print out too many points
      // Bounds Min:        504489.920000 4794521.660000 2401.740000
      // Bounds Max:        505896.670000 4795848.390000 2943.580000
      // 
      Bounds bounds(504489.0, 4794521.0,  // x range
                    504550.0, 4794580.0,    // y range
                    -HUGE_VAL, +HUGE_VAL);  // all z values
      // only extract half the points in the region
      double fraction = 0.5;

      // create an PointIterator for points in bounds
      // and get all the channels
      iter = reader->createIterator(bounds,
                                    fraction,
                                    reader->getPointInfo(),
                                    NULL);

      // create a buffer to store the point data
      PointData points;
      points.init(reader->getPointInfo(), 256);

      size_t count = 0;
      PointSource::count_type totalNumPoints = 0;
      while((count = iter->getNextPoints(points)) != 0)
      {
         totalNumPoints += count;

         // print out each point
         for(size_t i = 0; i < count; i += 1)
         {
            for(size_t j = 0; j < points.getNumChannels(); j += 1)
            {
               const ChannelData &c = points.getChannel(j);
               const void *data = c.getData();
               switch(c.getDataType())
               {
               case DATATYPE_FLOAT64:
                  printf(" %f", static_cast<const double *>(data)[i]);
                  break;
               case DATATYPE_FLOAT32:
                  printf(" %f", static_cast<const float *>(data)[i]);
                  break;

               case DATATYPE_SINT32:
                  printf(" %d", static_cast<const lt_int32 *>(data)[i]);
                  break;
               case DATATYPE_UINT32:
                  printf(" %u", static_cast<const lt_uint32 *>(data)[i]);
                  break;

               case DATATYPE_SINT16:
                  printf(" %d", static_cast<const lt_int16 *>(data)[i]);
                  break;
               case DATATYPE_UINT16:
                  printf(" %u", static_cast<const lt_uint16 *>(data)[i]);
                  break;

               case DATATYPE_SINT8:
                  printf(" %d", static_cast<const lt_int8 *>(data)[i]);
                  break;
               case DATATYPE_UINT8:
                  printf(" %u", static_cast<const lt_uint8 *>(data)[i]);
                  break;

               default:
                  ASSERT(!"unknown supported datatype");
                  break;
               }
            }
            printf("\n");
         }
      }
      printf("totalNumPoints = %lld\n", totalNumPoints);
      ASSERT(totalNumPoints == 3126);

      RELEASE(iter);
      RELEASE(reader);
   }
   catch(...)
   {
      RELEASE(iter);
      RELEASE(reader);
      throw;
   }
}
