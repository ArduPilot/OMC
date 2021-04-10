/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2010 LizardTech, Inc, 1008 Western           //
// Avenue, Suite 200, Seattle, WA 98104.  Unauthorized use or distribution //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
#include <stdlib.h> //malloc, free
#include <string.h> // memcpy

#include "lidar/GeoTIFFUtils.h"

LT_USE_LIDAR_NAMESPACE

#include "geotiff.h"
#include "geo_simpletags.h"

static void loadProjectionKey(ST_TIFF *tiff, const Metadata &metadata,
                              int key, int type, int size)
{
   char tag[64];
   snprintf(tag, sizeof(tag), "LASF_Projection::%d", key);
   if(metadata.has(tag))
   {
      const char *description = NULL;
      MetadataDataType datatype = METADATA_DATATYPE_INVALID;
      const void *values = NULL;
      size_t length = 0;
      metadata.get(tag, description, datatype, values, length);
      int count = static_cast<int>(length / size);

      // LAS stores the GeoTIFF Keys in little endian order so we will need to
      // endian swap on big endian CPUs
      if(HOST_IS_BIG_ENDIAN && size > 1)
      {
         unsigned char *temp = static_cast<unsigned char *>(malloc(length));
         memcpy(temp, values, length);
         for(size_t i = 0; i < length; i += size)
            Endian::swap(temp + i, size);
         ST_SetKey(tiff, key, count, type, temp);
         free(temp);
      }
      else
         ST_SetKey(tiff, key, count, type, const_cast<void *>(values));
   }
}
                         
void
GeoTIFFUtils::dumpKeys(FILE *file, const Metadata &metadata)
{
   if(metadata.has("LASF_Projection::34735"))
   {
      ST_TIFF *tiff = ST_Create();
      loadProjectionKey(tiff, metadata, 34735, STT_SHORT, 2);
      loadProjectionKey(tiff, metadata, 34736, STT_DOUBLE, 8);
      loadProjectionKey(tiff, metadata, 34737, STT_ASCII, 1);

      GTIF *gtif = GTIFNewSimpleTags(tiff);
      if(gtif != NULL)
      {
         GTIFPrint(gtif, (int (*)(char*, void*))fputs, file);
         GTIFFree(gtif);
      }
      ST_Destroy(tiff);
   }
}

