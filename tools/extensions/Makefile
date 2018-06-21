all: extensions.xml gains-ext.tex

extensions.xml: extensions.sh $(wildcard ../../benchmark/*-full.xml)
	@bash extensions.sh > extensions.xml

gains-ext.tex: gains.sh $(wildcard ../../benchmark/*-full.xml)
	./gains.sh > gains-ext.tex
