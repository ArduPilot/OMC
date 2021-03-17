/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
#include <QCoreApplication>

#include <QDebug>
#include <QtConcurrent/QtConcurrent>

#include <trinitylogreader.h>

int main(int argc, char *argv[])
{
  QCoreApplication a(argc, argv);

  QString path;
  if (argc < 2) {
    path = "S:/Libraries/TrinityLog/test/testlogs/9";
  } else {
    path = argv[1];
  }

  QString infoFile = path+"/ASCTEC.IFO";
  QString logFile = path+"/ASCHP.LOG";

  {
    using namespace trinityLog;
    TrinityLogReader* logreader = new TrinityLogReader();
    try {
      logreader->setLogFilename(logFile.toStdString());
      logreader->setInfoFilename(infoFile.toStdString());
    } catch(LogReaderException e) {
      qDebug() << "Error: " << e.what();
      return 0;
    }

    TrinityLogPtr log = logreader->read();

    delete logreader;

    QFile f(path+"/log.ubx");
    f.open(QIODevice::WriteOnly);

    try {
      auto timestamps = log->getTimeStamps("ATOS_MSG_GPS_RAW_DATA");
      for(auto v : timestamps) {
        const char * data = log->getRaw("ATOS_MSG_GPS_RAW_DATA", "rawData", v);
        f.write(data, 1024);
      }
    }
    catch(LogException e)
    {
      qDebug() << e.what();
    }
  }

  return a.exec();
}
