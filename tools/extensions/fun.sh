#!/bin/bash
echo "<last>"
sum=0
for FILE in `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='functionName' and (.='count' or .='concat' or .='contains' or .='substring-before' or .='substring-after' or .='string-length' or .='string-join' or .='normalize-space' or .='starts-with' or .='translate')) or (local-name()='addOp' or local-name()='subtractOp' or local-name()='unaryMinusOp' or local-name()='multiplyOp')]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</last>"
