// xml_reciever.cpp - DMI - 7-9-02

/* Usage: xml_reciever [-inspect] */

#include <scop.h>
#include <scopxml.h>

#include "address_book.h"

int main(int argc, char **argv)
{
	int sock;
	AddressBook *ab;
	vertex *v;
	
	sock = scop_open("localhost", "xml_receiver");
	v = scop_get_struct(sock);
	if(argc == 2 && !strcmp(argv[1], "-inspect"))
	{
		char *c = pretty_print(v);
		printf("%s\n", c);
		delete[] c;
	}
	ab = new AddressBook(v);
	delete v;
	ab->dump();
	delete ab;
	
	close(sock);
	return 0;
}
