all: dist.dat axis-count.tex size-gte-100.tex

size-gte-100.tex: $(wildcard ../../benchmark/*-full.xml)
	./size-gte.sh 100 ../../benchmark/*-full.xml > size-gte-100.tex

axis-count.tex: $(wildcard ../../benchmark/*-axis-step.xml)
	./axis-count.sh > axis-count.tex

dist.dat: $(wildcard ../../benchmark/*-full.xml)
	./distribution.sh > dist.dat

