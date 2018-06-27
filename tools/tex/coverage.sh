#!/bin/bash

files=`grep "$1" benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\)".*/\1/'`
total=0
value=0
for file in $files
do
    value=$(xmlstarlet sel -t -c "$2" $file)
    total=$(($total+$value))
done
echo $total
