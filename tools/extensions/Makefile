all: extensions.xml

extensions.xml: extensions.sh $(wildcard ../../benchmark/*-full.xml)
	@bash extensions.sh > extensions.xml
