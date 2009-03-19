/* scoplib.cpp - DMI - 22-10-2001

Copyright (C) 2001-06 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdarg.h>
#include <string.h>
#include <syslog.h>
#include <errno.h>
#include <signal.h>
#include <time.h>

#include <sys/types.h>
#include <sys/socket.h>

#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <pwd.h>

#include "scop.h"
#include "scopxml.h"

#define MAX_ERR_LEN 100

extern int errno;

static void log(const char *format, ...);
static int do_connect(const char *remote_hostname);

static const char *SERVICE_NAME = "scop";

// Low-level:
int write_header(int sock, int len);
char *read_protocol(int sock);
int fixed_read(int sock, char *buf, int nbytes);
char hex(int d);
int dec(char c);
int read_int(int sock);

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

int transmit(int sock, const char *buf)
{
	int query_len;
	
	query_len = strlen(buf);
	if(write_header(sock, query_len) < 0)
		return -1;
	if(write(sock, buf, query_len) < 0)
		return -1;
	return 0;
}

/* scop_get_message serves a dual purpose, being used in both RPC's and
	plain message passing: */

static const char *SCOP_RPC_CALL = "scop-rpc-call ";
			
char *scop_get_message(int sock, int *rpc_flag)
{
	char *buf;

	buf = read_protocol(sock);
	if(buf == NULL) return NULL;

	if(!prefix(buf, SCOP_RPC_CALL))
	{	
		char *body = new char[strlen(buf)];
		strcpy(body, buf + strlen(SCOP_RPC_CALL));
		delete[] buf;
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
	
	buf = new char[100 + strlen(endpoint) + strlen(args)];
	sprintf(buf, "call %s! %s", endpoint, args);

	if(transmit(sock, buf) < 0)
	{
		delete[] buf;
		return NULL;
	}
	delete[] buf;
	
	char *reply;
	reply = scop_get_message(sock); // FIXME: Probably should check rpc_flag
	if(!strcmp(reply, "scop-rpc-error"))
	{
		delete[] reply;
		return NULL;
	}
	
	return reply;
}

int scop_send_reply(int sock, const char *reply)
{
	char *buf;
	int ret;
	
	buf = new char[100 + strlen(reply)];
	sprintf(buf, "reply %s", reply);

	ret = transmit(sock, buf);
	delete[] buf;
	return ret;
}

vertex *scop_rpc(int sock, const char *endpoint, vertex *args,
		const char *method)
{
	char *request, *reply;

	if(method != NULL)
		request = vertex_to_string(args, method);
	else
		request = vertex_to_string(args);
	
	reply = scop_rpc(sock, endpoint, request);
	delete[] request;
	
	if(reply == NULL)
		return NULL;
	
	vertex *ans = string_to_vertex(reply);
	delete[] reply;
	return ans;
}

vertex *scop_get_request(int sock)
{
	char *buf;
	int rpc_flag;

	buf = scop_get_message(sock, &rpc_flag);
	if(buf == NULL || rpc_flag != 1)
		return NULL;
	
	vertex *v = string_to_vertex(buf);
	delete[] buf;
	return v;
}

int scop_send_reply(int sock, vertex *reply)
{
	char *buf;
	int ret;
	
	buf = vertex_to_string(reply);
	ret = scop_send_reply(sock, buf);
	delete[] buf;
	return ret;
}
		
int scop_send_message(int sock, const char *endpoint, const char *message,
		int verify)
{
	char *buf;
	
	buf = new char[100 + strlen(endpoint) + strlen(message)];
	if(verify)
		sprintf(buf, "verify %s! %s", endpoint, message);
	else
		sprintf(buf, "message %s! %s", endpoint, message);

	if(transmit(sock, buf) < 0)
	{
		delete[] buf;
		return -1;
	}
	delete[] buf;
	
	if(verify)
	{
		int status = read_int(sock);
		return status;
	}
	else
		return 0;
}

int scop_emit(int sock, const char *message, int verify)
{
	char *buf;

	buf = new char[100 + strlen(message)];
	sprintf(buf, "emit %s", message);

	if(transmit(sock, buf) < 0)
	{
		delete[] buf;
		return -1;
	}
	delete[] buf;

	int status = read_int(sock);
	if(status < 0 || verify)
		return status;
	else
		return 0;
}

int scop_send_struct(int sock, const char *endpoint, vertex *args,
		const char *method)
{
	char *msg;
	int ret;

	if(method != NULL)
		msg = vertex_to_string(args, method);
	else
		msg = vertex_to_string(args);
	
	ret = scop_send_message(sock, endpoint, msg, 0);
	delete[] msg;
	return ret;
}

vertex *scop_get_struct(int sock, int *rpc_flag)
{
	char *buf;

	buf = scop_get_message(sock, rpc_flag);
	if(buf == NULL)
		return NULL;
	
	vertex *v = string_to_vertex(buf);
	delete[] buf;
	return v;
}

vertex *scop_get_cookie(int sock, const char *name)
{
	char *s;
	
	s = scop_get_plain_cookie(sock, name);
	if(s == NULL)
		return NULL;
	
	vertex *v = string_to_vertex(s);
	delete[] s;
	return v;
}

void scop_set_cookie(int sock, vertex *data)
{
	char *text;
	
	text = vertex_to_string(data);
	scop_set_plain_cookie(sock, text);
	delete[] text;
}

int scop_query(int sock, const char *endpoint)
{
	char *buf;

	buf = new char[100 + strlen(endpoint)];
	sprintf(buf, "query %s", endpoint);

	transmit(sock, buf);
	delete[] buf;

	int answer = read_int(sock);
	return answer;
}

list_node::list_node(char *n, char *ep, char *sh)
{
	name = new char[strlen(n) + 1];
	interest = new char[strlen(ep) + 1];
	src_hint = new char[strlen(sh) + 1];
	strcpy(name, n);
	strcpy(interest, ep);
	strcpy(src_hint, sh);
	next = NULL;
}

list_node::~list_node()
{
	if(next)
		delete next;
	delete[] name;
	delete[] interest;
	delete[] src_hint;
}

/* This function reads components from a string separated by plings.
	It returns NULL at the end of the string: */

const char *read_cpt(const char *s, char *buf)
{
	int len;
	const char *t;
	t = s;

	if(s == NULL)
		return NULL; // Nope, won't read past the end of string!
		
	if(*t == '\0')
		return NULL;
	
	while(*t != '!' && *t != '\0')
		t++;
	
	len = t - s;
	for(int i = 0; i < len; i++)
		buf[i] = s[i];
	buf[len] = '\0';
	
	if(*t == '!')
		return t + 1;
	else
		return t; // Next read will encounter end of string
}

list_node *scop_list(int sock, int *count)
{
	const char *buf, *pos;
	char *name, *interest, *src_hint;
	list_node *a = NULL, *ln;
	
	*count = 0;
	
	buf = "list";
	transmit(sock, buf);
	
	buf = read_protocol(sock);
	if(buf == NULL) return NULL;
	
	int len = strlen(buf);
	name = new char[len + 1];
	interest = new char[len + 1];
	src_hint = new char[len + 1];
	
	pos = buf;
	while(1)
	{
		pos = read_cpt(pos, name);
		pos = read_cpt(pos, interest);
		pos = read_cpt(pos, src_hint);
		if(pos == NULL)
			break; // Hit end of string before reading all 3 items
		
		(*count)++;
		ln = new list_node(name, interest, src_hint);
		ln->next = a;
		a = ln;
	}
	
	delete[] buf;
	delete[] name;
	delete[] interest;
	delete[] src_hint;
	
	return a;
}

void scop_clear(int sock, const char *endpoint)
{
	char *buf;

	buf = new char[100 + strlen(endpoint)];
	sprintf(buf, "clear %s", endpoint);

	transmit(sock, buf);
	delete[] buf;
}

void scop_set_log(int sock, int log_level)
{
	char *buf;

	buf = new char[100];
	sprintf(buf, "log %d", log_level);

	transmit(sock, buf);
	delete[] buf;
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

	buf = new char[100 + strlen(text)];
	sprintf(buf, "set-cookie %s", text);

	transmit(sock, buf);
	delete[] buf;
}

void scop_set_source_hint(int sock, const char *endpoint)
{
	char *buf;

	buf = new char[100 + strlen(endpoint)];
	sprintf(buf, "set-source-hint %s", endpoint);

	transmit(sock, buf);
	delete[] buf;
}

char *scop_get_plain_cookie(int sock, const char *name)
{
	char *buf;

	buf = new char[100 + strlen(name)];
	sprintf(buf, "get-cookie %s", name);

	transmit(sock, buf);
	delete[] buf;

	buf = read_protocol(sock);
	if(buf == NULL) return NULL;
	
	return buf;
}

int scop_open(const char *remote_hostname, const char *name, int unique)
{
	int sock;
	char *buf;
	
	signal(SIGPIPE, SIG_IGN);
	sock = do_connect(remote_hostname);
	if(sock == -1)
		return -1;
	
	if(name != NULL)
	{
		buf = new char[strlen(name) + 100];
		
		if(unique)
			scop_clear(sock, name);
		
		sprintf(buf, "register %s", name);
		
		transmit(sock, buf);		
		delete[] buf;
	}
	
	return sock;
}

void scop_listen(int sock, const char *interest, int unique)
{
	char *buf;

	if(unique)
		scop_clear(sock, interest);
	
	buf = new char[100 + strlen(interest)];
	sprintf(buf, "listen %s", interest);

	transmit(sock, buf);
	delete[] buf;
}

/* Networking code */

// Solaris:
#ifndef INADDR_NONE
#define INADDR_NONE -1
#endif

static int separator_pos(const char *name)
// Returns -1 if no seperator, more than one separator, or incorrectly placed
{
	int len = strlen(name);
	int pos = -1;
	for(int i = 0; i < len; i++)
	{
		if(name[i] == ':')
		{
			if(i == len - 1)
				return -1;
			if(pos != -1)
				return -1;
			pos = i;
		}
	}
	return pos;
}

static int do_connect(const char *remote_hostname)
{
	struct hostent *phe;
	struct servent *pse;
	struct protoent *ppe;
	struct sockaddr_in remote_addr;
	int sock;
	const char *transport = "tcp";
	const char *service = SERVICE_NAME;
	int override_port;
	char *target_host;
	
	memset(&remote_addr, 0, sizeof(remote_addr));
	remote_addr.sin_family = AF_INET;

	if(remote_hostname == NULL || remote_hostname[0] == '\0')
	{
		// No host or port specified:
		override_port = -1;
		target_host = new char[20];
		strcpy(target_host, "localhost");
	}
	else
	{
		int pos = separator_pos(remote_hostname);
		target_host = new char[strlen(remote_hostname) + 20];
		if(pos == 0)
		{
			// Port number only (no host specified):
			override_port = atoi(remote_hostname + 1);
			strcpy(target_host, "localhost");
		}
		else if(pos == -1)
		{
			// Host only (no port number):
			override_port = -1;
			strcpy(target_host, remote_hostname);
		}
		else
		{
			// Host and port number:
			override_port = atoi(remote_hostname + pos + 1);
			strcpy(target_host, remote_hostname);
			target_host[pos] = '\0';
		}
	}

	if(override_port != -1)
		remote_addr.sin_port = htons(override_port);
	else if((pse = getservbyname(service, transport)) != NULL)
		remote_addr.sin_port = pse->s_port;
	else
		remote_addr.sin_port = htons(FALLBACK_PORT);
	
	// Map host name to IP address, allowing for dotted decimal:
	if((phe = gethostbyname(target_host)) != NULL)
	{
		memcpy(&remote_addr.sin_addr, phe->h_addr, phe->h_length);
	}
	else if((remote_addr.sin_addr.s_addr = inet_addr(target_host))
		== INADDR_NONE)
	{
		log("Can't get \"%s\" host entry", target_host);
		return -1;
	}
	delete[] target_host;
	
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
	
	// printf("DO CONNECT 4\n"); fflush(stdout);
	// Connect the socket:
	if(connect(sock, (struct sockaddr *)&remote_addr, sizeof(remote_addr)) < 0)
	{
		log("Can't connect to %s: %s", remote_hostname, strerror(errno));
		return -1;
	}
	// printf("DO CONNECT 5\n"); fflush(stdout);
	return sock;
}

int write_header(int sock, int len)
{
	char buf[21];
	unsigned int version, mask;
	int shift;

	strcpy(buf, "sCoP ");
	version = SCOP_MAJOR_VERSION << 16;
	version += SCOP_MINOR_VERSION << 8;
	version += SCOP_RELEASE_NUMBER;
		
	mask = 0xF00000;
	shift = 20;
	for(int i = 0; i < 6; i++)
	{
		buf[5 + i] = hex((version & mask) >> shift);
		mask >>= 4;
		shift -= 4;
	}
	buf[11] = ' ';
	
	mask = 0xF0000000;
	shift = 28;
	for(int i = 0; i < 8; i++)
	{
		buf[12 + i] = hex((len & mask) >> shift);
		mask >>= 4;
		shift -= 4;
	}
	buf[20] = ' ';
	
	int bytes = write(sock, buf, 21);
	return bytes;
}

char *read_protocol(int sock)
{
	char header[21], *buf;
	int len = 0, version = 0;
	int shift;
	
	if(fixed_read(sock, header, 21) == -1)
		return NULL;
	
	if(strncmp(header, "sCoP", 4))
	{
		log("Protocol magic mismatch");
		return NULL;
	}
	
	shift = 20;
	for(int i = 0; i < 6; i++)
	{
		version += dec(header[5 + i]) << shift;
		shift -= 4;
	}
	int major_version = (version & 0xFF0000) >> 16;
	int minor_version = (version & 0x00FF00) >> 8;
	if(major_version != SCOP_MAJOR_VERSION ||
			minor_version != SCOP_MINOR_VERSION)
	{
		log("Protocol version mismatch");
		return NULL;
	}
	
	shift = 28;
	for(int i = 0; i < 8; i++)
	{
		len += dec(header[12 + i]) << shift;
		shift -= 4;
	}
	buf = new char[len + 1];
	if(fixed_read(sock, buf, len) == -1)
	{
		delete[] buf;
		return NULL;
	}
	buf[len] = '\0';
	return buf;
}

static void log(const char *format, ...)
{
	va_list args;
	char c[MAX_ERR_LEN], d[MAX_ERR_LEN + 100];
	uid_t uid = geteuid();
	char *filename;

	FILE *fp = NULL;
	filename = getenv("SCOP_LOGFILE");
	if(filename)
	{
		fp = fopen(filename, "a");
	}
	else if(uid != 0)
	{
		/* The environment variable SCOP_LOGFILE isn't set, however we're
			not root, hence logging via syslog may be unreadable by our user.
			Therefore we use the default ~/.scoplog instead: */
		struct passwd *pw;
		pw = getpwuid(uid);
		filename = new char[strlen(pw->pw_dir) + 20];
		sprintf(filename, "%s/.scoplog", pw->pw_dir);
		fp = fopen(filename, "a");
		delete[] filename;
	}
		
	va_start(args, format);
	vsnprintf(c, MAX_ERR_LEN, format, args);
	va_end(args);
	c[MAX_ERR_LEN - 1] = '\0';
	
	if(fp != NULL)
	{
		time_t t = time(NULL);
		char *timestr = ctime(&t);
		strcpy(d, timestr);
		d[strlen(d) - 6] = '\0';
		strcat(d, " [scoplib] ");
		strcat(d, c);
		strcat(d, "\n");
		fwrite(d, strlen(d), 1, fp);
		fclose(fp);
	}
	else
	{
		sprintf(d, "[scoplib] %s\n", c);
		syslog(LOG_INFO, d);
	}
}

static const int warn_partial_reads = 0;

int fixed_read(int sock, char *buf, int nbytes)
{
	int remain = nbytes;
	char *pos = buf;
	int amount;
	
	while(remain > 0)
	{
		amount = read(sock, pos, remain);
		if(warn_partial_reads && amount < remain && amount > 0)
		{
			char c = *pos;
			log("Possible protocol error: read only %d of %d bytes, "
					"first value %d", amount, remain, (int)c);
		}
		if(amount <= 0)
			return -1; // EOF or error
		remain -= amount;
		pos += amount;
	}
	return 0;
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

int read_int(int sock)
{
	unsigned char c[8];
	int n = 0;
	
	if(fixed_read(sock, (char *)c, 8) < 0)
		return -1;
	
	int shift = 28;
	for(int i = 0; i < 8; i++)
	{
		n += dec(c[i]) << shift;
		shift -= 4;
	}	
	return n;
}
