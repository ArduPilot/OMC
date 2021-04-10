
%module(directors="1") LidarDSDK

%include "LidarBase.i"
%include "LidarCore.i"

%header %{
#include "lidar/MG4PointReader.h"
%}

FIXUP_RC(MG4PointReader)
%include "lidar/MG4PointReader.h"


