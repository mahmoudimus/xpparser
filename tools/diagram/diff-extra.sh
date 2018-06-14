#!/bin/bash

#Extra
line="Extra"
for schema in $(xmlstarlet sel -t -v "/schemas/schema/@file" ../../relaxng/fragments-full.xml); do
  full=$schema
  extra=$(echo $schema | sed s/[a-z]*.rnc/extra.rnc/)
  total=0
  value=0
  for file in ../../benchmark/*-full.xml; do 
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[schemas[validation[@schema='$full' and @valid='no'] and validation[@schema='$extra' and @valid='yes']]])" $file)
    total=$(($total+$value))
  done
  line="$line $total"
done
echo $line
