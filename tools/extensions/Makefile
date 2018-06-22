all: extensions.xml gains-ext.tex

extensions.xml: extensions.sh $(wildcard ../../benchmark/*-full.xml)
	@bash extensions.sh > extensions.xml

gains-ext.tex: gains.sh $(wildcard ../../benchmark/*-full.xml)
	./gains.sh > gains-ext.tex

non-standard-xquery.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl  ../../benchmark/existdb-full.xml ../../benchmark/guidomatic-full.xml ../../benchmark/histei-full.xml ../../benchmark/marklogic-full.xml ../../benchmark/xqjson-full.xml ../../benchmark/xquerydoc-full.xml > non-standard-xquery.xml

non-standard-xslt.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl ../../benchmark/docbook-full.xml ../../benchmark/teixsl-full.xml ../../benchmark/htmlbook-full.xml > non-standard-xslt.xml

%.reasons: %.xml
	xmlstarlet sel -t -v '//xpath/schemas/validation[@valid="no" and contains(@schema, "1.0-core-extra")]/text()' -n $*.xml | sort | uniq -c > $*.reasons

non-standard-no-last-no-position-xslt.xml: non-standard-xslt.xml
	echo "<?xml version=\"1.0\"?>" > $@
	echo "<benchmark>" >> $@
	xmlstarlet sel -N xqx="http://www.w3.org/2005/XQueryX" -t -c "//xpath[schemas][not(ast//xqx:functionName[text()='last' or text()='position'])]" -n $< >> $@
	echo "</benchmark>" >> $@
