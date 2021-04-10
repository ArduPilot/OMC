Examples for LizardTech LiDAR SDK

The example applications must be run from within this directory,
as they contain hard-coded references to files within the data dir.

For Android, complete the following steps before you run the example applications:
1. Set up the development environment for NDK development. For more information,
   see http://developer.android.com.
   NOTE: This SDK was compiled using the GCC 4.6 compiler with exceptions enabled,
         linked to the gnustl_shared library (libgnustl_shared.so), and
         the android-12 ABI (APP_PLATFORM) using android-ndk-r10.
2. Copy the Lidar_DSDK/examples/data directory to /sdcard/LidarDSDK/data on the
   target device or emulator. Alternatively, modify the INPUT_PATH and OUTPUT_PATH
   variables defined in the support.h file.
3. If you use the Eclipse IDE, complete the following additional steps:
  * Import the existing Android code into the workspace specifying Lidar_DSDK/examples/src
    as the Root Directory.
   * Right-click the LidarExamples project, and click Android > Add Native Support...
