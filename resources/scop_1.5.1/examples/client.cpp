// client.cpp - DMI - 7-9-02

/* Usage: client [ <query> ]   (default query is "Hello world!") */

#include <scop.h>

int main(int argc, char **argv)
{
	int sock;
	char *query = argc > 1 ? argv[1] : (char *)"Hello world!";
	char *reply;
	
	sock = scop_open("localhost", "client");
	reply = scop_rpc(sock, "server", query);
	printf("Query <%s>, Reply <%s>\n", query, reply);
	delete[] reply;
	
	close(sock);
	return 0;
}
