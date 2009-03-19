// method_server.cpp - DMI - 7-9-02

#include <scop.h>
#include <scopxml.h>

int invocations = 0;

double cent_to_faren(double c);
double faren_to_cent(double f);

int main()
{
	int sock;
	vertex *v, *w, *args;
	char *method;
	
	sock = scop_open("localhost", "method_server");
	while(1)
	{
		v = scop_get_request(sock);
		method = v->extract_method();
		args = v->extract_args();
		if(!strcmp(method, "ctof"))
			w = pack(cent_to_faren(args->extract_double()));
		else if(!strcmp(method, "ftoc"))
			w = pack(faren_to_cent(args->extract_double()));			
		else if(!strcmp(method, "stats"))
			w = pack(invocations);
		else
			exit(1);
		delete v;
		scop_send_reply(sock, w);
		delete w;
	}
	
	close(sock);
	return 0;
}

double cent_to_faren(double c)
{
	invocations++;
	return (9.0 * c / 5.0) + 32.0;
}

double faren_to_cent(double f)
{
	invocations++;
	return (f - 32.0) * 5.0 / 9.0;
}
