#!/bin/bash

xpmft=`grep 'xpathmark-ft' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
xpmpt=`grep 'xpathmark-pt' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
xm=`grep 'xmark' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`

printf '\\begin{tabular}{lrrrrr}\n'
printf '\\toprule\n'
printf '\\multicolumn{1}{c}{Sources} & \\multicolumn{1}{c}{Queries} & \\multicolumn{4}{c}{Coverage}\\\\\n'
printf ' & & \\multicolumn{1}{c}{XPath\\,1.0} & \\multicolumn{1}{c}{XPath\\,2.0} & \\multicolumn{1}{c}{XPath\,3.0} & \\multicolumn{1}{c}{XPath\,3.0\,std}\\\\\n'
printf '\\midrule\n'

# total number of XPathMark-FT queries
printf 'XPathMark-FT '
n=`grep '<ast' $xpmft | wc -l`
printf "& %'.0f " $n
#coverage of standard XPath languages
for ((i=1; i < 4; ++i))
do
    count=0
    for file in $xpmft
    do
        c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-$i.0.rnc\" and @valid=\"yes\"]])" $file`
        count=$((count+c))
    done
    counts[$i]=$count
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
done
count=0
for file in $xpmft
do
    c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-3.0-std.rnc\" and @valid=\"yes\"]])" $file`
    count=$((count+c))
done
counts[4]=$count
percent=`echo "scale=1; 100*$count/$n" | bc`
printf "& $percent\\\\%% "
printf '\\\\\n'

N=$n
# total number of XPathMark-PT queries
printf 'XPathMark-PT '
n=`grep '<ast' $xpmpt | wc -l`
printf "& %'.0f " $n
#coverage of standard XPath languages
for ((i=1; i < 4; ++i))
do
    count=0
    for file in $xpmpt
    do
        c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-$i.0.rnc\" and @valid=\"yes\"]])" $file`
        count=$((count+c))
    done
    counts[$i]=$((count + counts[i]))
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
done
count=0
for file in $xpmpt
do
    c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-3.0-std.rnc\" and @valid=\"yes\"]])" $file`
    count=$((count+c))
done
counts[4]=$((count + counts[4]))
percent=`echo "scale=1; 100*$count/$n" | bc`
printf "& $percent\\\\%% "
printf '\\\\\n'

N=$((N + n))
# total number of XQuery queries
printf 'XMark '
n=`grep '<ast' $xm | wc -l`
printf "& %'.0f " $n
# coverage of standard XPath languages
for ((i=1; i < 4; ++i))
do
    count=0
    for file in $xm
    do
        c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-$i.0.rnc\" and @valid=\"yes\"]])" $file`
        count=$((count+c))
    done
    counts[$i]=$((count + counts[i]))
    percent=`echo "scale=1; 100*$count/$n" | bc`
    printf "& $percent\\\\%% "
done
count=0
for file in $xm
do
    c=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[schemas/validation[@schema=\"xpath-3.0-std.rnc\" and @valid=\"yes\"]])" $file`
    count=$((count+c))
done
counts[4]=$((count + counts[4]))
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
