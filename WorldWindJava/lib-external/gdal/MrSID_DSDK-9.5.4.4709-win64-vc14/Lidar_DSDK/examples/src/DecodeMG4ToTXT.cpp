/* //////////////////////////////////////////////////////////////////////////
//                                                                         //
// This code is Copyright (c) 2009 LizardTech, Inc, 1008 Western Avenue,   //
// Suite 200, Seattle, WA 98104.  Unauthorized use or distribution         //
// prohibited.  Access to and use of this code is permitted only under     //
// license from LizardTech, Inc.  Portions of the code are protected by    //
// US and foreign patents and other filings. All Rights Reserved.          //
//                                                                         //
////////////////////////////////////////////////////////////////////////// */
/* PUBLIC */


// This demonstrates how to decode a scene from an MG4/Lidar file to a
// text file, using a MG4PointReader and TXTPointWriter

#include "main.h"
#include "support.h"

#include "lidar/TXTPointWriter.h"
#include "lidar/MG4PointReader.h"

LT_USE_LIDAR_NAMESPACE

void
DecodeMG4ToTXT()
{
   MG4PointReader *reader = NULL;
   TXTPointWriter *writer = NULL;
   try
   {
      // open a MG4 file
      reader = MG4PointReader::create();
      reader->init(INPUT_PATH("Tetons_200k.xyz.sid"));

      // create a text file writer
      writer = TXTPointWriter::create();
      writer->init(reader, OUTPUT_PATH("Tetons_200k.txt"), "xyz");

      // Huge() for the whole thing, or specify some other bounds of interest
      writer->write(Bounds::Huge(),
                    1.0,
                    reader->getPointInfo(),
                    NULL);

      compareTXTFiles(OUTPUT_PATH("Tetons_200k.txt"), INPUT_PATH("Tetons_200k.xyz"));

      RELEASE(reader);
      RELEASE(writer);
      removeFile(OUTPUT_PATH("Tetons_200k.txt"));
   }
   catch(...)
   {
      RELEASE(reader);
      RELEASE(writer);
      throw;
   }
}

