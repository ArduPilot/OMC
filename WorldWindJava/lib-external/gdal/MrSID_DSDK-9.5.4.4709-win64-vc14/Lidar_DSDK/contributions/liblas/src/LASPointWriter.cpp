// Prevent conflicts between liblas's stdint.hpp and <stdint.h>
#define __STDC_CONSTANT_MACROS

//self
#include "lidar/LASPointWriter.h"
#include "lidar/formats_status.h"
#include "lidar/core_status.h"
#include "lidar/Version.h"
#include "lidar/FileIO.h"
#include "lidar/Error.h"
#include "lidar/Private.h"

#include "laswriter.hpp"
#include "ogr_spatialref.h"
#include "cpl_conv.h"
#include "geo_simpletags.h"
#include "gt_wkt_srs.h"

#include <time.h>

LT_USE_LIDAR_NAMESPACE

#define LASF_Projection            "LASF_Projection"

struct LASPointWriter::Handler
{
   enum {
      X,
      Y,
      Z,
      Intensity,
      ReturnNumber,
      NumberOfReturns,
      ScanDirection,
      FlightLineEdge,
      Classification,
      ScanAngleRank,
      UserData,
      PointSourceId,
      GpsTime,
      Red,
      Green,
      Blue,
      NearInfrared,
      ReturnNumberEx,
      NumberOfReturnsEx,
      ScannerChannel,
      ClassificationFlags,
      ClassificationEx,
      ScanAngleEx,
   } fieldName;

   DataType dstDatatype;
   DataType srcDatatype;

   const void *data;
};

LASPointWriter::LASPointWriter(void) :
   m_output(NULL),
   m_fileVersion(VERSION_ANY),
   m_writeLAZ(false),
   m_header(NULL),
   m_writer(NULL),
   m_stats(NULL),
   m_handler(NULL),
   m_numHandlers(0),
   m_total(0),
   m_overrideProjectionVLRs(false)
{
}

LASPointWriter::~LASPointWriter(void)
{
   delete m_writer;
   delete m_header;
   delete m_stats;
   DEALLOC(m_output);
}

IMPLEMENT_OBJECT_CREATE(LASPointWriter);

void
LASPointWriter::setOverrideProjectionVLRs(bool value)
{
   m_overrideProjectionVLRs = value;
}


static bool needsMoreBits(const PointInfo &pointInfo, const char *name, int bits)
{
   const ChannelInfo *channel = pointInfo.getChannel(name);
   return channel != NULL && channel->getBits() > bits;
}

int
LASPointWriter::getRecordFormat(const PointInfo &pointInfo)
{
   bool hasGPSTime = (pointInfo.hasChannel(CHANNEL_NAME_GPSTime_Week) ||
                      pointInfo.hasChannel(CHANNEL_NAME_GPSTime_Adjusted));

   bool hasColor = (pointInfo.hasChannel(CHANNEL_NAME_Red) ||
                    pointInfo.hasChannel(CHANNEL_NAME_Green) ||
                    pointInfo.hasChannel(CHANNEL_NAME_Blue));

   bool hasNIR = pointInfo.hasChannel(CHANNEL_NAME_NearInfrared);

   bool hasClassFlags = (pointInfo.hasChannel(CHANNEL_NAME_ClassFlags) ||
                         pointInfo.hasChannel(CHANNEL_NAME_ScannerChannel));

   bool has4bitRetruns = (needsMoreBits(pointInfo, CHANNEL_NAME_NumReturns, 3) ||
                          needsMoreBits(pointInfo, CHANNEL_NAME_ReturnNum, 3));

   bool has16bitScanAngle = needsMoreBits(pointInfo, CHANNEL_NAME_ScanAngle, 8);

   // we don't support full wavefrom so we should not return 4,5,9,10

   if (hasClassFlags || has4bitRetruns || has16bitScanAngle || hasNIR)
   {
      if (hasColor)
      {
         if (hasNIR)
            return 8;
         else
            return 7;
      }
      else
      {
         return 6;
      }
   }
   else
   {
      if (hasColor)
      {
         if (hasGPSTime)
            return 3;
         else
            return 2;
      }
      else
      {
         if (hasGPSTime)
            return 1;
         else
            return 0;
      }
   }
}

LASPointWriter::FileVersion
LASPointWriter::getFileVersion(const PointInfo &pointInfo)
{
   int format = getRecordFormat(pointInfo);

   if (format < 2)
      return VERSION_1_1;
   else if (format < 4)
      return VERSION_1_2;
   else if (format < 6)
      return VERSION_1_3;
   else
      return VERSION_1_4;
}


void
LASPointWriter::init(const PointSource *src,
                     const char *path,
                     FileVersion fileVersion)
{
   assert(path != NULL);
   SimplePointWriter::init(src);
   m_output = STRDUP(path);
   m_fileVersion = fileVersion;
}

void
LASPointWriter::setWriteLAZ(bool laz)
{
   m_writeLAZ = laz;
}

static void add_vlr(LASheader *header,
                    const char *userId, U16 recordId,
                    const char *description,
                    size_t length, const void *data)
{
   if (length <= MAX_VALUE<U16>())
   {
      U8 *buffer = new U8[length];
      memcpy(buffer, data, length);
      header->add_vlr(userId, recordId, static_cast<U16>(length), buffer);
      LASvlr *vlr = &(header->vlrs[header->number_of_variable_length_records - 1]);
      if (description)
         strncpy(&(vlr->description[0]), description, sizeof(vlr->description));
      else
         strncpy(&(vlr->description[0]), "", sizeof(vlr->description));
   }
}


void
LASPointWriter::writeBegin(const PointInfo &pointInfo)
{
   m_total = 0;
   int recordFormat = getRecordFormat(pointInfo);
   int fileVersion = m_fileVersion;
   if(fileVersion == VERSION_ANY)
      fileVersion = getFileVersion(pointInfo);

   bool useExtended = recordFormat > 5;

   // writer should write out intersection of requested fields
   // and available fields
   
   delete m_header;
   m_header = new LASheader();
   sprintf(m_header->generating_software, "LizardTech %s", Version::getSDKVersionString());
   m_header->system_identifier[0] = '\0';
   time_t theTime = time(0);
   tm *t = gmtime(&theTime);
   // tm_yday is days since January 1 (0-365)
   // but LAS Spec says 1 January is day 1
   m_header->file_creation_day = static_cast<U16>(t->tm_yday + 1);
   m_header->file_creation_year = static_cast<U16>(t->tm_year + 1900);
   m_header->version_major = static_cast<U8>(fileVersion / 10);
   m_header->version_minor = static_cast<U8>(fileVersion % 10);
   m_header->point_data_format = static_cast<U8>(recordFormat);
   if (pointInfo.hasChannel(CHANNEL_NAME_GPSTime_Adjusted))
      m_header->global_encoding |= 0x1;

   if (m_header->version_minor > 2)
   {
      m_header->header_size += 8;
      m_header->offset_to_point_data += 8;

      if (m_header->version_minor > 3)
      {
         m_header->header_size += 140;
         m_header->offset_to_point_data += 140;
      }
   }

   switch (m_header->point_data_format)
   {
   case 0:
      m_header->point_data_record_length = 20;
      break;
   case 1:
      m_header->point_data_record_length = 28;
      break;
   case 2:
      m_header->point_data_record_length = 26;
      break;
   case 3:
      m_header->point_data_record_length = 34;
      break;
   case 4:
      m_header->point_data_record_length = 57;
      break;
   case 5:
      m_header->point_data_record_length = 63;
      break;
   case 6:
      m_header->point_data_record_length = 30;
      break;
   case 7:
      m_header->point_data_record_length = 36;
      break;
   case 8:
      m_header->point_data_record_length = 38;
      break;
   case 9:
      m_header->point_data_record_length = 59;
      break;
   case 10:
      m_header->point_data_record_length = 67;
      break;
   default:
      break;
   }



   m_header->x_scale_factor = m_scale[0];
   m_header->y_scale_factor = m_scale[1];
   m_header->z_scale_factor = m_scale[2];
   m_header->x_offset = m_offsets[0];
   m_header->y_offset = m_offsets[1];
   m_header->z_offset = m_offsets[2];

   bool mustWriteWKT = m_header->point_data_format > 5;
   bool hasGeoTIFFVLRs = false;
   bool hasWKTVLRs = false;

   const Metadata &metadata = getMetadata();
   
   for(size_t idx = 0; idx < metadata.getNumRecords(); idx += 1)
   {
      const char *key;
      const char *description;
      MetadataDataType datatype;
      const void *value;
      size_t length;

      metadata.get(idx, key, description, datatype, value, length);

      if (!strcmp(METADATA_KEY_SystemID, key))
         strncpy(m_header->system_identifier, static_cast<const char *>(value), sizeof(m_header->system_identifier));
      else if (!strcmp(METADATA_KEY_ProjectID, key))
      {
         unsigned int data[11];
         ::sscanf(static_cast<const char *>(value),
                  "{%08X-%04X-%04X-%02X%02X-%02X%02X%02X%02X%02X%02X}",
                  &data[0], &data[1], &data[2], &data[3], &data[4], &data[5],
                  &data[6], &data[7], &data[8], &data[9], &data[10]);

         m_header->project_ID_GUID_data_1 = data[0];
         m_header->project_ID_GUID_data_2 = data[1];
         m_header->project_ID_GUID_data_3 = data[2];
         m_header->project_ID_GUID_data_4[0] = data[3];
         m_header->project_ID_GUID_data_4[1] = data[4];
         m_header->project_ID_GUID_data_4[2] = data[5];
         m_header->project_ID_GUID_data_4[3] = data[6];
         m_header->project_ID_GUID_data_4[4] = data[7];
         m_header->project_ID_GUID_data_4[5] = data[8];
         m_header->project_ID_GUID_data_4[6] = data[9];
         m_header->project_ID_GUID_data_4[7] = data[10];
      }
      else if (!strcmp(METADATA_KEY_PointRecordsByReturnCount, key))
      {
         const double *p = static_cast<const double *>(value);
         for(int i = 0; i < 5; i += 1)
            m_header->number_of_points_by_return[i] = static_cast<lt_uint32>(p[i]);
      }
      else if (!strcmp(METADATA_KEY_FileSourceID, key))
      {
         int fileSourceId;
         sscanf(static_cast<const char *>(value), "%d", &fileSourceId);
         m_header->file_source_ID = static_cast<lt_uint16>(fileSourceId);
      }
      else if (!strcmp(METADATA_KEY_FileCreationDate, key))
      {
         // to do.
         //LASHeader_SetProjectId(m_header, static_cast<const char *>(value));
      }
      else if (strcmp(METADATA_KEY_GeneratingSoftware, key) == 0)
         ;// Always "LizardTech V-X.Y"
      else if(strcmp(METADATA_KEY_LASBBox, key) == 0)
      {
         LASMin[0] = static_cast<const double*>(value)[0];
         LASMin[1] = static_cast<const double*>(value)[2];
         LASMin[2] = static_cast<const double*>(value)[4];
         LASMax[0] = static_cast<const double*>(value)[1];
         LASMax[1] = static_cast<const double*>(value)[3];
         LASMax[2] = static_cast<const double*>(value)[5];
      }
      else
      {
         const char *e = strstr(key, "::");
         if (e != NULL)
         {
            char userId[17];
            memset(userId, 0, sizeof(userId));
            memcpy(userId, key, MIN(e - key, 16));

            int recordId;
            sscanf(e + 2, "%d", &recordId);
            bool add = false;
            if (strcmp(LASF_Projection, userId) == 0)
            {
               // GeoTIFF
               if (recordId == 34735 || recordId == 34736 || recordId == 34737)
               {
                  if (!m_overrideProjectionVLRs && !mustWriteWKT)
                     add = hasGeoTIFFVLRs = true;
               }
               // WKT
               else if (recordId == 2111 || recordId == 2112)
               {
                  if (!m_overrideProjectionVLRs)
                     add = hasWKTVLRs = true;
               }
            }

            if (add)
               add_vlr(m_header, userId, recordId, description, length, value);
         }
      }
   }

   if (!hasGeoTIFFVLRs && !hasWKTVLRs)
   {
      const char *wkt = getSrc()->getWKT();
      if (wkt != NULL && *wkt != '\0')
      {
         if (m_fileVersion > VERSION_1_3 || mustWriteWKT)
         {
            // 2112 (coordinate system)
            add_vlr(m_header, LASF_Projection, 2112, NULL, strlen(wkt) + 1, wkt);
            hasWKTVLRs = true;
         }
         else
         {
            ST_TIFF* tiff = ST_Create();
            GTIF* gtiff = GTIFNewSimpleTags(tiff);
            if (GTIFSetFromOGISDefn(gtiff, wkt) && GTIFWriteKeys(gtiff))
            {
               int count, type;
               void *buffer;

               if (ST_GetKey(tiff, 34735, &count, &type, &buffer))
               {
                  assert(type == STT_SHORT);
                  add_vlr(m_header, LASF_Projection, 34735, NULL, count * sizeof(U16), buffer);
               }

               if (ST_GetKey(tiff, 34736, &count, &type, &buffer))
               {
                  assert(type == STT_DOUBLE);
                  add_vlr(m_header, LASF_Projection, 34736, NULL, count * sizeof(double), buffer);
               }

               if (ST_GetKey(tiff, 34737, &count, &type, &buffer))
               {
                  assert(type == STT_ASCII);
                  add_vlr(m_header, LASF_Projection, 34737, NULL, count * sizeof(U8), buffer);
               }
            }

            GTIFFree(gtiff);
            ST_Destroy(tiff);
            hasGeoTIFFVLRs = true;
         }
      }
   }

   if (hasWKTVLRs)
      m_header->global_encoding |= (static_cast<U16>(1) << 4);
   else
      m_header->global_encoding &= ~(static_cast<U16>(1) << 4);

   m_numHandlers = pointInfo.getNumChannels();
   m_handler = ALLOC(Handler, sizeof(Handler) * m_numHandlers);
   for (size_t i = 0; i < m_numHandlers; i += 1)
   {
      const ChannelInfo &channelInfo = pointInfo.getChannel(i);
      const char *name = channelInfo.getName();

      Handler &h = m_handler[i];

      h.srcDatatype = channelInfo.getDataType();
      h.data = NULL;

      if (::strcmp(name, CHANNEL_NAME_X) == 0)
      {
         h.fieldName = Handler::X;
         h.dstDatatype = DATATYPE_FLOAT64;
      }
      else if (::strcmp(name, CHANNEL_NAME_Y) == 0)
      {
         h.fieldName = Handler::Y;
         h.dstDatatype = DATATYPE_FLOAT64;
      }
      else if (::strcmp(name, CHANNEL_NAME_Z) == 0)
      {
         h.fieldName = Handler::Z;
         h.dstDatatype = DATATYPE_FLOAT64;
      }
      else if (::strcmp(name, CHANNEL_NAME_Intensity) == 0)
      {
         h.fieldName = Handler::Intensity;
         h.dstDatatype = DATATYPE_UINT16;
      }
      else if (::strcmp(name, CHANNEL_NAME_ReturnNum) == 0)
      {
         h.fieldName = useExtended ? Handler::ReturnNumberEx : Handler::ReturnNumber;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_NumReturns) == 0)
      {
         h.fieldName = useExtended ? Handler::NumberOfReturnsEx : Handler::NumberOfReturns;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_ScanDir) == 0)
      {
         h.fieldName = Handler::ScanDirection;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_EdgeFlightLine) == 0)
      {
         h.fieldName = Handler::FlightLineEdge;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_ClassId) == 0)
      {
         h.fieldName = useExtended ? Handler::ClassificationEx : Handler::Classification;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_ScanAngle) == 0)
      {
         h.fieldName = useExtended ? Handler::ScanAngleEx : Handler::ScanAngleRank;
         h.dstDatatype = useExtended ? DATATYPE_SINT16 :  DATATYPE_SINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_UserData) == 0)
      {
         h.fieldName = Handler::UserData;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_SourceId) == 0)
      {
         h.fieldName = Handler::PointSourceId;
         h.dstDatatype = DATATYPE_UINT16;
      }
      else if (::strcmp(name, CHANNEL_NAME_GPSTime_Week) == 0 ||
               ::strcmp(name, CHANNEL_NAME_GPSTime_Adjusted) == 0)
      {
         h.fieldName = Handler::GpsTime;
         h.dstDatatype = DATATYPE_FLOAT64;
      }
      else if (::strcmp(name, CHANNEL_NAME_Red) == 0)
      {
         h.fieldName = Handler::Red;
         h.dstDatatype = DATATYPE_UINT16;
      }
      else if (::strcmp(name, CHANNEL_NAME_Green) == 0)
      {
         h.fieldName = Handler::Green;
         h.dstDatatype = DATATYPE_UINT16;
      }
      else if (::strcmp(name, CHANNEL_NAME_Blue) == 0)
      {
         h.fieldName = Handler::Blue;
         h.dstDatatype = DATATYPE_UINT16;
      }
      else if (::strcmp(name, CHANNEL_NAME_NearInfrared) == 0)
      {
         h.fieldName = Handler::NearInfrared;
         h.dstDatatype = DATATYPE_UINT16;
      }
      else if (::strcmp(name, CHANNEL_NAME_ClassFlags) == 0)
      {
         h.fieldName = Handler::ClassificationFlags;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else if (::strcmp(name, CHANNEL_NAME_ScannerChannel) == 0)
      {
         h.fieldName = Handler::ScannerChannel;
         h.dstDatatype = DATATYPE_UINT8;
      }
      else
         THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_INVALID_PARAM)
            ("invalid LAS field: %s", name);
   }

   LASwriteOpener opener;

   if (m_writeLAZ)
      opener.set_format(LAS_TOOLS_FORMAT_LAZ);
   else
      opener.set_format(LAS_TOOLS_FORMAT_LAS);

   opener.set_file_name(m_output);

   delete m_writer;
   m_writer = opener.open(m_header);
   opener.set_file_name(0);

   if(m_writer == NULL)
   {
      THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_COULD_NOT_CREATE_LAS_WRITER)
         ("Unable to create output %s.", m_output);
   }

   delete m_stats;
   m_stats = new LASsummary;
}

void
LASPointWriter::writePoints(const PointData &points,
                            size_t numPoints,
                            ProgressDelegate *delegate)
{
   static const char message[] = "writing LAS file";
   if(delegate != NULL)
   {
      delegate->updateCompleted(0, message);
      if(delegate->getCancelled())
         THROW_LIBRARY_ERROR(LTL_STATUS_CORE_OPERATION_CANCELLED)
            ("LAS Write operation cancelled.");
   }

   m_total += numPoints;
   if (m_total > MAX_VALUE<lt_uint32>() && m_header->version_minor < 4)
      THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_INVALID_PARAM)
      ("Attempt to write 2 ^ 32 or more points to a LAS file.");

   for (size_t i = 0; i < m_numHandlers; i += 1)
      m_handler[i].data = points.getChannel(i).getData();

   Handler *stop = m_handler + m_numHandlers;
   LASpoint pt;
   pt.init(m_header,
           m_header->point_data_format,
           m_header->point_data_record_length,
           m_header);
   for (size_t i = 0; i < numPoints; i += 1)
   {
      // marshal the various fields from LT SDK (in handler->data) into LASlib SDK (in pt)

      for (Handler *handler = m_handler; handler < stop; handler += 1)
      {
         const void *values = handler->data;
         double f64 = 0;
         lt_uint8 ui8 = 0;
         lt_int8 si8 = 0;
         lt_uint16 ui16 = 0;
         lt_uint16 si16 = 0;

         switch (handler->dstDatatype)
         {
#define CONVERT_TYPE(TAG, DST, FIELD) \
   case DATATYPE_##TAG: \
      switch(handler->srcDatatype) \
      { \
         case DATATYPE_UINT8:   FIELD = static_cast<DST>(static_cast<const lt_uint8  *>(values)[i]); break; \
         case DATATYPE_SINT8:   FIELD = static_cast<DST>(static_cast<const lt_int8   *>(values)[i]); break; \
         case DATATYPE_UINT16:  FIELD = static_cast<DST>(static_cast<const lt_uint16 *>(values)[i]); break; \
         case DATATYPE_SINT16:  FIELD = static_cast<DST>(static_cast<const lt_int16  *>(values)[i]); break; \
         case DATATYPE_UINT32:  FIELD = static_cast<DST>(static_cast<const lt_uint32 *>(values)[i]); break; \
         case DATATYPE_SINT32:  FIELD = static_cast<DST>(static_cast<const lt_int32  *>(values)[i]); break; \
         case DATATYPE_UINT64:  FIELD = static_cast<DST>(static_cast<const lt_uint64 *>(values)[i]); break; \
         case DATATYPE_SINT64:  FIELD = static_cast<DST>(static_cast<const lt_int64  *>(values)[i]); break; \
         case DATATYPE_FLOAT32: FIELD = static_cast<DST>(static_cast<const float     *>(values)[i]); break; \
         case DATATYPE_FLOAT64: FIELD = static_cast<DST>(static_cast<const double    *>(values)[i]); break; \
      } \
      break

            CONVERT_TYPE(UINT8, lt_uint8, ui8);
            CONVERT_TYPE(SINT8, lt_int8,  si8);
            CONVERT_TYPE(UINT16, lt_uint16, ui16);
            CONVERT_TYPE(SINT16, lt_int16, si16);
            CONVERT_TYPE(FLOAT64, double, f64);
         }

         switch (handler->fieldName)
         {
         case Handler::X:                 pt.set_x(f64);                   break;
         case Handler::Y:                 pt.set_y(f64);                   break;
         case Handler::Z:                 pt.set_z(f64);                   break;
         case Handler::Intensity:         pt.set_intensity(ui16);          break;
         case Handler::ReturnNumber:      pt.set_return_number(ui8);       break;
         case Handler::NumberOfReturns:   pt.set_number_of_returns(ui8);   break;
         case Handler::ScanDirection:     pt.set_scan_direction_flag(ui8); break;
         case Handler::FlightLineEdge:    pt.set_edge_of_flight_line(ui8); break;
         case Handler::Classification:
            pt.set_classification(ui8);
            pt.set_synthetic_flag((ui8 >> 5) & 0x1);
            pt.set_keypoint_flag((ui8 >> 6) & 0x1);
            pt.set_withheld_flag((ui8 >> 7) & 0x1);
            break;
         case Handler::ScanAngleRank:     pt.scan_angle_rank = si8;        break;
         case Handler::UserData:          pt.set_user_data(ui8);           break;
         case Handler::PointSourceId:     pt.set_point_source_ID(ui16);    break;
         case Handler::GpsTime:           pt.set_gps_time(f64);            break;
         case Handler::Red:               pt.rgb[0] = ui16;                break;
         case Handler::Green:             pt.rgb[1] = ui16;                break;
         case Handler::Blue:              pt.rgb[2] = ui16;                break;
         case Handler::NearInfrared:      pt.rgb[3] = ui16;                break;

         case Handler::ReturnNumberEx:       pt.extended_return_number = ui8;          break;
         case Handler::NumberOfReturnsEx:    pt.extended_number_of_returns = ui8;      break;
         case Handler::ScannerChannel:       pt.extended_scanner_channel = ui8;        break;
         case Handler::ClassificationFlags:
            pt.extended_classification_flags = ui8;
            pt.set_synthetic_flag((ui8 >> 0) & 0x1);
            pt.set_keypoint_flag((ui8 >> 1) & 0x1);
            pt.set_withheld_flag((ui8 >> 2) & 0x1);
            break;
         case Handler::ClassificationEx:
            pt.extended_classification = ui8;
            pt.set_classification(ui8);
            break;

         case Handler::ScanAngleEx:          pt.extended_scan_angle = si16;         break;
         }
      }

      m_stats->add(&pt);
      if (!m_writer->write_point(&pt))
         THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_LIBLAS_WRITER)
         ("Unable to write point");
   }

   if(delegate != NULL)
      delegate->updateCompleted(static_cast<double>(numPoints), message);
}

void
LASPointWriter::writeEnd(PointSource::count_type numPoints,
                         const Bounds &bounds)
{
   bool failed = numPoints == 0 && bounds == Bounds::Huge();
   if(m_writer != NULL)
   {
      if(!failed)
      {
         assert(m_total == m_stats->number_of_point_records);
         if (m_header->version_minor < 4)
         {
            if (m_total > MAX_VALUE<lt_uint32>())
               THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_INVALID_PARAM)
                  ("Attempt to write 2 ^ 32 or more points to a LAS file.");

            m_header->number_of_point_records = static_cast<lt_uint32>(m_total);

            for (int i = 0; i < 5; i++)
               m_header->number_of_points_by_return[i] = static_cast<lt_uint32>(m_stats->number_of_points_by_return[i + 1]);
         }
         else
         {
            if (m_total > MAX_VALUE<lt_uint32>())
               m_header->number_of_point_records = m_total > MAX_VALUE<lt_uint32>() ? 0 : static_cast<lt_uint32>(m_total);

            m_header->extended_number_of_point_records = m_total;

            for (int i = 0; i < 15; i += 1)
            {
               I64 count = m_stats->number_of_points_by_return[i + 1];
               m_header->extended_number_of_points_by_return[i] = count;

               if (i < 5)
                  m_header->number_of_points_by_return[i] = count > MAX_VALUE<lt_uint32>() ? 0 : static_cast<lt_uint32>(count);
            }
         }

         m_header->max_x = m_header->get_x(m_stats->max.X);
         m_header->min_x = m_header->get_x(m_stats->min.X);
         m_header->max_y = m_header->get_y(m_stats->max.Y);
         m_header->min_y = m_header->get_y(m_stats->min.Y);
         m_header->max_z = m_header->get_z(m_stats->max.Z);
         m_header->min_z = m_header->get_z(m_stats->min.Z);

         if (! m_writer->update_header(m_header, FALSE, TRUE))
            THROW_LIBRARY_ERROR((LTL_STATUS_FORMATS_LIBLAS_WRITER))
            ("Unable to update header");
      }
      m_writer->close();
      if (m_header != NULL)
         delete m_header;
      m_header = NULL;
   }
   if(failed && m_output != NULL)
      FileIO::deleteFile(m_output); 
}


static bool isSupportChannel(const char *name)
{
   return (strcmp(name, CHANNEL_NAME_X) == 0 ||
           strcmp(name, CHANNEL_NAME_Y) == 0 ||
           strcmp(name, CHANNEL_NAME_Z) == 0 ||
           strcmp(name, CHANNEL_NAME_Intensity) == 0 ||
           strcmp(name, CHANNEL_NAME_ReturnNum) == 0 ||
           strcmp(name, CHANNEL_NAME_NumReturns) == 0 ||
           strcmp(name, CHANNEL_NAME_ClassFlags) == 0 ||
           strcmp(name, CHANNEL_NAME_ScannerChannel) == 0 ||
           strcmp(name, CHANNEL_NAME_ScanDir) == 0 ||
           strcmp(name, CHANNEL_NAME_EdgeFlightLine) == 0 ||
           strcmp(name, CHANNEL_NAME_ClassId) == 0 ||
           strcmp(name, CHANNEL_NAME_ScanAngle) == 0 ||
           strcmp(name, CHANNEL_NAME_UserData) == 0 ||
           strcmp(name, CHANNEL_NAME_SourceId) == 0 ||
           strcmp(name, CHANNEL_NAME_GPSTime_Week) == 0 ||
           strcmp(name, CHANNEL_NAME_GPSTime_Adjusted) == 0 ||
           strcmp(name, CHANNEL_NAME_Red) == 0 ||
           strcmp(name, CHANNEL_NAME_Green) == 0 ||
           strcmp(name, CHANNEL_NAME_Blue) == 0 ||
           strcmp(name, CHANNEL_NAME_NearInfrared) == 0);
}


bool
LASPointWriter::lasSupportedChannels(const PointInfo &inputPointInfo,
                                     PointInfo &supportedPointInfo)
{
   size_t dstNumChannels = 0;
   size_t srcNumChannels = inputPointInfo.getNumChannels();
   for (size_t i = 0; i < srcNumChannels; i += 1)
   {
      const ChannelInfo &srcChannel = inputPointInfo.getChannel(i);
      if (isSupportChannel(srcChannel.getName()))
         dstNumChannels += 1;
   }

   if (dstNumChannels != srcNumChannels)
   {
      supportedPointInfo.init(dstNumChannels);
      for (size_t i = 0, j = 0; i < srcNumChannels; i += 1)
      {
         const ChannelInfo &srcChannel = inputPointInfo.getChannel(i);
         if (isSupportChannel(srcChannel.getName()))
            supportedPointInfo.getChannel(j++).init(srcChannel);
      }
      return true;
   }
   else
   {
      supportedPointInfo.init(inputPointInfo);
      return false;
   }
}

bool LASPointWriter::supportedChannels(const PointInfo &inputPointInfo,
                                       PointInfo &supportedPointInfo) const
{
   return lasSupportedChannels(inputPointInfo, supportedPointInfo);
}
