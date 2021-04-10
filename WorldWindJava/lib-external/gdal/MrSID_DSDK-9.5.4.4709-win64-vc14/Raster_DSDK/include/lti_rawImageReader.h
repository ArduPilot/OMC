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

#ifndef LTIRAWIMAGEREADER_H
#define LTIRAWIMAGEREADER_H

// lt_lib_mrsid_core
#include "lti_geoImageReader.h"


LT_BEGIN_NAMESPACE(LizardTech)


/**
 * class for reading RAW files
 *
 * This class reads a RAW image.
 *
 * The RAW format used is simple packed BIP form.
 */
class LTIRawImageReader : public LTIGeoImageReader
{
   LTI_REFERENCE_COUNTED_BOILERPLATE(LTIRawImageReader);
public:
   /**
    * initialize (stream)
    *
    * This function constructs an LTIImageReader using the data in the
    * stream with the image properties specified.  The input taken from a
    * stream.
    *
    * @param stream        stream containing the RAW image data (may not be NULL)
    * @param pixelProps    the pixel properties of the image (colorspace, datatype, etc)
    * @param width         the width of the image
    * @param height        the height of the image
    * @param useWorldFile  use world file information if available
    */
   LT_STATUS initialize(LTIOStreamInf* stream,
                        const LTIPixel& pixelProps,
                        lt_uint32 width,
                        lt_uint32 height,
                        bool useWorldFile = true);

   /**
    * initialize (FileSpec)
    *
    * This function constructs an LTIImageReader using the data in the
    * stream with the image properties specified. The input taken from a
    * stream, and the background and nodata pixels may be specified.
    *
    * @param fileSpec      file containing the RAW image data
    * @param pixelProps    the pixel properties of the image (colorspace, datatype, etc)
    * @param width         the width of the image
    * @param height        the height of the image
    * @param useWorldFile  use world file information if available
    */
   LT_STATUS initialize(const LTFileSpec& fileSpec,
                        const LTIPixel& pixelProps,
                        lt_uint32 width,
                        lt_uint32 height,
                        bool useWorldFile = true);

   /**
    * initialize (char*)
    *
    * This function constructs an LTIImageReader using the data in the
    * stream with the image properties specified.  The input taken from a
    * stream, and the background and nodata pixels may be specified.
    *
    * @param file          file containing the RAW image data (may not be NULL)
    * @param pixelProps    the pixel properties of the image (colorspace, datatype, etc)
    * @param width         the width of the image
    * @param height        the height of the image
    * @param useWorldFile  use world file information if available
    */
   LT_STATUS initialize(const char* file,
                        const LTIPixel& pixelProps,
                        lt_uint32 width,
                        lt_uint32 height,
                        bool useWorldFile = true);


   /**
    * @name Properties of the RAW image
    *
    * These must be called prior to calling initialize(), unlike most other SDK
    * objects.
    */
   /*@{*/

   /**
    * set stream ownership
    *
    * Sets the ownership of the stream, to indicate responsibility for
    * deleting the stream when done. This only pertains to objects which were
    * passed a stream in the ctor.
    *
    * If not set, the default is for the object to not take ownership of the
    * stream.
    *
    * @note This must be called prior to calling initialize().
    *
    * @param  takeOwnership  set to true to have the reader delete the stream
    */
   void setStreamOwnership(bool takeOwnership);

   /**
    * sets the number of bytes in each row
    *
    * Sets the number of bytes in each row.  
    *
    * If not set, the bytes per row is simply the number of data items per
    * row times the size of a data item.  (Note that a "data item" in this
    * case may be either a pixel or a sample, depending on the layout being
    * used.)
    *
    * @note This must be called prior to calling initialize().
    *
    * @param  rowBytes  the number of bytes per row
    */
   void setRowBytes(lt_uint32 rowBytes);

    /**
    * sets the layout or organization of the raw data
    *
    * Sets the layout or organization of the raw data.
    *
    * If not set, the default is BIP format.
    *
    * @note This must be called prior to calling initialize().
    *
    * @param  layout  the layout of the raw data
    */
   void setLayout(LTILayout layout);

    /**
    * sets the number of bytes to skip at the beginning and end of the file
    *
    * Sets the number of bytes to skip at the beginning of the file and
    * the end of the file.  (The "ending skip bytes" property is sometimes
    * required because LTIRawImageReader::initialize() does a sanity check
    * to make sure the number of data bytes in the file is equal to the
    * number of bytes needed for the given width, height, etc.
    *
    * @note This must be called prior to calling initialize().
    *
    * @param  leadingBytes  the number of bytes to skip at the top
    * @param  trailingBytes  the number of bytes to skip at the bottom
    */
   void setSkipBytes(lt_int64 leadingBytes, lt_int64 trailingBytes = 0);

   /**
    * sets endianness of input file
    *
    * Sets the byte order or endianness of the input file.
    *
    * @note This must be called prior to calling initialize().
    *
    * @param  byteOrder  the endian setting to use
    */
   void setByteOrder(LTIEndian byteOrder);

   /*@}*/

protected:
   LT_STATUS decodeBegin(const LTIPixel &pixelProps,
                         const LTIScene &fullScene);
   LT_STATUS decodeStrip(LTISceneBuffer &stripBuffer,
                         const LTIScene &stripScene);
   LT_STATUS decodeEnd(void);

   const char *getSourceName(void) const;

protected:
   LTFileSpec* m_fileSpec;

private:
   LTIOStreamInf* m_stream;
   bool m_ownsStream;
   lt_int64 m_fileSize;
   lt_uint32 m_rowBytes;
   LTILayout m_layout;
   lt_int64 m_leadingSkipBytes;
   lt_int64 m_trailingSkipBytes;
   LTIEndian m_byteOrder;

};


LT_END_NAMESPACE(LizardTech)


#endif // LTIRAWIMAGEREADER_H
