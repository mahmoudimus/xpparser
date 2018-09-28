#!/bin/bash
echo "<last>"
sum=0
for FILE in `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='functionName' and .='last') or (local-name()='predicates' and (child::*[local-name()='integerConstantExpr'] or child::*[(local-name()='equalOp' or local-name()='notEqualOp') and child::*/child::*[local-name()='functionCallExpr' and child::* = 'position'] and child::*/child::*[local-name()='integerConstantExpr']]))]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</last>"

printf " %'.0f &" $sum >> occurrences-ext.tex
