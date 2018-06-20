all: dist.dat axis-count.tex

axis-count.tex: $(wildcard ../../benchmark/*-axis-step.xml)
	./axis-count.sh > axis-count.tex

dist.dat: $(wildcard ../../benchmark/*-full.xml)
	./distribution.sh > dist.dat
