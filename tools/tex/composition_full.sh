#!/bin/bash

fragment='xpath-2.0-core-join.rnc'

xslt=`grep 'xslt' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
xquery=`grep 'xquery' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`

printf '\\begin{tabular}{lrrrrr}\n'
printf '\\toprule\n'
printf 'Source & queries & XPath\\,1.0 & XPath\\,2.0 & XPath\,3.0 & \\textsf{CoreXPath~2.0}\\,extended\\\\\n'
printf '\\midrule\n'

# XSLT files
for file in $xslt
do
  name=`grep $file benchmarks-all-full.xml | sed -e 's/.*name="\([^"]*\).*/\1/'`
  url=`grep $file benchmarks-all-full.xml | sed -e 's/.*url="\([^"]*\).*/\1/'`
  printf '\\vspace{-0.5em} '
  printf "$name"
  n=`grep '<ast' $file | wc -l`
  printf "& %'.0f " $n
  #coverage of standard XPath languages
  for ((i=1; i < 4; ++i))
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-$i.0.rnc\" and @valid=\"yes\"]])" $file`
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
  done
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"$fragment\" and @valid=\"yes\"]])" $file`
  percent=`echo "scale=1; 100*$count/$n" | bc`
  printf "& $percent\\\\%% "
  printf '\\\\\n'
  printf "{\\\\tiny \\\\url{$url}} & & & & \\\\\\\\\n"
done

# total number of XSLT queries
printf '\\midrule\n'
printf 'Total (XSLT) '
n=`grep '<ast' $xslt | wc -l`
printf "& %'.0f " $n
#coverage of standard XPath languages
for ((i=1; i < 4; ++i))
do
    count=0
    for file in $xslt
    do
        c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-$i.0.rnc\" and @valid=\"yes\"]])" $file`
        count=$((count+c))
    done
    counts[$i]=$count
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
done
count=0
for file in $xslt
do
  c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"$fragment\" and @valid=\"yes\"]])" $file`
  count=$((count+c))
done
counts[4]=$count
percent=`echo "scale=1; 100*$count/$n" | bc`
printf "& $percent\\\\%% "
printf '\\\\\n'
printf '\\midrule\n'

N=$n

# XQuery files
for file in $xquery
do
  name=`grep $file benchmarks-all-full.xml | sed -e 's/.*name="\([^"]*\).*/\1/'`
  url=`grep $file benchmarks-all-full.xml | sed -e 's/.*url="\([^"]*\).*/\1/'`
  printf '\\vspace{-0.5em} '
  printf "$name"
  n=`grep '<ast' $file | wc -l`
  printf "& %'.0f " $n
  #coverage of standard XPath languages
  for ((i=1; i < 4; ++i))
  do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-$i.0.rnc\" and @valid=\"yes\"]])" $file`
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
  done
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"$fragment\" and @valid=\"yes\"]])" $file`
  percent=`echo "scale=1; 100*$count/$n" | bc`
  printf "& $percent\\\\%% "
  printf '\\\\\n'
  printf "{\\\\tiny \\\\url{$url}} & & & & \\\\\\\\\n"
done

# total number of XQuery queries
printf '\\midrule\n'
printf 'Total (XQuery) '
n=`grep '<ast' $xquery | wc -l`
printf "& %'.0f " $n
# coverage of standard XPath languages
for ((i=1; i < 4; ++i))
do
    count=0
    for file in $xquery
    do
        c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-$i.0.rnc\" and @valid=\"yes\"]])" $file`
        count=$((count+c))
    done
    counts[$i]=$((count + counts[i]))
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
done
    count=0
    for file in $xquery
    do
        c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"$fragment\" and @valid=\"yes\"]])" $file`
        count=$((count+c))
    done
    counts[4]=$((count + counts[i]))
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
printf '\\\\\n'

N=$((N + n))
printf '\\midrule\n'
printf 'Total '
printf "& %'.0f " $N
for ((i=1; i < 5; ++i))
do
    count=${counts[i]}
    percent=`echo "scale=1; 100*$count/$N" | bc`
    printf "& $percent\\\\%% "
done
printf '\\\\\n'

printf '\\bottomrule\n'
printf '\\end{tabular}\n'


# \begin{tabular}{lrrrr}
#     \toprule
#     Source & \!\!queries & \!\!XPath\,1.0 & \!\!XPath\,2.0 & \!\!XPath\,3.0\\
#     % \midrule
#     % W3C QT     & 207 & 132 & 138 & 207\\
#     % XPathMark & 38 & 38 & 38 & 38\\
#     \midrule
#     DocBook & 7,620 & 7,620 & 7,620 & 7,620 \\
#     HTMLBook & 752 & 752 & 752 & 752 \\
#     eXist-db & 1,236 & 955 & 1,105 & 1,236 \\
#     HisTEI & 483 & 361 & 471 & 483 \\
#     MarkLogic & 196 & 139 & 184 & 191 \\
#     XQJSON & 90 & 67 & 90 & 90 \\
#     \midrule
#     Total & 10,377 & 9,894 & 10,222 & 10,372\\
#           &        & (95\%) & (98\%)  & (100\%)\\
#     \bottomrule
# \end{tabular}
