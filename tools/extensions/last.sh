#!/bin/bash
echo "<last>"
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='functionName' and .='last']])" $FILE)
  sum=$(($sum+$value))
  echo "  <source @name='$filename'>$value</source>"
done
echo "  <total>$sum</total>"
echo "</last>"
