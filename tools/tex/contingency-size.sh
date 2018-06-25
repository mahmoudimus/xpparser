#!/bin/bash
# compute contingency table of benchmark results
# with rows = `accepted by at least one full fragment'
# and  cols = `query size'
# using list of files provided on command-line

MAX=`grep 'MAX_AST_SIZE =' ../../src/main/java/fr/lsv/xpparser/XPathEntry.java| sed -e 's/[^0-9]*\([0-9]*\).*/\1/'`
fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )
step=4
cutoff=48

full="(@schema=\"${fragments[0]}\")"
for ((f = 1; f < ${#fragments[@]}; ++f))
do
    full="$full or (@schema=\"${fragments[f]}\")"
done

# first row: accepted
for ((i = 1; i < $cutoff; i+=$step))
do
    value=0
    for file in $@
    do
        count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $i and @size < ($i+$step)]) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
        value=$((count + value))
    done
    printf "$value\t"
done
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $cutoff]) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
printf "$value\t"
echo
# second row: not accepted
for ((i = 1; i < $cutoff; i+=$step))
do
    value=0
    for file in $@
    do
        count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $i and @size < ($i+$step)]) and not(schemas/validation[($full) and @valid=\"yes\"])])" $file`
        value=$((count + value))
    done
    printf "$value\t"
done
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $cutoff]) and not(schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
printf "$value\t"
echo
