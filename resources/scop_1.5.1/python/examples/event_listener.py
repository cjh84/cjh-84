#!/usr/bin/python
# event_listener.py - DMI - 7-11-03

import scop, sys

sock = scop.scop_open("localhost", "event_listener")
scop.scop_listen(sock, "news")
while 1:
	msg, rpc_flag = scop.scop_get_message(sock)
	print "Received <" + msg + ">"
