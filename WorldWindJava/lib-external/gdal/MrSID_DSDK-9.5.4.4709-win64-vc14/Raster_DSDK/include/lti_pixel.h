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

#ifndef LTI_PIXEL_H
#define LTI_PIXEL_H

// lt_lib_mrsid_core
#include "lti_sample.h"


LT_BEGIN_NAMESPACE(LizardTech)


/**
 * basic properties of a pixel
 *
 * This class stores the basic properties of a pixel:
 * \li the number of bands
 * \li the colorspace
 * \li the datatype
 *
 * This is done by representing the pixel as a set of samples.
 * Since the colorspace cannot in general be inferred from the
 * colors in the samples, the user must declare the colorspace
 * for the pixel.
 */
class LTIPixel
{
public:
   /**
    * constructor
    *
    * This constructor creates an invalid LTIPixel object.
    *
    */
   LTIPixel(void);

   /**
    * constructor
    *
    * This constructor creates an LTIPixel object with the given
    * properties: all the samples will be of the same type.
    *
    * @param  colorSpace  the pixel colorspace
    * @param  numBands    the number of bands (samples per pixel)
    * @param  dataType    the datatype of the samples
    * @param  samples     optional sample objects (default is uint8 set to 0)
    */
   LTIPixel(LTIColorSpace colorSpace,
            lt_uint16 numBands,
            LTIDataType dataType,
            const LTISample* samples = NULL);

   /**
    * constructor
    *
    * TBD
    *
    * @param  pixel           the pixel to copy
    * @param  bandSelection   the band to choose (if NULL use the first numBands)
    * @param  numBands        the length of bandSelection
    * @param  keepAlpha       
    */
   LTIPixel(const LTIPixel &pixel,
            const lt_uint16 *bandSelection,
            lt_uint16 numBands);

   /**
    * constructor
    *
    * TBD
    *
    * @param  pixel           the pixel to copy
    * @param  bandSelection   the band to choose (if NULL use the first numBands)
    * @param  numBands        the length of bandSeclection
    * @param  colorSpace
    */
   LTIPixel(const LTIPixel &pixel,
            const lt_uint16 *bandSelection,
            lt_uint16 numBands,
            LTIColorSpace colorSpace);


   /**
    * constructor
    *
    * This constructor creates an LTIPixel object made up of the
    * given sample types.  If the colorspace given is LTI_COLORSPACE_INVALID,
    * then the function will attempt to infer the colorspace from the
    * underlying samples; if there is no obvious colorspace, e.g. RGB,
    * the LTI_COLORSPACE_MULTISPECTRAL will be used.
    *
    * @param  samples     the samples of the pixel
    * @param  numBands    the number of bands (samples per pixel)
    * @param  colorSpace  the overall colorspace
    */
   LTIPixel(const LTISample* samples,
            lt_uint16 numBands,
            LTIColorSpace colorSpace = LTI_COLORSPACE_INVALID);

   /**
    * copy constructor
    */
   LTIPixel(const LTIPixel&);

   /**
    * destructor
    */
   virtual ~LTIPixel();

   /**
    * assignment operator
    */
   LTIPixel& operator=(const LTIPixel&);

   /**
    * equality operator
    */
   bool operator==(const LTIPixel&) const;

   /**
    * equality operator
    */
   bool operator!=(const LTIPixel&) const;

   /**
    * get the number of bands
    *
    * This function returns the number of bands in the pixel (samples
    * per pixel).
    *
    * @return the number of bands
    */
   lt_uint16 getNumBands() const;
   
   /**
    * get the number of bands not counting alpha
    *
    * This function returns the number of bands in the pixel (samples
    * per pixel).
    *
    * @return the number of bands without counting the alpha band
    */
   lt_uint16 getNumBandsWithoutAlpha() const;
   
   /**
    * get the sample datatype
    *
    * This function returns the datatype of the samples.  Returns
    * LTI_DATATYPE_INVALID if the samples are not all the same
    * datatype.
    *
    * @return the datatype of the samples
    */
   LTIDataType getDataType() const;
   bool isSignedData() const;

   /**
    * get the colorspace
    *
    * This function returns the colorspace of the pixel.
    *
    * @return the colorspace of the pixel
    */
   LTIColorSpace getColorSpace() const;

   /**
    * get the colorspace without alpha
    *
    * This function returns the colorspace of the pixel.
    *
    * @return the colorspace of the pixel
    */
   LTIColorSpace getColorSpaceWithoutAlpha() const;

   /**
    * returns true if colorspace has alpha channel flags
    *
    * @return true for colorspaces with alpha channel flags
    */
   bool hasAlphaBand() const;
   
   /**
    * returns true if colorspace has the pre-multiplied alpha channel flag
    *
    * @return true for colorspaces with pre-multiplied alpha channel flag
    */
   bool hasPreMultipliedAlphaBand() const;

   /**
    * returns true if the pixel has multi-spectral bands
    */
   bool isMultiSpectral() const;

   /**
    * get the size of a pixel
    *
    * This function returns the size of a single pixel, in bytes.
    *
    * This is equivalent to the sum of getBytesPerSample() for each
    * of the samples.
    *
    * @return the number of bytes per pixel
    */
   lt_uint32 getNumBytes() const;

   /**
    * get the size of a pixel not counting the alpha band
    *
    * This function returns the size of a single pixel, in bytes.
    *
    * This is equivalent to the sum of getBytesPerSample() for each
    * of the samples.
    *
    * @return the number of bytes per pixel
    */
   lt_uint32 getNumBytesWithoutAlpha() const;
   
   /**
    * returns the largest size of any sample
    *
    * Returns the largest size of any sample.  This is equivalent to the
    * computing the maximum of getBytesPerSample() for each of the samples.
    *
    * @return  the number of bytes per sample
    */
   lt_uint32 getMaxBytesPerSample() const;

   /**
    * returns status code comparing two pixels
    *
    * Returns status code comparing two pixels.  This is just a different
    * version of operator==, which returns a status code instead of a bool.
    *
    * @param   pixel  the sample to compare this sample to
    * @return  a specific code indicating if impedance matches
    */
   LT_STATUS checkImpedance(const LTIPixel& pixel,
                            bool enforceColorSpace = true) const;
   LT_STATUS checkImpedanceWithoutAlpha(const LTIPixel& pixel) const;

   LTISample *getSamples() const;
   LTISample &getSample(lt_uint16) const;

//#define DEPRECATE_PIXEL_HELPERS
#ifndef DEPRECATE_PIXEL_HELPERS
   /**
    * @name Helper functions to get sample values
    */
   /*@{*/

   /**
    * returns the address of the specified sample's value
    *
    * Returns the address of the specified sample's value.  The caller must
    * cast the pointer to the appropriate type before using.
    *
    * @param  band  the band number of the sample to use
    * @return  the address of the sample's value
    */
   const void* getSampleValueAddr(lt_uint16 band) const;

   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the UINT8 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   lt_uint8 getSampleValueUint8(lt_uint16 band) const;

   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the SINT8 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   lt_int8 getSampleValueSint8(lt_uint16 band) const;

   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the UINT16 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   lt_uint16 getSampleValueUint16(lt_uint16 band) const;

   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the SINT16 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   lt_int16 getSampleValueSint16(lt_uint16 band) const;

   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the UINT32 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   lt_uint32 getSampleValueUint32(lt_uint16 band) const;
   
   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the SINT32 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   lt_int32 getSampleValueSint32(lt_uint16 band) const;

   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the FLOAT32 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   float getSampleValueFloat32(lt_uint16 band) const;

   /**
    * returns the specified sample's value
    *
    * Returns the specified sample's value.  The sample is assumed to be
    * known to have the FLOAT64 datatype.
    *
    * @param  band  the band number of the sample to use
    * @return  the sample's value
    */
   double getSampleValueFloat64(lt_uint16 band) const;
#endif 

   /**
    * are all samples equal to the minimum value
    *
    * Returns true if all samples are the the minimum value of the datatype.
    */
   bool areSampleValuesMin() const;

   /**
    * are all samples equal to the maximum value
    *
    * Returns true if all samples are the the maximum value of the datatype.
    */
   bool areSampleValuesMax() const;


   /*@}*/

   /**
    * @name Helper functions to set sample values
    */
   /*@{*/

   /**
    * sets all samples to minimum
    *
    * Sets all samples to the minimum value of the datatype.
    */
   void setSampleValuesToMin();

   /**
    * sets all samples to maximum
    *
    * Sets all samples to the maximum value of the datatype.
    */
   void setSampleValuesToMax();

#ifndef DEPRECATE_PIXEL_HELPERS
   /**
    * sets sample value by address
    *
    * Sets sample value to value pointed to.
    *
    * @param  band  which sample to set
    * @param  data  value to use
    */
   void setSampleValueAddr(lt_uint16 band, const void* data) const;

   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the UINT8 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesUint8(lt_uint8 value);

   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the SINT8 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesSint8(lt_int8 value);

   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the UINT16 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesUint16(lt_uint16 value);

   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the SINT16 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesSint16(lt_int16 value);

   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the UINT32 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesUint32(lt_uint32 value);
   
   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the SINT32 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesSint32(lt_int32 value);
   
   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the FLOAT32 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesFloat32(float value);

   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value of the datatype.  The samples are
    * assumed to be known to have the FLOAT64 datatype.
    *
    * @param  value  the value to set the samples to
    */
   void setSampleValuesFloat64(double value);
#endif

   /**
    * sets all samples to the given value
    *
    * Sets all samples to the given value.  The value will be coerced to each
    * sample's actual datatype.
    */
   void setSampleValuesFromDouble(double value);
   void setNonAlphaSampleValuesFromDouble(double value);
   void setSampleValuesFromPixel(const LTIPixel &src);
   void setNonAlphaSampleValuesFromPixel(const LTIPixel &src);

#ifndef DEPRECATE_PIXEL_HELPERS
   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the UINT8 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueUint8(lt_uint16 band, lt_uint8 value);

   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the SINT8 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueSint8(lt_uint16 band, lt_int8 value);

   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the UINT16 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueUint16(lt_uint16 band, lt_uint16 value);

   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the SINT16 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueSint16(lt_uint16 band, lt_int16 value);

   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the UINT32 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueUint32(lt_uint16 band, lt_uint32 value);
   
   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the SINT32 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueSint32(lt_uint16 band, lt_int32 value);
   
   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the FLOAT32 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueFloat32(lt_uint16 band, float value);

   /**
    * sets the given sample to the given value
    *
    * Sets the given sample to the given value of the datatype.  The sample is
    * assumed to be known to have the FLOAT64 datatype.
    *
    * @param  band   the band number of the sample to use
    * @param  value  the value to set the samples to
    */
   void setSampleValueFloat64(lt_uint16 band, double value);

   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the UINT8 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesUint8(const lt_uint8 values[]);

   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the SINT8 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesSint8(const lt_int8 values[]);

   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the UINT16 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesUint16(const lt_uint16 values[]);

   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the SINT16 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesSint16(const lt_int16 values[]);

   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the UINT32 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesUint32(const lt_uint32 values[]);
   
   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the SINT32 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesSint32(const lt_int32 values[]);

   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the FLOAT32 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesFloat32(const float values[]);

   /**
    * sets all samples to the given values
    *
    * Sets all samples to the given values of the datatype.  The sample is
    * assumed to be known to have the FLOAT64 datatype.
    *
    * @param  values  the values to set the samples to
    */
   void setSampleValuesFloat64(const double values[]);
   /*@}*/
#endif
   
   LT_STATUS getBandSelection(lt_uint16 *&bandSelection) const;
   /**
   * checks to ensure band mapping is valid
   *
   * This checks that the datatypes are the same and that the bandmapping in dstPixelProps is valid for the srcPixelProps
   *  (that is, you can't ask for the 5th band of a 3-banded image)
   *
   * @param dstPixelProps has the subset of the bands we are interested in
   * @param srcPixelProps has all the bands
   */
   static LT_STATUS checkCompatible(const LTIPixel &dstPixelProps,
                                    const LTIPixel &srcPixelProps);

private:
   void createSamples(LTIDataType dt, lt_uint16 numSamples);

   void copySamples(const LTISample *samples, lt_uint16 numSamples,
                    const lt_uint16 *bandSelection, lt_uint16 numBands);

   LTISample* m_samples;
   lt_uint16 m_numBands;
   LTIColorSpace m_colorSpace;
};


LT_END_NAMESPACE(LizardTech)


#endif // LTI_PIXEL_H
