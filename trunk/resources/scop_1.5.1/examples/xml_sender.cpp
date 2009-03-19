// xml_sender.cpp - DMI - 7-9-02

#include <scop.h>
#include <scopxml.h>

#include "address_book.h"

int main()
{
	int sock;
	AddressBook ab(3);
	vertex *v;
	
	ab.set_entry(0, "Poirot", "Belgium");
	ab.set_entry(1, "Morse", "Oxford, UK");
	ab.set_entry(2, "Danger Mouse", "London, UK");
	v = ab.marshall();
	
	sock = scop_open("localhost", "xml_sender");
	scop_send_struct(sock, "xml_receiver", v);
	delete v;
	
	close(sock);
	return 0;
}
