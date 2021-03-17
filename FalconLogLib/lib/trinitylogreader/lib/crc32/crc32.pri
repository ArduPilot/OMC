isEmpty(CRC32_PRI) { # include guard
CRC32_PRI = 1

INCLUDEPATH += $$PWD

CONFIG += c++11

SOURCES += $$PWD/crc32.cpp

HEADERS += $$PWD/crc32.h

} # end of include guard
