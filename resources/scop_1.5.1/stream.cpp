/* stream.cpp - DMI - 31-7-2006

Copyright (C) 2006 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>

#include "scop.h"

// Globals:

const char *remote_hostname = "localhost";
const char *client_name = "stream";
char *endpoint;
int persist = 0;

void usage()
{
	printf("Usage: stream [option...] <endpoint>\n");
	printf("Options: -r <hostname>   remote scopserver\n");
	printf("         -p              persistent\n");
//	printf("         -p <buffer>     persistent (and number of lines to replay)\n");
	exit(0);
}

void parse_args(int argc, char **argv)
{
	int i;
	
	if(argc < 2)
		usage();

	// Parse options:
	for(i = 1; i < argc; i++)
	{
		if(argv[i][0] != '-')
			break;
			
		if(!strcmp(argv[i], "-r"))
		{
			if(i == argc - 1)
				usage(); // No parameter
			remote_hostname = argv[++i];
		}
		else if(!strcmp(argv[i], "-p"))
			persist = 1;
		else
			usage(); // Unknown option
	}
		
	int spare_args = argc - i;
	if(spare_args != 1)
		usage();
	endpoint = argv[i];
}

int reconnect()
{
	const int attempts = 60;
	const int max_delay = 64;
	int sock;
	int secs = 1, retries = 0;
	fd_set read_fds;
	int max_fd;
	int stdin_fd = 0;
	struct timeval tv;
	int line_len = 200;
	char *line = new char[line_len];
	
	while(1)
	{
		sock = scop_open(remote_hostname, client_name);
		if(sock != -1)
		{
			delete[] line;
			return sock;
		}
		
		FD_ZERO(&read_fds);
		max_fd = stdin_fd;
		FD_SET(stdin_fd, &read_fds);
		tv.tv_sec = secs;
		tv.tv_usec = 0;
		select(max_fd + 1, &read_fds, NULL, NULL, &tv);
		if(FD_ISSET(stdin_fd, &read_fds))
		{
			if(fgets(line, line_len, stdin) == NULL)
			{
				printf("End of file on stdin.\n");
				exit(0);
			}
		}
		
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
	int line_len = 200;
	char *line = new char[line_len];
	int chars_read;

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
	scop_set_source_hint(sock, endpoint);
	while(1)
	{
		if(fgets(line, line_len, stdin) == NULL)
		{
			printf("End of file on stdin.\n");
			exit(0);
		}
		chars_read = strlen(line);
		if(chars_read > 0)
		{
			if(line[chars_read - 1] == '\n')
				line[chars_read - 1] = '\0';
			if(scop_emit(sock, line) < 0)
			{
				if(persist)
				{
					printf("Lost connection; will try to re-establish.\n");
					sock = reconnect();
					scop_set_source_hint(sock, endpoint);
					printf("OK, reconnected.\n");
				}
				else
				{
					printf("Lost connection.\n");
					exit(0);
				}
			}
		}
	}
	close(sock);
}
