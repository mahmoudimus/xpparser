#!/bin/bash

coverage_full=`cat $1`
shift
coverage_orig=`cat $1`
shift

# which files to take into account
files="$@"

# which XPath fragments to take into account
fragments=( `grep 'file=' ../../relaxng/fragments-full.xml | sed 's/.*file=\"\([\.a-zA-Z0-9\-]*.rnc\).*/\1/g'` )
names=( `grep 'nametex=' ../../relaxng/fragments-full.xml | sed 's/.*nametex=\"\([^\"]*\)\"/\1/g'` )

# which variants of each fragment (full variant is implicit)
variants=( "extra" "full" "orig" )

# the coverage of XPath 3.0
total=`grep 'xpath-3.0.rnc.*yes' $files | wc -l`

max=0
maxf=0
min=$total
minf=0
# main loop on every fragment
for ((f=0; f<${#fragments[@]}; ++f))
do    
    fname=`echo ${names[f]} | sed 's/~/ /'`
    printf "\"\\\\\\\textsf{$fname}\"\t" # the fragment's name
    if [ ${#fname} -lt 7 ]
    then
        printf "\t"
    fi

    for ((v=0; v<${#variants[@]}; ++v))
    do
        name=$(echo ${fragments[f]} | sed s/[a-z]*.rnc/${variants[v]}.rnc/)
        if [ "${variants[v]}" = "full" ]
        then
            name=${fragments[f]}
        fi
        # reserve `extras' to the positive and core 1.0 fragments
        if [ "${variants[v]}" = "extra" ]
        then
            if [ "${names[f]}" = "Positive" ] #-o "${names[f]}" = "Core~1.0" ]
            then
                :
            else
                name="thisisadummy"
            fi
        fi
        count=`grep $name.*yes $files | wc -l`
        percent=`echo "scale=2; 100*$count/$total" | bc`
        printf "$percent\t"
        # update min/max
        if [ $v -eq 1 ]
        then
            if [ $min -gt $count ] && [ "${names[f]}" != "Downward" ] && [ "${names[f]}" != "Forward" ]
            then
                min=$count
                minf=$f
            fi
            if [ $max -lt $count ]
            then
                max=$count
                maxf=$f
            fi
        fi            
    done
    echo
done

minpercent=`echo "scale=2; 100*$min/$total" | bc`
maxpercent=`echo "scale=2; 100*$max/$total" | bc`
printf "$minpercent\\\\%% for \\\\textsf{" >&2
if [ "${names[minf]}" != "Core~1.0" ] && [ "${names[minf]}" != "Core~2.0" ]
then
    printf "${names[minf]}\\\\-XPath" >&2
else
    mname=`echo "${names[minf]}" | sed -e 's/~/\\\\-XPath~/'`
    printf "$mname" >&2
fi
printf "} and~" >&2
printf "$maxpercent\\\\%% for \\\\textsf{" >&2
if [ "${names[maxf]}" != "Core~1.0" ] && [ "${names[maxf]}" != "Core~2.0" ]
then
    printf "${names[maxf]}\\\\-XPath" >&2
else
    mname=`echo "${names[maxf]}" | sed -e 's/~/\\\\-XPath~/'`
    printf "$mname" >&2
fi
printf "}" >&2

# printf '"\\\\textit{Combined}"'
# printf "\t0\t"
# percent=`echo "scale=2; 100*$coverage_full/$total" | bc`
# printf "$percent" > ext-overall-cov.tex
# percent=`echo "scale=2; 100*$coverage_orig/$total" | bc`
# printf "$percent\n"
