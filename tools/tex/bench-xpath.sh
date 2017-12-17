#!/bin/bash

# path to benchmark files
path=$1
echo "\\toprule"
echo "Source & queries & XPath~1.0 & XPath~2.0 & XPath~3.0\\\\"
echo "\\midrule"
echo "DocBook `./numbers.sh $path/docbook.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
echo "HTMLBook `./numbers.sh $path/htmlbook.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
echo "\\midrule"
echo "eXist-db `./numbers.sh $path/existdb.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
echo "HisTEI `./numbers.sh $path/histei.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
echo "MarkLogic `./numbers.sh $path/marklogic.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
echo "XQJSON `./numbers.sh $path/xqjson.xml '<schemas>' 'xpath-1.0.rnc.*yes' 'xpath-2.0.rnc.*yes' 'xpath-3.0.rnc.*yes'`\\\\"
echo "\\bottomrule"
