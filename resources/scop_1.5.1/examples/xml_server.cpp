// xml_server.cpp - DMI - 7-9-02

#include <scop.h>
#include <scopxml.h>

int combi(int n, int k);

int main()
{
	int sock;
	vertex *v, *w;
	
	sock = scop_open("localhost", "xml_server");
	while(1)
	{
		v = scop_get_request(sock);
		w = pack(combi(v->extract_int(0), v->extract_int(1)));
		delete v;
		scop_send_reply(sock, w);
		delete w;
	}
	
	close(sock);
	return 0;
}

int combi(int n, int k)
{
	int result = 1;
	if(k > n || k < 0)
		return 0;
	
	for(int i = 0; i < k; i++)
		result *= n - i;
	
	for(int i = 1; i <= k; i++)
		result /= i;
	
	return result;
}
