#!/bin/bash
echo "<fullJoin>"
sum=0
for FILE in ../../benchmark/*-full.xml
do
  filename=$(basename "$FILE")
  value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and child::*[local-name()='firstOperand']/child::*[local-name()='pathExpr'] and child::*[local-name()='secondOperand']/child::*[local-name()='pathExpr']]])" $FILE)
  sum=$(($sum+$value))
  echo "  <source @name='$filename'>$value</source>"
done
echo "  <total>$sum</total>"
echo "</fullJoin>"
