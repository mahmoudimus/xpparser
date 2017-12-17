echo "<constantJoin>"
sum=0
for FILE in ../../benchmark/*xml
do
  filename=$(basename "$FILE")
  value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and */*[local-name()='intergerConstantExpr' or local-name()='decimalConstantExpr' or local-name()='doubleConstantExpr' or local-name()='stringConstantExpr']]])" $FILE)
  sum=$(($sum+$value))
  echo "  <source @name='$filename'>$value</source>"
done
echo "  <total>$sum</total>"
echo "</constantJoin>"
