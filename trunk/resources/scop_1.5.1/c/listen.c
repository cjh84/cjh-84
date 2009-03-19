/* listen.cpp - DMI - 24-9-2001

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>

#include <sys/types.h>
#include <sys/socket.h>

#include "scop.h"
#include "scopxml.h"

char *remote_hostname = "localhost";
char *endpoint = NULL;
char *exec_command = NULL;
int unique = 0;

void usage()
{
	printf("Usage: listen [option...] <endpoint>\n\n");
	printf("Options: -r <hostname>   remote scopserver\n");
	printf("         -u              unique endpoint\n");
	printf("         -e <command>    execute command with message arguments\n");
	exit(0);
}

void parse_args(int argc, char **argv)
{
        int i;
	for(i = 1; i < argc; i++)
	{
		if(!strcmp(argv[i], "-u"))
		{
			unique = 1;
		}
		else if(!strcmp(argv[i], "-r"))
		{
			if(i == argc - 1)
				usage();
			remote_hostname = argv[++i];
		}
		else if(!strcmp(argv[i], "-e"))
		{
			if(i == argc - 1)
				usage();
			exec_command = argv[++i];
		}
		else
		{
			if(argv[i][0] == '-')
				usage();
			// Must be the endpoint:
			if(endpoint != NULL)
				usage();
			endpoint = argv[i];
		}
	}
	if(endpoint == NULL)
		usage();
}

int main(int argc, char **argv)
{
	int sock;
	char *buf;
	int rpc_flag;

	parse_args(argc, argv);
	sock = scop_open(remote_hostname, "listen", 0);
	scop_listen(sock, endpoint, unique);
	
	while(1)
	{
		buf = scop_get_message(sock, &rpc_flag);
		if(buf == NULL)
		{
			printf("Lost connection.\n");
			exit(0);
		}
		if(rpc_flag)
		{
			// RPC:
			
			int len = strlen(buf);
			
			if(len >= 2 && buf[0] == '<' && buf[len - 1] == '>')
			{
				// Seems to be an XML request:
				
				vertex *v, *w;
				
				v = string_to_vertex(buf);
				printf("Request %s\n\n%s", buf, pretty_print(v));
				
				w = pack_string("OK");
				scop_send_reply_x(sock, w);
				free(w);
				
				free(v);
			}
			else
			{
				// Plain text request - reverse it:
				
				char *reply = malloc(len + 1);
                                int i;
			
				for(i = 0; i < len; i++)
					reply[i] = buf[len - 1 - i];
				reply[len] = '\0';
			
				printf("Request %s, Replying %s\n", buf, reply);
				scop_send_reply(sock, reply);
				free(reply);
			}
		}
		else
		{
			// Message/event (not RPC):
			
			int len = strlen(buf);
			
			if(len >= 2 && buf[0] == '<' && buf[len - 1] == '>')
			{
				// Seems to be an XML structured message:
				
				vertex *v;
                                char* formatted;
				
				v = string_to_vertex(buf);
				formatted = pretty_print(v);
				
				printf("Received %s\n\n%s\n", buf, formatted);
				
				free(formatted);
				free(v);
			}
			else
			{
				// Plain text message:
				if(exec_command != NULL)
				{
					char *cmd;
					
					cmd = malloc(len + strlen(exec_command) + 10);
					sprintf(cmd, "%s %s &", exec_command, buf);
					system(cmd);
					free(cmd);
				}
				else
				{
					printf("Received \"%s\"\n", buf);
				}
			}
		}
		free(buf);
	}
	close(sock);
}
