#!/bin/bash

xslt=`grep 'xslt' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
xquery=`grep 'xquery' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`

printf '\\begin{tabular}{lrrr}\n'
printf '\\toprule\n'
printf 'Axis & XSLT & XQuery & Total \\\\\n'
printf '\\midrule\n'

axis_list=("ancestor" "attribute" "child" "descendant" "following" "following-sibling" "namespace" "parent" "descendant" "preceding" "preceding-sibling" "self")

for axis in ${axis_list[*]}
do
  printf '\\texttt{'
  printf $axis
  printf '}'
  printf ' & '
  # XSLT files
  count_xslt=0
  for file in $xslt
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[ast//xqx:xpathAxis[. = \"$axis\" or . = \"$axis-or-self\"]])" $file`
    count_xslt=$(($count_xslt+$count))
  done
  printf $count_xslt
  printf ' & '
  # XQuery files
  count_xquery=0
  for file in $xquery
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[ast//xqx:xpathAxis[. = \"$axis\" or . = \"$axis-or-self\"]])" $file`
    count_xquery=$(($count_xquery+$count))
  done
  printf $count_xquery
  printf ' & '
  count_total=$(($count_xslt+$count_xquery))
  printf $count_total
  printf ' \\\\\n'
done
printf '\\bottomrule\n'
printf '\\end{tabular}\n'
