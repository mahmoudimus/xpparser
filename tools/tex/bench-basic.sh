#!/bin/bash

# path to benchmark files
path=$1
full=$2
xmlarray=($(grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*benchmark\/\(.*\)-full.xml.*/\1/'))
names=($(grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*name="\(.*\)".*/\1/'))

echo "\\toprule"
echo "Source & \\textsf{Positive} &  \\textsf{Core~1.0} & \\textsf{Core~2.0} & \\textsf{Downward} & \\textsf{Vertical} & \\textsf{Forward} & \\textsf{EMSO\$^\\textsf 2\$} & \\textsf{NonMixing}\\\\"
echo "\\midrule"

for ((i = 0; i < ${#xmlarray[@]}; ++i))
do
    printf "${names[$i]}  \t"
    echo "`./numbers.sh $path/${xmlarray[$i]}$full.xml '<schemas>' 'xpath-efo-basic.rnc.*yes' 'xpath-1.0-core-basic.rnc.*yes' 'xpath-2.0-core-basic.rnc.*yes' 'xpath-1.0-downward-basic.rnc.*yes' 'xpath-1.0-vertical-basic.rnc.*yes' 'xpath-1.0-forward-basic.rnc.*yes' 'xpath-emso2-basic.rnc.*yes' 'xpath-non-mixing-basic.rnc.*yes'`\\\\"
done
echo "\\bottomrule"

