/* scopserver.cpp - DMI - 24-9-2001

Copyright (C) 2001-06 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <syslog.h>
#include <pwd.h>
#include <grp.h>
#include <errno.h>
#include <time.h>

#include <sys/time.h>		
#include <sys/types.h>
#include <sys/socket.h>

#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <sys/time.h>

#include "scop.h"
#include "datatype.h"

#ifndef INADDR_NONE
#define INADDR_NONE 0xffffffff
#endif

#define MAX_ERR_LEN 100

extern int errno;

const char *HOST_FILE = "/etc/scophosts";
int OVERRIDE_PORT = -1;

class client;

// Constants:
			
const char *SERVICE_NAME = "scop";
const char *remote_hostname = "localhost";
const char *UNNAMED = "Unnamed";
const char *SCOP_RPC_ERROR = "scop-rpc-error";

int debug = 0;

// Globals:

client *client_list = NULL;

SCOP_linefile *auth_hosts;

int access_control = 1;
uid_t first_uid;

// Functions:

void process_command(client *c);
void parse_args(int argc, char **argv);
void mutual_exclusion();
static void log(const char *format, ...);
void do_send(char *endpoint, char *msg, int ack_sock);

void sock_nodelay(int sock);
void sock_reuseaddr(int sock);
int passivesock(const char *service);
int connectsock(const char *remote_hostname, const char *service);

int write_header(int sock, int len);
char *read_protocol(int sock);

// Lowlevel:
int write_int(int sock, int n);
int read_int(int sock);
int fixed_read(int sock, char *buf, int nbytes);
char hex(int d);
int dec(char c);

int post_message(client *c, const char *msg);
int post_message(int sock, const char *msg);

// Helper class:

class client
{
	public:
			
		int sock;
		char *name, *interest, *source_hint, *cookie;
		int id;
		
		client *prev, *next;
		
		client(int s);
		~client();
		
		void set_name(char *s);
		void set_interest(char *s);
		void set_source_hint(char *s);
		void set_cookie(char *s);
		
		int rpc_to_id; // Unique ID of client we are calling, 0 if not in a call
		int rpc_to_seq; // Sequence of reply from target, ignored if not in call
		
		int rpc_next_call; // Sequence number for next call to this server
		int rpc_next_reply; // Sequence number of next reply from this server
				
	private:
			
		static int unique_id;
};

client::client(int s)
{
	sock = s;
	name = interest = source_hint = cookie = NULL;
	id = unique_id++;
	rpc_to_id = 0;
	
	rpc_next_call = 1;
	rpc_next_reply = 1;
}

void client::set_name(char *s)
{
	if(name) delete[] name;
	name = new char[strlen(s) + 1];
	strcpy(name, s);
}

void client::set_interest(char *s)
{
	if(interest) delete[] interest;
	interest = new char[strlen(s) + 1];
	strcpy(interest, s);
}

void client::set_source_hint(char *s)
{
	if(source_hint) delete[] source_hint;
	source_hint = new char[strlen(s) + 1];
	strcpy(source_hint, s);
}

void client::set_cookie(char *s)
{
	if(cookie) delete[] cookie;
	cookie = new char[strlen(s) + 1];
	strcpy(cookie, s);
}

int client::unique_id = 1;

client::~client()
{
	close(sock);
	if(name) delete[] name;
	if(interest) delete[] interest;
	if(source_hint) delete[] source_hint;
	if(cookie) delete[] cookie;
}

client *add_client(int sock)
{
	client *c = new client(sock);
	if(client_list)
		client_list->prev = c;
	c->next = client_list;
	c->prev = NULL;
	client_list = c;
	return c;
}

void remove_client(client *c)
{
	if(c == NULL)
	{
		// Not found
		return;
	}
	client *left = c->prev;
	client *right = c->next;
	if(right != NULL)
	{
		right->prev = left;
	}
	if(left != NULL)
	{
		left->next = right;
	}
	else
		client_list = right;
	delete c;
}

int count_clients()
{
	int count = 0;
	client *c = client_list;
	while(c != NULL)
	{
		count++;
		c = c->next;
	}
	return count;
}

int sequal(const char *s1, const char *s2)
{
	if(s1 == NULL || s2 == NULL)
		return 0;
	if(!strcmp(s1, s2))
		return 1;
	return 0;
}

void dump_state(int sock)
{
	client *c;
	char *name, *interest, *src_hint;
	SCOP_StringBuf sb;

	if(debug)
		log("Dumping state");
	
	c = client_list;
	while(c != NULL)
	{
		if(c->sock != sock) // Again, that's the process doing the list
		{		
			name = c->name == NULL ? (char *)(UNNAMED) : c->name;
			interest = c->interest;
			src_hint = c->source_hint;

			sb.cat(name); sb.cat('!');
			if(interest) sb.cat(interest); sb.cat('!');
			if(src_hint) sb.cat(src_hint); sb.cat('!');
		}
		c = c->next;
	}
	
	char *s = sb.compact();
	post_message(sock, s);
	delete[] s;
}

void set_log_level(char *buf)
{
	char *level = new char[strlen(buf)];
	
	if(sscanf(buf, "log %s", level) != 1)
	{
		log("Error parsing message");
		delete[] level;
		return;
	}
	debug = atoi(level) == 1 ? 1 : 0;
	if(debug)
		log("Logging activated");
	else
		log("Logging deactivated");
}

int post_message(client *c, const char *msg)
{
	if(post_message(c->sock, msg) == 0)
		return 0;
	
	if(debug)
	{
		// Truncate message for log purposes
		log("Sent message, target: %s, body: %.30s", c->name, msg);
	}
	return 1;
}

int post_message(int sock, const char *msg)
{
	int reply_len = strlen(msg);
	
	if(write_header(sock, reply_len) < 21)
		return 0;
	if(write(sock, msg, reply_len) < reply_len)
		return 0;
	
	return 1;
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

void rpc_call(char *buf, client *caller)
{
	char *endpoint = new char[strlen(buf)];
	char *args = new char[strlen(buf) + 50];
	client *target;
	int pos;
	
	if(caller->rpc_to_id != 0)
	{
		// Error - still waiting for previous RPC:
		log("Error: Attempt to RPC twice without reply");
		post_message(caller, SCOP_RPC_ERROR);
		delete[] endpoint;
		delete[] args;
		return;
	}
	
	if(sscanf(buf, "call %[^!]! %n", endpoint, &pos) < 1)
	{
		log("Error parsing message");
		post_message(caller, SCOP_RPC_ERROR);
		delete[] endpoint;
		delete[] args;
		return;
	}
	strcpy(args, "scop-rpc-call ");
	strcat(args, buf + pos);
	
	client *c = client_list;
	target = NULL;
	while(c != NULL)
	{
		if(sequal(c->name, endpoint) || sequal(c->interest, endpoint))
		{
			if(target)
			{
				// Error - endpoint is not unique
				log("Error: RPC endpoint %s not unique", endpoint);
				post_message(caller, SCOP_RPC_ERROR);
				delete[] endpoint;
				delete[] args;
				return;
			}
			target = c;
		}
		c = c->next;
	}
	if(target == NULL)
	{
		// Error - no such server:
		log("Error: No such RPC server '%s'", endpoint);
		post_message(caller, SCOP_RPC_ERROR);
		delete[] endpoint;
		delete[] args;
		return;
	}
	post_message(target, args);
	
	caller->rpc_to_id = target->id;
	caller->rpc_to_seq = target->rpc_next_call;
	target->rpc_next_call++;

	delete[] endpoint;
	delete[] args;
}

void rpc_reply(char *buf, client *server)
{
	char *results = buf + strlen("reply ");
	int id = server->id;
	int seq = server->rpc_next_reply;
	server->rpc_next_reply++;
	
	client *c = client_list;	
	while(c != NULL)
	{
		if(c->rpc_to_id == id && c->rpc_to_seq == seq)
			break;
		c = c->next;
	}
	if(c == NULL)
	{
		// The client which made this RPC call has vanished; drop the result:
		log("RPC client departed before reply");
		return;
	}
	
	post_message(c, results);
	c->rpc_to_id = 0;
}

void emit(char *buf, client *c)
{
	char *msg = buf + strlen("emit ");
	char *target = c->source_hint;
	int ack_sock = c->sock;
	
	if(target == NULL)
	{
		log("Can't emit without a source_hint");
		write_int(ack_sock, -1);
		return;
	}
	
	do_send(target, msg, ack_sock);
}

void send_message(char *buf, int ack_sock)
{
	int matches;
	char *endpoint = new char[strlen(buf)];
	char *msg = new char[strlen(buf)];
	int pos;
	
	if(ack_sock == -1)
		matches = sscanf(buf, "message %[^!]! %n", endpoint, &pos);
	else
		matches = sscanf(buf, "verify %[^!]! %n", endpoint, &pos);
	
	if(matches < 1 || matches > 2)
	{
		log("Error parsing message (%d fields matched)", matches);
		if(ack_sock != -1)
			write_int(ack_sock, VERIFY_SYNTAX_ERROR);
		
		delete[] endpoint;
		delete[] msg;
		return;
	}
	strcpy(msg, buf + pos);	

	do_send(endpoint, msg, ack_sock);
			
	delete[] endpoint;
	delete[] msg;
}

void do_send(char *endpoint, char *msg, int ack_sock)
{
	int sent_ok = 0;
	
	client *c = client_list;
	while(c != NULL)
	{
		if((c->rpc_to_id == 0) &&
				(sequal(c->name, endpoint) || sequal(c->interest, endpoint)))
		{
			// Found client, but connection may be dead...
			if(!post_message(c, msg))
			{
				log("Client %s has gone!", c->name);
				remove_client(c);
			}
			else
			{
				sent_ok++;
			}
		}
		c = c->next;
	}
	
	if(ack_sock != -1)
		write_int(ack_sock, sent_ok);
	
	if(sent_ok == 0 && debug)
		log("No client registered for endpoint %s", endpoint);
}

void query_client(char *buf, int sock)
{
	char *endpoint = new char[strlen(buf)];
	int found = 0;
	client *c;
	
	if(sscanf(buf, "query %[^!]", endpoint) != 1)
	{
		log("Error parsing message");
		delete[] endpoint;
		return;
	}
	
	c = client_list;
	while(c != NULL)
	{
		if(sequal(c->name, endpoint) || sequal(c->interest, endpoint))
			found++;
		c = c->next;
	}
	
	write_int(sock, found);
	
	delete[] endpoint;
}

void remove_all(char *endpoint)
{
	client *c = client_list, *next_c;

	while(c != NULL)
	{
		next_c = c->next;
		if(sequal(c->name, endpoint) || sequal(c->interest, endpoint))
			remove_client(c);
		c = next_c;
	}
}

void clear_name(char *buf)
{
	char *endpoint = new char[strlen(buf)];
	
	if(sscanf(buf, "clear %[^!]", endpoint) != 1)
	{
		log("Error parsing clear");
		delete[] endpoint;
		return;
	}
	remove_all(endpoint);
	
	delete[] endpoint;
}

void set_source_hint(char *buf, client *c)
{
	char *endpoint;

	endpoint = buf + strlen("set-source-hint ");
	c->set_source_hint(endpoint);
}

void set_cookie(char *buf, client *c)
{
	char *cookie;

	cookie = buf + strlen("set-cookie ");
	if(strlen(cookie) < 4096)
		c->set_cookie(cookie);
	// Silently fail to replace if new cookie is too long.
}

void get_cookie(char *buf, int sock)
{
	/* Note: we don't check if the name is unique, just return the
		first match found. */
	
	char *name = new char[strlen(buf)];
	int matches;
	client *c;

	matches = sscanf(buf, "get-cookie %[^!]", name);
	if(matches != 1)
	{
		log("Error parsing get-cookie");
		delete[] name;
		return;
	}
	
	c = client_list;
	while(c != NULL)
	{
		if(sequal(c->name, name))
			break;
		c = c->next;
	}
	if(c != NULL && c->cookie != NULL)
	{
		post_message(sock, c->cookie);
	}
	
	delete[] name;
}

void start_listening(char *buf, client *c)
{
	char *interest = new char[strlen(buf)];
	int matches;

	matches = sscanf(buf, "listen %[^!]", interest);
	if(matches != 1)
	{
		log("Error parsing listen");
		delete[] interest;
		return;
	}
	
	c->set_interest(interest);
	if(debug)
		log("Client %s listening to %s", c->name, interest);
	delete[] interest;
}

void register_client(char *buf, client *c)
{
	char *name = new char[strlen(buf)];
	int matches;

	matches = sscanf(buf, "register %[^!]", name);
	if(matches != 1)
	{
		log("Error parsing registration");
		delete[] name;
		return;
	}
	
	c->set_name(name);
	if(debug)
		log("Client %s registered", name);
	delete[] name;
}

void drop_privileges()
{
	struct passwd *pw;
	struct group *gr;
	
	if(first_uid != 0)
		return;
	pw = getpwnam("nobody");
	if(pw == NULL)
	{
		log("Warning: can't find user nobody - unable to drop root");
		return;
	}
	gr = getgrnam("nobody");
	if(gr == NULL)
	{
		log("Warning: can't find group nobody - unable to drop root");
		return;
	}
	setgid(gr->gr_gid);
	setuid(pw->pw_uid);
}

void reconfigure(int signum)
{
	/* Warning: this signal handler function (for SIGHUP) is currently
		unsafe because it uses library routines which should not be
		called asynchronously... */

	if(!access_control)
		return;	
	delete auth_hosts;
	auth_hosts = new SCOP_linefile(HOST_FILE);
	if(!auth_hosts->valid())
		log("Error: can't open authorised hosts file %s", HOST_FILE);
	log("Server restarted (%d authorised host%s", auth_hosts->count(),
			auth_hosts->count() == 1 ? ")" : "s)");
}

int main(int argc, char **argv)
{
	char local_ip_address[4], *remote_ip_address;
	int master_sock, dyn_sock;
	struct sockaddr_in remote_addr;
	int addr_len;
	client *c;

	fd_set read_fds;
	int max_fd;
	int done;

	first_uid = geteuid();
	parse_args(argc, argv);
	if(access_control)
	{
		auth_hosts = new SCOP_linefile(HOST_FILE);
		if(!auth_hosts->valid())
		{
			log("Error: can't open authorised hosts file %s", HOST_FILE);
			exit(0);
		}
	}
	
	mutual_exclusion();
	master_sock = passivesock(SERVICE_NAME);
	drop_privileges();
	if(fork() > 0) exit(0); // Detach
	signal(SIGPIPE, SIG_IGN);
	signal(SIGHUP, reconfigure);
	if(access_control)
		log("Server started (%d authorised host%s", auth_hosts->count(),
				auth_hosts->count() == 1 ? ")" : "s)");
	else
		log("Server started (access control disabled)");
	
	struct hostent *lhe;
	lhe = gethostbyname("localhost");
	memcpy(local_ip_address, lhe->h_addr_list[0], 4);
	
	while(1)
	{
		FD_ZERO(&read_fds);
		max_fd = 0;
		for(c = client_list; c != NULL; c = c->next)
		{
			FD_SET(c->sock, &read_fds);
			if(c->sock > max_fd) max_fd = c->sock;
		}
		FD_SET(master_sock, &read_fds);
		if(master_sock > max_fd) max_fd = master_sock;
		select(max_fd + 1, &read_fds, NULL, NULL, NULL);
		
		done = 0;
		for(c = client_list; c != NULL; c = c->next)
		{
			if(FD_ISSET(c->sock, &read_fds))
			{
				process_command(c);
				/* Quit after servicing just one client, in case it has
					caused the list of clients to change (e.g. clear_name
					or even register could do this): */
				done = 1;
				break;
			}
		}
		if(done)
			continue;
		if(FD_ISSET(master_sock, &read_fds))
		{
			int access_ok;
			
			addr_len = sizeof(remote_addr);
			dyn_sock = accept(master_sock, (struct sockaddr *)(&remote_addr),
					(socklen_t *)(&addr_len));
			if(dyn_sock < 0)
			{
				log("accept failed: %s", strerror(errno));
				exit(0);
			}
			remote_ip_address = (char *)&(remote_addr.sin_addr);

			struct hostent *he;
			he = gethostbyaddr((const char *)&(remote_addr.sin_addr), 4, AF_INET);
			
			access_ok = 0;
			if(!access_control)
				access_ok = 1; // Access control is switched off
			else if(auth_hosts->search(he->h_name))
				access_ok = 1; // Exact match with authorised host name
			else if(auth_hosts->search("localhost"))
			{
				// We're supposed to allow local connections:
				if(!memcmp(local_ip_address, remote_ip_address, 4))
					access_ok = 1; // Local and remote IP address match
			}
						
			if(access_ok)
			{
				sock_nodelay(dyn_sock);
				c = add_client(dyn_sock);
				if(debug)
					log("Authorized connection from %s", he->h_name);
			}
			else
			{
				close(dyn_sock);
				log("Denied connection from %s - unauthorised host!", he->h_name);
			}
		}
	}
	return 0;
}

void terminate()
{
	log("Shutting down...");
	exit(0);
}

int prefix(const char *s, const char *pre)
{
	return strncmp(s, pre, strlen(pre));
}

void process_command(client *c)
{
	char *buf;

	buf = read_protocol(c->sock);
	if(buf == NULL)
	{
		if(c->name != NULL && debug)
			log("Client %s has disconnected", c->name);
		remove_client(c);
		return;
	}

	if(!prefix(buf, "log"))
		set_log_level(buf);
	else if(!prefix(buf, "message"))
		send_message(buf, -1);
	else if(!prefix(buf, "verify"))
		send_message(buf, c->sock);
	else if(!prefix(buf, "emit"))
		emit(buf, c);
	else if(!prefix(buf, "register"))
		register_client(buf, c);
	else if(!prefix(buf, "listen"))
		start_listening(buf, c);
	else if(!prefix(buf, "list"))
		dump_state(c->sock);
	else if(!prefix(buf, "call"))
		rpc_call(buf, c);
	else if(!prefix(buf, "reply"))
		rpc_reply(buf, c);
	else if(!prefix(buf, "query"))
		query_client(buf, c->sock);
	else if(!prefix(buf, "clear"))
		clear_name(buf);
	else if(!prefix(buf, "set-cookie"))
		set_cookie(buf, c);
	else if(!prefix(buf, "get-cookie"))
		get_cookie(buf, c->sock);
	else if(!prefix(buf, "set-source-hint"))
		set_source_hint(buf, c);
	else if(!prefix(buf, "terminate"))
		terminate();
	else if(!prefix(buf, "reconfigure"))
		reconfigure(0);
	else
		log("Invalid protocol");

	delete[] buf;
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

// Initialisation:

void usage(void)
{
	printf("Usage: scopserver [-f <hostfile>] [--allhosts] [-p <portnumber>]\n");
	exit(0);
}

void parse_args(int argc, char **argv)
{
	for(int i = 1; i < argc; i++)
	{		
		if(!strcmp(argv[i], "-f"))
		{
			if(i == argc - 1)
				usage();
			HOST_FILE = argv[++i];
		}
		else if(!strcmp(argv[i], "-p"))
		{
			if(i == argc - 1)
				usage();
			OVERRIDE_PORT = atoi(argv[++i]);
		}
		else if(!strcmp(argv[i], "--allhosts"))
			access_control = 0;
		else
			usage();
	}
}

void mutual_exclusion()
{
	int sock = connectsock(remote_hostname, SERVICE_NAME);
	if(sock == -1)
		return; // Fine, we're the only one

	close(sock);
	log("Detected another scopserver; aborting");
	exit(0);
}

static void log(const char *format, ...)
{
	va_list args;
	char c[MAX_ERR_LEN], d[MAX_ERR_LEN + 100];
	char *filename;

	FILE *fp = NULL;
	filename = getenv("SCOP_LOGFILE");
	if(filename)
	{
		fp = fopen(filename, "a");
	}
	else if(first_uid != 0)
	{
		/* The environment variable SCOP_LOGFILE isn't set, however we're
			not root, hence logging via syslog may be unreadable by our user.
			Therefore we use the default ~/.scoplog instead: */
		struct passwd *pw;
		pw = getpwuid(first_uid);
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
		strcat(d, " [scopserver] ");
		strcat(d, c);
		strcat(d, "\n");
		fwrite(d, strlen(d), 1, fp);
		fclose(fp);
	}
	else
	{
		sprintf(d, "[scopserver] %s\n", c);
		syslog(LOG_INFO, d);
	}
}

// Networking:

int connectsock(const char *remote_hostname, const char *service)
{
	struct hostent *phe;
	struct servent *pse;
	struct protoent *ppe;
	struct sockaddr_in remote_addr;
	int sock;
	const char *transport = "tcp";
	
	memset(&remote_addr, 0, sizeof(remote_addr));
	remote_addr.sin_family = AF_INET;
	
	if(OVERRIDE_PORT > 0)
		remote_addr.sin_port = htons(OVERRIDE_PORT);
	else if((pse = getservbyname(service, transport)) != NULL)
		remote_addr.sin_port = pse->s_port;
	else
		remote_addr.sin_port = htons(FALLBACK_PORT);
	if(debug)
		log("Service %s at port number %d", service,
				ntohs(remote_addr.sin_port));
	
	// Map host name to IP address, allowing for dotted decimal:
	if((phe = gethostbyname(remote_hostname)) != NULL)
	{
		memcpy(&remote_addr.sin_addr, phe->h_addr, phe->h_length);
	}
	else if((remote_addr.sin_addr.s_addr = inet_addr(remote_hostname))
		== INADDR_NONE)
	{
		log("Can't get \"%s\" host entry", remote_hostname);
		exit(0);
	}
	
	// Map transport protocol name to protocol number:
	if((ppe = getprotobyname(transport)) == 0)
	{
		log("Can't get %s protocol entry", transport);
		exit(0);
	}
	
	// Allocate a socket:
	sock = socket(PF_INET, SOCK_STREAM, ppe->p_proto);
	if(sock < 0)
	{
		log("Can't create socket: %s", strerror(errno));
		exit(0);
	}
	sock_nodelay(sock);
	
	// Connect the socket:
	if(connect(sock, (struct sockaddr *)&remote_addr, sizeof(remote_addr)) < 0)
	{
		return -1;
	}
	
	return sock;
}

void sock_nodelay(int sock)
{
	struct protoent *pe = getprotobyname("tcp");
	int opt_true = 1;
	setsockopt(sock, pe->p_proto, TCP_NODELAY, &opt_true, sizeof(int));
}

void sock_reuseaddr(int sock)
{
	int opt_true = 1;
	setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &opt_true, sizeof(opt_true));
}

int passivesock(const char *service)
{
	struct servent *pse;
	struct protoent *ppe;
	struct sockaddr_in local_addr;
	int sock;
	const int qlen = 4;
	const char *transport = "tcp";
	
	memset(&local_addr, 0, sizeof(local_addr));
	local_addr.sin_family = AF_INET;
	local_addr.sin_addr.s_addr = INADDR_ANY;
	
	if(OVERRIDE_PORT > 0)
		local_addr.sin_port = htons(OVERRIDE_PORT);
	else if((pse = getservbyname(service, transport)) != NULL)
		local_addr.sin_port = pse->s_port;
	else
	{
		local_addr.sin_port = htons(FALLBACK_PORT);
		log("Can't get \"%s\" service entry, "
				"falling back on default port (%d)", service, FALLBACK_PORT);
	}
	if(debug)
		log("Service %s at port number %d", service,
				ntohs(local_addr.sin_port));
	
	// Map protocol name to protocol number:
	if((ppe = getprotobyname(transport)) == 0)
	{
		log("Can't get %s protocol entry", transport);
		exit(0);
	}
	
	// Allocate a socket:
	sock = socket(PF_INET, SOCK_STREAM, ppe->p_proto);
	if(sock < 0)
	{
		log("Can't create socket: %s", strerror(errno));
		exit(0);
	}
	sock_reuseaddr(sock);	
	
	// Bind this socket:
	if(bind(sock, (struct sockaddr *)&local_addr, sizeof(local_addr)) < 0)
	{
		log("Can't bind: %s", strerror(errno));
		exit(0);
	}
	if(listen(sock, qlen) < 0)
	{
		log("Can't listen: %s", strerror(errno));
		exit(0);
	}

	return sock;
}

int write_int(int sock, int n)
{
	unsigned char c[8];
	int bytes;
	
	unsigned int mask = 0xF0000000;
	int shift = 28;
	for(int i = 0; i < 8; i++)
	{
		c[i] = hex((n & mask) >> shift);
		mask >>= 4;
		shift -= 4;
	}	
	bytes = write(sock, c, 8);
	return bytes;
}

int fixed_read(int sock, char *buf, int nbytes)
{
	int remain = nbytes;
	char *pos = buf;
	int amount;
	
	while(remain > 0)
	{
		amount = read(sock, pos, remain);
		if(debug && amount < remain && amount > 0)
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
