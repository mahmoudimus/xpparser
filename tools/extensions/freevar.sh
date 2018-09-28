#!/bin/bash
echo "<freevar>"
sum=0
for FILE in `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='varRef' and not(*[local-name()='name'] = ancestor::*[local-name()='flworExpr']/*/*/*/*[local-name()='varName'])]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</freevar>"

printf " %'.0f &" $sum >> occurrences-ext.tex
