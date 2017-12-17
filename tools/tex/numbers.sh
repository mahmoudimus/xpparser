#!/bin/bash

file=$1
shift

while test ${#} -gt 0
do
    printf "& `grep \"$1\" $file | wc -l` "
    shift
done

#echo "& `grep '<query>' $1 | wc -l` & `grep 'xpath-1.0.rnc' $1 | grep 'yes' | wc -l` & `grep 'xpath-2.0.rnc' $1 | grep 'yes' | wc -l` & `grep 'xpath-3.0.rnc' $1 | grep 'yes' | wc -l`\\\\"
