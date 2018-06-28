#!/bin/bash
echo "<count>"
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:functionName[text()='count']])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</count>"
echo "<countcst>"
printf "%'.0f" $sum > count-aggregation.tex
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel  -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[descendant::*[(local-name()='ltOp' or local-name()='leOp' or local-name()='gtOp' or local-name()='geOp' or local-name()='eqOp' or local-name()='neOp' or local-name()='equalOp' or local-name()='notEqualOp' or local-name()='lessThanOp' or local-name()='lessThanOrEqualOp' or local-name()='greaterThanOp' or local-name()='greaterThanOrEqualOp') and ((xqx:firstOperand | xqx:secondOperand)/xqx:functionCallExpr/xqx:functionName[text()='count']) and ((xqx:firstOperand | xqx:secondOperand)/xqx:integerConstantExpr)]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</countcst>"
printf "%'.0f" $sum > count-aggregation-cst.tex
