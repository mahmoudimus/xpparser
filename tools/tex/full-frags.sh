#!/bin/bash

fragments=( `grep 'file=' $1 | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )

printf 'count(/benchmark/xpath[schemas[validation[('
printf "@schema='${fragments[0]}' "
for ((f=1; f<${#fragments[@]}; ++f))
do
    printf "or @schema='${fragments[f]}' "
done
echo ") and @valid='yes']]])"
