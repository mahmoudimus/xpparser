#!/bin/bash
# compute coverage table of benchmark results by size
# with rows = `accepted by at least one full fragment'
# and  cols = `query size'
# using list of files provided on command-line

MAX=`grep 'MAX_AST_SIZE =' ../../src/main/java/fr/lsv/xpparser/XPathEntry.java| sed -e 's/[^0-9]*\([0-9]*\).*/\1/'`
fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )
step=4
cutoff=48

full="(@schema=\"${fragments[0]}\")"
for ((f = 1; f < ${#fragments[@]}; ++f))
do
    full="$full or (@schema=\"${fragments[f]}\")"
done


# header
printf '\\begin{tabular}{l'
for ((i = 1; i < $cutoff; i+=$step))
do
    printf 'r'
done
printf 'r}\n\\toprule\n'
printf 'sizes '
for ((i = 1; i < $cutoff; i+=$step))
do
    printf "& $i--$((i+step-1)) "
done
printf "& $\ge $i$"
printf '\\\\\n'
printf '\\midrule\n'

# number of queries of each size
printf 'queries '
for ((i = 1; i < $cutoff; i+=$step))
do
    value=0
    for file in $@
    do
        count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $i and @size < ($i+$step)])])" $file`
        value=$((count + value))
    done
    array[$i]=$value
    printf "& %'.0f " ${array[i]}
done
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $i])])" $file`
    value=$((count + value))
done
array[$i]=$value
printf "& %'.0f" $value
printf '\\\\\n'

# first row: accepted
printf 'coverage '
for ((i = 1; i < $cutoff; i+=$step))
do
    value=0
    for file in $@
    do
        count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $i and @size < ($i+$step)]) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
        value=$((count + value))
    done
    percent=`echo "scale=1; 100*$value/${array[i]}" | bc`
    printf "& $percent\\%% "
done
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast[@size >= $i]) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
percent=`echo "scale=1; 100*$value/${array[i]}" | bc`
printf "& $percent\\%% "
printf '\\\\\n'
printf '\\bottomrule\n'
printf '\\end{tabular}\n'
