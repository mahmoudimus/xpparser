echo "<positiveJoin>"
sum=0
for FILE in ../../benchmark/*xml
do
  filename=$(basename "$FILE")
  if [[ $filename = *"-full.xml" || $filename = "w3c.xml" || $filename = "xpathmark.xml" ]]; then
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and not(ancestor::*[local-name()='functionCallExpr']/*[local-name()='functionName' and . = 'not'])]])" $FILE)
    sum=$(($sum+$value))
    echo "  <source @name='$filename'>$value</source>"
  fi
done
echo "  <total>$sum</total>"
echo "</positiveJoin>"
