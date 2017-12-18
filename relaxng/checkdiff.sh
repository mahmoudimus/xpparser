#!/bin/bash

for file in ../benchmark/*-full.xml
do
    echo $file:
    xmlstarlet sel -t -n -c "//xpath[schemas/validation[@schema='$1' and @valid='yes'] and schemas/validation[@schema='$2' and @valid='no']]/query" $file
    echo
    echo
done
