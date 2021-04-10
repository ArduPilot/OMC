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

#ifndef LTI_SCENEBUFFER_H
#define LTI_SCENEBUFFER_H

// lt_lib_mrsid_core
#include "lti_types.h"


LT_BEGIN_NAMESPACE(LizardTech)

/**
 * class to hold data passed between image stages
 *
 * This class holds a buffer of data which is used as the target of decode
 * operations in LTIImageStage.
 *
 * The data within the buffer is always represented as an array of bytes in
 * BSQ (band-sequential) format.  Each band is stored separately, so that the
 * underlying data is an array of N pointers, each array element being a
 * buffer for one band of the image.
 *
 * The dimensions of the buffer are set in the constructor.  The \b total
 * number of rows and columns represents the actual extent of the data array
 * in memory.  However, it is often desirable to only expose a subset of the
 * full rectangle, e.g. to access a large buffer in a stripwise fashion or to
 * overlay a small image into a large buffer.  This \b window may also be set
 * via the constructor, by providing a second set of row/column dimensions
 * and giving an offset for the upper-left position of the window.
 *
 * If the data pointer passed to the constructor is NULL, the class will
 * internally allocate the required memory (and retain ownership of it).
 *
 * Functions are provided to access that data within the buffer in a variety
 * of ways, relative to both the total buffer and the exposed window within
 * it.  You may also construct a buffer which is relative to another buffer.
 *
 * For convenience, a number of functions are also provided which allow the
 * user to copy data to and from an LTISceneBuffer object, using a variety of
 * formats.  For example, there are functions to import and export the data
 * in the buffer to BIP (band-interleaved) format.
 *
 * @note The pixel properties of the LTISceneBuffer must exactly match the
 * pixel properties of the image being decoded.
 */

class LTISceneBuffer
{
   LT_DISALLOW_COPY_CONSTRUCTOR(LTISceneBuffer);
public:
   static const double DefaultFuzzyThreshold;

   /**
    * @name Constructors and destructor
    */
   /*@{*/
   /** destructor */
   ~LTISceneBuffer();

   /**
    * constructor with default window
    *
    * Constructs an LTISceneBuffer with the window set to the total region of
    * the buffer.
    *
    * The \c data parameter may be NULL, in which case the memory will be
    * allocated internally.
    *
    * @param  pixelProps    pixel type to be used in the buffer
    * @param  numCols       width of the buffer
    * @param  numRows       height of the buffer
    * @param  bsqData       pointer to the data array (may be NULL)
    * @param  totalNumCols  rowBytes = sizeof(datatype) * totalNumCols
    */
   LTISceneBuffer(const LTIPixel &pixelProps,
                  lt_uint32 numCols,
                  lt_uint32 numRows,
                  void **bsqData,
                  lt_uint32 totalNumCols = 0);

   /**
    * constructor with explicit window
    *
    * Constructs an LTISceneBuffer with the window set to the given size and
    * positioned at the given offset.  The offset is given relative to the
    * total region, and the window must lie entirely within the region.
    *
    * The \c data parameter may be NULL, in which case the memory will be
    * allocated internally.
    *
    * @param  pixelProps     pixel type to be used in the buffer
    * @param  totalNumCols   width of the buffer
    * @param  totalNumRows   height of the buffer
    * @param  colOffset      x-position of the window
    * @param  rowOffset      y-position of the window
    * @param  windowNumCols  width of the window
    * @param  windowNumRows  height of the window
    * @param  data           pointer to the data array (may be NULL)
    */

   LTISceneBuffer(const LTIPixel &pixelProps,
                  lt_uint32 totalNumCols,
                  lt_uint32 totalNumRows,
                  lt_uint32 colOffset,
                  lt_uint32 rowOffset,
                  lt_uint32 numCols,
                  lt_uint32 numRows,
                  void **bsqData);

   /**
    * constructor to overlay existing LTISceneBuffer
    *
    * Constructs an LTISceneBuffer which is a window into the given existing
    * LTISceneBuffer object.  The window of the new buffer is set to start at
    * the given offset (which is relative to the window of the original
    * buffer).  The dimensions of the new window are passed in, and the new
    * window must not extend beyond the dimensions of the original buffer.
    *
    * @param  original   the existing buffer, to be overlaid
    * @param  colOffset  x-position of the new window
    * @param  rowOffset  y-position of the new window
    * @param  windowNumCols  width of the window
    * @param  windowNumRows  height of the window
    */
   LTISceneBuffer(const LTISceneBuffer &original,
                  lt_uint32 colOffset,
                  lt_uint32 rowOffset,
                  lt_uint32 numCols,
                  lt_uint32 numRows);

   /**
    * constructor to overlay existing LTISceneBuffer
    *
    * Constructs an LTISceneBuffer which is a window into the given existing
    * LTISceneBuffer object.  The window of the new buffer is set to cover the
    * full window of the original buffer, starting at the given offset
    * (which is relative to the window of the original buffer).
    *
    * @param  original   the existing buffer, to be overlaid
    * @param  colOffset  x-position of the new window
    * @param  rowOffset  y-position of the new window
    */
   LTISceneBuffer(const LTISceneBuffer &original,
                  lt_uint32 colOffset,
                  lt_uint32 rowOffset);

   /*@}*/

   /**
    * @name Buffer property accessors
    */
   /*@{*/

   /**
    * get width of buffer
    *
    * Returns the width of the buffer.
    *
    * @return the width, in pixels
    */
   lt_int32 getNumCols() const;

   /**
    * get height of buffer
    *
    * Returns the height of the buffer.
    *
    * @return the height, in pixels
    */
   lt_int32 getNumRows() const;

   /**
    * get width of (entire) buffer
    *
    * Returns the total width of the buffer. 
    * RowBytes equals TotalNumCols * sizeof(DataType).
    *
    * @return the total width, in pixels
    */
   lt_int32 getTotalNumCols() const;

   
   /**
    * get size of exposed window
    *
    * Returns the total size of the window of the buffer, in pixels.
    *
    * This is equal to getNumCols() * getNumRows().
    *
    * @return size of exposed in buffer
    */
   lt_int32 getNumPixels() const;

   /**
    * get pixel type of buffer
    *
    * Returns the pixel type of the buffer.
    *
    * @return the pixel type
    */
   const LTIPixel &getPixelProps() const;

   /**
    * get number of bands
    *
    * Returns the number of bands of the pixel of the imager.
    *
    * This is the same as calling getPixelProps().getNumBands().
    *
    * @return the number of bands
    */
   lt_uint16 getNumBands() const;
   
   lt_uint16 getSourceBandIndex(lt_uint16 dstBand) const;

   // return -1 for not wanted
   int getDestinationBandIndex(lt_uint16 srcBand) const;
   /*@}*/

   /**
    * @name Data buffer accessors
    */
   /*@{*/

   
   /**
    * get pointer to data (for all bands)
    *
    * This function returns a pointer to the array of data buffers, one per
    * band.
    *
    * @return  a pointer to the array of data buffers
    */
   void **getBSQData() const;
   
   /**
    * get pointer to data (for 1 band)
    *
    * This function returns a pointer to the data buffer for the given band.
    *
    * @param   band  the band to access
    * @return  a pointer to the array of data buffers
    */
   void *getBandData(lt_uint16 band) const;
   
   /**
    * get pointer to sample
    *
    * This function returns a pointer to the data for the given band of the
    * specified pixel.
    *
    * @param   x     the x-position of the pixel
    * @param   y     the y-position of the pixel
    * @param   band  the band to access
    * @return  a pointer to the sample
    */
   void *getSample(lt_uint32 x, lt_uint32 y, lt_uint16 band) const;

   /*@}*/

   /**
    * @name Import functions
    *
    * These functions provide an easy way to copy data from a variety of
    * layouts into an LTISceneBuffer object, in an efficient manner.
    *
    * The copying is performed relative to the exposed window of the buffer.
    */
   /*@{*/

   /**
    * import from another LTISceneBuffer
    *
    * This function copies data from one source LTISceneBuffer object into
    * another destination LTISceneBuffer object.
    *
    * @param   sourceData   the data to be imported
    * @return  status code indicating success or failure
    */
    LT_STATUS importData(const LTISceneBuffer &sourceData);

   /**
    * import one band from another LTISceneBuffer
    *
    * This function copies just one band of data from one source LTISceneBuffer object into
    * another destination LTISceneBuffer object.
    *
    * @param   dstBand      the band number of this buffer to be written to
    * @param   sourceData   the data to be imported
    * @param   srcBand      the band number of \a sourceData to be read from
    * @return  status code indicating success or failure
    */
    LT_STATUS importDataBand(lt_uint16 dstBand,
                            const LTISceneBuffer& sourceData,
                            lt_uint16 srcBand);

   /**
    * Merge pixels from another LTISceneBuffer, observing transparency.
    *
    * This function copies pixel data from one LTISceneBuffer into another,
    * observing pixel transparency and possibly applying heuristics to help
    * achieve a good mosaicking result.  The source and destination nodata
    * values, as well as any available alpha channel, will be used together
    * to determine pixel transparency.
    *
    * @param   fillMethod   specifies the heuristic to use in merging the data
    * @param   srcBuf       the buffer containing data to be imported
    * @param   srcNodata    transparency sentinel pixel for the source buffer (may be NULL)
    * @param   dstNodata    transparency sentinel pixel for the destination buffer (may be NULL)
    * @param   mag          scene magnification, used in merging heuristics
    * @param   fuzzyThreshold the threshold used when doing fuzzy no-data merging
    * @param   blend        in presence of alpha channel, use alpha blending
    * @return  status code indicating success or failure
    */
   LT_STATUS mergeData(LTIPixelFillMethod fillMethod,
                       const LTISceneBuffer& srcBuf,
                       const LTIPixel *srcNodata,
                       const LTIPixel *dstNodata,
                       double mag,
                       double fuzzyThreshold,
                       bool blend);

   /**
    * import from memory (BSQ)
    *
    * This function copies data from a buffer in memory.  The source pointer
    * is assumed to be organized as an array of pointers to BSQ buffers, one
    * per band.
    *
    * @param   data  the source data
    * @return  status code indicating success or failure
    */
   LT_STATUS importDataBSQ(void** data);

   /**
    * import from memory (BSQ)
    *
    * This function copies data from a buffer in memory.  The source pointer
    * is assumed to be organized as one large buffer in BSQ format.
    *
    * @param   data  the source data
    * @return  status code indicating success or failure
    */
   LT_STATUS importDataBSQ(void* data);

   /**
    * import from memory (BIP)
    *
    * This function copies data from a buffer in memory.  The source pointer
    * is assumed to be organized as one large buffer in BIP format.
    *
    * @param   data  the source data
    * @return  status code indicating success or failure
    */
   LT_STATUS importDataBIP(void* data);

   /**
    * import from stream (BSQ)
    *
    * This function copies data from a buffer contained in the given stream.
    * The data is assumed to be organized as one large buffer in BSQ format.
    *
    * @param   stream  the source data
    * @return  status code indicating success or failure
    */
   LT_STATUS importDataBSQ(LTIOStreamInf& stream);

   /**
    * import from stream (BIP)
    *
    * This function copies data from a buffer contained in the given stream.
    * The data is assumed to be organized as one large buffer in BIP format.
    *
    * @param   stream  the source data
    * @return  status code indicating success or failure
    */
   LT_STATUS importDataBIP(LTIOStreamInf& stream);
   /*@}*/

   // for LizardTech internal use only
   LT_STATUS importData(const LTIMask *binaryMask,
                        const LTISceneBuffer &sourceData,
                        bool blend);

   // for LizardTech internal use only
   LT_STATUS importDataBand(lt_uint16 dstBand,
                            const LTISceneBuffer& sourceData,
                            lt_uint16 srcBand,
                            const LTIMask *binaryMask);

   // for LizardTech internal use only
   LT_STATUS mergeData(LTIPixelFillMethod fillMethod,
                       const LTISceneBuffer& srcBuf,
                       const LTIPixel *srcNodata,
                       const LTIPixel *dstNodata,
                       LTIMaskSource *mask,
                       LTIScene &scene,
                       double fuzzyThreshold,
                       bool blend);

   /**
    * @name Export functions
    *
    * These functions provide an easy way to copy data from an LTISceneBuffer
    * object to a variety of layouts, in an efficient manner.
    *
    * The copying is performed relative to the exposed window of the buffer.
    */
   /*@{*/

   /**
    * export to memory (BSQ)
    *
    * This function copies data to a buffer in memory.  The destination
    * pointer is assumed to be organized as an array of pointers to BSQ
    * buffers, one per band.
    *
    * If the \c data parameter is NULL, the function will allocate it (but not
    * retain ownership).  In this case it is the caller's responsibility to
    * deallocate each band using the delete[] operator.
    *
    * @param   data  the destination data (may be NULL) [in/out]
    * @return  status code indicating success or failure
    */
   LT_STATUS exportDataBSQ(void**& data) const;

   /**
    * export to memory (BSQ)
    *
    * This function copies data to a buffer in memory.  The destination
    * pointer is assumed to be organized as one large buffer in BSQ
    * format.
    *
    * If the \c data parameter is NULL, the function will allocate it (but not
    * retain ownership).  In this case it is the caller's responsibility to
    * deallocate the buffer using the delete[] operator.
    *
    * @param   data  the destination data (may be NULL) [in/out]
    * @return  status code indicating success or failure
    */
   LT_STATUS exportDataBSQ(void*& data) const;

   /**
    * export to memory (BIP)
    *
    * This function copies data to a buffer in memory.  The destination
    * pointer is assumed to be organized as one large buffer in BIP
    * format.
    *
    * If the \c data parameter is NULL, the function will allocate it (but not
    * retain ownership).  In this case it is the caller's responsibility to
    * deallocate the buffer using the delete[] operator.
    *
    * @param   data  the destination data (may be NULL) [in/out]
    * @return  status code indicating success or failure
    */
   LT_STATUS exportDataBIP(void*& data) const;

   /**
    * export to stream (BSQ)
    *
    * This function copies data to a stream.  The destination is organized as
    * one large buffer in BSQ format.
    *
    * @param   stream  the destination stream
    * @return  status code indicating success or failure
    */
   LT_STATUS exportDataBSQ(LTIOStreamInf& stream) const;

   /**
    * export to stream (BIP)
    *
    * This function copies data to a stream.  The destination is organized as
    * one large buffer in BIP format.
    *
    * @param   stream  the destination stream
    * @return  status code indicating success or failure
    */
   LT_STATUS exportDataBIP(LTIOStreamInf& stream) const;

   /**
    * export to (arbitrary) memory
    *
    * This function copies data to a buffer.  The layout of the destination
    * is determined by the input parameters.
    *
    * For example, assuming RGB/uint8 data and WxH pixels:
    *
    * \li BIP format: pixelBytes=3, rowBytes=W*3, bandBytes=1
    * \li BIL: pixelBytes=1, rowBytes=W*3, bandBytes=W*1
    * \li BSQ: pixelBytes=1, rowBytes=W*1, bandBytes=W*H*1
    *
    * @param   data        the destination buffer (may not be NULL)
    * @param   pixelBytes  width of pixel, in bytes (e.g. distance from "red" to "red")
    * @param   rowBytes    width of buffer, in bytes
    * @param   bandBytes   distance from sample to the next, in bytes (e.g. distance from "red" to "blue")
    * @return  status code indicating success or failure
    */
   LT_STATUS exportData(void* data,
                        lt_uint32 pixelBytes,
                        lt_uint32 rowBytes,
                        lt_uint32 bandBytes) const;
   /*@}*/

   void byteSwap();

   // for LizardTech internal use only
   LT_STATUS applyMask(const LTIMask &mask, const LTIPixel &color);
   void applyMask(const LTIMask &mask, const LTIPixel &color, lt_uint16 band);

   LT_STATUS fill(const LTIPixel &color);
   void fill(const LTIPixel &color, lt_uint16 band);

   void zero(void);
   void zero(lt_uint16 band);


   /**
    * compute alignment constraint
    *
    * This utility function returns a value which is equal to or greater than
    * the given value, when aligned to the given constraint.  This is useful
    * for determining proper row widths for certain applications.
    *
    * For example, given the value 99 and an alignment of 4, the function
    * will return 100.  Given a value of 128 and an alignment of 8, the
    * function will return 128.
    *
    * @param   value          the nominal buffer width
    * @param   byteAlignment  the alignment required
    * @return  the aligned width
    */
   static lt_uint32 addAlignment(lt_uint32 value, lt_uint32 byteAlignment);

   // for LizardTech internal use only
   static LT_STATUS buildMask(LTIPixelFillMethod fillMethod,
                              double mag,
                              double fuzzyThreshold,
                              bool blend,
                              const LTISceneBuffer &dstBuffer, 
                              const LTIPixel *dstNodata,
                              const LTISceneBuffer &srcBuffer,
                              const LTIPixel *srcNodata,
                              LTIMask &binaryMask);
   LT_STATUS calculateMask(LTIPixelFillMethod fillMethod,
                           double mag,
                           double fuzzyThreshold,
                           bool blend,
                           const LTIPixel *srcNodata,
                           LTIMask &mask) const;
   static void shiftNoDataValue(LTIPixelFillMethod fillMethod,
                                double fuzzyThreshold,
                                double mag,
                                const LTIPixel &dstMin,
                                const LTIPixel &dstMax,
                                const LTIPixel &dstNoData,
                                LTIPixel &shiftedValue);


#if !defined(SWIG) && 1
#define DEPRECATE(OLD, NEW) LT_DEPRECATED(NEW) OLD { return NEW; }
   DEPRECATE(lt_int32 getTotalNumRows() const, getNumRows());
   DEPRECATE(lt_int32 getWindowColOffset() const, 0);
   DEPRECATE(lt_int32 getWindowRowOffset() const, 0);
   DEPRECATE(lt_int32 getWindowNumCols() const, getNumCols());
   DEPRECATE(lt_int32 getWindowNumRows() const, getNumRows());
   DEPRECATE(lt_int32 getTotalNumPixels() const, getNumPixels());
   DEPRECATE(lt_int32 getWindowNumPixels() const, getNumPixels());
   DEPRECATE(void** getTotalBSQData() const, getBSQData());
   DEPRECATE(void* getTotalBandData(lt_uint16 band) const, getBandData(band));
   DEPRECATE(void** getWindowBSQData() const, getBSQData());
   DEPRECATE(void* getWindowBandData(lt_uint16 band) const, getBandData(band));
   DEPRECATE(void* getTotalSample(lt_uint32 x, lt_uint32 y, lt_uint16 band) const, getSample(x, y, band));
   DEPRECATE(void* getWindowSample(lt_uint32 x, lt_uint32 y, lt_uint16 band) const, getSample(x, y, band));
   DEPRECATE(bool inWindow(lt_int32 x, lt_int32 y) const, (x < getNumCols() && y < getNumRows()));
#undef DEPRECATE
#endif
protected:
   /**
    * verify that a buffer with the given pixel props could be imported
    */
   LT_STATUS checkImpedance(const LTIPixel &p) const;

private:
   void init(const LTIPixel& pixelProps,
             lt_uint32 colOffset,
             lt_uint32 rowOffset,
             lt_uint32 numCols,
             lt_uint32 numRows,
             void **bsqData,
             lt_uint32 totalNumCols,
             lt_uint32 totalNumRows);

   lt_uint16 m_numBands;
   LTIPixel *m_pixelProps;

   lt_uint32 m_numCols;
   lt_uint32 m_numRows;
   lt_uint32 m_totalNumCols;
   
   void **m_data;
   bool m_ownsData;


   bool operator==(const LTISceneBuffer&) const;
   bool operator!=(const LTISceneBuffer&) const;
};

LT_END_NAMESPACE(LizardTech)

#endif // LTI_SCENEBUFFER_H
