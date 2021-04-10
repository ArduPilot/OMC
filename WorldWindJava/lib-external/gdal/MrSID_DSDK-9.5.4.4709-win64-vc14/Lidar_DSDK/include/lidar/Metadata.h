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

#ifndef __LIDAR_METADATA_H__
#define __LIDAR_METADATA_H__

#include "lidar/Base.h"
#include "lidar/Stream.h"
#include <stdio.h>

LT_BEGIN_LIDAR_NAMESPACE

/**
 * Canonical names of Metadata Keys
 */
#define METADATA_KEY_FileSourceID               "FileSourceID"
#define METADATA_KEY_ProjectID                  "ProjectID"
#define METADATA_KEY_SystemID                   "SystemID"
#define METADATA_KEY_GeneratingSoftware         "GeneratingSoftware"
#define METADATA_KEY_FileCreationDate           "FileCreationDate"
#define METADATA_KEY_PointRecordsByReturnCount  "PointRecordsByReturnCount"
#define METADATA_KEY_PreCompressionPointCount   "PreCompressionPointCount"
#define METADATA_KEY_LASBBox                    "LAS_BoundingBox"

/**
 * Metdata data types
 *
 * This enum is used to repersent the data type of metadata values.
 *
 * \note All data types are arrays.
 */
enum MetadataDataType
{
   // don't change the values they are serialized
   // all values must be less then 1 << 16

   METADATA_DATATYPE_INVALID = 0,
   /** A string including the terminating null. */
   METADATA_DATATYPE_STRING = 1,
   /** Block of raw data. */
   METADATA_DATATYPE_BLOB = 2,
   /** An array of doubles. */
   METADATA_DATATYPE_REAL_ARRAY = 3
};

/**
 * Metadata is a container for storing metadata about the point cloud.
 *
 * The Metadata class is a Key-Value container for storing metadata about the
 * point cloud.  It can hold three data types Strings, Arrays of Doubles and 
 * Raw binary data (Blobs).
 *
 * \see See examples/src/DumpMG4Info.cpp dumpMetadata() for an example of using
 *  this class.
 */
class Metadata
{
   SIMPLE_OBJECT(Metadata);
public:
   ~Metadata(void);
   Metadata(void);

   /**
    * Get the number of key-value pairs.
    *
    * This method returns the number of key-value pairs.
    */
   size_t getNumRecords(void) const;

   /**
    * Determine if there is a key-value pair with a given key.
    *
    * The method determines if this object has a pair with the given key.
    *
    * \param key the key name
    * \return true if the key was found
    */
   bool has(const char *key) const;
   
   /**
    * Get the key-value pair at a given index.
    *
    * This method retrieves the idx'th key-value pair.  The last 5 
    *   arguments are output parameters.
    *
    * \param idx the index of the key-value pair
    * \param key the key name
    * \param description a short description of the metadata (can by NULL)
    * \param datatype the data type of the value array
    * \param value the value array
    * \param length number of elements in the value array.
    */
   void get(size_t idx, const char *&key, const char *&description,
            MetadataDataType &datatype,
            const void *&value, size_t &length) const;
   /**
    * Get the key-value pair with a given key.
    *
    * This method retrieves the named key-value pair.  The last 4 
    *   arguments are output parameters.
    *
    * \param key the key name
    * \param description a short description of the metadata (can by NULL)
    * \param datatype the data type of the value array
    * \param value the value array
    * \param length number of elements in the value array.
    * \return true if the key was found
    * \note For stirngs the length includes the '\0'
    */
   bool get(const char *key, const char *&description,
            MetadataDataType &datatype,
            const void *&value, size_t &length) const;
   
   /**
    * Find the index of the key-value pair with the given key.
    *
    * This method returns the index of the first pair with the given key.
    * \param key the key name
    * \return the index of the first key-value pair 
    */
   size_t getKeyIndex(const char *key) const;
   
   /**
    * Get the key name for the given index.
    *
    * \param idx the index of the key-value pair
    * \reutrn the key name
    */
   const char *getKey(size_t idx) const;
   
   /**
    * Get the description for the given index.
    *
    * \param idx the index of the key-value pair
    * \reutrn the description (this may be NULL)
    */
   const char *getDescription(size_t idx) const;
   
   /**
    * Get the datatype for the given index.
    *
    * \param idx the index of the key-value pair
    * \reutrn the datatype
    */
   MetadataDataType getDataType(size_t idx) const;
   
   /**
    * Get the data buffer for the given index.
    *
    * \param idx the index of the key-value pair
    * \reutrn a pointer to the data array
    */
   const void *getValue(size_t idx) const;
   
   /**
    * Get the data buffer length for the given index.
    *
    * \param idx the index of the key-value pair
    * \reutrn the number of elements in the value array
    * \note For stirngs the length includes the '\0'
    */
   size_t getValueLength(size_t idx) const;

   /**
    * Add a key-value pair
    *
    * This method adds a key-value pair to the metadata.
    *
    * \param key the key name
    * \param description a short description of the metadata (can by NULL)
    * \param datatype the data type of the value array
    * \param value the value array
    * \param length number of elements in the value array.  This is ignored for
    *      METADATA_DATATYPE_STRING.
    */
   void add(const char *key, const char *description,
            MetadataDataType datatype,
            const void *value, size_t length);
   /**
    * Add all the key-value pairs from the given Metadata object.
    *
    * The method adds all the key-value pairs in meta.
    *
    * \param meta source Metadata object
    */
   void add(const Metadata &meta);

   /**
    * Remove all key-value pairs.
    *
    * This method removes all the key-value form the container.
    */
   void remove(void);

   /**
    * Remove the key-value pair at a given index.
    *
    * This method removes the idx'th key-value pair.
    *
    * \param idx the index of the key-value pair
    */
   void remove(size_t idx);
   
   /**
    * Remove the first key-value pair with key.
    *
    * This method removes the first key-value pair with the given key.
    *
    * \param key the key name of the key-value pair
    */
   void remove(const char *key);

   /**
    * Sort the pairs.
    *
    * This method sorts the key-value pair list by key name.
    */
   void sort(void);

   /**
    * Wirte metadata to a stream.
    *
    * This method reads the metadata from a stream in a binary format.
    */
   void read(StreamReader &stream);
   
   /**
    * Read metadata from a stream.
    *
    * This method reads the metadata from a stream as a binary format.
    */
   void write(StreamWriter &stream) const;

   /**
    * Write the metadata in a human readable format.
    *
    * The method writes the metadata in a human readable format.
    *
    * \param file the destination Standard C Library FILE object
    */
   void dump(FILE *file) const;

protected:
   struct Record;
   class RecordStore;

   RecordStore *m_records;
};

LT_END_LIDAR_NAMESPACE
#endif // __LIDAR_METADATA_H__
