#!/usr/bin/env python
import sys
import os

## add lidar.py to python's search path
if os.getenv('LT_PROJ_LIBS') != None:
   sys.path.append(os.path.join(os.getenv('LT_PROJ_LIBS'),'python'))
import LidarDSDK as lidar


def output(str):
   sys.stdout.write(str)

def error(str):
   sys.stderr.write(str)

def load(path, options):
   output(path + '\n')
   ext = os.path.splitext(path)[1]
   if ext == '.sid':
      ps = lidar.MG4PointReader()
      ps.init(path)
   else:
      if options.inputformat == None:
         error('need --inputformat for reading text file')
      ps = lidar.TXTPointReader()
      ps.init(path, options.inputformat)
   return ps

def parse_cmdline():
   import optparse
   parser = optparse.OptionParser()
   parser.add_option('-m', '--metadata',
                     action = 'store_true', dest = 'metadata', default = False,
                     help = 'print the metadata')
   parser.add_option('-i', '--inputformat',
                     dest = 'inputformat', default = None,
                     help = 'input text file format string')
   parser.add_option('-b', '--bounds',
                     action = 'store_true', dest = 'bounds', default = False,
                     help = 'decode the points and display the real bounds of the data')

   return parser.parse_args()


class MyProgressDelegate(lidar.ProgressDelegate):
   def getCancelled(self):
      return False
   def reportProgress(self, progress):
      print progress

(options, paths) = parse_cmdline()

for path in paths:
   ps = load(path, options)
   output('   Number of Points:   %d\n' % ps.getNumPoints())
   
   b = ps.getBounds();
   output('   Bounds Min:         %f %f %f\n' % (b.x.min, b.y.min, b.z.min))
   output('   Bounds Max:         %f %f %f\n' % (b.x.max, b.y.max, b.z.max))
   output('   Scale:              %s\n' % str(ps.getScale()))
   output('   Offset:             %s\n' % str(ps.getOffset()))
   
   pi = ps.getPointInfo()
   output('   Number of Channels: %d\n' % ps.getNumChannels())
   output('   Supported Fields:  ')
   for i in xrange(pi.getNumChannels()):
      output(' %s' % pi.getChannel(i).getName())
   output('\n')

   output('   Spatial Reference:  %s\n' % ps.getWKT())

   if options.metadata:
      meta = lidar.Metadata()
      ps.loadMetadata(meta, False)
      if meta.getNumRecords() != 0:
         output('   Metadata:           %d\n' % meta.getNumRecords())
         for i in xrange(meta.getNumRecords()):
            output('      %s (%s):\n' % (meta.getKey(i), meta.getDescription(i)))
            if meta.getDataType(i) == lidar.METADATA_DATATYPE_STRING:
               output('         "%s"\n' % meta.getValue(i))
            elif meta.getDataType(i) == lidar.METADATA_DATATYPE_BLOB:
               output('         (blob of %d bytes)\n' % meta.getValueLength(i))
            elif meta.getDataType(i) == lidar.METADATA_DATATYPE_REAL_ARRAY:
               output('         { %s }\n' % ', '.join(map(lambda x: str(x), meta.getValue(i))))
      else:
         output('   Metadata:           None\n')

      if ps.getNumClassIdNames() != 0:
         output('   ClassId Names:      %d\n' % ps.getNumClassIdNames())
         for name in ps.getClassIdNames():
            output('      %s\n' % name)
      else:
         output('   ClassId Names:      None\n')
   
   if options.bounds:
      pi = lidar.PointInfo()
      pi.init(3)
      pi.getChannel(0).init(ps.getChannel('X'))
      pi.getChannel(1).init(ps.getChannel('Y'))
      pi.getChannel(2).init(ps.getChannel('Z'))

      buffer = lidar.PointData()
      buffer.init(pi, 4096)
      x = buffer.getChannel('X')
      y = buffer.getChannel('Y')
      z = buffer.getChannel('Z')

      numPoints = 0
      delegate = MyProgressDelegate()
      delegate.setTotal(ps.getTotalWork(lidar.Bounds.Huge(), 1.0))


      iter = ps.createIterator(lidar.Bounds.Huge(), 1.0, pi, delegate)
      bounds = lidar.Bounds()
      count = iter.getNextPoints(buffer)
      while count != 0:
         numPoints += count
         for i in xrange(count):
            bounds.grow(x.getValue(i), y.getValue(i), z.getValue(i))
         count = iter.getNextPoints(buffer)

      b = bounds;
      output('# of Points Read:     %d\n' % numPoints)
      output('Real Bounds:\n');
      output('   Bounds Min:        %f %f %f\n' % (b.x.min, b.y.min, b.z.min))
      output('   Bounds Max:        %f %f %f\n' % (b.x.max, b.y.max, b.z.max))

