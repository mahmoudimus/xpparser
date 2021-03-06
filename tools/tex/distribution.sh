#!/bin/bash
# count the number of queries of each size
# argument: the size of blocks of queries, 1 by default

if [ "$#" -ne 1 ]
then
    step=1
else
    step=$1
fi
if [ x${FRAGTYPE}x = xx ] ; then FRAGTYPE='xslt\|xquery' ; fi
fragments=`grep 'type="\('${FRAGTYPE}'\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
MAX=`grep 'MAX_AST_SIZE =' ../../src/main/java/fr/lsv/xpparser/XPathEntry.java| sed -e 's/[^0-9]*\([0-9]*\).*/\1/'`

numqueries=`cat numqueries`
half=$((numqueries / 2))
total=0
for ((i = 1; i < $MAX; i+=$step))
do
    C=0
    for ((j = $i; j < $i+$step; ++j))
    do
        G=`grep "ast depth=.* size=\"$j\"" $fragments | wc -l`
        C=$((C + G))
    done
    total=$((C + total))
    if [ "$total" -gt "$half" ]
    then
        printf "%'.0f" $i > median.tex
        half=$numqueries
    fi
    echo "$i $C $total"
done

