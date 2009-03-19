all: scop scopserver listen xmltest

COMP=gcc -g

scop: scop.o libscop.a
	$(COMP) -o scop scop.o libscop.a

scop.o: scop.c scop.h scopxml.h
	$(COMP) -c scop.c

listen: listen.o libscop.a
	$(COMP) -o listen listen.o libscop.a

listen.o: listen.c scop.h scopxml.h
	$(COMP) -c listen.c

xmltest: xmltest.o libscop.a
	$(COMP) -o xmltest xmltest.o libscop.a

xmltest.o: xmltest.c scopxml.h
	$(COMP) -c xmltest.c

scopserver: scopserver.o datatype.o
	$(COMP) -o scopserver scopserver.o datatype.o

scopserver.o: scopserver.c
	$(COMP) -c scopserver.c

clean:
	rm -f *.o *.a *.so* scop scopserver listen xmltest

# Installation:

install: install-libs install-headers install-binaries

install-headers:
	cp scop.h /usr/local/include
	cp scopxml.h /usr/local/include

install-libs:
	cp libscop.a /usr/local/lib
	cp scophosts /etc

install-binaries:
	cp scop /usr/local/bin
	cp listen /usr/local/bin
	cp scopserver /usr/local/sbin

# Building libraries:

libscop.a: scoplib.o scopxml.o datatype.o
	ar rs libscop.a scoplib.o scopxml.o datatype.o

scoplib.o: scoplib.c scop.h
	$(COMP) -fPIC -c scoplib.c

scopxml.o: scopxml.c scopxml.h
	$(COMP) -fPIC -c scopxml.c

datatype.o: datatype.c datatype.h
	$(COMP) -fPIC -c datatype.c
