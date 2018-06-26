#!/bin/bash
echo "<constantJoin>"
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp' or local-name()='ltOp' or local-name()='leOp' or local-name()='gtOp' or local-name()='geOp' or local-name()='lessThanOp' or local-name='lessThanOrEqualOp' or local-name()='greaterThanOp' or local-name()='greaterThanOrEqualOp') and child::*[local-name()='firstOperand' or local-name()='secondOperand']/child::*[local-name()='integerConstantExpr' or local-name()='decimalConstantExpr' or local-name()='doubleConstantExpr' or local-name()='stringConstantExpr']]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</constantJoin>"

printf " %'.0f &" $sum >> occurrences-ext.tex
