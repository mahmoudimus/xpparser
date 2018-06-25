#!/usr/bin/Rscript
# compute Pearson's chi square of the contingency table provided as arg1

args <- commandArgs(TRUE)
contingency = read.table(args[1])

library(MASS)

res <- chisq.test(contingency)

res

q(status=0)
