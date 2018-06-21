#!/bin/bash

# path to benchmark files
path=$1
full=$2
xmlarray=($(grep href benchmarks-all-full.xml | sed -e 's/.*benchmark\/\(.*\)-full.xml.*/\1/'))
names=($(grep href benchmarks-all-full.xml | sed -e 's/.*name="\(.*\)".*/\1/'))

echo "\\toprule"
echo "Source & queries & XPath~1.0 & XPath~2.0 & XPath~3.0\\\\"
echo "\\midrule"

for ((i = 0; i < ${#xmlarray[@]}; ++i))
do
    printf "${names[$i]} "
    echo "`./numbers.sh $path/${xmlarray[$i]}$full.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
done
echo "\\bottomrule"

