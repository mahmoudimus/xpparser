#!/bin/bash

for schema in $(xmlstarlet sel -t -v "/schemas/schema/@file" `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`); do
  extra=$(echo $schema | sed s/[a-z]*.rnc/extra.rnc/)
  echo "Differences between" $schema "and" $extra
  total=0
  value=0
  for file in `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`; do 
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[schemas[validation[@schema='$schema' and @valid='no'] and validation[@schema='$extra' and @valid='yes']]])" $file)
    echo "    "$(basename $file)":" $value
    total=$(($total+$value))
  done
  echo "Total:" $total
done
