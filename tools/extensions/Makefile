all: extensions.xml gains-ext.tex

extensions.xml: extensions.sh $(wildcard ../../benchmark/*-full.xml)
	@bash extensions.sh > extensions.xml

gains-ext.tex: gains.sh $(wildcard ../../benchmark/*-full.xml)
	./gains.sh > gains-ext.tex

non-standard-xquery.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl  ../../benchmark/existdb-full.xml ../../benchmark/guidomatic-full.xml ../../benchmark/histei-full.xml ../../benchmark/marklogic-full.xml ../../benchmark/xqjson-full.xml ../../benchmark/xquerydoc-full.xml > non-standard-xquery.xml

non-standard-xslt.xml: nonstandard.pl ../../benchmark/*-full.xml
	./nonstandard.pl ../../benchmark/docbook-full.xml ../../benchmark/teixsl-full.xml ../../benchmark/htmlbook-full.xml > non-standard-xslt.xml

non-standard-xquery.reasons: non-standard-xquery.xml
	xmlstarlet sel -t -v '//xpath/schemas/validation[@valid="no" and contains(@schema, "1.0-core-extra")]/text()' -n non-standard-xquery.xml | sort | uniq -c > non-standard-xquery.reasons

non-standard-xslt.reasons: non-standard-xslt.xml
	xmlstarlet sel -t -v '//xpath/schemas/validation[@valid="no" and contains(@schema, "1.0-core-extra")]/text()' -n non-standard-xslt.xml | sort | uniq -c > non-standard-xslt.reasons
