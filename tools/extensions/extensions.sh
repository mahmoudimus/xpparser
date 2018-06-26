#!/bin/bash
echo '\begin{tabular}{cccccc}'   > occurrences-ext.tex
echo '\toprule'                 >> occurrences-ext.tex
echo '$/\pi$ & $\$x$ & $\pi\mathbin\trianglep d$ & $\pi\mathbin\triangle\pi$ & $\last()$ & $\texttt{id}()$\\'  >> occurrences-ext.tex
echo '\midrule'                 >> occurrences-ext.tex

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

echo '\bottomrule'              >> occurrences-ext.tex
echo '\end{tabular}'            >> occurrences-ext.tex
