#!/bin/bash
echo "<root>"
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='functionName' and .='root']] | /benchmark/xpath[ast/descendant::*[local-name()='rootExpr']])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</root>"

printf " %'.0f &" $sum >> occurrences-ext.tex
