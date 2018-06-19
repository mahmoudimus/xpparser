#!/bin/bash

# path to benchmark files
path=$1
full=$2
xmlarray=(docbook teixsl htmlbook existdb histei xquerydoc marklogic xqjson guidomatic)
names=(DocBook TEI HTMLBook eXist-db HisTEI xquerydoc MarkLogic XQJSON guid-o-matic)
#xmlarray=(docbook htmlbook existdb histei marklogic xqjson)
#names=(DocBook HTMLBook eXist-db HisTEI MarkLogic XQJSON)

echo "\\toprule"
echo "Source & queries & EFO & EFO-Xtra & \\textsf{Core~1.0} & Core1-Xtra\\textsf{Core~2.0} & \\textsf{Downward} & \\textsf{Vertical} & \\textsf{Forward} & \\textsf{EMSO\$^\\textsf 2\$} & \\textsf{NonMixing}\\\\"
echo "\\midrule"

for ((i = 0; i < ${#xmlarray[@]}; ++i))
do
    printf "${names[$i]} "
    echo "`./numbers.sh $path/${xmlarray[$i]}$2.xml '<schemas>' 'xpath-1.0-efo-extra.rnc' 'xpath-1.0-efo-join.rnc.*yes' 'xpath-1.0-core-full.rnc.*yes' 'xpath-1.0-core-extra.rnc.*yes' 'xpath-2.0-core-join.rnc.*yes' 'xpath-1.0-downward-last.rnc.*yes' 'xpath-1.0-vertical-full.rnc.*yes' 'xpath-1.0-forward-last.rnc.*yes' 'xpath-emso2-full.rnc.*yes' 'xpath-non-mixing-basic.rnc.*yes'`\\\\"
done
echo "\\bottomrule"

