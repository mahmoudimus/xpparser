#!/bin/bash

xslt=`grep 'xslt' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`
xquery=`grep 'xquery' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'`

# XSLT queries (total)
total_xslt=0
for file in $xslt
do
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath)" $file`
  total_xslt=$(($total_xslt+$count))
done

# Xquery queries (total)
total_xquery=0
for file in $xquery
do
  count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath)" $file`
  total_xquery=$(($total_xquery+$count))
done

# Total queries
total=$(($total_xslt+$total_xquery))

printf '\\begin{tabular}{lr@{\\hspace{1pt}}rr@{\\hspace{1pt}}rr@{\\hspace{1pt}}r}\n'
printf '\\toprule\n'
printf 'Grammar construct & \\multicolumn{2}{c}{XSLT} & \\multicolumn{2}{c}{XQuery} & \\multicolumn{2}{c}{Total} \\\\\n'
printf '\\midrule\n'

list=(xpathAxis rootExpr stepExpr predicates unionOp functionCallExpr varRef letExpr forExpr orOp isOp data datap)
legend=('$\\alpha\\dd$' '$/\\pi$' '$\\pi/\\pi$' '$\\pi[\\varphi]$' '$\\pi\\mathrel\\texttt{union}\\pi$' '$f(\\pi_1,\\dots,\\pi_n)$' '$\\mathtt{\\$x}$' '$\\texttt{let}\\:\\mathtt{\\$x}:=\\pi\\mathbin\\texttt{return}\\pi$' '$\\texttt{for}\\:\\mathtt{\\$x}\\mathrel\\texttt{in}\\pi\\mathrel\\texttt{return}\\pi$' '$\\varphi\\mathrel\\texttt{or}\\varphi,~\\varphi\\mathrel\\texttt{and}\\varphi$' '$\\pi\\mathrel\\texttt{is}\\pi$' '$\\pi\\mathbin\\triangle\\pi$' '$\\pi\\mathbin{\\trianglep}d$')

n=`echo ${#list[@]}`
for (( i=0; i<$n; i++))
do
  name=`echo ${list[$i]}`
  printf ${legend[$i]}
  printf ' & '
  # XSLT files
  count_xslt=0
  for file in $xslt
  do
    if [ $name = "rootExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='functionName' and .='root']] | /benchmark/xpath[ast/descendant::*[local-name()='rootExpr']])" $file`
    elif [ $name = "stepExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:stepExpr/following-sibling::*[1 and self::xqx:stepExpr]])" $file`
    elif [ $name = "varRef" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='varRef' and not(*[local-name()='name'] = ancestor::*[local-name()='flworExpr']/*/*/*/*[local-name()='varName'])]])" $file`
    elif [ $name = "forExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast[.//xqx:forExpr or .//xqx:quantifiedExpr]])" $file`
    elif [ $name = "orOp" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast[.//xqx:orOp or .//xqx:andOp]])" $file`
    elif [ $name = "data" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and child::*[local-name()='firstOperand']/child::*[local-name()='pathExpr' or local-name()='contextItemExpr'] and child::*[local-name()='secondOperand']/child::*[local-name()='pathExpr' or local-name()='contextItemExpr']] or ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and not(child::*[local-name()='firstOperand' or local-name()='secondOperand']/child::*[local-name()='integerConstantExpr' or local-name()='decimalConstantExpr' or local-name()='doubleConstantExpr' or local-name()='stringConstantExpr'])]])" $file`
    elif [ $name = "datap" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp' or local-name()='ltOp' or local-name()='leOp' or local-name()='gtOp' or local-name()='geOp' or local-name()='lessThanOp' or local-name='lessThanOrEqualOp' or local-name()='greaterThanOp' or local-name()='greaterThanOrEqualOp') and child::*[local-name()='firstOperand' or local-name()='secondOperand']/child::*[local-name()='integerConstantExpr' or local-name()='decimalConstantExpr' or local-name()='doubleConstantExpr' or local-name()='stringConstantExpr']]])" $file`
    else
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:$name])" $file`
    fi
    count_xslt=$(($count_xslt+$count))
  done
  percent=`echo "scale=1; 100*$count_xslt/$total_xslt" | bc`
  printf "%'.0f & (%.1f\\%%)" $count_xslt $percent
  printf ' & '
  # XQuery files
  count_xquery=0
  for file in $xquery
  do
    if [ $name = "rootExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='functionName' and .='root']] | /benchmark/xpath[ast/descendant::*[local-name()='rootExpr']])" $file`
    elif [ $name = "stepExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:stepExpr/following-sibling::*[1 and self::xqx:stepExpr]])" $file`
    elif [ $name = "varRef" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[local-name()='varRef' and not(*[local-name()='name'] = ancestor::*[local-name()='flworExpr']/*/*/*/*[local-name()='varName'])]])" $file`
    elif [ $name = "forExpr" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast[.//xqx:forExpr or .//xqx:quantifiedExpr]])" $file`
    elif [ $name = "orOp" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast[.//xqx:orOp or .//xqx:andOp]])" $file`
    elif [ $name = "data" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and child::*[local-name()='firstOperand']/child::*[local-name()='pathExpr' or local-name()='contextItemExpr'] and child::*[local-name()='secondOperand']/child::*[local-name()='pathExpr' or local-name()='contextItemExpr']] or ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp') and not(child::*[local-name()='firstOperand' or local-name()='secondOperand']/child::*[local-name()='integerConstantExpr' or local-name()='decimalConstantExpr' or local-name()='doubleConstantExpr' or local-name()='stringConstantExpr'])]])" $file`
    elif [ $name = "datap" ]; then
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast/descendant::*[(local-name()='eqOp' or local-name()='equalOp' or local-name()='neOp' or local-name()='notEqualOp' or local-name()='ltOp' or local-name()='leOp' or local-name()='gtOp' or local-name()='geOp' or local-name()='lessThanOp' or local-name='lessThanOrEqualOp' or local-name()='greaterThanOp' or local-name()='greaterThanOrEqualOp') and child::*[local-name()='firstOperand' or local-name()='secondOperand']/child::*[local-name()='integerConstantExpr' or local-name()='decimalConstantExpr' or local-name()='doubleConstantExpr' or local-name()='stringConstantExpr']]])" $file`
    else
      count=`xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "count(/benchmark/xpath[ast//xqx:$name])" $file`
    fi
    count_xquery=$(($count_xquery+$count))
  done
  percent=`echo "scale=1; 100*$count_xquery/$total_xquery" | bc`
  printf "%'.0f & (%.1f\\%%)" $count_xquery $percent
  printf ' & '
  count_total=$(($count_xslt+$count_xquery))
  percent=`echo "scale=1; 100*$count_total/$total" | bc`
  printf "%'.0f & (%.1f\\%%)" $count_total $percent
  printf ' \\\\\n'
done
printf '\\bottomrule\n'
printf '\\end{tabular}\n'
