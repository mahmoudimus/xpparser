all: dist.dat axis-count.tex size-gte-100.tex matrices captured_dist.dat

BENCHMARKS=benchmarks-all.xml \
		   benchmarks-xslt-full.xml benchmarks-xslt.xml \
		   benchmarks-xquery-full.xml benchmarks-xquery.xml 
matrices: $(BENCHMARKS)
	ant
clean:
	rm -f dist.dat axis-count.tex size-gte-100.tex captured_dist.dat
	rm -f $(BENCHMARKS)
	rm -f matrix_*_*.tex totals_*_*.tex

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

%.xml: %-full.xml
	cat $< | sed -e 's/-full.xml/.xml/' > $@
benchmarks-xslt-full.xml: benchmarks-all-full.xml
	grep -v 'type="xquery"' $< > $@
benchmarks-xquery-full.xml: benchmarks-all-full.xml
	grep -v 'type="xslt"' $< > $@
