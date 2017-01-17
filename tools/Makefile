all: $(shell ls ../benchmark/*xml | grep -v build.xml | sed -e 's/..\///') benchmarks.js
	@echo XXX
	@echo XXX Links to EXISTING benchmarks have been created.
	@echo XXX No benchmark has been generated: do it manually in ../benchmark if needed.
	@echo XXX
benchmark/%.xml:
	@mkdir -p benchmark
	ln -f ../$@ $@
.PHONY: benchmarks.js
benchmarks.js:
	@echo Generating benchmarks.js...
	@echo "var benchmarks = [];" > $@
	@cd benchmark ; for i in *xml ; do echo "benchmarks.push('$$i');" >> ../$@ ; done
	@echo "benchmarks = benchmarks.map(function(x){return(x.split('.')[0]);});" >> $@
clean:
	rm -rf benchmarks.js benchmark
