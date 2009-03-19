// sender.cpp - DMI - 7-9-02

/* Usage: sender [ <message> ]   (default message is "Hello world!") */

#include <scop.h>

int main(int argc, char **argv)
{
	int sock;
	char *msg = argc > 1 ? argv[1] : (char *)"Hello world!";
	
	sock = scop_open("localhost", "sender");
	scop_send_message(sock, "receiver", msg);
	
	close(sock);
	return 0;
}
