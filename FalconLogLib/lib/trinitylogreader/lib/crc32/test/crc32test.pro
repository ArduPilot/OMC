#-------------------------------------------------
#
# Project created by QtCreator 2015-03-24T20:48:33
#
#-------------------------------------------------

QT       += core

QT       -= gui

TARGET = crc32test
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app

include(../crc32.pri)


SOURCES += main.cpp \
    crcmodel.c

HEADERS += \
    crcmodel.h
