/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2008 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


// This demonstrates how to read the metadata in an image.

#include "main.h"
#include "support.h"

#include "lt_fileSpec.h"
#include "lti_metadataStatus.h"
#include "lti_metadataDatabase.h"
#include "lti_metadataRecord.h"
#include "lti_metadataUtils.h"
#include "MrSIDImageReader.h"

LT_USE_NAMESPACE(LizardTech);


static LT_STATUS dumpItem(const void *data, int idx, LTIMetadataDataType type)
{
   switch (type)
   {
   case LTI_METADATA_DATATYPE_UINT8:
      printf("%u", static_cast<const lt_uint8 *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_SINT8:
      printf("%d", static_cast<const lt_int8 *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_UINT16:
      printf("%u", static_cast<const lt_uint16 *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_SINT16:
      printf("%d", static_cast<const lt_int16 *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_UINT32:
      printf("%u", static_cast<const lt_uint32 *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_SINT32:
      printf("%d", static_cast<const lt_int32 *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_FLOAT32:
      printf("%f", static_cast<const float *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_FLOAT64:
      printf("%f", static_cast<const double *>(data)[idx]);
      break;
   case LTI_METADATA_DATATYPE_ASCII:
      printf("%s", ((const char **)data)[idx]);
      break;
   default:
      return LTI_STS_Metadata_UnsupDataType;
   }

   return LT_STS_Success;
}


static void
dumpRecord(const LTIMetadataRecord& rec)
{
   printf("tag %s\n", rec.getTagName());
  
   printf("  type: %s\n", LTIMetadataUtils::name(rec.getDataType()));
      
   {
      // print dims array
      const lt_uint32 numDims = rec.getNumDims();
      const lt_uint32* dims = rec.getDims();
      printf("  dimensions: ");
      for (lt_uint32 d=0; d<numDims; d++)
      {
         printf("[%d]", dims[d]);
      }
      printf("\n");
   }
   
   if (rec.isScalar())
   {
      const void *data = rec.getScalarData();
      printf("  scalar value: ");
      dumpItem(data, 0, rec.getDataType());
      printf("\n");
   }
   else if (rec.isVector())
   {
      printf("  vector values:\n");
      lt_uint32 len=0;
      const void *data = rec.getVectorData(len);
      for (lt_uint32 l=0; l<len; l++)
      {
         printf("  [%d]: ", l);
         dumpItem(data, l, rec.getDataType());
         printf("\n");
      }
   }
   else if (rec.isArray())
   {
      printf("  array values:\n");
      lt_uint32 numDims=0;
      const lt_uint32* dims=NULL;
      const void *data = rec.getArrayData(numDims, dims);
      lt_uint32 i=0;
      for (lt_uint32 d=0; d<numDims; d++)
      {
         printf("  [%d]:\n", d);
         for (lt_uint32 nd=0; nd<dims[d]; nd++)
         {
            printf("  [%d]: ", nd);
            dumpItem(data, i, rec.getDataType());
            printf("\n");
            ++i;
         }
      }
   }
} 


LT_STATUS MetadataDump()
{
   LT_STATUS sts = LT_STS_Uninit;

   // make the image reader
   const LTFileSpec fileSpec(INPUT_PATH("meg_cr20.sid"));
   MrSIDImageReader *reader = MrSIDImageReader::create();
   TEST_BOOL(reader != NULL);

   TEST_SUCCESS(reader->initialize(fileSpec));

   const LTIMetadataDatabase& db = reader->getMetadata();
   
   const lt_uint32 numRecs = db.getIndexCount();
   for (lt_uint32 i = 0; i < numRecs; i++)
   {
      const LTIMetadataRecord* rec = NULL;
      TEST_SUCCESS(db.getDataByIndex(i, rec));
      
      dumpRecord(*rec);
   }
   
   reader->release();
   reader = NULL;
   
   return LT_STS_Success;
}
