latex:
	latexmk -pdf -pdflatex="pdflatex --shell-escape" -synctex=1 diagram.tex

clean:
	latexmk -c diagram.tex

diff:
	@bash diff-full.sh > diff-full.txt
	@cp diff-full.txt diff-extra.txt
	@bash diff-extra.sh >> diff-extra.txt
