#!/usr/bin/env bash
set -e

srcdir='./src/main/java'
dstdir='../../../src/desktop/generated'

#aidl='com/o3dr/services/android/lib/model/IDroneApi.aidl'

cd $srcdir
for aidl in `find . -wholename "*/I*aidl"`; do
    java_name="$dstdir/${aidl%.aidl}.java"
    java_dir=${java_name%/*}
    if [ ! -d "$java_dir" ]; then
        mkdir -p "$java_dir"
    fi
    cp "$aidl" "$java_name"

    # remove 'oneway' annotations
    sed -i -r 's|(oneway\|in\|inout)\s|/* \1 */ |g' "$java_name"

    # add stub implmentation
    class_name=$(echo "$aidl" | sed -r 's|.*/(.+)[.].*|\1|')

    stub_impl="\n    public abstract class Stub implements $class_name {}\n"
    sed -i "s|^}|\n    $stub_impl\n}|" "$java_name"
    echo "- created class for aidl $class_name : $java_name"

done

