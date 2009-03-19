// receiver.cpp - DMI - 7-9-02

#include <scop.h>

int main()
{
	int sock;
	char *msg;
	
	sock = scop_open("localhost", "receiver");
	while(1)
	{
		msg = scop_get_message(sock);
		printf("Received <%s>\n", msg);
		if(!strcmp(msg, "quit")) break;
		delete[] msg;
	}
	
	close(sock);
	return 0;
}
