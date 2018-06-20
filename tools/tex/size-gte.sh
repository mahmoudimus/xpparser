#!/bin/bash
num=`grep 'size="[1-9][1-9][1-9][1-9]*"' $@ | wc -l`
printf "%'.0f" $num
