all: SCOP.class SCOPXML.class Vertex.class XMLTest.class

%.class: %.java
	javac $<

install:
	cp *.class ${HOME}/classes/scop

clean:
	rm -f *.class
