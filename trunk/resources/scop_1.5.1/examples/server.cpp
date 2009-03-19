// server.cpp - DMI - 7-9-02

#include <scop.h>

int main()
{
	int sock;
	char *query, *reply;
	int len;
	
	sock = scop_open("localhost", "server");
	while(1)
	{
		query = scop_get_message(sock);
		len = strlen(query);
		reply = new char[len * 2 + 1];
		for(int i = 0; i < len; i++)
			reply[i * 2] = reply[i * 2 + 1] = query[i];
		reply[len * 2] = '\0';
		scop_send_reply(sock, reply);
		delete[] reply;
		delete[] query;
	}
	
	close(sock);
	return 0;
}
