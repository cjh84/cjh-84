/* scoplib.cpp - DMI - 22-10-2001

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdarg.h>
#include <string.h>
#include <syslog.h>

#include <sys/types.h>
#include <sys/socket.h>

#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <netdb.h>

#include "scop.h"
#include "scopxml.h"

#define MAX_ERR_LEN 100

extern int errno;

static void error(const char *format, ...);
static void log(const char *format, ...);
static int do_connect(const char *remote_hostname);

static const char *SERVICE_NAME = "scop";

// Low-level:
int write_header(int sock, int len);
char *read_protocol(int sock);
int read_int(int sock);
int fixed_read(int sock, char *buf, int nbytes);

static void sock_nodelay(int sock)
{
	struct protoent *pe = getprotobyname("tcp");
	int opt_true = 1;
	setsockopt(sock, pe->p_proto, TCP_NODELAY, &opt_true, sizeof(int));
}

static int prefix(const char *s, const char *pre)
{
	return strncmp(s, pre, strlen(pre));
}

void transmit(int sock, char *buf)
{
	int query_len;
	
	query_len = strlen(buf);
	write_header(sock, query_len);
	write(sock, buf, query_len);
}

/* scop_get_message serves a dual purpose, being used in both RPC's and
	plain message passing: */

static const char *SCOP_RPC_CALL = "scop-rpc-call ";
			
char *scop_get_message(int sock, int *rpc_flag) // = NULL
{
	char *buf;

	buf = read_protocol(sock);
	if(buf == NULL) return NULL;

	if(!prefix(buf, SCOP_RPC_CALL))
	{	
		char *body = malloc(strlen(buf));
		strcpy(body, buf + strlen(SCOP_RPC_CALL));
		free(buf);
		buf = body;
		
		if(rpc_flag)
			*rpc_flag = 1;
	}
	else
	{
		if(rpc_flag)
			*rpc_flag = 0;
	}
	return buf;
}

char *scop_rpc(int sock, const char *endpoint, const char *args)
{
	char *buf;
	char *reply;
	int rpc_flag;
	
	buf = malloc(100 + strlen(endpoint) + strlen(args));
	sprintf(buf, "call %s! %s", endpoint, args);

	transmit(sock, buf);
	free(buf);
	
	reply = scop_get_message(sock, NULL);
	if(!strcmp(reply, "scop-rpc-error"))
	{
		free(reply);
		return NULL;
	}
	
	return reply;
}

void scop_send_reply(int sock, const char *reply)
{
	char *buf;
	
	buf = malloc(100 + strlen(reply));
	sprintf(buf, "reply %s", reply);

	transmit(sock, buf);
	free(buf);
}

vertex *scop_rpc_x(int sock, const char *endpoint, vertex *args,
		const char *method) // = NULL
{
	char *request, *reply;
        vertex *ans;

	if(method != NULL)
		request = vertex_to_string_m(args, method);
	else
		request = vertex_to_string(args);
	
	reply = scop_rpc(sock, endpoint, request);
	free(request);
	
	if(reply == NULL)
		return NULL;
	
	ans = string_to_vertex(reply);

	free(reply);
	return ans;
}

vertex *scop_get_request(int sock)
{
	char *buf;
	int rpc_flag;
        vertex *v;

	buf = scop_get_message(sock, &rpc_flag);
	if(buf == NULL || rpc_flag != 1)
		return NULL;
	
	v = string_to_vertex(buf);
	free(buf);
	return v;
}

void scop_send_reply_x(int sock, vertex *reply)
{
	char *buf;
	
	buf = vertex_to_string(reply);
	scop_send_reply(sock, buf);
	free(buf);
}
		
int scop_send_message(int sock, const char *endpoint, const char *message,
		int verify) // = 0
{
	char *buf;
	
	buf = malloc(100 + strlen(endpoint) + strlen(message));
	if(verify)
		sprintf(buf, "verify %s! %s", endpoint, message);
	else
		sprintf(buf, "message %s! %s", endpoint, message);

	transmit(sock, buf);
	free(buf);
	
	if(verify)
	{
		int status = read_int(sock);
		return status;
	}
	else
		return -1;
}

int scop_emit(int sock, const char *message, int verify) // = 0
{
	char *buf;
        int status;

	buf = malloc(100 + strlen(message));
	sprintf(buf, "emit %s", message);

	transmit(sock, buf);
	free(buf);

	status = read_int(sock);
	
	if(verify)
		return status;
	else
		return 0;
}

void scop_send_struct(int sock, const char *endpoint, vertex *args,
		const char *method) // = NULL
{
	char *msg;

	if(method != NULL)
		msg = vertex_to_string_m(args, method);
	else
		msg = vertex_to_string(args);
	
	scop_send_message(sock, endpoint, msg, 0);
	free(msg);
}

vertex *scop_get_struct(int sock, int *rpc_flag) // = NULL
{
	char *buf;
        vertex *v;

	buf = scop_get_message(sock, rpc_flag);
	if(buf == NULL)
		return NULL;
	
	v = string_to_vertex(buf);
	free(buf);
	return v;
}

vertex *scop_get_cookie(int sock, const char *name)
{
	char *s;
        vertex *v;
	
	s = scop_get_plain_cookie(sock, name);
	if(s == NULL)
		return NULL;
	
	v = string_to_vertex(s);
	free(s);
	return v;
}

void scop_set_cookie(int sock, vertex *data)
{
	char *text;
	
	text = vertex_to_string(data);
	scop_set_plain_cookie(sock, text);
	free(text);
}

int scop_query(int sock, const char *endpoint)
{
	char *buf;
        int answer;

	buf = malloc(100 + strlen(endpoint));
	sprintf(buf, "query %s", endpoint);

	transmit(sock, buf);
	free(buf);

	answer = read_int(sock);
	return answer;
}

list_node* list_node_con(char *n, char *ep, char *sh)
{
        list_node *node = malloc(sizeof(list_node));

	node->name = malloc(strlen(n) + 1);
	node->interest = malloc(strlen(ep) + 1);
	node->src_hint = malloc(strlen(sh) + 1);
	strcpy(node->name, n);
	strcpy(node->interest, ep);
	strcpy(node->src_hint, sh);
	node->next = NULL;
        return node;
}

void list_node_des(list_node* node)
{
	if(node->next)
		free(node->next);
	free(node->name);
	free(node->interest);
	free(node->src_hint);
}

/* This function reads components from a string separated by plings.
	It returns NULL at the end of the string: */

char *read_cpt(char *s, char *buf)
{
	int i,len;
	char *t;
	t = s;

	if(s == NULL)
		return NULL; // Nope, won't read past the end of string!
		
	if(*t == '\0')
		return NULL;
	
	while(*t != '!' && *t != '\0')
		t++;
	
	len = t - s;
	for(i = 0; i < len; i++)
		buf[i] = s[i];
	buf[len] = '\0';
	
	if(*t == '!')
		return t + 1;
	else
		return t; // Next read will encounter end of string
}

list_node *scop_list(int sock, int *count)
{
	char *buf, *orig_buf, *pos;
	char *name, *interest, *src_hint;
	list_node *a = NULL, *ln;
        int len;
	
	*count = 0;
	
	buf = "list";
	transmit(sock, buf);
	
	buf = read_protocol(sock);
	if(buf == NULL) return NULL;
	
	len = strlen(buf);
	name = malloc(len + 1);
	interest = malloc(len + 1);
	src_hint = malloc(len + 1);
	
	pos = buf;
	while(1)
	{
		pos = read_cpt(pos, name);
		pos = read_cpt(pos, interest);
		pos = read_cpt(pos, src_hint);
		if(pos == NULL)
			break; // Hit end of string before reading all 3 items
		
		(*count)++;
		ln = list_node_con(name, interest, src_hint);
		ln->next = a;
		a = ln;
	}
	
	free(buf);
	free(name);
	free(interest);
	free(src_hint);
	
	return a;
}

void scop_clear(int sock, const char *endpoint)
{
	char *buf;

	buf = malloc(100 + strlen(endpoint));
	sprintf(buf, "clear %s", endpoint);

	transmit(sock, buf);
	free(buf);
}

void scop_set_log(int sock, int log_level)
{
	char *buf;

	buf = malloc(100);
	sprintf(buf, "log %d", log_level);

	transmit(sock, buf);
	free(buf);
}

void scop_terminate(int sock)
{
	transmit(sock, "terminate");
}

void scop_reconfigure(int sock)
{
	transmit(sock, "reconfigure");
}

void scop_set_plain_cookie(int sock, const char *text)
{
	char *buf;

	buf = malloc(100 + strlen(text));
	sprintf(buf, "set-cookie %s", text);

	transmit(sock, buf);
	free(buf);
}

void scop_set_source_hint(int sock, const char *endpoint)
{
	char *buf;

	buf = malloc(100 + strlen(endpoint));
	sprintf(buf, "set-source-hint %s", endpoint);

	transmit(sock, buf);
	free(buf);
}

char *scop_get_plain_cookie(int sock, const char *name)
{
	char *buf;

	buf = malloc(100 + strlen(name));
	sprintf(buf, "get-cookie %s", name);

	transmit(sock, buf);
	free(buf);

	buf = read_protocol(sock);
	if(buf == NULL) return NULL;
	
	return buf;
}

int scop_open(const char *remote_hostname, const char *name, int unique) // = 0
{
	int sock;
	char *buf;
	
	sock = do_connect(remote_hostname);
	if(sock == -1)
		return -1;
	
	if(name != NULL)
	{
		buf = malloc(strlen(name) + 100);
		
		if(unique)
			scop_clear(sock, name);
		
		sprintf(buf, "register %s", name);
		
		transmit(sock, buf);		
		free(buf);
	}
	
	return sock;
}

void scop_listen(int sock, const char *interest, int unique) // = 0
{
	char *buf;

	if(unique)
		scop_clear(sock, interest);
	
	buf = malloc(100 + strlen(interest));
	sprintf(buf, "listen %s", interest);

	transmit(sock, buf);
	free(buf);
}

/* Networking code */

// Solaris:
#ifndef INADDR_NONE
#define INADDR_NONE -1
#endif

static int do_connect(const char *remote_hostname)
{
	struct hostent *phe;
	struct servent *pse;
	struct protoent *ppe;
	struct sockaddr_in remote_addr;
	int sock;
	const char *transport = "tcp";
	const char *service = SERVICE_NAME;
	
	memset(&remote_addr, 0, sizeof(remote_addr));
	remote_addr.sin_family = AF_INET;
	
	if(pse = getservbyname(service, transport))
		remote_addr.sin_port = pse->s_port;
	else
		remote_addr.sin_port = htons(FALLBACK_PORT);
	
	// Map host name to IP address, allowing for dotted decimal:
	if(phe = gethostbyname(remote_hostname))
	{
		memcpy(&remote_addr.sin_addr, phe->h_addr, phe->h_length);
	}
	else if((remote_addr.sin_addr.s_addr = inet_addr(remote_hostname))
		== INADDR_NONE)
	{
		log("Can't get \"%s\" host entry", remote_hostname);
		return -1;
	}
	
	// Map transport protocol name to protocol number:
	if((ppe = getprotobyname(transport)) == 0)
	{
		log("Can't get %s protocol entry", transport);
		return -1;
	}
	
	// Allocate a socket:
	sock = socket(PF_INET, SOCK_STREAM, ppe->p_proto);
	if(sock < 0)
	{
		log("Can't create socket: %s", strerror(errno));
		return -1;
	}
	sock_nodelay(sock);
	
	// Connect the socket:
	if(connect(sock, (struct sockaddr *)&remote_addr, sizeof(remote_addr)) < 0)
	{
		log("Can't connect to %s: %s", remote_hostname, strerror(errno));
		return -1;
	}
	
	return sock;
}

char hex(int d)
{
	if(d >= 0 && d <= 9) return d + '0';
	if(d >= 10 && d <= 15) return d - 10 + 'A';
	return '0';
}

int dec(char c)
{
	if(c >= '0' && c <= '9') return c - '0';
	if(c >= 'A' && c <= 'F') return c - 'A' + 10;
	return 0;
}

int write_header(int sock, int len)
{
	char buf[21];
	unsigned int version, mask;
	int bytes, i, shift;

	strcpy(buf, "sCoP ");
	version = SCOP_MAJOR_VERSION << 16;
	version += SCOP_MINOR_VERSION << 8;
	version += SCOP_RELEASE_NUMBER;
		
	mask = 0xF00000;
	shift = 20;
	for(i = 0; i < 6; i++)
	{
		buf[5 + i] = hex((version & mask) >> shift);
		mask >>= 4;
		shift -= 4;
	}
	buf[11] = ' ';
	
	mask = 0xF0000000;
	shift = 28;
	for(i = 0; i < 8; i++)
	{
		buf[12 + i] = hex((len & mask) >> shift);
		mask >>= 4;
		shift -= 4;
	}
	buf[20] = ' ';
	
	bytes = write(sock, buf, 21);
        return bytes;
}

char *read_protocol(int sock)
{
	char header[21], *buf;
	int i, len = 0, version = 0;
	int major_version, minor_version, shift;
	
	if(fixed_read(sock, header, 21) == -1)
		return NULL;
	
	if(strncmp(header, "sCoP", 4))
	{
		log("Protocol magic mismatch");
		return NULL;
	}
	
	shift = 20;
	for(i = 0; i < 6; i++)
	{
		version += dec(header[5 + i]) << shift;
		shift -= 4;
	}
	major_version = (version & 0xFF0000) >> 16;
	minor_version = (version & 0x00FF00) >> 8;
	if(major_version != SCOP_MAJOR_VERSION ||
			minor_version != SCOP_MINOR_VERSION)
	{
		log("Protocol version mismatch");
		return NULL;
	}
	
	shift = 28;
	for(i = 0; i < 8; i++)
	{
		len += dec(header[12 + i]) << shift;
		shift -= 4;
	}
	buf = malloc(len + 1);
	if(fixed_read(sock, buf, len) == -1)
	{
		free(buf);
		return NULL;
	}
	buf[len] = '\0';
	return buf;
}

int read_int(int sock)
{
	unsigned char c[8];
	int i, n = 0, shift = 28;
	
	if(fixed_read(sock, (char *)c, 8) < 0)
		return -1;
	
	for(i = 0; i < 8; i++)
	{
		n += dec(c[i]) << shift;
		shift -= 4;
	}	
	return n;
}

int fixed_read(int sock, char *buf, int nbytes)
{
	int remain = nbytes;
	char *pos = buf;
	int amount;
	
	while(remain > 0)
	{
		amount = read(sock, pos, remain);
		if(amount <= 0)
			return -1; // EOF or error
		remain -= amount;
		pos += amount;
	}
	return 0;
}

static void log(const char *format, ...)
{
	va_list args;
	char c[MAX_ERR_LEN], d[MAX_ERR_LEN + 20];

	FILE *fp = NULL;
	char *filename = getenv("SCOP_LOGFILE");
	if(filename)
		fp = fopen(filename, "a");
		
	va_start(args, format);
	vsnprintf(c, MAX_ERR_LEN, format, args);
	va_end(args);
	c[MAX_ERR_LEN - 1] = '\0';
	sprintf(d, "[scoplib] %s\n", c);
	
	if(fp)
	{
		fwrite(d, strlen(d), 1, fp);
		fclose(fp);
	}
	else
		syslog(LOG_INFO, d);
}
