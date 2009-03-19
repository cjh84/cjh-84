// event_source.cpp - DMI - 7-9-02

/* Usage: event_source [ <source> ]   (default source is "news") */

#include <scop.h>

int main(int argc, char **argv)
{
	int sock;
	int count = 1;
	char msg[80];
	
	sock = scop_open("localhost", "event_source");
	scop_set_source_hint(sock, argc > 1 ? argv[1] : (char *)"news");
	
	while(1)
	{
		sprintf(msg, "Item %d", count);
		scop_emit(sock, msg);
		count++;
		sleep(1);
	}
	
	return 0;
}
