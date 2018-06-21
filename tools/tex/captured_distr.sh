#!/bin/bash

# Maximum allowed size of XPath queries in benchmark
MAX=`grep 'MAX_AST_SIZE =' ../../src/main/java/fr/lsv/xpparser/XPathEntry.java| sed -e 's/[^0-9]*\([0-9]*\).*/\1/'`

# XPath query that recognizes schemas of -full fragments
Q="false"
for f in `xmlstarlet sel -t -v "//schema/@file" \
  -n ../../relaxng/fragments-full.xml` ; do
  Q="$Q or @schema='$f'"
done

# For each possible size, count queries in benchmark,
# and count queries that fall in at least one -full fragment.
for ((i = 1; i < $MAX; ++i))
do
    C=`grep "ast depth=.* size=\"$i\"" ../../benchmark/*-full.xml | wc -l`
    N=0
    [ C = 0 ] || for n in `xmlstarlet sel -t -c \
      "count(//xpath[ast/@size=$i and schemas/validation[$Q]/@valid='yes'])" \
      -n ../../benchmark/*-full.xml` ; do
      N=$(($n+$N))
    done
    echo "$i $C $N"
done

