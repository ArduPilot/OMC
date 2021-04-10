We are including Experimental SWIG based language bindings for the Decode
SDK.  They currently support C#, python and ruby.  You do not need to install
SWIG to use the bindings.

The bindings are a direct translation of the C++ API, consequently they may
feel a little out of place in the host language.  The bindings do not have
their own documentation but the C++ User and Reference Manuals are a good
starting point.  The main difference between the C++ API and the bindings is
the bindings handle the reference counting used by the C++ API.  Each language
has a lidarinfo example that uses most of the API.

When the language bindings are being used, it is possible that the garbage
collector may not receive full information about the intended lifetimes of
objects and may delete them prematurely. We have only seen this behavior in the
C# bindings and we believe we have fixed the errors that cause this behavior.
However, pending more rigorous testing we consider all these bindings
experimental. The C# bindings are the most mature and are currently being used
in our GUI applications, such as LiDAR Compressor, GeoViewer and MG4 Decode.

Building and Installing the bindings:
   For python and ruby you should use the same compiler to build the VM and
   the bindings as the SDK was built with, see the "System Requirements"
   section in the User Manual for a listing of platforms and compiler versions.

C# Bindings:
   Open LidarDSDK.sln with MS Visual Studio 2008 and build the solution file.

Python Bindings:

   > python setup.py build
   $ python setup.py install

   On Windows, if you are using the prebuilt python binaries you will need
   version 2.6 so the compilers match.

Ruby Bindings:

   > ruby extconf.rb
   > make all
   $ make install

   The ruby bindings have not been tested on Windows because the prebuilt ruby
   binaries use VC6 and the SDK uses VC9.

Examples:
   Each language has a lidarinfo.{cs,py,rb} example that uses most of
   the API.

Recreating the SWIG wraps:
   If you need to recreate the SWIG binding files run the following commands
   in the appropriate subdirectories:

For Linux and Darwin:
   > swig -c++ -fvirtual -I../../../include -python -o LidarDSDK.cpp \
       ../LidarDSDK.i
   > swig -c++ -fvirtual -I../../../include -ruby -o LidarDSDK.cpp \
       ../LidarDSDK.i

For Windows:
   > swig -c++ -fvirtual -I../../../include -D_WIN32 -python -o LidarDSDK.cpp \
       ../LidarDSDK.i
   > swig -c++ -fvirtual -I../../../include -D_WIN32 -ruby -o LidarDSDK.cpp \
       ../LidarDSDK.i
   > swig -c++ -fvirtual -namespace LizardTech.LidarSDK \
       -dllimport LizardTech.LidarDSDKp.dll -I../../../include -csharp \
       -o LidarDSDK.cpp ../LidarDSDK.i

