#!/bin/bash
# compute contingency table of benchmark results
# with rows = `accepted by at least one full fragment'
# and  cols = `contains at least one axis step'
# using list of files provided on command-line

fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )

full="(@schema=\"${fragments[0]}\")"
for ((f = 1; f < ${#fragments[@]}; ++f))
do
    full="$full or (@schema=\"${fragments[f]}\")"
done

# first row: accepted
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast//xqx:xpathAxis) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
printf "$value\t"

value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[not(ast//xqx:xpathAxis) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
printf "$value\t"
echo

# second row: not accepted
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast//xqx:xpathAxis) and not(schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
printf "$value\t"

value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[not(ast//xqx:xpathAxis) and not(schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
printf "$value\t"
echo
