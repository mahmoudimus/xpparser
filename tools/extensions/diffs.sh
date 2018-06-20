#!/bin/bash

no=$1
shift
yes=$1
shift
total=0
for file in $@
do 
    value=$(xmlstarlet sel -t -c "count(/benchmark/xpath[schemas[validation[@schema='$no' and @valid='no'] and validation[@schema='$yes' and @valid='yes']]])" $file)
    total=$(($total+$value))
done
printf "%'.0f" $total
