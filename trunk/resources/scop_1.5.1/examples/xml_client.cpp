// xml_client.cpp - DMI - 7-9-02

/* Usage: xml_client [<n> <k>]    (default values n = 4, k = 2) */

#include <scop.h>
#include <scopxml.h>

int main(int argc, char **argv)
{
	int sock;
	int n, k;
	vertex *v, *w;

	if(argc != 3)
	{
		n = 4;
		k = 2;
	}
	else
	{
		n = atoi(argv[1]);
		k = atoi(argv[2]);
	}
		
	sock = scop_open("localhost", "xml_client");	
	v = pack(pack(n), pack(k));
	w = scop_rpc(sock, "xml_server", v);
	delete v;
	printf("%d choose %d equals %d.\n", n, k, w->extract_int());
	delete w;
		
	close(sock);
	return 0;
}
