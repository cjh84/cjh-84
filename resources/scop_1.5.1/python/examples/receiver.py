#!/usr/bin/python
# receiver.py - DMI - 29-10-03

import scop, sys

sock = scop.scop_open("localhost", "receiver")
if sock == None:
	print "Error on open"
	sys.exit()
while 1:
	msg, rpc_flag = scop.scop_get_message(sock)
	print "Received <" + msg + ">"
	if msg == "quit":
		break
scop.scop_close(sock)
