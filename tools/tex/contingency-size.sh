#!/bin/bash
# compute contingency table of benchmark results
# with rows = `accepted by at least one full fragment'
# and  cols = `query size'
# using list of files provided on command-line

MAX=`grep 'MAX_AST_SIZE =' ../../src/main/java/fr/lsv/xpparser/XPathEntry.java| sed -e 's/[^0-9]*\([0-9]*\).*/\1/'`
fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )

accepted="@schema=\"${fragments[0]}\""
for ((f = 1; f < ${#fragments[@]}; ++f))
do
    accepted="$accepted or @schema=\"${fragments[f]}\""
done

# first row: accepted
for ((i = 1; i < $MAX; ++i))
do
    value=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[ast/@size=\"$i\" and schemas/validation[$accepted and @valid=\"yes\"]])" $@`
    printf "$value\t"
done
echo
# second row: not accepted
for ((i = 1; i < $MAX; ++i))
do
    value=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[ast/@size=\"$i\" and not(schemas/validation[$accepted and @valid=\"yes\"])])" $@`
    printf "$value\t"
done
echo
