all: dist.dat axis-count.tex size-gte-100.tex matrices xslt.dat xqy.dat

BENCHMARKS=benchmarks-all.xml \
		   benchmarks-xslt-full.xml benchmarks-xslt.xml \
		   benchmarks-xquery-full.xml benchmarks-xquery.xml 
matrices: $(BENCHMARKS)
	ant

# Remove generated files
clean: miniclean
	rm -f dist.dat axis-count.tex size-gte-100.tex captured_dist.dat
	rm -f $(BENCHMARKS)
	rm -f matrix_*_*.tex totals_*_*.tex

# Remove some generated files that are not useful as end products
miniclean:
	rm -f *gnuplottex-fig*.eps
	rm -f *gnuplottex-fig*.gnuplot
	rm -f *gnuplottex-fig*.tex
	rm -f *gnuplottex-fig*.pdf
	rm -f *.aux *.gnuploterrors *.log

size-gte-100.tex: $(wildcard ../../benchmark/*-full.xml)
	./size-gte.sh ../../benchmark/*-full.xml > size-gte-100.tex

axis-count.tex: $(wildcard ../../benchmark/*-axis-step.xml)
	./axis-count.sh > axis-count.tex

dist.dat: $(wildcard ../../benchmark/*-full.xml)
	./distribution.sh > dist.dat
captured_dist.dat: $(wildcard ../../benchmark/*-full.xml)
	./captured_distr.sh > captured_dist.dat
captured_dist.pdf: captured_dist.dat captured_dist.tex
	pdflatex --shell-escape captured_dist.tex

xslt-full.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh xslt "`./full-frags.sh ../../relaxng/fragments-full.xml`" > xslt-full.cov

xslt-orig.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh xslt "`./full-frags.sh ../../relaxng/fragments-orig.xml`" > xslt-orig.cov

xquery-full.cov: coverage.sh full-frags.xpath
	./coverage.sh xquery "`./full-frags.sh ../../relaxng/fragments-full.xml`" > xquery-full.cov

xquery-orig.cov: coverage.sh full-frags.sh ../../relaxng/fragments-full.xml
	./coverage.sh xquery "`./full-frags.sh ../../relaxng/fragments-orig.xml`" > xquery-orig.cov

xslt.dat: bench.sh xslt-full.cov xslt-orig.cov $(wildcard ../../benchmark/*-full.xml) benchmarks-all-full.xml
	./bench.sh  xslt-full.cov xslt-orig.cov `grep 'type="xslt"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > xslt.dat

xqy.dat: bench.sh xquery-full.cov xquery-orig.cov $(wildcard ../../benchmark/*-full.xml) benchmarks-all-full.xml
	./bench.sh xquery-full.cov xquery-orig.cov `grep 'type="xquery"' benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > xqy.dat

test.pdf: xslt.dat xqy.dat test.tex
	pdflatex --shell-escape test.tex

%.xml: %-full.xml
	cat $< | sed -e 's/-full.xml/.xml/' > $@
benchmarks-xslt-full.xml: benchmarks-all-full.xml
	grep -v 'type="xquery"' $< > $@
benchmarks-xquery-full.xml: benchmarks-all-full.xml
	grep -v 'type="xslt"' $< > $@
