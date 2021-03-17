QT     += core
QT     -= gui

CONFIG += c++11

TARGET = TrinityLogReaderTest
CONFIG += console
CONFIG -= app_bundle

TEMPLATE = app

SOURCES += main.cpp

INCLUDEPATH += $$PWD/../inc
INCLUDEPATH += $$PWD/../lib/asl/inc
INCLUDEPATH += $$PWD/../lib/nowide_standalone


  kit = $$basename(QMAKESPEC)
  CONFIG(debug, debug|release) {
    unix: LIBS += -L$$PWD/../bin/$$kit/ -lTrinityLogReader_debug
    else: LIBS += -L$$PWD/../bin/$$kit/ -lTrinityLogReaderd
  } else {
    LIBS += -L$$PWD/../bin/$$kit/ -lTrinityLogReader
  }
