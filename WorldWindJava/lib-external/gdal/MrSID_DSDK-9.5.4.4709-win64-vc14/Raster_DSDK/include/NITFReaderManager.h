/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2010 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */

#ifndef NITFReaderManager_H
#define NITFReaderManager_H

// lt_lib_base
#include "lt_base.h"

// lt_lib_mrsid_core
#include "lti_geoImageReader.h"
#include "lti_imageStageManager.h"

LT_BEGIN_LIZARDTECH_NAMESPACE
class LTFileSpec;
class LTIOStreamInf;

namespace Nitf
{
   class ImageReader;
   class FileHeader;
   class ImageSegment;
   class GraphicSegment;
   class LabelSegment;
   class TextSegment;
   class DataSegment;
   class ReservedSegment;
   class FileMetadata;
   class SecurityMetadata;
}


/**
 * class for reading an NITF file
 *
 * This class is the main entry point for reading from an NITF file.
 * You should initially construct a NITFReaderManager,
 * then use the functions it provides to access the segments in the image,
 * such as getImageSegment() or getTextSegment.
 *
 * To extract pixels from an image, you need to createReader() for the image
 * segment desired.  This will return an object derived from LTIImageReader
 * which can be used for decode requests.
 */
class NITFReaderManager : public LTIImageStageManager
{
   LTI_REFERENCE_COUNTED_BOILERPLATE(NITFReaderManager);
public:
   /**
    * initialization
    * Creates an NITFReaderManager object from the given file.
    *
    * @param fileSpec  the NITF file to be opened
    * @param useWorldFile indicates world file to be used for geo coords
    *         (applies to the first image segment only)
    * @return success or failure
    */
   LT_STATUS initialize(const LTFileSpec& fileSpec, bool useWorldFile=true);

   /**
    * initialization
    * Creates an NITFReaderManager object from the given stream.
    *
    * @param stream  the NITF stream to be opened
    * @return success or failure
    */
   LT_STATUS initialize(LTIOStreamInf *stream);

   /**
    * creates a reader for a single segment
    *
    * This function is used to create an ImageReader for a 
    * single given image segment, specified by number.
    *
    * The \a reader is allocated and initialized by this function,
    * but the caller has ownership of the object.
    * 
    * @param reader  the created segment reader
    * @param idx     index of the image segment (one-based index)
    * @return status code
    */
   LT_STATUS createReader(Nitf::ImageReader*& reader, lt_uint32 idx);

   /**
    * creates a reader for a single element, band mapped according to irep
    *
    * This function is used to create an LTIImageStage pipeline including
    * the reader itself fronted by a bandmap filter accommodating images
    * that are not in RGB band order.
    *
    * Note: the index used is zero-based for consistency with this virtual
    * method, in contrast to the createReader() method which uses a one-based
    * segment index.
    *
    * @param imageNumber  index of the image segment (zero-based index)
    * @param imageStage   the created band-mapped reader pipeline
    * @return status code
    */
   LT_STATUS createImageStage(lt_uint32 imageNumber,
                              LTIImageStage *&imageStage);

   /**
    * COMRAT compliance control
    *
    * This function is used to control whether or not the COMRAT
    * field is to be read.  The 2500C specification requires
    * this field be present; the 2500B Note 2 specification does
    * not.  This function is used to indicate which version
    * of the specification this file adheres to.
    *
    * The default is false, i.e. 2500C formatting.
    *
    * This function must be called prior to initialize().
    *
    * @param use2500B set to true for 2500B/Note2 formatting
    */
   void setCompat_2500B_N2(bool use2500B);

   /**
    * COMRAT compliance setting
    *
    * Returns the 2500B compatability setting; see the
    * setCompat_2500B_N2() function for details.
    *
    * @return true iff 200B / Note 2 formatting is being used
    */
   bool getCompat_2500B_N2() const;

   lt_int64 getFileSize() const;

   /**
    * returns FileHeader metadata
    */
   const Nitf::FileHeader* getFileHeader() const;

   /**
    * returns number of image segments
    */
   lt_uint32 getNumImageSegments() const;

   /**
    * returns an image segment
    *
    * This function returns an object representing an image segment.  (Note
    * this object does not support accessing the pixel data; use 
    * createReader() for that.)
    *
    * @param num the index of the segment to return (numbered starting with 1)
    * @return the segment object
    */
   const Nitf::ImageSegment* getImageSegment(lt_uint32 num) const;

   /**
    * returns the IID1 field of the given image segment
    */
   const char* getImageSegmentIID1(lt_uint32 num) const;

   /**
    * returns the number of graphic segments
    */
   lt_uint32 getNumGraphicSegments() const;

   /**
    * returns a graphic segment
    *
    * This function returns an object representing a graphic segment.
    *
    * @param num the index of the segment to return (numbered starting with 1)
    * @return the segment object
    */
   const Nitf::GraphicSegment* getGraphicSegment(lt_uint32 num) const;

   /**
    * returns the number of label segments
    */
   lt_uint32 getNumLabelSegments() const;

   /**
    * returns a label segment
    *
    * This function returns an object representing a label segment.
    *
    * @param num the index of the segment to return (numbered starting with 1)
    * @return the segment object
    */
   const Nitf::LabelSegment* getLabelSegment(lt_uint32 num) const;

   /**
    * returns the number of text segments
    */
   lt_uint32 getNumTextSegments() const;

   /**
    * returns a text segment
    *
    * This function returns an object representing a text segment.
    *
    * @param num the index of the segment to return (numbered starting with 1)
    * @return the segment object
    */
   const Nitf::TextSegment* getTextSegment(lt_uint32 num) const;

   /**
    * returns the number of data segments
    */
   lt_uint32 getNumDataSegments() const;

   /**
    * returns a data segment
    *
    * This function returns an object representing a data segment.
    *
    * @param num the index of the segment to return (numbered starting with 1)
    * @return the segment object
    */
   const Nitf::DataSegment* getDataSegment(lt_uint32 num) const;

   /**
    * returns the number of RES segments
    */
   lt_uint32 getNumResSegments() const;

   /**
    * returns a RES segment
    *
    * This function returns an object representing a RES segment.
    *
    * @param num the index of the segment to return (numbered starting with 1)
    * @return the segment object
    */
   const Nitf::ReservedSegment* getResSegment(lt_uint32 num) const;

   /**
    * returns NITF version information
    */
   const char* getVersionString() const;

   /**
    * returns the file-level metadata
    */
   const Nitf::FileMetadata* getFileMetadata() const;

   /**
    * returns file-level security metadata
    */
   const Nitf::SecurityMetadata* getSecurityMetadata() const;

   // not for public use
   LTFileSpec* getFileSpec() const;

private:
    LT_STATUS fixMetadata(LTIImageStage *mos);

   LTFileSpec *m_fileSpec;
   LTIOStreamInf *m_stream;
   bool m_ownStream;
   lt_int64 m_fileSize;
   bool m_useWorldFile;

   Nitf::FileHeader* m_fileHeader;
   Nitf::ImageSegment** m_imageSegments;
   Nitf::GraphicSegment** m_graphicSegments;
   Nitf::LabelSegment** m_labelSegments;
   Nitf::TextSegment** m_textSegments;
   Nitf::DataSegment** m_dataSegments;
   Nitf::ReservedSegment** m_resSegments;
   lt_uint32 m_numImageSegments;
   lt_uint32 m_numGraphicSegments;
   lt_uint32 m_numLabelSegments;
   lt_uint32 m_numTextSegments;
   lt_uint32 m_numDataSegments;
   lt_uint32 m_numResSegments;

   char* m_versionString;

   bool m_compat_2500B_N2;
};


LT_END_LIZARDTECH_NAMESPACE

#endif // NITFReaderManager_H
