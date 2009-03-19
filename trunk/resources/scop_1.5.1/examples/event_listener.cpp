// event_listener.cpp - DMI - 7-9-02

#include <scop.h>

int main()
{
	int sock;
	char *msg;
	
	sock = scop_open("localhost", "event_listener");
	scop_listen(sock, "news");
	while(1)
	{
		msg = scop_get_message(sock);
		printf("Received <%s>\n", msg);
		delete[] msg;
	}
	
	return 0;
}
