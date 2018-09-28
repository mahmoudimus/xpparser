#!/bin/bash

echo '\begin{tabular}{lrrrr}'
echo '\toprule'
#echo '& \multicolumn{3}{c}{basic} & \multicolumn{3}{c}{advanced}\\'
echo '& basic & $\pi\mathbin\triangle\pi$ & $\last()$ & $\texttt{id}()$\\'
echo '\midrule'
# which XPath fragments to take into account
fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )
names=( `grep 'nametex=' ../../relaxng/fragments-full.xml | sed 's/.*nametex=\"\([^\"]*\)\"/\1/g'` )

for ((f=0; f<${#fragments[@]}; ++f))
do
    fname=${names[f]}
    full=${fragments[f]}
    orig=$(echo $full | sed s/[a-z]*.rnc/orig.rnc/)
    basic=$(echo $full | sed s/[a-z]*.rnc/basic.rnc/)
    join=$(echo $full | sed s/[a-z]*.rnc/join.rnc/)
    last=$(echo $full | sed s/[a-z]*.rnc/last.rnc/)
    id=$(echo $full | sed s/[a-z]*.rnc/id.rnc/)
    
    printf "\\\\textsf{$fname} & "
    #printf '\multicolumn{3}{c}{'

    # basic
    ./diffs.sh $orig $basic `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
    #printf '} & '
    printf ' & '

    # join
    if [ -f ../../relaxng/$join ]
    then
        printf "+"
        ./diffs.sh $basic $join `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
    fi
    printf ' & '

    # last
    if [ -f ../../relaxng/$last ]
    then
        printf "+"
        ./diffs.sh $basic $last `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
    fi
    printf ' & '

    # id
    if [ -f ../../relaxng/$id ]
    then
        printf "+"
        ./diffs.sh $basic $id `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
    fi
    echo '\\'
done
echo '\bottomrule'
echo '\end{tabular}'
