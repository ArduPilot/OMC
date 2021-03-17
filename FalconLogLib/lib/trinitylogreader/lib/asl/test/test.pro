QT     += core
QT     -= gui

CONFIG += c++14

TARGET = asltest
CONFIG += console
CONFIG -= app_bundle

TEMPLATE = app

SOURCES += main.cpp

INCLUDEPATH += $$PWD/../inc

  kit = $$basename(QMAKESPEC)
  CONFIG(debug, debug|release) {
    LIBS += -L$$PWD/../bin/$$kit/ -lasld
  } else {
    LIBS += -L$$PWD/../bin/$$kit/ -lasl
  }
