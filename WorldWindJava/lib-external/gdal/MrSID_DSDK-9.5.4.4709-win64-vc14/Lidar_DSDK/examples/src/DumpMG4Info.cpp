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

static void dumpHeaderInfo(const PointSource *pointSource)
{
   printf("   Number of Points:  %llu\n", pointSource->getNumPoints());

   const Bounds &b = pointSource->getBounds();
   printf("   Bounds Min:        %f %f %f\n", b.x.min, b.y.min, b.z.min);
   printf("   Bounds Max:        %f %f %f\n", b.x.max, b.y.max, b.z.max);

   if(pointSource->getScale() != NULL)
   {
      const double *s = pointSource->getScale();
      printf("   Scale:             %g %g %g\n", s[0], s[1], s[2]);
   }
   else
      printf("   Scale:             None\n");

   if(pointSource->getOffset() != NULL)
   {
      const double *o = pointSource->getOffset();
      printf("   Offset:            %f %f %f\n", o[0], o[1], o[2]);
   }
   else
      printf("   Offset:            None\n");

   {
      size_t numChannels = pointSource->getNumChannels();
      const PointInfo &pointInfo = pointSource->getPointInfo();
      printf("   Supported Fields:  ");
      for(size_t i = 0; i < numChannels; i += 1)
      printf(" %s", pointInfo.getChannel(i).getName());
      printf("\n");
   }

   if(pointSource->getWKT() != NULL)
      printf("   WKT:               %s\n", pointSource->getWKT());
   else
      printf("   WKT:               None\n");
}

static void dumpMetadata(const Metadata &metadata)
{
   size_t numRecords = metadata.getNumRecords();
   if(numRecords != 0)
   {
      printf("   Metadata:          %lu\n", numRecords);
      for(size_t i = 0; i < numRecords; i += 1)
      {
         const char *key = NULL;
         const char *description = NULL;
         MetadataDataType datatype;
         const void *value = NULL;
         size_t length = 0;

         metadata.get(i, key, description, datatype, value, length);
         if(description == NULL)
            printf("      %s:\n", key);
         else
            printf("      %s (%s):\n", key, description);

         switch(datatype)
         {
            case METADATA_DATATYPE_STRING:
               printf("         '%s'\n", static_cast<const char *>(value));
               break;
            case METADATA_DATATYPE_REAL_ARRAY:
            {
               const double *d = static_cast<const double *>(value);
               printf("         {");
               for(size_t j = 0; j < length; j += 1)
                  printf(" %f", d[j]);
               printf(" }\n");
            }
            break;
            case METADATA_DATATYPE_BLOB:
               printf("         (blob of %lu bytes)\n", length);
               break;
            default:
               ;// empty
         }
      } 
   }
   else
      printf("   Metadata:          None\n");
}


void DumpMG4Info()
{
   MG4PointReader *reader = NULL;
   try
   {
      // open a MG4 file
      reader = MG4PointReader::create();
      reader->init(INPUT_PATH("Tetons_200k.las.sid"));

      printf("   File Format:       %s\n", reader->getFileFormatString());
      dumpHeaderInfo(reader);

      Metadata metadata;
      reader->loadMetadata(metadata, false);
      dumpMetadata(metadata);

      RELEASE(reader);
   }
   catch(...)
   {
      RELEASE(reader);
      throw;
   }
}
