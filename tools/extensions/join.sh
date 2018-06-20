#!/bin/bash
echo "<pureJoin>"
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and child::*[local-name()='firstOperand']/child::*[local-name()='pathExpr' or local-name()='contextItemExpr'] and child::*[local-name()='secondOperand']/child::*[local-name()='pathExpr' or local-name()='contextItemExpr']]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</pureJoin>"

printf "%'.0f" $sum > occurrences-pure-joins.tex

echo "<fullJoin>"
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and not(child::*[local-name()='firstOperand' or local-name()='secondOperand']/child::*[local-name()='integerConstantExpr' or local-name()='decimalConstantExpr' or local-name()='doubleConstantExpr' or local-name()='stringConstantExpr'])]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</fullJoin>"

printf " %'.0f &" $sum >> occurrences-ext.tex
