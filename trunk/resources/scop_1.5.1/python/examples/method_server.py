#!/usr/bin/python
# method_server.py - DMI - 11-11-03

import scop, sys

invocations = 0

def cent_to_faren(c):
	global invocations
	invocations += 1
	return (9.0 * c / 5.0) + 32.0

def faren_to_cent(f):
	global invocations
	invocations += 1
	return (f - 32.0) * 5.0 / 9.0

sock = scop.scop_open("localhost", "method_server")
while 1:
	v = scop.scop_get_request(sock);
	method, args = v
	if method == "ctof":
		w = cent_to_faren(args)
	elif method == "ftoc":
		w = faren_to_cent(args)
	elif method == "stats":
		w = invocations
	else:
		sys.exit()
	scop.scop_send_reply(sock, w)
scop.scop_close(sock)
