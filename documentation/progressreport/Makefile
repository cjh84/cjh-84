# This is the makefile for the progress report

PROGREP = progressreport.tex progrepbody.tex

help:
	@echo
	@echo "USAGE:"
	@echo
	@echo "make          display help information"
	@echo "make progrep  make the progress report and view it using evince"
	@echo "make pdf      make the PDF progress report"
	@echo "make count    display an estimated word count"
	@echo "make clean    remove all remakeable files"
	@echo

progrep:	progressreport.dvi
	evince progressreport.dvi

makefile.txt:	Makefile
	expand Makefile >makefile.txt
	
progressreport.dvi: $(PROGREP)
	latex progressreport

ps: $(PROGREP)
	latex progressreport
	dvips -Ppdf -G0 -t a4 -pp 0-200 -o ProgressReport.ps progressreport.dvi	

pdf:	ps
	ps2pdf ProgressReport.ps
	evince ProgressReport.pdf
		
count:
	detex progressreport.tex | tr -cd '0-9A-Za-z \n' | wc -w

clean:
	rm -f diss.ps *.dvi *.aux *.log *.err
	rm -f core *~ *.lof *.toc *.blg *.bbl
	rm -f makefile.txt
