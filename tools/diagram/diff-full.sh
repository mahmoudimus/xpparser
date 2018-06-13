#!/bin/bash

# Names
line="Extensions"
for name in $(xmlstarlet sel -t -v "/schemas/schema/@nametex" ../../relaxng/fragments-full.xml); do
  line="$line $name"
done
echo $line

# Orig
line="None"
for schema in $(xmlstarlet sel -t -v "/schemas/schema/@file" ../../relaxng/fragments-full.xml); do
  orig=$(echo $schema | sed s/[a-z]*.rnc/orig.rnc/)
  value=0
  total=0
  for file in ../../benchmark/*-full.xml; do 
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[schemas[validation[@schema='$orig' and @valid='yes']]])" $file)
    total=$(($total+$value))
  done
  line="$line $total"
done
echo $line

#Basic
line="Basic"
for schema in $(xmlstarlet sel -t -v "/schemas/schema/@file" ../../relaxng/fragments-full.xml); do
  orig=$(echo $schema | sed s/[a-z]*.rnc/orig.rnc/)
  basic=$(echo $schema | sed s/[a-z]*.rnc/basic.rnc/)
  total=0
  value=0
  for file in ../../benchmark/*-full.xml; do 
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[schemas[validation[@schema='$orig' and @valid='no'] and validation[@schema='$basic' and @valid='yes']]])" $file)
    total=$(($total+$value))
  done
  line="$line $total"
done
echo $line

#Advanced
line="Advanced"
for schema in $(xmlstarlet sel -t -v "/schemas/schema/@file" ../../relaxng/fragments-full.xml); do
  basic=$(echo $schema | sed s/[a-z]*.rnc/basic.rnc/)
  full=$schema
  total=0
  value=0
  for file in ../../benchmark/*-full.xml; do 
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[schemas[validation[@schema='$basic' and @valid='no'] and validation[@schema='$full' and @valid='yes']]])" $file)
    total=$(($total+$value))
  done
  line="$line $total"
done
echo $line
