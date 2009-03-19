#!/usr/bin/python
# sender.py - DMI - 7-11-03

# Usage: sender [ <message> ]   (default message is "Hello world!")

import scop, sys

if len(sys.argv) > 1:
	msg = sys.argv[1]
else:
	msg = "Hello world!"
sock = scop.scop_open("localhost", "sender")
if sock == None:
	print "Error on open"
	sys.exit()
scop.scop_send_message(sock, "receiver", msg)
scop.scop_close(sock)
