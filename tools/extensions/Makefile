all: extensions.xml gains-ext.tex countfuns_dist.dat

countfuns_dist.dat: countfuns.pl $(wildcard ../../benchmark/*-full.xml)
	./countfuns.pl ../../benchmark/*-full.xml > /dev/null

extensions.xml: extensions.sh $(wildcard ../../benchmark/*-full.xml)
	@bash extensions.sh > extensions.xml

gains-ext.tex: gains.sh $(wildcard ../../benchmark/*-full.xml)
	./gains.sh > gains-ext.tex

non-standard-xquery.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl  `grep 'type="xquery"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > non-standard-xquery.xml

non-standard-xslt.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl `grep 'type="xslt"' ../tex/benchmarks-all-full.xml | sed -e 's/.*href="\([^"]*\).*/\1/'` > non-standard-xslt.xml

%.reasons: %.xml
	xmlstarlet sel -t -v '//xpath/schemas/validation[@valid="no" and contains(@schema, "1.0-core-extra")]/text()' -n $*.xml | sort | uniq -c > $*.reasons

non-standard-no-last-no-position-xslt.xml: non-standard-xslt.xml
	echo "<?xml version=\"1.0\"?>" > $@
	echo "<benchmark>" >> $@
	xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "//xpath[schemas][not(ast//xqx:functionName[text()='last' or text()='position'])]" -n $< >> $@
	echo "</benchmark>" >> $@
