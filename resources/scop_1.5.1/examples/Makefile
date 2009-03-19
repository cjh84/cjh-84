all: sender receiver client server xml_sender xml_receiver \
event_source event_listener multi_listener xml_client xml_server \
method_client method_server sos rtt_client multiplex_listener

# Defines to use if SCOP is installed as root:

# SCOP_INCLUDE=
# SCOP_LIBRARY=-lscop

# Defines to use in non-root installations:

# SCOP_INCLUDE=-I ${HOME}/include
# SCOP_LIBRARY=-L ${HOME}/lib -lscop

# Defines to use if you have built but not installed SCOP:

SCOP_INCLUDE=-I ../
SCOP_LIBRARY=-L ../ -lscop

sender: sender.o
	g++ -o sender sender.o ${SCOP_LIBRARY}

receiver: receiver.o
	g++ -o receiver receiver.o ${SCOP_LIBRARY}

event_source: event_source.o
	g++ -o event_source event_source.o ${SCOP_LIBRARY}

event_listener: event_listener.o
	g++ -o event_listener event_listener.o ${SCOP_LIBRARY}

multi_listener: multi_listener.o
	g++ -o multi_listener multi_listener.o ${SCOP_LIBRARY}

client: client.o
	g++ -o client client.o ${SCOP_LIBRARY}

rtt_client: rtt_client.o
	g++ -o rtt_client rtt_client.o ${SCOP_LIBRARY}

server: server.o
	g++ -o server server.o ${SCOP_LIBRARY}

xml_sender: xml_sender.o address_book.o
	g++ -o xml_sender xml_sender.o address_book.o ${SCOP_LIBRARY}

xml_receiver: xml_receiver.o address_book.o
	g++ -o xml_receiver xml_receiver.o address_book.o ${SCOP_LIBRARY}

xml_client: xml_client.o
	g++ -o xml_client xml_client.o ${SCOP_LIBRARY}

xml_server: xml_server.o
	g++ -o xml_server xml_server.o ${SCOP_LIBRARY}

method_client: method_client.o
	g++ -o method_client method_client.o ${SCOP_LIBRARY}

method_server: method_server.o
	g++ -o method_server method_server.o ${SCOP_LIBRARY}

sos: sos.o
	g++ -o sos sos.o ${SCOP_LIBRARY}

multiplex_listener: multiplex_listener.o multiplex.o
	g++ -o multiplex_listener multiplex_listener.o multiplex.o ${SCOP_LIBRARY}

%.o: %.cpp
	g++ ${SCOP_INCLUDE} -c $<

clean:
	rm -f *.o sender receiver client server xml_sender xml_receiver \
	event_source event_listener multi_listener xml_client xml_server \
	method_client method_server sos rtt_client multiplex_listener
