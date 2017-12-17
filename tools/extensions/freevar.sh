echo "<freevar>"
sum=0
for FILE in ../../benchmark/*xml
do
  filename=$(basename "$FILE")
  value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='varRef' and not(name = ancestor::*[local-name()='flworExpr']/*/*/*/*[local-name()='varName'])]])" $FILE)
  sum=$(($sum+$value))
  echo "  <source @name='$filename'>$value</source>"
done
echo "  <total>$sum</total>"
echo "</freevar>"
