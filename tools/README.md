# Benchmark statistics


## Counting queries using extensions

For each extension (constants, joins, last(), etc.) we provide a script
that counts the number of queries using the extension in our benchmarks.

All scripts are in `extensions/*.sh` and the master script 
`extensions/extensions.sh` includes all results.
All scripts are based on `../benchmark/*-full.xml` files.


## Totals and matrices

Subdirectory `tex` contains utilities for computing the size distribution
of queries as well as totals and difference matrices for various sets
of benchmarks and fragments.

Typical commands to run in `tools/tex`:
```
# Generate difference matrix and array of totals
# for XQuery benchmarks and the core-1.0 fragments.

ant -Dbenchmarks=xquery-full -Dfragments=core-1.0 matrix totals

# Generate difference matrix for all benchmarks and
# basic fragments, only taking into account non-trivial
# queries.

ant -Dbenchmarks=all -Dfragments=basic matrix
```
