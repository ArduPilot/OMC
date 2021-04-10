The example applications must be run from within this directory
as they contain hard-coded references to files within the data directory.

The following notes are for Android only:
Complete the following steps to compile the example application:
1. Install Android Studio (version 2.2.3 as of this writing)
2. Enable support for NDK, CMake, and LLDB in the Android SDK Manager -> SDK Tools.
3. In Android Studio, "Open an existing Android Studio project" and choose the
<install_dir>/RasterDSDK/examples/src/studio directory.
4. Accept any prompts to update gradle settings.

The Android sample attempts to unpack its test data in MainActivity.copyAssets(): 
  File outDir = new File(Environment.getExternalStorageDirectory(), "LTRasterExamples");
If this fails for whatever reason, the examples will fail. Adjust this code or manually
copy the examples/data folder to the test device.