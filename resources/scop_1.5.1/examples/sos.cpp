// sos.cpp - DMI - 3-1-2002

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <syslog.h>

#include <sys/types.h>
#include <sys/socket.h>

#include <scop.h>

int main(int argc, char **argv)
{
	int sock;
	char *buf;
	int rpc_flag;

	sock = scop_open("localhost", "sos", 1);
	if(sock == -1)
	{
		printf("Can't connect to scopserver.\n");
		exit(0);
	}
	
	if(fork() > 0) exit(0); // Detach
	
	while(1)
	{
		buf = scop_get_message(sock);
		if(buf == NULL)
		{
			syslog(LOG_INFO, "Lost connection to scopserver.\n");
			exit(0);
		}
		syslog(LOG_INFO, "%s\n", buf);
		delete[] buf;
	}
	
	close(sock);
	return 0;
}
