all: scop scopserver listen xmltest stream bridge

SYSPREFIX = /usr/local
USERPREFIX = ${HOME}

SYSBINDIR = ${SYSPREFIX}/bin
USERBINDIR = ${USERPREFIX}/bin

scop: scop.o libscop.a
	g++ -Wall -o scop scop.o libscop.a ${SOLARIS_LIBS}

scop.o: scop.cpp scop.h scopxml.h
	g++ -Wall -c scop.cpp

stream: stream.o libscop.a
	g++ -Wall -o stream stream.o libscop.a ${SOLARIS_LIBS}

stream.o: stream.cpp scop.h
	g++ -Wall -c stream.cpp

listen: listen.o libscop.a
	g++ -Wall -o listen listen.o libscop.a ${SOLARIS_LIBS}

listen.o: listen.cpp scop.h scopxml.h
	g++ -Wall -c listen.cpp

bridge: bridge.o libscop.a
	g++ -Wall -o bridge bridge.o libscop.a ${SOLARIS_LIBS}

bridge.o: bridge.cpp scop.h scopxml.h
	g++ -Wall -c bridge.cpp

xmltest: xmltest.o libscop.a
	g++ -Wall -o xmltest xmltest.o libscop.a

xmltest.o: xmltest.cpp scopxml.h
	g++ -Wall -c xmltest.cpp

scopserver: scopserver.o datatype.o
	g++ -Wall -o scopserver scopserver.o datatype.o ${SOLARIS_LIBS}

scopserver.o: scopserver.cpp
	g++ -Wall -c scopserver.cpp

clean:
	rm -f *.o *.a *.so* scop scopserver listen xmltest stream bridge

# Installation:

install: install-libs install-headers install-binaries

install-headers:
	cp scop.h ${SYSPREFIX}/include
	cp scopxml.h ${SYSPREFIX}/include

install-libs:
	cp libscop.a ${SYSPREFIX}/lib
	cp scophosts /etc

install-binaries:
	cp scop ${SYSBINDIR}
	cp listen ${SYSBINDIR}
	cp scopserver ${SYSBINDIR}
	cp stream ${SYSBINDIR}
	cp bridge ${SYSBINDIR}

# Non-root installation:

userinstall: userinstall-libs userinstall-headers userinstall-binaries

userinstall-headers:
	cp scop.h ${USERPREFIX}/include
	cp scopxml.h ${USERPREFIX}/include

userinstall-libs:
	cp libscop.a ${USERPREFIX}/lib
	cp scophosts ${USERPREFIX}/.scophosts

userinstall-binaries:
	cp scop ${USERBINDIR}
	cp listen ${USERBINDIR}
	cp scopserver ${USERBINDIR}
	cp stream ${USERBINDIR}
	cp bridge ${USERBINDIR}

# Building libraries:

libscop.a: scoplib.o scopxml.o datatype.o
	ar rs libscop.a scoplib.o scopxml.o datatype.o

scoplib.o: scoplib.cpp scop.h
	g++ -Wall -fPIC -c scoplib.cpp

scopxml.o: scopxml.cpp scopxml.h
	g++ -Wall -fPIC -c scopxml.cpp

datatype.o: datatype.cpp datatype.h
	g++ -Wall -fPIC -c datatype.cpp
