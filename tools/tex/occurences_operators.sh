#!/bin/bash

xslt=`grep 'xslt' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
xquery=`grep 'xquery' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`

printf '\\begin{tabular}{lrrr}\n'
printf '\\toprule\n'
printf 'Operator & XSLT & XQuery & Total \\\\\n'
printf '\\midrule\n'

list=(xpathAxis rootExpr functionCallExpr ifThenElseExpr letExpr forExpr simpleMapExpr dynamicFunctionInvocationExpr inlineFunctionExpr namedFunctionRef quantifiedExpr rangeSequenceExpr instanceOfExpr treatExpr nameTest piTest castableExpr castExpr)

for name in ${list[*]}
do
  printf $name
  printf ' & '
  # XSLT files
  count_xslt=0
  for file in $xslt
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[ast//xqx:$name])" $file`
    count_xslt=$(($count_xslt+$count))
  done
  printf $count_xslt
  printf ' & '
  # XQuery files
  count_xquery=0
  for file in $xquery
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[ast//xqx:$name])" $file`
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
