
all: chord.pdf


numqueries: ../tex/benchmarks-all-full.xml $(wildcard ../../benchmark/*-axis-step.xml) $(wildcard ../../benchmark/*-full.xml)
	grep '<ast' `grep 'href' $< | sed -e 's/.*href="\([^"]*\).*/\1/'` | wc -l > $@

site/matrix.json: ../../xslt/matrix.xsl build.xml index.html numqueries xpp.css render.js lang-xpath.js prettify.css $(wildcard ../../benchmark/*-full.xml)
	ant

chord.inner.svg: site/matrix.json phantom.js chord.css
	phantomjs phantom.js "file://$(PWD)/site/index.html" chord.inner.svg

chord.svg: chord.inner.svg chord.css
	echo '<?xml version="1.0" standalone="no"?>' > $@
	echo '<svg id="chord" width="650" height="650"><defs><style type="text/css"><![CDATA[' >> $@
	cat chord.css >> $@
	echo ']]></style></defs>' >> $@
	# Remove opening <svg>
	# + remove buggy NS[1..6] namespaces that show up on some systems...
	sed -e 's/<svg[^>]*>//' -e 's/NS.:/xlink:/g' chord.inner.svg >> $@

chord.pdf: chord.svg
	inkscape $< --export-pdf=$@

clean:
	rm -f chord.inner.svg chord.svg chord.pdf
