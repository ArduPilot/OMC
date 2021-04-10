/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2004 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef MRSIDIMAGEREADERBASE_H
#define MRSIDIMAGEREADERBASE_H

// lt_lib_mrsid_core
#include "lti_types.h"
#include "lti_geoImageReader.h"

LT_BEGIN_NAMESPACE(LizardTech)

class MrSIDImageStageManager;
class MrSIDPasswordDelegate;
class MrSIDSimplePasswordDelegate;


/*
 * memory settings for creating MrSID decoders
 *
 * "Small", "medium", and "large" refer to how much memory the
 * decoder will use when opening the image and constructing certain
 * internal data structures and tables.  In general, decode
 * performance will increase if more memory can be used.
 */
enum MrSIDMemoryUsage
{
   MRSID_MEMORY_USAGE_INVALID    = 0,
   MRSID_MEMORY_USAGE_DEFAULT    = 1,
   MRSID_MEMORY_USAGE_SMALL      = 2,
   MRSID_MEMORY_USAGE_MEDIUM     = 3,
   MRSID_MEMORY_USAGE_LARGE      = 4
};

/*
 * stream settings for creating MrSID decoders
 *
 * Normally, the stream used by the decoder is
 * only opened when doing actual decode work, as resources like
 * file handles can be a scarce resource in some environments.  This
 * obviously incurs a performance penalty; the "KeepStreamOpen" modes
 * can be used to change the behaviour.
 */
enum MrSIDStreamUsage
{
   MRSID_STREAM_USAGE_INVALID    = 0,
   MRSID_STREAM_USAGE_KEEPOPEN   = 2,
   MRSID_STREAM_USAGE_KEEPCLOSED = 3,
   MRSID_STREAM_USAGE_DEFAULT    = MRSID_STREAM_USAGE_KEEPCLOSED
};


/**
 * base class for MrSID image readers
 *
 * All the MrSID image readers (MrSIDImageReader, MG2ImageReader
 *  and MG3ImageReader) inherit from this class.
 */
class MrSIDImageReaderInterface
{
   LT_DISALLOW_COPY_CONSTRUCTOR(MrSIDImageReaderInterface);
public:
   virtual ~MrSIDImageReaderInterface();

   /**
    * get number of resolution levels
    *
    * Returns the number of resolution levels supported by the image.
    * This value returned corresponds to the LTIImage::getMinMagnification()
    * function.
    *
    * @return the number of resolution levels in the MrSID image
    */
   virtual lt_uint8 getNumLevels() const = 0;

   /**
    * image encryption query
    *
    * Returns true iff the image is password-protected.  If the image is
    * locked, the setPasswordDelegate() or setPassword() function must be
    * used to provide the decoder with information to decrypt the image
    * as decode requests are processed.
    *
    * @return true, if image is password-protected
    */
   virtual bool isLocked() const = 0;

   /**
    * set password handler
    *
    * This function is used to set up a password delegate, which will be
    * automatically called form within the internal decoder logic to obtain
    * a text password, if one is needed for decoded the image.
    *
    * Alternatively, the more direct setPassword() function may be used.
    *
    * See the isLocked() function for more details.
    * 
    * @param  passwordDelegate  the delegate to be called
    */
   void setPasswordDelegate(MrSIDPasswordDelegate* passwordDelegate);

   /**
    * set password handler
    *
    * This function is set the password used by the decoder logic
    * to decode the image, if one is needed.
    *
    * The password must be set prior to performing any decode (read)
    * requests; for more flexibility, the setPasswordDelegate() function
    * may be used.
    *
    * See the isLocked() function for more details.
    * 
    * @param  password  the password for the image
    */
   void setPassword(const lt_utf8* password);

public:
   /**
    * set the maximum number of worker threads
    *
    * This function sets the maximum number of worker threads that can
    * be used during a decode request.
    *
    * @param numThreads the maximum number of threads to use, including the calling thread
    *                   numThreads < 1 defaults to the number of logical cores
    *
    * @note: The internal decoder may use fewer threads.  For example, MG2
    *     does not use wroker threads.
    * @note: The internal decoder may limit the number of threads
    *     to the number of logical core on the system.
    */
   virtual void setMaxWorkerThreads(int numThreads) = 0;

   /**
    *  get the maximum number of worker threads
    *
    * This function gets the maximun number of worker threads that can
    * be used during a decode request.
    *
    * @see setMaxWorkerThreads() for details.
    *
    * @return the maximum number of worker threads that may be used
    *
    */
   virtual int getMaxWorkerThreads() const = 0;

public:
   /**
    * get MrSID generation
    *
    * Returns the MrSID generation number for a specific MrSID image.  This is
    * a static function, which is passed a filename.
    *
    * The \a gen value returned will be 2 (for MrSID/MG2), 3 (for MrSID/MG3),
    * 4 (for MrSID/MG4) or 0 (if error).
    *
    * @param fileSpec  the file to get the version of
    * @param gen       the MrSID generation
    * @param raster    is the file raster or point cloud?
    * @return status   code indicating success or failure
    */
   static LT_STATUS getMrSIDGeneration(const LTFileSpec& fileSpec, lt_uint8& gen, bool &raster);

   /**
    * get MrSID generation
    *
    * Returns the MrSID generation number for a specific MrSID image.  This is
    * a static function, which is passed a stream.
    *
    * The \a gen value returned will be 2 (for MrSID/MG2), 3 (for MrSID/MG3),
    * 4 (for MrSID/MG4) or 0 (if error).
    *
    * @param stream    the file to get the version of
    * @param gen       the MrSID generation
    * @param raster    is the file raster or point cloud?
    * @return status   code indicating success or failure
    */
   static LT_STATUS getMrSIDGeneration(LTIOStreamInf& stream, lt_uint8 &gen, bool &raster);

   /**
    * get MrSID generation
    *
    * Returns the MrSID generation number for a specific MrSID image.  This is
    * a static function, which is passed an 8-byte buffer.
    *
    * The \a gen value returned will be 2 (for MrSID/MG2), 3 (for MrSID/MG3),
    * 4 (for MrSID/MG4) or 0 (if error).
    *
    * @param version   the full version signature (1st 8 bytes of the file)
    * @param gen       the MrSID generation
    * @param raster    is the file raster or point cloud?
    * @return status   code indicating success or failure
    */
   static LT_STATUS getMrSIDGeneration(const lt_uint8 version[8], lt_uint8& gen, bool &raster);

   /**
    * get MrSID generation
    *
    * Returns the MrSID generation number for the image.
    *
    * The \a gen value returned will be 2 (for MrSID/MG2), 3 (for MrSID/MG3),
    * 4 (for MrSID/MG4) or 0 (if error).
    *
    * @param gen       the major version number
    * @param raster    is the file raster or point cloud?
    * @return status   code indicating success or failure
    */
   LT_STATUS getMrSIDGeneration(lt_uint8 &gen, bool &raster) const;

   /**
    * get MrSID image version (for LizardTech internal use only)
    *
    * Returns detailed version information for the MrSID image.  Typical
    * version numbers will be 1.0.1 for MG2 (the \a letter value is not used),
    * 3.0.26.q for MG3, 3.4.0.a for MG4 raster, and 4.0.0.a for MG4 lidar.
    * To avoid confusion, developers should avoid interpreting the meaning of
    * this number, but rather use getMrSIDGeneration().
    *
    * @param major  the major version number
    * @param minor  the minor version number
    * @param tweak  the revision number
    * @param letter the revision build number (not used by MG2)
    */
   LT_STATUS getMrSIDVersion(lt_uint8& major, lt_uint8& minor,
                             lt_uint8& tweak, char& letter) const;

protected:
   MrSIDImageReaderInterface();
   LT_STATUS init(MrSIDMemoryUsage memoryUsage,
                  MrSIDStreamUsage streamUsage,
                  const lt_uint8 preamble[8]);

   static LT_STATUS getGeoCoordFromMetadata(LTIMetadataDatabase &metadata,
                                            LTIGeoCoord &geoCoord,
                                            bool &hasGeo);

   static LTIOStreamInf *openWorldFileStream(const LTFileSpec &fileSpec,
                                             bool useWorldFile);

   MrSIDMemoryUsage m_memoryUsage;
   MrSIDStreamUsage m_streamUsage;
   lt_uint8 m_magic[8];

private:
   MrSIDPasswordDelegate* m_pwdDelegate;
   MrSIDSimplePasswordDelegate* m_localPwdDelegate;
};


class MrSIDSingleImageReaderBase : public LTIGeoImageReader,
                                   public MrSIDImageReaderInterface
{
   LT_DISALLOW_COPY_CONSTRUCTOR(MrSIDSingleImageReaderBase);
public:
   // LTIImageStage
   virtual lt_int64 getEncodingCost(const LTIScene& scene) const;
   virtual lt_uint32 getModifications(const LTIScene &scene) const;

protected:
   MrSIDSingleImageReaderBase(bool supportBandSelection);
   ~MrSIDSingleImageReaderBase(void);

   LT_STATUS init(MrSIDImageStageManager *manager);

   MrSIDImageStageManager *m_manager;
};

LT_END_NAMESPACE(LizardTech)

#endif // MRSIDIMAGEREADERBASE_H
