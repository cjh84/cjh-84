/* bridge.cpp - DMI - 18-10-2006

Copyright (C) 2006 David Ingram

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

const char *remote_hostname = "localhost";
const char *client_name = "listen";
char *endpoint = NULL;
int persist = 0;

void usage()
{
	printf("Usage: bridge [-p] <server1> <endpoint1> <server2> "
			"<endpoint2>\n");
	printf("Options: -p = persistent\n");
	exit(0);
}

void parse_args(int argc, char **argv)
{
	for(int i = 1; i < argc; i++)
	{
		if(!strcmp(argv[i], "-p"))
		{
			persist = 1;
		}
		else if(!strcmp(argv[i], "-r"))
		{
			if(i == argc - 1)
				usage();
			remote_hostname = argv[++i];
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

int reconnect()
{
	const int attempts = 60;
	const int max_delay = 64;
	int sock;
	int secs = 1, retries = 0;
	while(1)
	{
		sock = scop_open(remote_hostname, client_name);
		if(sock != -1)
			return sock;
		sleep(secs);
		retries++;
		if(retries > attempts && secs < max_delay)
		{
			secs *= 2;
			retries = 0;
		}
	}
}

int main(int argc, char **argv)
{
	int sock;
	char *buf;
	int rpc_flag;

	parse_args(argc, argv);
	sock = scop_open(remote_hostname, client_name);
	if(sock == -1)
	{
		if(persist)
		{
			printf("Can't connect to scopserver; will keep trying.\n");
			sock = reconnect();
			printf("OK, connection established.\n");
		}
		else
		{
			printf("Error: can't connect to scopserver.\n");
			exit(0);
		}
	}
	scop_listen(sock, endpoint);
	
	while(1)
	{
		buf = scop_get_message(sock, &rpc_flag);
		if(buf == NULL)
		{
			if(persist)
			{
				printf("Lost connection; will try to re-establish.\n");
				sock = reconnect();
				scop_listen(sock, endpoint);
				printf("OK, reconnected.\n");
				continue;
			}
			else
			{
				printf("Lost connection.\n");
				exit(0);
			}
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
				
				w = pack("OK");
				scop_send_reply(sock, w);
				delete w;
				
				delete v;
			}
			else
			{
				// Plain text request - reverse it:
				
				char *reply = new char[len + 1];
			
				for(int i = 0; i < len; i++)
					reply[i] = buf[len - 1 - i];
				reply[len] = '\0';
			
				printf("Request %s, Replying %s\n", buf, reply);
				scop_send_reply(sock, reply);
				delete[] reply;
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
				
				v = string_to_vertex(buf);
				char *formatted = pretty_print(v);
				
				printf("Received %s\n\n%s\n", buf, formatted);
				
				delete[] formatted;
				delete v;
			}
			else
			{
				// Plain text message:
				printf("Received \"%s\"\n", buf);
			}
		}
		delete[] buf;
	}
	close(sock);
}
