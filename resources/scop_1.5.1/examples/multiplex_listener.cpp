// multiplex_listener.cpp - DMI - 14-9-02

/* Usage: multiplex_listener [ <source-one> <source-two> ]
   (default sources are "news" and "updates") */

#include <scop.h>
#include "multiplex.h"

int main(int argc, char **argv)
{
	int sock[2];
	char *msg;
	multiplex mp;
	int fd;
	
	for(int i = 0; i < 2; i++)
	{
		sock[i] = scop_open("localhost", "multiplex_listener");
		mp.add(sock[i]);
	}
	scop_listen(sock[0], argc == 3 ? argv[1] : (char *)"news");
	scop_listen(sock[1], argc == 3 ? argv[2] : (char *)"updates");
	
	while(1)
	{
		fd = mp.wait();
		
		for(int i = 0; i < 2; i++)
		{
			if(fd == sock[i])
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
