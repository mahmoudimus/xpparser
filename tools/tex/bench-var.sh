#!/bin/bash

# path to benchmark files
path=$1
full=$2
xmlarray=($(grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*benchmark\/\(.*\)-full.xml.*/\1/'))
names=($(grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*name="\(.*\)".*/\1/'))

echo "\\toprule"
echo "Source & \\textsf{Core~1.0} & \\textsf{Core~2.0} & \\textsf{Downward} & \\textsf{Vertical} & \\textsf{Forward} & \\textsf{EMSO\$^\\textsf 2\$} & \\textsf{NonMixing}\\\\"
echo "\\midrule"

for ((i = 0; i < ${#xmlarray[@]}; ++i))
do
    printf "${names[$i]} "
    echo "`./numbers.sh $path/${xmlarray[$i]}$full.xml 'xpath-1.0-core-var.rnc.*yes' 'xpath-2.0-core-orig.rnc.*yes' 'xpath-1.0-downward-orig.rnc.*yes' 'xpath-1.0-vertical-var.rnc.*yes' 'xpath-1.0-forward-orig.rnc.*yes' 'xpath-emso2-var.rnc.*yes' 'xpath-non-mixing-orig.rnc.*yes'`\\\\"
done
echo "\\bottomrule"

