#!/bin/bash
echo '\begin{tabular}{lcccccc}'   > occurrences-ext.tex
echo '\toprule'                 >> occurrences-ext.tex
echo '& $/\pi$ & $\$x$ & $\pi\mathbin\triangle^+ d$ & $\pi\mathbin\triangle\pi$ & $\last()$ & $\texttt{id}()$\\'  >> occurrences-ext.tex
echo '\midrule'                 >> occurrences-ext.tex
printf 'occurrences &'          >> occurrences-ext.tex


echo '<?xml version="1.0"?>'
echo "<extensions>"
echo ""
echo "<!-- Number of queries using root -->"
bash root.sh
echo ""
echo "<!-- Number of queries using free variables -->"
bash freevar.sh
echo ""
echo "<!-- Number of queries with joins against a constant -->"
bash cst_join.sh
echo ""
echo "<!-- Number of queries with arbitrary joins -->"
bash join.sh
echo ""
echo "<!-- Number of queries using last -->"
bash last.sh
echo ""
echo "<!-- Number of queries using id -->"
bash id.sh
echo ""
echo "<!-- Number of queries with arbitrary use of variables -->"
bash var.sh
echo ""
echo "<!-- Number of queries with positive joins -->"
bash positive_join.sh
echo "</extensions>"


echo '\midrule'              >> occurrences-ext.tex
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
    
    printf "\\\\textsf{$fname} & " >> occurrences-ext.tex
    printf '\multicolumn{3}{c}{'   >> occurrences-ext.tex

    # basic
    ./diffs.sh $orig $basic ../../benchmark/*-full.xml >> occurrences-ext.tex
    printf '} & ' >> occurrences-ext.tex

    # join
    if [ -f ../../relaxng/$join ]
    then
        printf "+" >> occurrences-ext.tex
        ./diffs.sh $basic $join ../../benchmark/*-full.xml >> occurrences-ext.tex
    fi
    printf ' & ' >> occurrences-ext.tex

    # last
    if [ -f ../../relaxng/$last ]
    then
        printf "+" >> occurrences-ext.tex
        ./diffs.sh $basic $last ../../benchmark/*-full.xml >> occurrences-ext.tex
    fi
    printf ' & ' >> occurrences-ext.tex

    # id
    if [ -f ../../relaxng/$id ]
    then
        printf "+" >> occurrences-ext.tex
        ./diffs.sh $basic $id ../../benchmark/*-full.xml >> occurrences-ext.tex
    fi
    echo '\\' >> occurrences-ext.tex
done
echo '\bottomrule'              >> occurrences-ext.tex
echo '\end{tabular}'            >> occurrences-ext.tex
