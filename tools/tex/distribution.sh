#!/bin/bash

MAX=`grep 'MAX_AST_SIZE =' ../../src/main/java/fr/lsv/xpparser/XPathEntry.java| sed -e 's/[^0-9]*\([0-9]*\).*/\1/'`

for ((i = 1; i < $MAX; ++i))
do
    C=`grep "ast depth=.* size=\"$i\"" ../../benchmark/*-full.xml | wc -l`
    echo "$i $C"
done

