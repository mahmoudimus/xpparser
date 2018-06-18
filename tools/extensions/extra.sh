#!/bin/bash

for schema in $(xmlstarlet sel -t -v "/schemas/schema/@file" ../../relaxng/fragments-full.xml); do
  extra=$(echo $schema | sed s/[a-z]*.rnc/extra.rnc/)
  echo "Differences between" $schema "and" $extra
  total=0
  value=0
  for file in ../../benchmark/*-full.xml; do 
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[schemas[validation[@schema='$schema' and @valid='no'] and validation[@schema='$extra' and @valid='yes']]])" $file)
    echo "    "$(basename $file)":" $value
    total=$(($total+$value))
  done
  echo "Total:" $total
done
