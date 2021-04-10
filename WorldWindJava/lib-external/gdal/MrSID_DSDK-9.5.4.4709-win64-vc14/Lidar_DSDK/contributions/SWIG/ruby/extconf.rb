#!/usr/bin/env ruby

require 'mkmf' 

libs = ['lti_lidar_dsdk']

if RUBY_PLATFORM =~ /mswin32/ then
  $CFLAGS << ' /EHsc'
  $CFLAGS << ' /Zc:wchar_t-'
else
  libs << 'stdc++'
end

# there should be a better way to do this but I could not find it
libs.each do |lib|
   $libs = append_library($libs, lib)
end

dir_config('LidarDSDK',
           idefault=['../../..//include'],
           ldefault=['../../..//lib//'])

create_makefile('LidarDSDK')


