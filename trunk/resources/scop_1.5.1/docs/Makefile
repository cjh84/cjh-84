all: scop_intro.ps scop_manual.ps scop_java.ps scop_python.ps scop_protocol.ps

pdf: scop_intro.pdf scop_manual.pdf scop_java.pdf scop_python.pdf scop_protocol.pdf

f: force

force:
	latex scop_intro.tex
	latex scop_manual.tex
	latex scop_java.tex
	latex scop_python.tex
	latex scop_protocol.tex
	dvips -t a4 -o scop_intro.ps scop_intro.dvi
	dvips -t a4 -o scop_manual.ps scop_manual.dvi
	dvips -t a4 -o scop_java.ps scop_java.dvi
	dvips -t a4 -o scop_python.ps scop_python.dvi
	dvips -t a4 -o scop_protocol.ps scop_protocol.dvi

%.ps: %.dvi
	dvips -t a4 -o $@ $<	

%.dvi: %.tex
	latex $<

%.pdf: %.tex
	pdflatex $<

clean:
	rm -f *.ps *.aux *.log *.dvi *.toc

reallyclean:
	rm -f *.ps *.aux *.log *.dvi *.toc *.pdf
