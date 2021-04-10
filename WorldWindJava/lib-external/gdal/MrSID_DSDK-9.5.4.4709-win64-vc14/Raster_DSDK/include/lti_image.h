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

#ifndef LTI_IMAGE_H
#define LTI_IMAGE_H

// lt_lib_mrsid_core
#include "lti_types.h"
#include "lti_referenceCountedObject.h"


LT_BEGIN_NAMESPACE(LizardTech)

/**
 * abstract class representing an image
 *
 * The LTIImage abstract class represents the basic properties of an image,
 * including dimensions, data type, background color, etc.
 *
 * LTIImage does not support extraction of pixels (decoding); that
 * functionality is provided by the derived class LTIImageStage.
 */
class LTIImage : public LTIReferenceCountedObject
{
   LTI_REFERENCE_COUNTED_BOILERPLATE_BASE(LTIImage);
public:
   /**
    * get image width
    *
    * This function returns the width of the image, in pixels.
    *
    * @return the image width
    */
   virtual lt_uint32 getWidth() const = 0;

   /**
    * get image height
    *
    * This function returns the height of the image, in pixels.
    *
    * @return the image height
    */
   virtual lt_uint32 getHeight() const = 0;

   /**
    * get image width and height at given magnification
    *
    * This function returns the width and height of the image, in pixels,
    * relative to the given magnification.
    *
    * @param  mag    magnification to determine width at
    * @param  width  the image width at the magnification
    * @param  height the image height at the magnification
    * @return status code indicating success or failure
    */
   virtual LT_STATUS getDimsAtMag(double mag,
                                  lt_uint32 &width,
                                  lt_uint32 &height) const = 0;

   /**
    * get number of bands
    *
    * This function returns the number of bands (colors) in the image.  This
    * value is closely related to the colorspace of the image.
    *
    * This function is equivalent to getPixelProps().getNumBands().
    *
    * @return the number of bands in the image
    */
   lt_uint16 getNumBands() const;

   /**
   * get number of bands without alpha
   *
   * This function returns the number of bands (colors) in the image.  This
   * value is closely related to the colorspace of the image.
   *
   * This function is equivalent to getPixelProps().getNumBands().
   *
   * @return the number of bands in the image
   */
   lt_uint16 getNumBandsWithoutAlpha() const;

   /**
    * get colorspace
    *
    * This function returns the colorspace of the image, e.g. RGB or greyscale.
    *
    * This function is equivalent to getPixelProps().getColorSpace().
    *
    * @return the colorspace of the image
    */
   LTIColorSpace getColorSpace() const;

   /**
    * get data type
    *
    * This function returns the data type of the samples in the image, e.g.
    * unsigned byte or 32-bit float.
    *
    * This function is equivalent to getPixelProps().getDataType().
    *
    * @return the datatype of the image
    */
   LTIDataType getDataType() const;

   /**
    * get the pixel properties of the image
    *
    * This function returns an object which describes the basic properties
    * of a pixel in the image.
    *
    * @return the pixel properties of the image
    */
   virtual const LTIPixel& getPixelProps() const = 0;

   /**
    * get the values of the background pixel
    *
    * This function returns a pointer to an object containing the
    * values of the background pixel of the image.  If the pointer
    * is NULL, no background color has been set for the image.
    *
    * @return pointer to the background pixel
    */
   virtual const LTIPixel* getBackgroundPixel() const = 0;

   /**
    * get the values of the "no data" (transparency) pixel
    *
    * This function returns a pointer to an object containing the
    * values of the no data pixel of the image.  If the pointer
    * is NULL, no transparency color has been set for the image.
    *
    * @return pointer to the no data pixel
    */
   virtual const LTIPixel* getNoDataPixel() const = 0;

   /**
    * get the color lookup table, if any
    *
    * This function returns a pointer the color lookup table, used for
    * indexed or palletized images.  Will return NULL if no table
    * is used.
    *
    * @return pointer to the pixel lookup table
    */
   virtual const LTIPixelLookupTable* getPixelLookupTable() const = 0;

   /**
    * get the minimum dynamic range value of image
    *
    * This function returns the pixels of minimum value of the
    * dynamic range of the image.
    *
    * @return  pixel object with the minimum dynamic range values
    */
   virtual const LTIPixel &getMinDynamicRange() const = 0;

   /**
    * get the maximum dynamic range value of image
    *
    * This function returns the pixels of maximum value of the
    * dynamic range of the image.
    *
    * @return  pixel object with the maximum dynamic range values
    */
   virtual const LTIPixel &getMaxDynamicRange() const = 0;

   /**
    * check if the range is "complete" or not
    *
    * This function returns true if the dynamic range min/max values for the
    * image correspond to the full range of the underlying datatype.
    *
    * @return  true if range spans the datatype's range
    */
   bool isNaturalDynamicRange() const;
   /**
    * get the geographic coordinates of the image
    *
    * This function returns the geographic coordinates of the image.
    *
    * Note that if the image has no explicit geographic coordinate information,
    * e.g. stored within the metadata of a file, the geographic coordinates
    * are implicitly set (upperleft is (0,-height), resolution is (1,-1))
    *
    * @return a pointer to an object containing the geographic coordinates
    */
   virtual const LTIGeoCoord& getGeoCoord() const = 0;

   /**
    * is geo information "real" or not
    *
    * This function returns true if the geo information is implicit, i.e.
    * the source image did not have native geo coord info.
    *
    * @return true if and only if the geo information is not "real"
    */
   virtual bool isGeoCoordImplicit() const = 0;

   /**
    * get the metadata associated with the image
    *
    * This function returns a reference to the object containing the
    * metadata associated with the image.
    *
    * @return a reference to the metadata database
    */
   virtual const LTIMetadataDatabase &getMetadata() const = 0;

   /**
    * get the minimum magnification
    *
    * This function returns the minimum magnification
    * of the image.  Images that do not support "zooming out" will return
    * 1.0; images that contain "overviews", e.g. MrSID, will return a
    * value greater than 1.0.
    *
    * @return the minimum magnification
    */
   virtual double getMinMagnification() const = 0;

   /**
    * get the maximum magnification
    *
    * This function returns the maximum magnification
    * of the image.  Images that do not support "zooming in" will return
    * 1.0; images that support "res-up", e.g. MrSID, will return a
    * value less than 1.0.
    *
    * @return the maximum magnification
    */
   virtual double getMaxMagnification() const = 0;
   
   /**
    * check if image supports "random access" decoding
    *
    * Some formats, notably JPEG, do not support "selective" decoding.
    * That is, they require that scenes being decoding must march
    * in order down the scanlines of the image.  Formats like TIFF
    * and MrSID, however, are "selective": any scene can be requested
    * at any time.
    *
    * @return true if and only if the image supports arbitrary scene requests
    */
   virtual bool isSelective() const = 0;

   /**
    * get the modification bitfield for this image
    *
    * This function returns a bitfield describing what kinds of modifications
    * have been made to the image during and since its initial encoding.
    *
    * @param scene     the area pertaining to this request
    * @return          a bitfield itemizing the encoding modifications
    */
   virtual lt_uint32 getModifications(const LTIScene &scene) const = 0;


   /**
    * extract the metadata associated with the image of the given type
    *
    * This function returns XMP or EXIF metadata as a blob.
    *
    * @param type    type of metadata to extract (xmp|exif)
    * @param stream  output stream containing the asked for metadata.  
    *                   The caller must delete the returned stream.
    * @return        status code indicating success or failure
    */
   virtual LT_STATUS getMetadataBlob(const char *type, LTIOStreamInf *&stream) const = 0;


   /**
    * get the nominal size of the image, not considering the alpha band
    *
    * Returns number of bytes of actual data in the image, e.g. as if the image were
    * a raw file but ignoring any alpha band.  This value is simply the product of:
    * \li image width,
    * \li image height,
    * \li samples per pixel (ignoring alpha), and
    * \li bytes per sample.
    *
    * Note this value may be substantially different
    * than the "physical" image size due to compression.
    *
    * @return the nominal size of the image, in bytes, ignoring alpha
    */
   lt_int64 getNominalImageSizeWithoutAlpha() const;

   /**
    * get the nominal size of the image, including the alpha band
    *
    * Returns number of bytes of actual data in the image, e.g. as if the image were
    * a raw file but including any alpha band.  This value is simply the product of:
    * \li image width,
    * \li image height,
    * \li samples per pixel (including alpha), and
    * \li bytes per sample.
    *
    * Note this value may be substantially different
    * than the "physical" image size due to compression.
    *
    * @return the nominal size of the image, in bytes, including alpha
    */
   lt_int64 getNominalImageSizeWithAlpha() const;

   /**
    * get position of a named point
    *
    * Returns the (x,y) position of the given named point.
    *
    * @param  position  the position to be returned
    * @param  x         the x-position of the point
    * @param  y         the y-position of the point
    */
   void getGeoPoint(LTIPosition position, double& x, double& y) const;

   /**
    * return new background pixel
    *
    * Creates and returns a new pixel of the correct background color for the
    * image.  If no background color has been set, a new black pixel will be
    * returned (unless the image is CMYK, in which case a white pixel will be
    * returned).
    *
    * The caller takes ownership of the returned pixel.
    *
    * @return a new background pixel
    */
   LTIPixel* createBackgroundPixel() const;
};

LT_END_NAMESPACE(LizardTech)

#endif // LTI_IMAGE_H
