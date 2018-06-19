#!/bin/bash

# which files to take into account
files="$@"

# which XPath fragments to take into account
fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )
names=( `grep 'nametex=' ../../relaxng/fragments-full.xml | sed 's/.*nametex=\"\([^\"]*\)\"/\1/g'` )

# which variants of each fragment (full variant is implicit)
variants=( "extra" "full" "orig" )

# the coverage of the simplified XPath 3.0 syntax provided in the paper
printf "\"\\\\\\\textsf{Full}\"\t`grep 'xpath-3.0-simplified.rnc.*yes' $files | wc -l`\t"
for ((v=0; v<${#variants[@]}; ++v))
do
    printf "0\t"
done
echo

# main loop on every fragment
for ((f=0; f<${#fragments[@]}; ++f))
do
    fname=`echo ${names[f]} | sed 's/~/ /'`
    printf "\"\\\\\\\textsf{$fname}\"\t" # the fragment's name
    if [ ${#fname} -lt 7 ]
    then
        printf "\t"
    fi
    printf "0\t"             # empty column against simplified XPath 3.0
    for ((v=0; v<${#variants[@]}; ++v))
    do
        name=$(echo ${fragments[f]} | sed s/[a-z]*.rnc/${variants[v]}.rnc/)
        if [ "${variants[v]}" = "full" ]
        then
            name=${fragments[f]}
        fi
        # reserve `extras' to the positive fragment
        if [ "${variants[v]}" = "extra" -a "${names[f]}" != "Positive" ]
        then
            name="thisisadummy"
        fi
        printf "`grep $name.*yes $files | wc -l`\t"
    done
    echo
done
