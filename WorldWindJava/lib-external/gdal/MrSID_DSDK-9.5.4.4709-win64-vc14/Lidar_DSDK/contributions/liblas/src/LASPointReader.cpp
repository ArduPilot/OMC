// Prevent conflicts between liblas's stdint.hpp and <stdint.h>
#define  __STDC_CONSTANT_MACROS

#include "lidar/LASPointReader.h"
#include "lidar/core_status.h"
#include "lidar/formats_status.h"
#include "lidar/Error.h"
#include "lidar/Private.h"

#include "lasreader.hpp"

// GDAL
#include "ogr_spatialref.h"
#include "geo_simpletags.h"
#include "gt_wkt_srs.h"


#include <string.h>
#include <stdio.h>

#include <iostream>

LT_USE_LIDAR_NAMESPACE
#define LASF_Spec                  "LASF_Spec"
#define LASF_Projection            "LASF_Projection"


static const char *defaultClassId0to5[] = {
 /*  0 */ "never classified",
 /*  1 */ "unclassified",
 /*  2 */ "ground",
 /*  3 */ "low vegetation",
 /*  4 */ "medium vegetation",
 /*  5 */ "high vegetation",
 /*  6 */ "building",
 /*  7 */ "noise",
 /*  8 */ "keypoint",
 /*  9 */ "water",
 /* 10 */ "Reserved for ASPRS Definition",
 /* 11 */ "Reserved for ASPRS Definition",
 /* 12 */ "overlap",
 /* 13 */ "Reserved for ASPRS Definition",
 /* 14 */ "Reserved for ASPRS Definition",
 /* 15 */ "Reserved for ASPRS Definition",
 /* 16 */ "Reserved for ASPRS Definition",
 /* 17 */ "Reserved for ASPRS Definition",
 /* 18 */ "Reserved for ASPRS Definition",
 /* 19 */ "Reserved for ASPRS Definition",
 /* 20 */ "Reserved for ASPRS Definition",
 /* 21 */ "Reserved for ASPRS Definition",
 /* 22 */ "Reserved for ASPRS Definition",
 /* 23 */ "Reserved for ASPRS Definition",
 /* 24 */ "Reserved for ASPRS Definition",
 /* 25 */ "Reserved for ASPRS Definition",
 /* 26 */ "Reserved for ASPRS Definition",
 /* 27 */ "Reserved for ASPRS Definition",
 /* 28 */ "Reserved for ASPRS Definition",
 /* 29 */ "Reserved for ASPRS Definition",
 /* 30 */ "Reserved for ASPRS Definition",
 /* 31 */ "Reserved for ASPRS Definition"
};

static const char *defaultClassId6to10[] = {
 /*  0 */ "never classified",
 /*  1 */ "unclassified",
 /*  2 */ "ground",
 /*  3 */ "low vegetation",
 /*  4 */ "medium vegetation",
 /*  5 */ "high vegetation",
 /*  6 */ "building",
 /*  7 */ "low noise",
 /*  8 */ "Reserved for ASPRS Definition",
 /*  9 */ "water",
 /* 10 */ "rail",
 /* 11 */ "road surface",
 /* 12 */ "Reserved for ASPRS Definition",
 /* 13 */ "wire guard",
 /* 14 */ "wire conductor",
 /* 15 */ "tower",
 /* 16 */ "wire connector",
 /* 17 */ "bridge deck",
 /* 18 */ "high noise",
 /* 19 */ "Reserved for ASPRS Definition",
 /* 20 */ "Reserved for ASPRS Definition",
 /* 21 */ "Reserved for ASPRS Definition",
 /* 22 */ "Reserved for ASPRS Definition",
 /* 23 */ "Reserved for ASPRS Definition",
 /* 24 */ "Reserved for ASPRS Definition",
 /* 25 */ "Reserved for ASPRS Definition",
 /* 26 */ "Reserved for ASPRS Definition",
 /* 27 */ "Reserved for ASPRS Definition",
 /* 28 */ "Reserved for ASPRS Definition",
 /* 29 */ "Reserved for ASPRS Definition",
 /* 30 */ "Reserved for ASPRS Definition",
 /* 31 */ "Reserved for ASPRS Definition"
};

#define SIMPLE_READER(type, tag, field) \
   static void read_##tag(const LASreader *reader, size_t idx, type *values) \
            { values[idx] = static_cast<type>(reader->point.field); }

#define XYZ_READER(type, tag) \
   static void read_##tag(const LASreader *reader, size_t idx, type *values) \
         { values[idx] = static_cast<type>(reader->tag()); }

#define BOOL_READER(type, tag, field) \
   static void read_##tag(const LASreader *reader, size_t idx, type *values) \
      { values[idx] = reader->point.field != 0; }

#define COLOR_READER(type, tag, field, icolor) \
   static void read_##tag(const LASreader *reader, size_t idx, type *values) \
      { values[idx] = (reader->point.field[icolor]); }


// make sure the datatype are in sync with the m_channelInfo datatypes
XYZ_READER(double, get_x)
XYZ_READER(double, get_y)
XYZ_READER(double, get_z)
SIMPLE_READER(lt_uint16, GetIntensity, intensity)
SIMPLE_READER(lt_uint8, GetReturnNumber, return_number)
SIMPLE_READER(lt_uint8, GetNumberOfReturns, number_of_returns)
SIMPLE_READER(lt_uint8, GetReturnNumberEx, extended_return_number)
SIMPLE_READER(lt_uint8, GetNumberOfReturnsEx, extended_number_of_returns)
SIMPLE_READER(lt_uint8, GetScannerChannel, extended_scanner_channel)
SIMPLE_READER(lt_uint8, GetClassificationFlags, extended_classification_flags)
BOOL_READER(lt_uint8, GetScanDirection, scan_direction_flag)
BOOL_READER(lt_uint8, GetFlightLineEdge, edge_of_flight_line)
SIMPLE_READER(lt_uint8, GetClassification, classification)
SIMPLE_READER(lt_uint8, GetClassificationEx, extended_classification)
SIMPLE_READER(lt_int8, GetScanAngleRank, scan_angle_rank)
SIMPLE_READER(lt_int16, GetScanAngleRankEx, extended_scan_angle)
SIMPLE_READER(lt_uint8, GetUserData, user_data)
SIMPLE_READER(lt_uint16, GetPointSourceId, point_source_ID)
SIMPLE_READER(double, GetTime, gps_time);
COLOR_READER(lt_uint16, GetRed, rgb, 0)
COLOR_READER(lt_uint16, GetGreen, rgb, 1)
COLOR_READER(lt_uint16, GetBlue, rgb, 2)
COLOR_READER(lt_uint16, GetNearInfrared, rgb, 3)

class LASPointReader::Iterator : public PointIterator
{
   CONCRETE_ITERATOR(Iterator);
protected:
   ~Iterator(void)
   {
	   if (m_reader != NULL)
		   m_reader->close();

      DEALLOC(m_handler);
   }
   
public:
	Iterator(void) :
		m_reader(NULL)
	{
		
	}
   void init(const Bounds &bounds,
             double fraction,
             const PointInfo &pointInfo,
             ProgressDelegate *delegate,
             const char *path,
             bool useExtended)
   {
      assert(path != NULL && *path != '\0');
      PointIterator::init(bounds, fraction, pointInfo, delegate);
	   m_opener.add_file_name(path);
	  
      if ((m_reader = m_opener.open()) == NULL)
         THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_LIBLAS_READER)
            ("Failed to create LASReader for path = '%s'", path);

      // Bounds::contains() uses min <= value <= max while it looks like
      // lastools uses min < value < max
      // PointIterator::init() grows the bounds for us
      m_reader->inside_rectangle(m_bounds.x.min, m_bounds.y.min,
                                 m_bounds.x.max, m_bounds.y.max);

      m_numHandlers = pointInfo.getNumChannels();
      m_handler = ALLOC(Handler, sizeof(Handler) * m_numHandlers);
      for (size_t i = 0; i < m_numHandlers; i += 1)
      {
#define HANDLE(tag, func) \
   if(::strcmp(name, CHANNEL_NAME_##tag) == 0) \
      reader = reinterpret_cast<ReadFunc>(read_##func)

#define HANDLEEX(tag, func) \
   if(::strcmp(name, CHANNEL_NAME_##tag) == 0) \
         reader = useExtended ? reinterpret_cast<ReadFunc>(read_##func##Ex) : reinterpret_cast<ReadFunc>(read_##func)

         const char *name = pointInfo.getChannel(i).getName();
         ReadFunc reader = NULL;
         HANDLE(X, get_x);
         else HANDLE(Y, get_y);
         else HANDLE(Z, get_z);
         else HANDLE(Intensity, GetIntensity);
         else HANDLEEX(ReturnNum, GetReturnNumber);
         else HANDLEEX(NumReturns, GetNumberOfReturns);
         else HANDLE(ScannerChannel, GetScannerChannel);
         else HANDLE(ClassFlags, GetClassificationFlags);
         else HANDLE(ScanDir, GetScanDirection);
         else HANDLE(EdgeFlightLine, GetFlightLineEdge);
         else HANDLEEX(ClassId, GetClassification);
         else HANDLEEX(ScanAngle, GetScanAngleRank);
         else HANDLE(UserData, GetUserData);
         else HANDLE(SourceId, GetPointSourceId);
         else HANDLE(GPSTime_Week, GetTime);
         else HANDLE(GPSTime_Adjusted, GetTime);
         else HANDLE(Red, GetRed);
         else HANDLE(Green, GetGreen);
         else HANDLE(Blue, GetBlue);
         else HANDLE(NearInfrared, GetNearInfrared);
         else
            THROW_LIBRARY_ERROR(-1);

         m_handler[i].reader = reader;
         m_handler[i].data = NULL;
      }
   }
      
   size_t getNextPoints(PointData &points)
   {
      static const char message[] = "reading LAS file";
      if(m_delegate != NULL)
         m_delegate->updateCompleted(0, message);

      for(size_t i = 0; i < m_numHandlers; i += 1)
         m_handler[i].data = points.getChannel(i).getData();

      size_t count = 0;
      size_t cancelCount = 0;

      Handler *stop = m_handler + m_numHandlers;
      while(count < points.getNumSamples() && m_reader->read_point())
      {

         if(useSample(m_reader->get_x(), m_reader->get_y(), m_reader->get_z()))
         {
            for(Handler *handler = m_handler; handler < stop; handler += 1)
               handler->reader(m_reader, count, handler->data);
            count += 1;
         }

         cancelCount += 1;
         if(cancelCount == 4096)
         {
            if (m_delegate != NULL)
            {
               m_delegate->updateCompleted(static_cast<double>(cancelCount), message);

               if(m_delegate->getCancelled())
                   THROW_LIBRARY_ERROR(LTL_STATUS_CORE_OPERATION_CANCELLED)
                      ("operation cancelled.");
            }
            cancelCount = 0;
         }
      }

      if(m_delegate != NULL)
         m_delegate->updateCompleted(static_cast<double>(cancelCount), message);
      return count;
   }
protected:

   typedef void (*ReadFunc)(const LASreader *reader, size_t idx, void *values);
   struct Handler
   {
      ReadFunc reader;
      void *data;
   };

   LASreader*  m_reader;
   LASreadOpener m_opener;
   size_t m_numHandlers;
   Handler *m_handler;
};

LASPointReader::LASPointReader(void) :
   m_path(NULL),
   //m_fileFormatString(0),
   m_classId(NULL),
   m_numClasses(0),
   m_recordFormat(-1),
   m_tolerateUnsupportedWaveFormData(false),
   m_useExtended(false)
{
   ::memset(m_fileFormatString, 0, sizeof(m_fileFormatString));
}

LASPointReader::~LASPointReader(void)
{
   DEALLOC(m_path);
   if(m_classId != NULL &&
      (m_classId != const_cast<char **>(defaultClassId0to5) &&
       m_classId != const_cast<char **>(defaultClassId6to10)))
   {
      for(size_t i = 0; i < m_numClasses; i += 1)
         DEALLOC(m_classId[i]);
      DEALLOC(m_classId);
   }
}

IMPLEMENT_OBJECT_CREATE(LASPointReader);

template<typename VLR>
static void extractProjectionAndClassNames(U32 number_of_variable_length_records, const VLR *vlrs,
                                           ST_TIFF *&tiff, char *&wkt,
                                           char **&classId, size_t &numClasses)
{
   for (U32 i = 0; i < number_of_variable_length_records; i++)
   {
      const VLR &vlr = vlrs[i];

      if ((::strcmp(LASF_Spec, vlr.user_id) == 0 && vlr.record_id == 0))
      {
         numClasses = vlr.record_length_after_header / 16; // should be 256
         classId = ALLOC(char *, sizeof(char *) * numClasses);
         ::memset(classId, 0, sizeof(char *) * numClasses);

         const LASvlr_classification *classes =
            reinterpret_cast<const LASvlr_classification *>(vlr.data);
         for (size_t i = 0; i < numClasses; i++)
            classId[classes[i].class_number] = STRDUP(classes[i].description, 15);
      }
      else if (strcmp(vlr.user_id, LASF_Projection) == 0 && vlr.data != NULL)
      {
         switch (vlr.record_id)
         {
         case 2111:  // ogc math transform (this may be a bug)
         case 2112:  // ogc coordinate system
            wkt = STRDUP(reinterpret_cast<const char *>(vlr.data),
                         vlr.record_length_after_header);
            break;

         case 34735:// GeoKeyDirectoryTag
            if (tiff == NULL)
               tiff = ST_Create();

            ST_SetKey(tiff, vlr.record_id,
                      static_cast<int>(vlr.record_length_after_header / sizeof(U16)),
                      STT_SHORT,
                      vlr.data);
            break;

         case 34736:// GeoDoubleParamsTag
            if (tiff == NULL)
               tiff = ST_Create();

            ST_SetKey(tiff, vlr.record_id,
                      static_cast<int>(vlr.record_length_after_header / sizeof(double)),
                      STT_DOUBLE,
                      vlr.data);
            break;

         case 34737:// GeoAsciiParamsTag
            if (tiff == NULL)
               tiff = ST_Create();

            ST_SetKey(tiff, vlr.record_id,
                      static_cast<int>(vlr.record_length_after_header / sizeof(U8)),
                      STT_ASCII,
                      vlr.data);
            break;

         default:
            break;
         }
      }
   }
}


void
LASPointReader::init(const char *path)
{
   init(path, false);
}

void
LASPointReader::init(const char *path, bool tolerateUnsupportedWaveFormData)
{
   if(path == NULL || *path == '\0')
      THROW_LIBRARY_ERROR(LTL_STATUS_CORE_INVALID_PARAM)
         ("path not given");

   m_path = STRDUP(path);

   // temporary hack until we can provide real FWF support.  
   // initialized this way, you can read the header data
   m_tolerateUnsupportedWaveFormData = tolerateUnsupportedWaveFormData;


   LASreadOpener opener;
   LASreader *reader = NULL;
   try
   {
      opener.add_file_name(m_path);
      if((reader = opener.open()) == NULL)
         THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_LIBLAS_READER)
            ("Failed to create LASReader for path = '%s'", m_path);    

      const LASheader &header = reader->header;

      ::snprintf(m_fileFormatString, sizeof(m_fileFormatString), "%s %d.%d",
                 header.laszip ? "LAZ" : "LAS",
                 header.version_major,
                 header.version_minor);

      if (!m_tolerateUnsupportedWaveFormData &&
          (header.start_of_waveform_data_packet_record ||
           header.global_encoding & 0x6))
         THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_FWF_NOT_SUPPORTED)("Input LAS/LAZ file contains waveform data.  This is not currently supported.");

      int dataFormat = header.point_data_format;
      m_recordFormat = dataFormat;
      m_useExtended = dataFormat > 5;

      size_t numChannels = 0;

      numChannels += 3; // X, Y, Z
      numChannels += 1; // Intensity
      numChannels += 2; // Return Number, Number of Retruns

      if (dataFormat > 5)
      {
         numChannels += 1; // Classifcation flags
         numChannels += 1; // Scanner channel
      }

      numChannels += 2; // Scan Direction Flag, Edge of Flight line
      numChannels += 1; // Classification
      numChannels += 1; // Scan Angle Rank (-90 to +90) left side / Scan Angle
      numChannels += 1; // User Data
      numChannels += 1; // Point Source Id
      if(dataFormat != 0 && dataFormat != 2)
         numChannels += 1; // GPSTime
      if (dataFormat == 2 || dataFormat == 3 || dataFormat == 5 || dataFormat == 7 || dataFormat == 8 || dataFormat == 10)
      {
         numChannels += 3;  // R, G and B.
         if (dataFormat == 8 || dataFormat == 10)
            numChannels += 1;  // NIR.
      }

#if 0 // full waveform
      if (dataFormat == 4 || dataFormat == 5 || dataFormat == 9 || dataFormat == 10)
      {
         numChannels += 1; // Wave packet descriptor Index
         numChannels += 1; // Byte offset to waveform data
         numChannels += 1; // Waveform packet size in bytes
         numChannels += 1; // Return point waveform location
         numChannels += 3; // X(t), Y(t), Z(t)
      }
#endif

      PointInfo pointInfo;
      pointInfo.init(numChannels);

      size_t i = 0;
      // make sure the datatypes are in sync with the read_*() functions
      pointInfo.getChannel(i++).init(CHANNEL_NAME_X, DATATYPE_FLOAT64, 32, header.x_scale_factor);
      pointInfo.getChannel(i++).init(CHANNEL_NAME_Y, DATATYPE_FLOAT64, 32, header.y_scale_factor);
      pointInfo.getChannel(i++).init(CHANNEL_NAME_Z, DATATYPE_FLOAT64, 32, header.z_scale_factor);
      pointInfo.getChannel(i++).init(CHANNEL_NAME_Intensity, DATATYPE_UINT16, 16);

      if (dataFormat < 6)
      {
         pointInfo.getChannel(i++).init(CHANNEL_NAME_ReturnNum, DATATYPE_UINT8, 3);
         pointInfo.getChannel(i++).init(CHANNEL_NAME_NumReturns, DATATYPE_UINT8, 3);
      }
      else
      {
         pointInfo.getChannel(i++).init(CHANNEL_NAME_ReturnNum, DATATYPE_UINT8, 4);
         pointInfo.getChannel(i++).init(CHANNEL_NAME_NumReturns, DATATYPE_UINT8, 4);
         pointInfo.getChannel(i++).init(CHANNEL_NAME_ClassFlags, DATATYPE_UINT8, 4);
         pointInfo.getChannel(i++).init(CHANNEL_NAME_ScannerChannel, DATATYPE_UINT8, 2);
      }

      pointInfo.getChannel(i++).init(CHANNEL_NAME_ScanDir, DATATYPE_UINT8, 1);
      pointInfo.getChannel(i++).init(CHANNEL_NAME_EdgeFlightLine, DATATYPE_UINT8, 1);
      pointInfo.getChannel(i++).init(CHANNEL_NAME_ClassId, DATATYPE_UINT8, 8);

      if (dataFormat < 6)
         pointInfo.getChannel(i++).init(CHANNEL_NAME_ScanAngle, DATATYPE_SINT8, 8);
      else
         pointInfo.getChannel(i++).init(CHANNEL_NAME_ScanAngle, DATATYPE_SINT16, 16);

      pointInfo.getChannel(i++).init(CHANNEL_NAME_UserData, DATATYPE_UINT8, 8);
      pointInfo.getChannel(i++).init(CHANNEL_NAME_SourceId, DATATYPE_UINT16, 16);

      if (dataFormat != 0 && dataFormat != 2)
      {
         if (reader->header.global_encoding & 0x01)
            pointInfo.getChannel(i++).init(CHANNEL_NAME_GPSTime_Adjusted, DATATYPE_FLOAT64, 64);
         else
            pointInfo.getChannel(i++).init(CHANNEL_NAME_GPSTime_Week, DATATYPE_FLOAT64, 64);

      }

      if(dataFormat == 2 || dataFormat == 3 || dataFormat == 5 || dataFormat == 7 || dataFormat == 8 || dataFormat == 10)
      {
         pointInfo.getChannel(i++).init(CHANNEL_NAME_Red, DATATYPE_UINT16, 16);
         pointInfo.getChannel(i++).init(CHANNEL_NAME_Green, DATATYPE_UINT16, 16);
         pointInfo.getChannel(i++).init(CHANNEL_NAME_Blue, DATATYPE_UINT16, 16);

         if (dataFormat == 8 || dataFormat == 10)
            pointInfo.getChannel(i++).init(CHANNEL_NAME_NearInfrared, DATATYPE_UINT16, 16);
      }
            
      assert(i == numChannels);

      setPointInfo(pointInfo);

      if (header.number_of_point_records > 0)
         setNumPoints(header.number_of_point_records);
      else
         setNumPoints(header.extended_number_of_point_records);

      Bounds bounds(header.min_x,
                    header.max_x,
                    header.min_y,
                    header.max_y,
                    header.min_z,
                    header.max_z);
      setBounds(bounds);

      double scale[3], offset[3];
      scale[0] = header.x_scale_factor;
      scale[1] = header.y_scale_factor;
      scale[2] = header.z_scale_factor;
      offset[0] = header.x_offset;
      offset[1] = header.y_offset;
      offset[2] = header.z_offset;

      setQuantization(scale, offset);

      ST_TIFF* tiff = NULL;
      char *wkt = NULL;
      extractProjectionAndClassNames<LASvlr>(header.number_of_variable_length_records, header.vlrs,
                                             tiff, wkt, m_classId, m_numClasses);
      extractProjectionAndClassNames<LASevlr>(header.number_of_extended_variable_length_records, header.evlrs,
                                              tiff, wkt, m_classId, m_numClasses);

      if (wkt != NULL)
      {
         setWKT(wkt);
         DEALLOC(wkt);
      }

      if (tiff != NULL)
      {
         GTIF* gtiff = GTIFNewSimpleTags(tiff);

         GTIFDefn defn;
         if (gtiff && GTIFGetDefn(gtiff, &defn))
         {
            char *wkt = GTIFGetOGISDefn(gtiff, &defn);
            if (wkt != NULL && *wkt != '\0' &&
                strcmp(wkt, "LOCAL_CS[\"unnamed\",UNIT[\"unknown\",1]]") != 0)
            {
               // check to make sure the EPSG code matches the WKT
               OGRSpatialReferenceH ref = OSRNewSpatialReference(wkt);
               if (ref != NULL)
               {
                  const char *name = OSRGetAuthorityName(ref, NULL);
                  if (name != NULL && strcmp(name, "EPSG") == 0)
                  {
                     int code = atoi(OSRGetAuthorityCode(ref, NULL));

                     OGRSpatialReferenceH ref2 = OSRNewSpatialReference(NULL);
                     if (ref2 != NULL)
                     {
                        if (OSRImportFromEPSG(ref2, code) == OGRERR_NONE)
                        {
                           if (!OSRIsSame(ref, ref2))
                           {
                              // BUG there is no way to delete the Authority
                              // (or any node) using the C API

                              OGR_SRSNode *root = static_cast<OGRSpatialReference *>(ref)->GetRoot();
                              root->DestroyChild(root->FindChild("AUTHORITY"));

                              char *temp = NULL;
                              if (OSRExportToWkt(ref, &temp) == OGRERR_NONE)
                              {
                                 free(wkt);
                                 wkt = temp;
                              }
                           }
                        }
                        OSRDestroySpatialReference(ref2);
                     }
                  }
                  OSRDestroySpatialReference(ref);
               }
               setWKT(wkt);
            }
            free(wkt);
         }
         GTIFFree(gtiff);
         ST_Destroy(tiff);
      }

      if(m_classId == NULL)
      {
         if (dataFormat < 6)
         {
            m_classId = const_cast<char **>(defaultClassId0to5);
            m_numClasses = sizeof(defaultClassId0to5) / sizeof(*defaultClassId0to5);
         }
         else if (dataFormat < 11)
         {
            m_classId = const_cast<char **>(defaultClassId6to10);
            m_numClasses = sizeof(defaultClassId6to10) / sizeof(*defaultClassId6to10);
         }
      }

      reader->close();
   }
   catch(...)
   {
      if (reader != NULL)
         reader->close();
      throw;
   }
}

static double GregorianToJulianDayNumber(int year, int month, int day)
{
   if(month < 3)
   {
      month += 12;
      year -= 1;
   }

   const double c = 2 - floor(year / 100.0) + floor(year / 400.0);

   return floor(1461.0 * (year + 4716.0) / 4.0) + c +
          floor(153.0 * (month + 1) / 5.0) +
          day - 1524.5;
}

static void JulianDayNumberToGregorian(double jd, int &year, int &mouth, int &day)
{
   const double p = floor(jd + 0.5);
   const double s1 = p + 68569;
   const double n = floor(4.0 * s1 / 146097.0);
   const double s2 = s1 - floor((146097.0 * n + 3.0) / 4.0);
   const double i = floor(4000.0 * (s2 + 1) / 1461001.0);
   const double s3 = s2 - floor(1461.0 * i / 4.0) + 31.0;
   const double q = floor(80.0 * s3 / 2447.0);
   const double e = s3 - floor(2447.0 * q / 80.0);
   const double s4 = floor(q / 11.0);
   year = static_cast<int>(100 * (n - 49) + i + s4);
   mouth = static_cast<int>(q + 2 - 12 * s4);
   day = static_cast<int>(e + jd - p + 0.5);
}

template<typename VLR>
static void addVLRMetadata(U32 number_of_variable_length_records, const VLR *vlrs,
                           Metadata &metadata, bool sanitize, bool ignoreNativeWKTMetadata)
{

   for (U32 i = 0; i < number_of_variable_length_records; i += 1)
   {
      const VLR &vlr = vlrs[i];

      // If we are sanitizing this, then only load the VLR records if they
      // are defined in the LAS Spec.  Otherwise get them all.
      bool add = false;

      // bad record of some kind -- we should tell the user.
      if (vlr.user_id == NULL || *vlr.user_id == '\0' ||
          vlr.record_length_after_header == 0 || vlr.data == NULL)
         add = false;
      // skip ClassIds
      else if (::strcmp(LASF_Spec, vlr.user_id) == 0 && vlr.record_id == 0)
         add = false;
      else if (::strcmp(LASF_Spec, vlr.user_id) == 0)
         add = true;
      else if (::strcmp(LASF_Projection, vlr.user_id) == 0)
         add = !ignoreNativeWKTMetadata;
      else
         add = !sanitize;
      if (add)
      {
         char str[64];
         sprintf(str, "%s::%d", vlr.user_id, vlr.record_id);
         metadata.add(str, vlr.description,
                      METADATA_DATATYPE_BLOB,
                      vlr.data, vlr.record_length_after_header);
      }
   }
}


void
LASPointReader::loadMetadata(Metadata &metadata, bool sanitize) const
{
   LASreadOpener opener;
   opener.add_file_name(m_path);
   LASreader *reader = NULL;
   try
   {
      if ((reader = opener.open()) == NULL)
         THROW_LIBRARY_ERROR(LTL_STATUS_FORMATS_LIBLAS_READER)
            ("Failed to create LASReader for path = '%s'", m_path);

      const LASheader &header = reader->header;

      if(header.file_source_ID != 0)
      {
         char str[32];
         snprintf(str, sizeof(str), "%d", static_cast<int>(header.file_source_ID));
         metadata.add(METADATA_KEY_FileSourceID, NULL,
                      METADATA_DATATYPE_STRING, str, 0);
      }

      if (header.project_ID_GUID_data_1 != 0 ||
          header.project_ID_GUID_data_2 != 0 ||
          header.project_ID_GUID_data_3 != 0 ||
          header.project_ID_GUID_data_4[0] != 0 ||
          header.project_ID_GUID_data_4[1] != 0 ||
          header.project_ID_GUID_data_4[2] != 0 ||
          header.project_ID_GUID_data_4[3] != 0 ||
          header.project_ID_GUID_data_4[4] != 0 ||
          header.project_ID_GUID_data_4[5] != 0 ||
          header.project_ID_GUID_data_4[6] != 0 ||
          header.project_ID_GUID_data_4[7] != 0)
      {
         char projectId[128];

         ::sprintf(projectId, "{%08X-%04hX-%04hX-%02hhX%02hhX-%02hhX%02hhX%02hhX%02hhX%02hhX%02hhX}",
                   header.project_ID_GUID_data_1,
                   header.project_ID_GUID_data_2,
                   header.project_ID_GUID_data_3,
                   header.project_ID_GUID_data_4[0],
                   header.project_ID_GUID_data_4[1],
                   header.project_ID_GUID_data_4[2],
                   header.project_ID_GUID_data_4[3],
                   header.project_ID_GUID_data_4[4],
                   header.project_ID_GUID_data_4[5],
                   header.project_ID_GUID_data_4[6],
                   header.project_ID_GUID_data_4[7]);

         metadata.add(METADATA_KEY_ProjectID, NULL,
                      METADATA_DATATYPE_STRING, projectId, 0);
      }

      if(*header.system_identifier != '\0')
         metadata.add(METADATA_KEY_SystemID, NULL,
                      METADATA_DATATYPE_STRING, header.system_identifier, 0);

      if(*header.generating_software != '\0')
         metadata.add(METADATA_KEY_GeneratingSoftware, NULL,
                      METADATA_DATATYPE_STRING, header.generating_software, 0);

      if(header.file_creation_year >= 1900 && header.file_creation_day >= 1)
      {
         double jd = GregorianToJulianDayNumber(header.file_creation_year, 1, 1) + header.file_creation_day - 1;
         int year, mouth, day;
         JulianDayNumberToGregorian(jd, year, mouth, day);

         char creationDate[32];
         ::snprintf(creationDate, sizeof(creationDate), "%04d-%02d-%02d", year, mouth, day);
         metadata.add(METADATA_KEY_FileCreationDate, NULL,
                      METADATA_DATATYPE_STRING, creationDate, 0);
      }

      {
         double returnCounts[5];
         bool haveCounts = false;
         for(int i = 0; i < 5; i += 1)
         {
            if (header.number_of_points_by_return[i] != 0)
               returnCounts[i] = header.number_of_points_by_return[i];
            else
               returnCounts[i] = header.extended_number_of_points_by_return[i];

            if(returnCounts[i] != 0)
               haveCounts = true;
         }
         if(haveCounts)
            metadata.add(METADATA_KEY_PointRecordsByReturnCount, NULL,
                         METADATA_DATATYPE_REAL_ARRAY, returnCounts, 5);
      }

      {
         double bbox[6];
         bbox[0] = header.min_x;
         bbox[1] = header.max_x;
         bbox[2] = header.min_y;
         bbox[3] = header.max_y;
         bbox[4] = header.min_z;
         bbox[5] = header.max_z;
         metadata.add(METADATA_KEY_LASBBox, NULL,
                      METADATA_DATATYPE_REAL_ARRAY, bbox, 6);
      }

      addVLRMetadata<LASvlr>(header.number_of_variable_length_records, header.vlrs,
                             metadata,
                             sanitize, m_ignoreNativeWKTMetadata);

      addVLRMetadata<LASevlr>(header.number_of_extended_variable_length_records, header.evlrs,
                              metadata,
                              sanitize, m_ignoreNativeWKTMetadata);

      reader->close();
   }
   catch(...)
   {
      if (reader != NULL)
         reader->close();
      throw;
   }
}


char const * const *
LASPointReader::getClassIdNames(void) const
{
   return m_classId;
}

size_t
LASPointReader::getNumClassIdNames(void) const
{
   return m_numClasses;
}

const char *
LASPointReader::getFileFormatString(void) const
{
   return m_fileFormatString;
}

PointIterator *
LASPointReader::createIterator(const Bounds &bounds,
                               double fraction,
                               const PointInfo &pointInfo,
                               ProgressDelegate *delegate) const
{
   Scoped<Iterator> iter;
   iter->init(bounds, fraction, pointInfo, delegate, m_path, m_useExtended);
   iter->retain();
   return iter;
}

int
LASPointReader::getRecordFormat(void) const
{
   return m_recordFormat;
}
