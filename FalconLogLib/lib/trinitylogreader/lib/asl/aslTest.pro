TEMPLATE = subdirs
SUBDIRS = asl \
    test

asl.file = asl.pro

test.depends = asl
