/* scop.cpp - DMI - 24-9-2001

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include <sys/types.h>
#include <sys/socket.h>

#include "scop.h"
#include "scopxml.h"

#define MODE_MSG 0
#define MODE_LIST 1
#define MODE_LOG 2
#define MODE_QUERY 4
#define MODE_VERIFY 5
#define MODE_CLEAR 6
#define MODE_RPC 7
#define MODE_XMLRPC 8
#define MODE_XMLSEND 9
#define MODE_TERM 10
#define MODE_RECONF 11

// Globals:

const char *remote_hostname = "localhost";
int mode, log_level;
char *client, *message, *method;

int first_client, num_clients;
int first_arg, num_args;

void usage()
{
	printf("Usage: scop [<opts>] send {<endpoint> <message>}+\n");
	printf("       scop [<opts>] verify <endpoint> <message>\n");
	printf("       scop [<opts>] list\n");
	printf("       scop [<opts>] log 0|1\n");
	printf("       scop [<opts>] query <endpoint>\n");
	printf("       scop [<opts>] clear <endpoint>\n");
	printf("       scop [<opts>] rpc <name> <message>\n");
	printf("       scop [<opts>] xmlrpc <name> <method or \"-\"> <arg>+\n");
	printf("       scop [<opts>] xmlsend <name> <method or \"-\"> <arg>+\n");
	printf("       scop [<opts>] terminate\n");
	printf("       scop [<opts>] reconfigure\n");
	printf("\n");
	printf("Options: -r <hostname>\n");
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
		else
			usage(); // Unknown option
	}
	if(i > argc - 1)
		usage(); // No command
		
	if(!strcmp(argv[i], "list"))
	{
		if(i != argc - 1)
			usage();
		mode = MODE_LIST;
	}
	else if(!strcmp(argv[i], "query"))
	{
		if(i != argc - 2)
			usage();
		client = argv[i + 1];
		mode = MODE_QUERY;
	}
	else if(!strcmp(argv[i], "clear"))
	{
		if(i != argc - 2)
			usage();
		client = argv[i + 1];
		mode = MODE_CLEAR;
	}
	else if(!strcmp(argv[i], "log"))
	{
		if(i != argc - 2 || argv[i + 1][1] != '\0')
			usage();
		switch(argv[i + 1][0])
		{
			case '0':
				log_level = 0;
				break;
			case '1':
				log_level = 1;
				break;
			default:
				usage();
		}
		mode = MODE_LOG;
	}
	else if(!strcmp(argv[i], "verify"))
	{
		if(i != argc - 3)
			usage();
		client = argv[i + 1];
		message = argv[i + 2];
		mode = MODE_VERIFY;
	}
	else if(!strcmp(argv[i], "send"))
	{
		int spare_args = argc - 1 - i;
		
		if(spare_args < 2 || spare_args % 2 != 0)
			usage();
		first_client = i + 1;
		num_clients = spare_args / 2;
		mode = MODE_MSG;
	}
	else if(!strcmp(argv[i], "rpc"))
	{
		if(i != argc - 3)
			usage();
		client = argv[i + 1];
		message = argv[i + 2];
		mode = MODE_RPC;
	}
	else if(!strcmp(argv[i], "xmlrpc"))
	{
		int spare_args = argc - 1 - i;
		
		if(spare_args < 2)
			usage();
		client = argv[i + 1];
		method = argv[i + 2];
		first_arg = i + 3;
		num_args = spare_args - 2;
		mode = MODE_XMLRPC;
	}
	else if(!strcmp(argv[i], "xmlsend"))
	{
		int spare_args = argc - 1 - i;
		
		if(spare_args < 2)
			usage();
		client = argv[i + 1];
		method = argv[i + 2];
		first_arg = i + 3;
		num_args = spare_args - 2;
		mode = MODE_XMLSEND;
	}
	else if(!strcmp(argv[i], "terminate"))
	{
		if(i != argc - 1)
			usage();
		mode = MODE_TERM;
	}
	else if(!strcmp(argv[i], "reconfigure"))
	{
		if(i != argc - 1)
			usage();
		mode = MODE_RECONF;
	}
	else
	{
		usage(); // Unknown command
	}
}

vertex *intelligent_pack(char *s)
{
	// Check for integer, then double - otherwise assume string:
	int len = strlen(s);
	int consumed, conversions;
	
	int n;
	float f;
	
	conversions = sscanf(s, "%d%n", &n, &consumed);
	if(conversions > 0 && consumed == len)
		return pack(n);
	
	conversions = sscanf(s, "%f%n", &f, &consumed);
	if(conversions > 0 && consumed == len)
		return pack((double)f);
	
	return pack(s);
}

int main(int argc, char **argv)
{
	int sock;

	parse_args(argc, argv);
	
	sock = scop_open(remote_hostname, "CLI");
	if(sock == -1)
	{
		printf("Error: can't connect to scopserver.\n");
		exit(0);
	}

	if(mode == MODE_LIST)
	{
		list_node *ln, *a;
		int count;
		
		ln = scop_list(sock, &count);
		
		if(count == 0)
			printf("0 clients connected.\n");
		else if(count == 1)
			printf("1 client connected:\n");
		else
			printf("%d clients connected:\n", count);
		
		a = ln;
		for(int i = 0; i < count; i++)
		{
			printf("Client <%s> listening to <%s>, "
					"source hint <%s>\n", a->name, a->interest, a->src_hint);
			a = a->next;
		}
		if(count > 0)
			delete ln;
	}
	else if(mode == MODE_MSG)
	{
		for(int i = 0; i < num_clients; i++)
		{
			client = argv[first_client + i * 2];
			message = argv[first_client + i * 2 + 1];
			scop_send_message(sock, client, message);
		}
	}
	else if(mode == MODE_VERIFY)
	{
		int status = scop_send_message(sock, client, message, 1);
		if(status < 0)
			printf("Verify protocol error.\n");
		else
			printf("Message sent to %d client%s\n", status,
					status == 1 ? "." : "s.");
	}
	else if(mode == MODE_RPC)
	{
		char *reply;
		int len;
		
		reply = scop_rpc(sock, client, message);
		if(reply == NULL)
		{
			printf("Error: can't contact endpoint '%s'.\n", client);
		}
		else
		{
			len = strlen(reply);

			if(len >= 2 && reply[0] == '<' && reply[len - 1] == '>')
			{
				// Reply seems to be in XML:
				vertex *w = string_to_vertex(reply);
				printf("RPC to client %s: args %s, XML reply:\n%s",
						client, message, pretty_print(w));
				delete w;
			}
			else
			{
				// Plain text reply:
				printf("RPC to client %s: args %s, reply %s.\n",
						client, message, reply);
			}
			delete[] reply;
		}
	}
	else if(mode == MODE_XMLRPC)
	{
		vertex *v, *w;
		
		if(num_args == 0)
		{
			// No args - just send the method name (or "-") as plain text:
			
			char *reply;
			int len;
			
			reply = scop_rpc(sock, client, method);
			if(reply == NULL)
			{
				printf("Error: can't contact endpoint '%s'.\n", client);
			}
			else
			{
				len = strlen(reply);
				if(len >= 2 && reply[0] == '<' && reply[len - 1] == '>')
				{
					// Reply seems to be in XML:
					w = string_to_vertex(reply);
					printf("%s", pretty_print(w));
					delete w;
				}
				else
				{
					// Plain text reply:
					printf("%s\n", reply);
				}
				delete[] reply;
			}
		}
		else if(num_args == 1)
		{
			// Single argument, pack it but don't need a list:
			v = intelligent_pack(argv[first_arg]);
		}
		else
		{
			// A list of at least 2 args:
			v = mklist();
			for(int i = 0; i < num_args; i++)
				append(v, intelligent_pack(argv[first_arg + i]));
		}

		if(num_args > 0)
		{
			// Perform RPC with XML argument:
			if(!strcmp(method, "-"))
				w = scop_rpc(sock, client, v);
			else
				w = scop_rpc(sock, client, v, method);
			delete v;
			
			// Print XML response:
			if(w == NULL)
			{
				printf("Error: can't contact endpoint '%s'.\n", client);
			}
			else
			{
				printf("%s", pretty_print(w));
				delete w;
			}
		}
	}
	else if(mode == MODE_XMLSEND)
	{
		vertex *v;
		
		if(num_args == 0)
		{
			// No args - just send the method name (or "-") as plain text:
			scop_send_message(sock, client, method);
		}
		else if(num_args == 1)
		{
			// Single argument, pack it but don't need a list:
			v = intelligent_pack(argv[first_arg]);
		}
		else
		{
			// A list of at least 2 args:
			v = mklist();
			for(int i = 0; i < num_args; i++)
				append(v, intelligent_pack(argv[first_arg + i]));
		}

		if(num_args > 0)
		{
			if(!strcmp(method, "-"))
				scop_send_struct(sock, client, v);
			else
				scop_send_struct(sock, client, v, method);
			
			delete v;
		}
	}
	else if(mode == MODE_QUERY)
	{
		int answer = scop_query(sock, client);
		if(answer > 1)
			printf("%d clients connected to this endpoint.\n", answer);
		else if(answer == 1)
			printf("Client connected.\n");
		else
			printf("Client not connected.\n");
	}
	else if(mode == MODE_CLEAR)
	{
		scop_clear(sock, client);
	}
	else if(mode == MODE_LOG)
	{
		scop_set_log(sock, log_level);		
	}
	else if(mode == MODE_TERM)
	{
		scop_terminate(sock);
	}
	else if(mode == MODE_RECONF)
	{
		scop_reconfigure(sock);
	}
	
	close(sock);
}
