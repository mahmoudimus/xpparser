#!/bin/bash

# path to benchmark files
path=$1
full=$2
xmlarray=(docbook htmlbook existdb histei marklogic xqjson)
names=(DocBook HTMLBook eXist-db HisTEI MarkLogic XQJSON)

echo "\\toprule"
echo "Source & \\textsf{Core~1.0} & \\textsf{Core~2.0} & \\textsf{Downward} & \\textsf{Vertical} & \\textsf{Forward} & \\textsf{EMSO\$^\\textsf 2\$} & \\textsf{NonMixing}\\\\"
echo "\\midrule"

for ((i = 0; i < ${#xmlarray[@]}; ++i))
do
    printf "${names[$i]} "
    echo "`./numbers.sh $path/${xmlarray[$i]}$2.xml 'xpath-1.0-core-orig.rnc.*yes' 'xpath-2.0-core-orig.rnc.*yes' 'xpath-1.0-downward-orig.rnc.*yes' 'xpath-1.0-vertical-orig.rnc.*yes' 'xpath-1.0-forward-orig.rnc.*yes' 'xpath-emso2-orig.rnc.*yes' 'xpath-non-mixing-orig.rnc.*yes'`\\\\"
done
echo "\\bottomrule"

