/* $Id$ */
/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2005 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef PNGIMAGEWRITER_H
#define PNGIMAGEWRITER_H

// lt_lib_mrsid_core
#include "lti_geoFileImageWriter.h"

#if defined(LT_COMPILER_MS)
   #pragma warning(push,4)
#endif

struct png_struct_def;
struct png_info_struct;
struct png_color_struct;

LT_BEGIN_NAMESPACE(LizardTech)

class LTFileSpec;

/**
 * writes an image stage to a PNG file
 *
 * This class writes an image stage to a PNG file.
 */
class PNGImageWriter : public LTIGeoFileImageWriter
{
   LT_DISALLOW_COPY_CONSTRUCTOR(PNGImageWriter);
public:
   PNGImageWriter(void);
   ~PNGImageWriter(void);

   LT_STATUS initialize(LTIImageStage *imageStage);


   /** 
    * Set the library compression level.  Currently, valid values range from
    * 0 - 9, corresponding directly to the zlib compression levels 0 - 9
    * (0 - no compression, 9 - "maximal" compression).  Note that tests have
    * shown that zlib compression levels 3-6 usually perform as well as level 9
    * for PNG images, and do considerably fewer caclulations.  In the future,
    * these values may not correspond directly to the zlib compression levels.
    * @note - call this before writing the image, after initialize()
    * @note - default is no compression (0)
    */   
   void setCompressionLevel( lt_int16 level );

   /**
    * Set the compression filter method.  These filters process each scanline
    * to prepare it for optimal compression.  Valid values are as enumerated
    * by the ScanlineFilter type, corresponding directly to filters defined
    * in the PNG spec.  Specifying more than one filter (by bitwise OR) will
    * cause libpng to select the best-performing filter on a scanline-by-
    * scanline basis (by trying them).
    */
   enum ScanlineFilter {
      NoFilters = 0x00,
      FilterNone  = 0x08,
      FilterSub   = 0x10,
      FilterUp    = 0x20,
      FilterAvg   = 0x40,
      FilterPaeth = 0x80,
      FilterAll   = 0xf8,
   };
   void setScanlineFilter(ScanlineFilter filters);

   /**
    * If true, and the image has a transparency/nodata value,
    * then this pixel value will be flagged as such in the PNG
    * output image
    * @note - call this before writing the image, after initialize()
    */
   LT_STATUS setWriteTransparencyColor(bool write);

   /**
    * If true, use the nodata pixel to build a alpha band.
    * @note - call this before writing the image, after initialize()
    */
   LT_STATUS setAddAlphaBand(bool addAlpha);

   /**
    * Will cause input buffer to have its colors quantized to the specified
    * number of output colors.  If the palette size is 256 or less, the
    * output PNG will be a natively palettized image.  Sizes greater than 256
    * will simply quantize the input colors to enhance its compressibility.
    *
    * @note - call this before writing the image, after initialize()
    * @note - the default palette size is 0, indicating that the colors will
    *         be left unquantized (true color)
    */
   LT_STATUS setPaletteSize(lt_uint32 size);

   /**
    * @see LTIImageWriter
    */
   //@{
   LT_STATUS write(const LTIScene &scene);
   //@}

   /**
    * @see LTIGeoFileImageWriter
    */
   //@{
   LT_STATUS writeBegin(const LTIScene& scene);
   LT_STATUS writeStrip(LTISceneBuffer& stripBuffer, const LTIScene& stripScene);
   LT_STATUS writeEnd();
   //@}

private:
   LT_STATUS checkImpedance() const;

   struct png_struct_def *m_png;
   struct png_info_struct *m_info;
   struct png_color_struct *m_pct;
   lt_int32 m_pctsize;

   bool m_writeTransparencyColor;
   bool m_addAlphaBand;
   lt_uint32 m_paletteSize;
   lt_int16 m_compressionLevel;
   ScanlineFilter m_scanlineFilters;

   char *m_errorMessage;
};


LT_END_NAMESPACE(LizardTech)

#if defined(LT_COMPILER_MS)
   #pragma warning(pop)
#endif

#endif // PNGIMAGEWRITER_H
