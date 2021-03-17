QT       -= core gui
CONFIG   -= app_bundle
CONFIG   -= qt
CONFIG += c++14

TARGET = asl
TEMPLATE = lib

DEFINES += ASL_LIBRARY_EXPORTING

SOURCES +=

HEADERS += inc/asl_global.h \
    inc/position.h \
    inc/vector3d.h \
    inc/quaternion.h \
    inc/asl_documentation.h \
    inc/atostypes.h \
    inc/conversions.h

INCLUDEPATH += inc

CONFIG(debug, debug|release) {
  unix: TARGET = $$join(TARGET,,,_debug)
  else: TARGET = $$join(TARGET,,,d)
}

kit = $$basename(QMAKESPEC)
DESTDIR = $$PWD/bin/$$kit/
