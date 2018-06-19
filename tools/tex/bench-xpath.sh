#!/bin/bash

# path to benchmark files
path=$1
full=$2
xmlarray=(docbook teixsl htmlbook existdb histei xquerydoc marklogic xqjson guidomatic)
names=(DocBook TEI HTMLBook eXist-db HisTEI xquerydoc MarkLogic XQJSON guid-o-matic)

echo "\\toprule"
echo "Source & queries & XPath~1.0 & XPath~2.0 & XPath~3.0\\\\"
echo "\\midrule"

for ((i = 0; i < ${#xmlarray[@]}; ++i))
do
    printf "${names[$i]} "
    echo "`./numbers.sh $path/${xmlarray[$i]}$2.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
done
echo "\\bottomrule"

