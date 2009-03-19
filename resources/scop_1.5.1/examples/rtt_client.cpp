// rtt_client.cpp - DMI - 11-9-02

/* Usage: rtt_client [ <iterations> ]   (default is 1000) */

#include <scop.h>

#include <sys/time.h>

int REPEATS = 1000;

int main(int argc, char **argv)
{
	int sock;
	char *reply;
	struct timeval tv_start, tv_end;
	long us;
	
	if(argc == 2)
		REPEATS = atoi(argv[1]);
	
	sock = scop_open("localhost", "rtt_client");
	gettimeofday(&tv_start, NULL);
	for(int i = 0; i < REPEATS; i++)
	{
		reply = scop_rpc(sock, "server", "test");
		if(strcmp(reply, "tteesstt"))
		{
			printf("Message error.\n");
			exit(0);
		}
		delete[] reply;
	}
	gettimeofday(&tv_end, NULL);
	us = (tv_end.tv_sec - tv_start.tv_sec) * 1000000;
	us += tv_end.tv_usec - tv_start.tv_usec;
	printf("%d round trips in %d us; time each = %d us; approx %d per second.\n",
			REPEATS, us, us / REPEATS, 1000000 / (us / REPEATS));
	
	close(sock);
	return 0;
}
