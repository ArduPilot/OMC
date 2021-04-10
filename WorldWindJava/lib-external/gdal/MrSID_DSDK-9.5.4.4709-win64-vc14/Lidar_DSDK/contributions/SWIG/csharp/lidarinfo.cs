using System;
using System.Collections.Generic;
using System.Text;

using LizardTech.LidarSDK; 

namespace lidarinfo
{
   class Program
   {
      static void output(string str)
      {
         Console.Write(str);
      }
      static PointReader load(string path)
      {
         output(path + "\n");
         if (path.EndsWith(".sid", StringComparison.OrdinalIgnoreCase))
         {
            MG4PointReader ps = new MG4PointReader();
            ps.init(path);
            return ps;
         }
         else
         {
/*
     if options.inputformat == None:
        error('need --inputformat for reading text file')
 */
            TXTPointReader ps = new TXTPointReader();
            ps.init(path, "", 0, true);
            return ps;
         }
      }


      static void Main(string[] args)
      {
         foreach (string path in args)
         {
            PointReader ps = load(path);
            output(String.Format("   Number of Points:   {0}\n", ps.getNumPoints()));

            {
               Bounds b = ps.getBounds();
               output(String.Format("   Bounds Min:         {0} {1} {2}\n", b.x.min, b.y.min, b.z.min));
               output(String.Format("   Bounds Max:         {0} {1} {2}\n", b.x.max, b.y.max, b.z.max));
            }
            output(String.Format("   Scale:              {0} {1} {2}\n", ps.getScale()[0], ps.getScale()[2], ps.getScale()[2]));
            output(String.Format("   Offset:             {0} {1} {2}\n", ps.getOffset()[0], ps.getOffset()[2], ps.getOffset()[2]));

            {
               PointInfo pi = ps.getPointInfo();
               output(String.Format("   Number of Channels: {0}\n", ps.getNumChannels()));
               output("   Supported Fields:  ");
               for (uint i = 0; i < pi.getNumChannels(); i += 1)
                  output(" " + pi.getChannel(i).getName());
               output("\n");
            }
            output(String.Format("   Spatial Reference:  {0}\n", ps.getWKT()));

            if (true /*options.metadata*/)
            {
               Metadata meta = new Metadata();
               ps.loadMetadata(meta, false);
               if (meta.getNumRecords() != 0)
               {
                  output(String.Format("   Metadata:           {0}\n", meta.getNumRecords()));
                  for (uint i = 0; i < meta.getNumRecords(); i += 1)
                  {
                     output(String.Format("      {0} ({1}):\n", meta.getKey(i), meta.getDescription(i)));
                     switch (meta.getDataType(i))
                     {
                        case MetadataDataType.METADATA_DATATYPE_STRING:
                           output(String.Format("         '{0}'\n", meta.getValue(i)));
                           break;
                        case MetadataDataType.METADATA_DATATYPE_BLOB:
                           output(String.Format("         (blob of {0} bytes)\n", meta.getValueLength(i)));
                           break;
                        case MetadataDataType.METADATA_DATATYPE_REAL_ARRAY:
                           output(String.Format("         {{"));
                           double[] values = (double [])meta.getValue(i);
                           for (uint j = 0; j < meta.getValueLength(i); j += 1)
                              output(String.Format(" {0}", values[j]));
                           output(String.Format(" }}\n"));
                           break;
                     }
                  }
               }
               else
                  output("   Metadata:           None\n");

               if (ps.getNumClassIdNames() != 0)
               {
                  output(String.Format("   ClassId Names:      {0}\n", ps.getNumClassIdNames()));
                  foreach (string name in ps.getClassIdNames())
                     output(String.Format("      {0}\n", name));
               }
               else
                  output("   ClassId Names:      None\n");
            }

            if (true /*options.bounds*/)
            {
               PointInfo pi = new PointInfo();
               pi.init(3);
               pi.getChannel(0).init(ps.getChannel(LidarDSDK.CHANNEL_NAME_X));
               pi.getChannel(1).init(ps.getChannel(LidarDSDK.CHANNEL_NAME_Y));
               pi.getChannel(2).init(ps.getChannel(LidarDSDK.CHANNEL_NAME_Z));

               PointData buffer = new PointData();
               buffer.init(pi, 4096);

               ChannelData x = buffer.getChannel(0);
               ChannelData y = buffer.getChannel(1);
               ChannelData z = buffer.getChannel(2);

               UInt64 numPoints = 0;

               PointIterator iter = ps.createIterator(Bounds.Huge(), 1.0, pi, null);
               Bounds bounds = new Bounds();
               uint count = iter.getNextPoints(buffer);
               while (count != 0)
               {
                  numPoints += count;
                  for (uint i = 0; i < count; i += 1)
                     bounds.grow(x.getValue(i), y.getValue(i), z.getValue(i));
                  count = iter.getNextPoints(buffer);
               }

               Bounds b = bounds;
               output(String.Format("   # of Points Read:   {0}\n", numPoints));
               output("Real Bounds:\n");
               output(String.Format("   Bounds Min:         {0} {1} {2}\n", b.x.min, b.y.min, b.z.min));
               output(String.Format("   Bounds Max:         {0} {1} {2}\n", b.x.max, b.y.max, b.z.max));
            }
         }
      }
   }
}
