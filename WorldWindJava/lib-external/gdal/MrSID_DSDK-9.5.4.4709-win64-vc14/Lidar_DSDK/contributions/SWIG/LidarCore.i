
%header %{
#include "lidar/Error.h"
#include "lidar/Version.h"
#include "lidar/FileIO.h"
#include "lidar/TXTPointReader.h"
#include "lidar/TXTPointWriter.h"
%}

%ignore XALLOC;
%ignore XDEALLOC;
%ignore XREALLOC;
%include "lidar/Base.h"


//%rename (copy) operator =;
%ignore operator !=;
%rename (Equals) operator ==;

%include "lidar/Types.h"

%ignore LizardTech::PointData::getX;
%ignore LizardTech::PointData::getY;
%ignore LizardTech::PointData::getZ;
%ignore LizardTech::ChannelData::setDataType;
%ignore LizardTech::ChannelData::getData;

ADD_REFERENCE(LizardTech::ChannelInfo)
FIXUP_GET_REFERENCE(LizardTech::ChannelInfo, getChannel)
FIXUP_GET_POINTER(LizardTech::ChannelInfo, getChannel)

//ADD_REFERENCE(LizardTech::ChannelData) we add this to ChannelInfo 
FIXUP_GET_REFERENCE(LizardTech::ChannelData, getChannel)
FIXUP_GET_POINTER(LizardTech::ChannelData, getChannel)

// for PointSource::getPointInfo()
ADD_REFERENCE(LizardTech::PointInfo)
FIXUP_GET_REFERENCE(LizardTech::PointInfo, getPointInfo)

%include "lidar/PointData.h"
%extend LizardTech::ChannelData {
   double getValue(size_t i)
   {
      const void *values = $self->getData();

      switch($self->getDataType())
      {
#define CASE(tag, type) \
   case LizardTech::DATATYPE_##tag: \
      return static_cast<double>(static_cast<const type *>(values)[i])
      CASE(UINT8, lt_uint8);
      CASE(SINT8, lt_int8);
      CASE(UINT16, lt_uint16);
      CASE(SINT16, lt_int16);
      CASE(UINT32, lt_uint32);
      CASE(SINT32, lt_int32);
      CASE(UINT64, lt_uint64);
      CASE(SINT64, lt_int64);
      CASE(FLOAT32, float);
      CASE(FLOAT64, double);
#undef CASE
      default:
         return 0;
      }
   }
}

//SETUP_RC(Object)
//%ignore LizardTech::Object;
%include "lidar/Object.h"

SETUP_RC(IO)
%include "lidar/IO.h"
%import "lidar/Stream.h"

FIXUP_RC(FileIO)
%ignore LizardTech::FileIO::deleteFile;
%ignore LizardTech::FileIO::fileExists;
#if defined(SWIGCSHARP)
// force the SWIG to only use the wchar versions
%ignore LizardTech::FileIO::init(const char *, const char *);
%ignore LizardTech::FileIO::init(const char *);
#endif
%include "lidar/FileIO.h"

%ignore LizardTech::Metadata::dump;
%ignore LizardTech::Metadata::read;
%ignore LizardTech::Metadata::write;
// BUG: only ignore this until we fully support getting metadata
%ignore LizardTech::Metadata::get;
%include "lidar/Metadata.h"

%feature("director") LizardTech::ProgressDelegate;
%include "lidar/ProgressDelegate.h"

SETUP_RC(PointIterator)
KEEP_REFERENCE(PointIterator, ProgressDelegate)
%include "lidar/PointIterator.h"

SETUP_RC(PointSource)
%newobject *::createIterator;
PASS_REFERENCE(LizardTech::PointIterator *, createIterator,
               ProgressDelegate, arg3)
%include "lidar/PointSource.h"

SETUP_RC(PointReader)
%include "lidar/PointReader.h"

SETUP_RC(PointWriter)
%include "lidar/PointWriter.h"

SETUP_RC(SimplePointWriter)
%include "lidar/SimplePointWriter.h"

FIXUP_RC(TXTPointReader)
%include "lidar/TXTPointReader.h"

FIXUP_RC(TXTPointWriter)
%include "lidar/TXTPointWriter.h"

%ignore LizardTech::Version::getSDKVersion;
%ignore LizardTech::Version::getMrSIDFileVersion;
%include "lidar/Version.h"


