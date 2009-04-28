TARGET=dissertation

all: pdf

ps: dissertation.ps

pdf: dissertation.ps
	ps2pdf $<

dissertation.ps: dissertation.dvi
	dvips -t a4 -o dissertation.ps dissertation.dvi

#%.ps: %.dvi
#	dvips -t a4 -o $@ $<

dissertation.dvi: dissertation.tex
	latex dissertation.tex

#%.dvi: %.tex
#	latex $<

bib:
	latex $(TARGET).tex
	bibtex $(TARGET)
	latex $(TARGET).tex
	latex $(TARGET).tex
	dvips -t a4 -o $(TARGET).ps $(TARGET).dvi

f: force

force:
	latex $(TARGET).tex
	dvips -t a4 -o $(TARGET).ps $(TARGET).dvi
	ps2pdf $(TARGET).ps

clean:
	rm -f *.dvi *.aux *.log *.toc *.bbl *.blg *.lof
