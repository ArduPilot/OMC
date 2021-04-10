#!/usr/bin/env python 
import os
from distutils.core import setup, Extension 


pathcat = os.path.join

cflags = []
if os.name == 'nt':
   cflags = ['/EHsc', '/Zc:wchar_t-']

setup(name = 'LidarDSDK', 
      version = '1.1.0.4709', 
      author      = "LizardTech MrSID LiDAR DSDK", 
      description = """Python bindings for the LizardTech MrSID LiDAR DSDK""", 
      ext_modules = [Extension('_LidarDSDK',
                               sources=['LidarDSDK.cpp'],
                               include_dirs=['../../..//include'],
                               library_dirs=['../../..//lib//'],
                               libraries=['lti_lidar_dsdk'],
                               extra_compile_args=cflags,
                               )], 
      py_modules = ["LidarDSDK"], 
) 

