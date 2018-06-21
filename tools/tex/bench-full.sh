#!/bin/bash

# path to benchmark files
path=$1
full=$2
xmlarray=($(grep href benchmarks-all-full.xml | sed -e 's/.*benchmark\/\(.*\)-full.xml.*/\1/'))
names=($(grep href benchmarks-all-full.xml | sed -e 's/.*name="\(.*\)".*/\1/'))

echo "\\toprule"
echo "Source & \\textsf{Full} & \\textsf{Positive} & \\textsf{Positive}-Xtra & \\textsf{Core~1.0} & \\textsf{Core~1.0}-Xtra & \\textsf{Core~2.0} & \\textsf{Core~2}-Xtra & \\textsf{Downward} & \\textsf{Vertical} & \\textsf{Forward} & \\textsf{EMSO\$^\\textsf 2\$} & \\textsf{NonMixing}\\\\"
echo "\\midrule"

for ((i = 0; i < ${#xmlarray[@]}; ++i))
do
    printf "${names[$i]}  \t"
    echo "`./numbers.sh $path/${xmlarray[$i]}$full.xml '<schemas>' 'xpath-3.0-simplified.rnc.*yes' 'xpath-efo-basic.rnc.*yes' 'xpath-efo-extra.rnc.*yes' 'xpath-1.0-core-full.rnc.*yes' 'xpath-1.0-core-extra.rnc.*yes' 'xpath-2.0-core-join.rnc.*yes' 'xpath-2.0-core-extra.rnc.*yes' 'xpath-1.0-downward-last.rnc.*yes' 'xpath-1.0-vertical-full.rnc.*yes' 'xpath-1.0-forward-last.rnc.*yes' 'xpath-emso2-full.rnc.*yes' 'xpath-non-mixing-basic.rnc.*yes'`\\\\"
done
echo "\\bottomrule"

