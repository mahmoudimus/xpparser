all: dist.dat dist-xquery.dat dist-xslt.dat \
  axis-count.tex size-gte-100.tex matrices xslt.dat xqy.dat xslt-std.dat xqy-std.dat coverage-size.tex coverage-axis-step.tex numqueries.tex MAXcount.tex composition.tex composition_full.tex all-full-cov-pc.tex all-full-cov.tex occurences_axis.tex occurences_operators.tex occurences_grammar.tex

SHELL := /bin/bash

BENCHMARKS=benchmarks-all.xml \
		   benchmarks-xslt-full.xml benchmarks-xslt.xml \
		   benchmarks-xquery-full.xml benchmarks-xquery.xml 
matrices: $(BENCHMARKS) $(wildcard ../../benchmark/*-full.xml) $(wildcard ../../relaxng/fragments-*.xml)
	rm -f matrix_*_*.tex
	ant
	touch $@

# Remove generated files
clean: miniclean
	rm -f dist.dat axis-count.tex size-gte-100.tex captured_dist.dat
	rm -f dist-xquery.dat dist-xslt.dat
	rm -f $(BENCHMARKS)
	rm -f matrix_*_*.tex totals_*_*.tex matrices

# Remove some generated files that are not useful as end products
miniclean:
	rm -f *gnuplottex-fig*.eps
	rm -f *gnuplottex-fig*.gnuplot
	rm -f *gnuplottex-fig*.tex
	rm -f *gnuplottex-fig*.pdf
	rm -f *.aux *.gnuploterrors *.log

numqueries: $(wildcard ../../benchmark/*-full.xml)
	grep '<ast' `grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` | wc -l > $@

numqueries.tex: numqueries
	printf "%'.0f" `cat $<` > $@

MAXcount: $(wildcard ../../benchmark/*-full.xml)
	grep 'MAX' `grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` | wc -l > $@

MAXcount.tex: MAXcount
	printf "%'.0f" `cat $<` > $@


size-gte-100.tex: $(wildcard ../../benchmark/*-full.xml)
	./size-gte.sh `grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

axis-count.tex: $(wildcard ../../benchmark/*-axis-step.xml)
	./axis-count.sh > $@

dist.dat: numqueries distribution.sh $(wildcard ../../benchmark/*-full.xml)
	./distribution.sh 1 > $@
dist-xquery.dat: numqueries distribution.sh $(wildcard ../../benchmark/*-full.xml)
	FRAGTYPE=xquery ./distribution.sh 1 > $@
dist-xslt.dat: numqueries distribution.sh $(wildcard ../../benchmark/*-full.xml)
	FRAGTYPE=xslt ./distribution.sh 1 > $@
captured_dist.dat: $(wildcard ../../benchmark/*-full.xml)
	./captured_distr.sh > captured_dist.dat
captured_dist.pdf: captured_dist.dat captured_dist.tex
	pdflatex --shell-escape captured_dist.tex

all-full.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh "href" "`./full-frags.sh ../../relaxng/fragments-full.xml`" > $@

all-full-cov.tex: all-full.cov
	printf "%'.0f" `cat $<` > $@

all-full-cov-pc.tex: all-full.cov numqueries
	echo "scale=2; 100*`cat all-full.cov`/`cat numqueries`" | bc > $@

xslt-full.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh 'type="xslt"' "`./full-frags.sh ../../relaxng/fragments-full.xml`" > xslt-full.cov

xslt-orig.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh 'type="xslt"' "`./full-frags.sh ../../relaxng/fragments-orig.xml`" > xslt-orig.cov

xquery-full.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh 'type="xquery"' "`./full-frags.sh ../../relaxng/fragments-full.xml`" > xquery-full.cov

xquery-orig.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh 'type="xquery"' "`./full-frags.sh ../../relaxng/fragments-orig.xml`" > xquery-orig.cov

bnch-full.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh 'type="bench"' "`./full-frags.sh ../../relaxng/fragments-full.xml`" > bnch-full.cov

bnch-orig.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh 'type="bench"' "`./full-frags.sh ../../relaxng/fragments-orig.xml`" > bnch-orig.cov

xslt.dat: bench.sh xslt-full.cov xslt-orig.cov $(wildcard ../../benchmark/*-full.xml) benchmarks-all-full.xml
	./bench.sh  xslt-full.cov xslt-orig.cov `grep 'type="xslt"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > xslt.dat 2> minmax-xslt.tex

xqy.dat: bench.sh xquery-full.cov xquery-orig.cov $(wildcard ../../benchmark/*-full.xml) benchmarks-all-full.xml
	./bench.sh xquery-full.cov xquery-orig.cov `grep 'type="xquery"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > xqy.dat 2> minmax-xqy.tex

bnch.dat: bench.sh bnch-full.cov bnch-orig.cov $(wildcard ../../benchmark/*-full.xml) benchmarks-all-full.xml
	./bench.sh bnch-full.cov bnch-orig.cov `grep 'type="bench"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > bnch.dat 2> minmax-bnch.tex

xslt-std.dat: bench-std.sh xslt-full.cov xslt-orig.cov $(wildcard ../../benchmark/*-full.xml) benchmarks-all-full.xml
	./bench-std.sh  xslt-full.cov xslt-orig.cov `grep 'type="xslt"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@ 2> minmax-std-xslt.tex

xqy-std.dat: bench-std.sh xquery-full.cov xquery-orig.cov $(wildcard ../../benchmark/*-full.xml) benchmarks-all-full.xml
	./bench-std.sh xquery-full.cov xquery-orig.cov `grep 'type="xquery"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@ 2> minmax-std-xqy.tex

test.pdf: xslt.dat xqy.dat bnch.dat test.tex
	pdflatex --shell-escape test.tex

%.xml: %-full.xml
	cat $< | sed -e 's/-full.xml/.xml/' > $@
benchmarks-xslt-full.xml: benchmarks-all-full.xml
	grep -v 'type="xquery"' $< > $@
benchmarks-xquery-full.xml: benchmarks-all-full.xml
	grep -v 'type="xslt"' $< > $@
benchmarks-bench-full.xml: benchmarks-all-full.xml
	grep -v 'type="bench"' $< > $@

contingency-size.dat: contingency-size.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./contingency-size.sh `grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

chisquare-size: contingency-size.dat chisquare.R
	./chisquare.R contingency-size.dat

contingency-axis-step.dat: contingency-axis-step.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./contingency-axis-step.sh `grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

chisquare-axis-step: contingency-axis-step.dat chisquare.R
	./chisquare.R contingency-axis-step.dat

coverage-size.tex: coverage-size.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./$<  `grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

coverage-axis-step.tex: coverage-axis-step.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./$<  `grep 'type="\(xslt\|xquery\)"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

composition.tex: composition.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./$<  > $@

composition_full.tex: composition_full.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./$<  > $@

occurences_axis.tex: occurences_axis.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./$<  > $@

occurences_operators.tex: occurences_operators.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./$<  > $@
occurences_grammar.tex: occurences_grammar.sh benchmarks-all-full.xml $(wildcard ../../benchmark/*-full.xml)
	./$<  > $@
