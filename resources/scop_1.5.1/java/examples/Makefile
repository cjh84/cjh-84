all: Sender.class Receiver.class Client.class Server.class RTTClient.class \
EventSource.class EventListener.class Status.class MultiListener.class \
SOS.class AddressBook.class XMLSender.class XMLReceiver.class \
XMLClient.class XMLServer.class MethodClient.class MethodServer.class

%.class: %.java
	javac -classpath .:.. $<

clean:
	rm -f *.class
