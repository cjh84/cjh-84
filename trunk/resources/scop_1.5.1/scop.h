/* scop.h - DMI - 22-10-2001 - SCOP Library API

Copyright (C) 2001-06 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

/* The example programs need these include files (otherwise they
	shouldn't strictly be in this header file: */
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>

#define FALLBACK_PORT 51234

#define SCOP_MAJOR_VERSION 1
#define SCOP_MINOR_VERSION 2
#define SCOP_RELEASE_NUMBER 0

class vertex;

class list_node
{
	public:
	
		char *name;
		char *interest;
		char *src_hint;
		
		list_node *next;
		
		list_node(char *n, char *ep, char *sh);
		~list_node();
};
		
#define VERIFY_SYNTAX_ERROR -1

// Connection setup:

int scop_open(const char *remote_hostname, const char *name, int unique = 0);
void scop_listen(int sock, const char *interest, int unique = 0);

// Messaging:

int scop_send_message(int sock, const char *endpoint, const char *message,
		int verify = 0);
char *scop_get_message(int sock, int *rpc_flag = NULL); // Also for RPC

// Predefined event sources:

void scop_set_source_hint(int sock, const char *endpoint);
int scop_emit(int sock, const char *message, int verify = 0);

// Admin:

int scop_query(int sock, const char *endpoint);
void scop_clear(int sock, const char *endpoint);
void scop_set_log(int sock, int log_level);
list_node *scop_list(int sock, int *count);
void scop_terminate(int sock);
void scop_reconfigure(int sock);

// Cookies:

void scop_set_plain_cookie(int sock, const char *text);
char *scop_get_plain_cookie(int sock, const char *name);

// XML Cookies:

vertex *scop_get_cookie(int sock, const char *name);
void scop_set_cookie(int sock, vertex *data);

// XML messaging:

int scop_send_struct(int sock, const char *endpoint, vertex *args,
		const char *method = NULL);
vertex *scop_get_struct(int sock, int *rpc_flag = NULL);

// RPC:

char *scop_rpc(int sock, const char *endpoint, const char *args);
int scop_send_reply(int sock, const char *reply);

// XML-RPC versions:

vertex *scop_rpc(int sock, const char *endpoint, vertex *args,
		const char *method = NULL);
vertex *scop_get_request(int sock);
int scop_send_reply(int sock, vertex *reply);
