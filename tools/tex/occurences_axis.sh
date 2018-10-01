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
printf 'Axis & \\multicolumn{2}{c}{XSLT} & \\multicolumn{2}{c}{XQuery} & \\multicolumn{2}{c}{Total} \\\\\n'
printf '\\midrule\n'

axis_list=("ancestor" "attribute" "child" "descendant" "following" "following-sibling" "namespace" "parent" "preceding" "preceding-sibling" "self")

for axis in ${axis_list[*]}
do
  printf '\\texttt{'
  printf $axis
  printf '}'
  if [ $axis = "ancestor" ] || [ $axis = "descendant" ]; then
    printf '(\\texttt{-or-self})'
  fi
  printf ' & '
  # XSLT files
  count_xslt=0
  for file in $xslt
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:xpathAxis[. = \"$axis\" or . = \"$axis-or-self\"]])" $file`
    count_xslt=$(($count_xslt+$count))
  done
  percent=`echo "scale=1; 100*$count_xslt/$total_xslt" | bc`
  printf "%'.0f & (%.1f\\%%)" $count_xslt $percent
  printf ' & '
  # XQuery files
  count_xquery=0
  for file in $xquery
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:xpathAxis[. = \"$axis\" or . = \"$axis-or-self\"]])" $file`
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
printf '\\midrule\n'
printf 'All axes & '
# XSLT files
count_xslt=0
for file in $xslt
do
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:xpathAxis])" $file`
  count_xslt=$(($count_xslt+$count))
done
percent=`echo "scale=1; 100*$count_xslt/$total_xslt" | bc`
printf "%'.0f & (%.1f\\%%)" $count_xslt $percent
printf ' & '
# XQuery files
count_xquery=0
for file in $xquery
do
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:xpathAxis])" $file`
  count_xquery=$(($count_xquery+$count))
done
percent=`echo "scale=1; 100*$count_xquery/$total_xquery" | bc`
printf "%'.0f & (%.1f\\%%)" $count_xquery $percent
printf ' & '
count_total=$(($count_xslt+$count_xquery))
percent=`echo "scale=1; 100*$count_total/$total" | bc`
printf "%'.0f & (%.1f\\%%)" $count_total $percent
printf ' \\\\\n'
printf '\\bottomrule\n'
printf '\\end{tabular}\n'
