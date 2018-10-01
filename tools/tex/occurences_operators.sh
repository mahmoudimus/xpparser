#!/bin/bash

xslt=`grep 'xslt' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
xquery=`grep 'xquery' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`

# XSLT queries (total)
total_xslt=0
for file in $xslt
do
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath)" $file`
  total_xslt=$(($total_xslt+$count))
done

# Xquery queries (total)
total_xquery=0
for file in $xquery
do
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath)" $file`
  total_xquery=$(($total_xquery+$count))
done

# Total queries
total=$(($total_xslt+$total_xquery))

printf '\\begin{tabular}{lr@{\\hspace{1pt}}rr@{\\hspace{1pt}}rr@{\\hspace{1pt}}r}\n'
printf '\\toprule\n'
printf 'Unsupported construct & \\multicolumn{2}{c}{XSLT} & \\multicolumn{2}{c}{XQuery} & \\multicolumn{2}{c}{Total} \\\\\n'
printf '\\midrule\n'

list=(ifThenElseExpr simpleMapExpr dynamicFunctionInvocationExpr inlineFunctionExpr namedFunctionRef rangeSequenceExpr instanceOfExpr piTest castExpr)
legend=('if then else' 'simple map' 'dynamic function invocation' 'inline function' 'named function' 'range sequence' 'instance of' 'proccessing instruction' 'cast related expressions')

n=`echo ${#list[@]}`
for (( i=0; i<$n; i++))
do
  name=`echo ${list[$i]}`
  printf '%s' "${legend[$i]}"
  printf ' & '
  # XSLT files
  count_xslt=0
  for file in $xslt
  do
    if [ $name = "castExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast[.//xqx:treatExpr or .//xqx:castableExpr or .//xqx:castExpr]])" $file`
    else
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:$name])" $file`
    fi
    count_xslt=$(($count_xslt+$count))
  done
  percent=`echo "scale=1; 100*$count_xslt/$total_xslt" | bc`
  printf "%'.0f & (%.1f\\%%)" $count_xslt $percent
  printf ' & '
  # XQuery files
  count_xquery=0
  for file in $xquery
  do
    if [ $name = "castExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast[.//xqx:treatExpr or .//xqx:castableExpr or .//xqx:castExpr]])" $file`
    else
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:$name])" $file`
    fi
    count_xquery=$(($count_xquery+$count))
  done
  percent=`echo "scale=1; 100*$count_xquery/$total_xquery" | bc`
  printf "%'.0f & (%.1f\\%%)" $count_xquery $percent
  printf ' & '
  count_total=$(($count_xslt+$count_xquery))
  percent=`echo "scale=1; 100*$count_total/$total" | bc`
  printf "%'.0f & (%.1f\\%%)" $count_total $percent
  printf ' \\\\\n'
done
printf '\\bottomrule\n'
printf '\\end{tabular}\n'
