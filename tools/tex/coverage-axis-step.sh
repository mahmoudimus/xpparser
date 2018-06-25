#!/bin/bash
# compute coverage according to the presence of at least one axis step
# with rows = `accepted by at least one full fragment'
# using list of files provided on command-line

fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )

full="(@schema=\"${fragments[0]}\")"
for ((f = 1; f < ${#fragments[@]}; ++f))
do
    full="$full or (@schema=\"${fragments[f]}\")"
done

# head
printf '\\begin{tabular}{lrr}\n'
printf '\\toprule\n'
printf '& $\\geq 1$ axis step & no axis step\\\\\n'
printf '\\midrule\n'
printf 'coverage '

# at least one axis step
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast//xqx:xpathAxis) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
total=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[(ast//xqx:xpathAxis)])" $file`
    total=$((count + total))
done
percent=`echo "scale=2; 100*$value/$total" | bc`
printf "& $percent\\%% "

# no axis step
value=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[not(ast//xqx:xpathAxis) and (schemas/validation[($full) and @valid=\"yes\"])])" $file`
    value=$((count + value))
done
total=0
for file in $@
do
    count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(//xpath[not(ast//xqx:xpathAxis)])" $file`
    total=$((count + total))
done
percent=`echo "scale=2; 100*$value/$total" | bc`
printf "& $percent\\%%"
printf '\\\\\n'
printf '\\bottomrule\n'
printf '\\end{tabular}\n'
