all: extensions.xml gains-ext.tex countfuns_all.dat countfuns_xslt.dat countfuns_xquery.dat non-standard-xquery.xml non-standard-xslt.xml non-standard-all.xml

countfuns_all.dat: countfuns.pl $(wildcard ../../benchmark/*-full.xml)
	grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | \
	  sed -e 's/.*href="\(.*\.xml\)".*/\1/' | \
	  XPPSUF="all" xargs ./countfuns.pl

countfuns_xslt.dat: countfuns.pl $(wildcard ../../benchmark/*-full.xml)
	grep 'type="xslt"' ../tex/benchmarks-all-full.xml | \
	  sed -e 's/.*href="\(.*\.xml\)".*/\1/' | \
	  XPPSUF="xslt" xargs ./countfuns.pl

countfuns_xquery.dat: countfuns.pl $(wildcard ../../benchmark/*-full.xml)
	grep 'type="xquery"' ../tex/benchmarks-all-full.xml | \
	  sed -e 's/.*href="\(.*\.xml\)".*/\1/' | \
	  XPPSUF="xquery" xargs ./countfuns.pl

extensions.xml: extensions.sh $(wildcard ../../benchmark/*-full.xml)
	@bash extensions.sh > extensions.xml

gains-ext.tex: gains.sh $(wildcard ../../benchmark/*-full.xml)
	./gains.sh > gains-ext.tex

non-standard-xquery.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl  xquery `grep 'type="xquery"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

non-standard-xslt.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl xslt `grep 'type="xslt"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

non-standard-all.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl all `grep 'type="\(xslt\|xquery\)"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > $@

%.reasons: %.xml
	xmlstarlet sel -t -v '//xpath/schemas/validation[@valid="no" and contains(@schema, "efo-extra")]/text()' -n $*.xml | sort | uniq -c > $*.reasons

non-standard-no-last-no-position-xslt.xml: non-standard-xslt.xml
	echo "<?xml version=\"1.0\"?>" > $@
	echo "<benchmark>" >> $@
	xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "//xpath[schemas][not(ast//xqx:functionName[text()='last' or text()='position'])]" -n $< >> $@
	echo "</benchmark>" >> $@
