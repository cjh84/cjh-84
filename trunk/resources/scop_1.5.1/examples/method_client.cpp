// method_client.cpp - DMI - 7-9-02

#include <scop.h>
#include <scopxml.h>

double cent_to_faren(int sock, double c);
double faren_to_cent(int sock, double f);
int count_uses(int sock);

int main()
{
	int sock;

	sock = scop_open("localhost", "method_client");
	
	printf("%g deg C = %g deg F.\n", 0.0, cent_to_faren(sock, 0.0));
	printf("%g deg C = %g deg F.\n", 20.0, cent_to_faren(sock, 20.0));
	printf("%g deg F = %g deg C.\n", 60.0, faren_to_cent(sock, 60.0));
	printf("The server has been accessed %d times.\n", count_uses(sock));
		
	close(sock);
	return 0;
}

double cent_to_faren(int sock, double c)
{
	vertex *v, *w;
	double f;
	
	v = pack(c);
	w = scop_rpc(sock, "method_server", v, "ctof");
	delete v;
	f = w->extract_double();
	delete w;
	return f;
}

double faren_to_cent(int sock, double f)
{
	vertex *v, *w;
	double c;
	
	v = pack(f);
	w = scop_rpc(sock, "method_server", v, "ftoc");
	delete v;
	c = w->extract_double();
	delete w;
	return c;
}

int count_uses(int sock)
{
	vertex *v, *w;
	int n;
	
	v = pack(0); // Dummy argument
	w = scop_rpc(sock, "method_server", v, "stats");
	n = w->extract_int();
	delete v;
	delete w;
	return n;
}
