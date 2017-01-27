all: $(shell ls ../benchmark/*xml | grep -v build.xml | sed -e 's/..\///') benchmarks.js
	@echo XXX
	@echo XXX Stripped copies of EXISTING benchmarks have been created.
	@echo XXX No benchmark has been generated: do it manually in ../benchmark if needed.
	@echo XXX

benchmark/%.xml: ../benchmark/%.xml
	@mkdir -p benchmark
	cat $< | perl -ne 's/&#0;//g; if (/<ast>/) { $$p = 1; } else { if (/<\/ast>/) { $$p = 0; } else { print unless $$p; } }' | \
	xsltproc strip_ast.xsl - > $@

.PHONY: benchmarks.js
benchmarks.js:
	@echo Generating benchmarks.js...
	@echo "var benchmarks = [];" > $@
	@cd benchmark ; for i in *xml ; do echo "benchmarks.push('$$i');" >> ../$@ ; done
	@echo "benchmarks = benchmarks.map(function(x){return(x.split('.')[0]);});" >> $@
clean:
	rm -rf benchmarks.js benchmark
