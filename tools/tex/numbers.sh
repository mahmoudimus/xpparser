#!/bin/bash

file=$1
shift
total=`grep "$1" $file | wc -l`
#printf "& $total "
shift


while test ${#} -gt 0
do
    count=`grep "$1" $file | wc -l`
    percent=`bc <<< "100*$count/$total"`
    if [ $percent -lt 10 ]
    then
       printf "&  $percent\\%% "
    else
        printf "& $percent\\%% "
    fi
    #printf "& $count "
    shift
done

