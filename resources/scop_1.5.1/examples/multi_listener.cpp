// multi_listener.cpp - DMI - 7-9-02

/* Usage: multi_listener [ <source-one> <source-two> ]
   (default sources are "news" and "updates") */

#include <sys/select.h>

#include <scop.h>

int main(int argc, char **argv)
{
	int sock[2];
	char *msg;
	fd_set read_fds;
	int max_fd;
	
	for(int i = 0; i < 2; i++)
		sock[i] = scop_open("localhost", "multi_listener");
	scop_listen(sock[0], argc == 3 ? argv[1] : (char *)"news");
	scop_listen(sock[1], argc == 3 ? argv[2] : (char *)"updates");
	
	while(1)
	{
		FD_ZERO(&read_fds);
		max_fd = 0;
		for(int i = 0; i < 2; i++)
		{
			FD_SET(sock[i], &read_fds);
			if(sock[i] > max_fd) max_fd = sock[i];
		}
		select(max_fd + 1, &read_fds, NULL, NULL, NULL);
		
		for(int i = 0; i < 2; i++)
		{
			if(FD_ISSET(sock[i], &read_fds))
			{		
				msg = scop_get_message(sock[i]);
				printf("Received <%s> from %s\n", msg,
						i == 1 ? "updates" : "news");
				delete[] msg;
			}
		}
	}
	
	return 0;
}
