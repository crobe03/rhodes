#-------------------------------------------------
#
# Project created by QtCreator 2011-06-15T21:19:15
#
#-------------------------------------------------

QT       -= gui

TARGET = sqlite3
TEMPLATE = lib
CONFIG += staticlib

SOURCES += \
    ../../shared/sqlite/sqlite3.c

HEADERS += \
    ../../shared/sqlite/sqlite3ext.h \
    ../../shared/sqlite/sqlite3.h
unix:!symbian {
    maemo5 {
        target.path = /opt/usr/lib
    } else {
        target.path = /usr/lib
    }
    INSTALLS += target
}
