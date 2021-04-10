#!/usr/bin/env ruby

# add Lidar bundle to ruby's search path
if ENV['LT_PROJ_LIBS'] != nil then
   $:.push(File.join(ENV['LT_PROJ_LIBS'], 'ruby'))
end

require 'LidarDSDK'
require 'pp'

def output(str)
   STDOUT.write(str)
end

def error(str)
   STDERR.write(str)
end

def load(path, options)
   output("#{path}\n")
   ext = File.extname(path)
   if ext == '.sid'
      ps = LidarDSDK::MG4PointReader.new
      ps.init(path)
   else
      if options.inputformat == nil
         error('need --inputformat for reading text file')
      end
      ps = LidarDSDK::TXTPointReader.new
      ps.init(path, options.inputformat)
   end
   return ps
end

def parse_cmdline()
   require 'optparse'
   require 'ostruct'
 
   options = OpenStruct.new
   options.metadata = false
   options.inputformat = nil

   OptionParser.new do |opts|
      opts.banner = "Usage: lidarinfo.rb [options] filenames ..."

      opts.on('-m', '--metadata',
              'print the metadata') do |v|
         options.metadata = v
      end
      opts.on('-i', '--inputformat STRING',
              'input text file format string') do |v|
         options.inputformat = v
      end
      opts.on('-b', '--bounds',
              'decode the points and display the real bounds of the data') do |v|
         options.bounds = v
      end
   end.parse!

   return options, ARGV
end


class MyProgressDelegate < LidarDSDK::ProgressDelegate
   def getCancelled()
      return false
   end
   def reportProgress(progress)
      output("#{100 * progress}\n")
   end
end

(options, paths) = parse_cmdline()
paths.each do |path|
   ps = load(path, options)
   output("   Number of Points:   #{ps.getNumPoints}\n")
   
   b = ps.getBounds()
   output("   Bounds Min:         #{b.x.min} #{b.y.min} #{b.z.min}\n")
   output("   Bounds Max:         #{b.x.max} #{b.y.max} #{b.z.max}\n")
   output("   Scale:              #{ps.getScale.join(' ')}\n")
   output("   Offset:             #{ps.getOffset.join(' ')}\n")
   
   pi = ps.getPointInfo
   output("   Number of Channels: #{ps.getNumChannels}\n")
   output("   Supported Fields:  ")
   pi.getNumChannels.times do |i|
      output(" #{pi.getChannel(i).getName}")
   end
   output("\n")

   output("   Spatial Reference:  #{ps.getWKT}\n")

   if options.metadata
      meta = LidarDSDK::Metadata.new
      ps.loadMetadata(meta, false)
      if meta.getNumRecords != 0
         output("   Metadata:           #{meta.getNumRecords}\n")
         meta.getNumRecords.times do |i|
            output("      #{meta.getKey(i)} (#{meta.getDescription(i)}):\n")
            if meta.getDataType(i) == LidarDSDK::METADATA_DATATYPE_STRING then
               output("         '#{meta.getValue(i)}'\n")
            elsif meta.getDataType(i) == LidarDSDK::METADATA_DATATYPE_BLOB then
               output("         (blob of #{meta.getValueLength(i)} bytes)\n")
            elsif meta.getDataType(i) == LidarDSDK::METADATA_DATATYPE_REAL_ARRAY then
               output("         { #{meta.getValue(i).join(', ')} }\n")
            end
         end
      else
         output("   Metadata:           None\n")
      end

      if ps.getNumClassIdNames != 0
         output("   ClassId Names:      #{ps.getNumClassIdNames}\n")
         ps.getClassIdNames.each do |name|
            output("      #{name}\n")
         end
      else
         output("   ClassId Names:      None\n")
      end
   end
   
   if options.bounds
      pi = LidarDSDK::PointInfo.new
      pi.init(3)
      pi.getChannel(0).init(ps.getChannel('X'))
      pi.getChannel(1).init(ps.getChannel('Y'))
      pi.getChannel(2).init(ps.getChannel('Z'))
      
      buffer = LidarDSDK::PointData.new
      buffer.init(pi, 4096)
      x = buffer.getChannel('X')
      y = buffer.getChannel('Y')
      z = buffer.getChannel('Z')

      numPoints = 0
      delegate = MyProgressDelegate.new
      delegate.setTotal(ps.getTotalWork(LidarDSDK::Bounds.Huge, 1.0))

      iter = ps.createIterator(LidarDSDK::Bounds.Huge, 1.0, pi, delegate)
      bounds = LidarDSDK::Bounds.new
      count = iter.getNextPoints(buffer)
      while count != 0 do
         numPoints += count
         count.times do |i|
            bounds.grow(x.getValue(i), y.getValue(i), z.getValue(i))
         end
         count = iter.getNextPoints(buffer)
      end

      b = bounds;
      output("Real Number of Points: #{numPoints}\n")
      output("Real Bounds:\n");
      output("   Bounds Min:         #{b.x.min} #{b.y.min} #{b.z.min}\n")
      output("   Bounds Max:         #{b.x.max} #{b.y.max} #{b.z.max}\n")
   end
end



