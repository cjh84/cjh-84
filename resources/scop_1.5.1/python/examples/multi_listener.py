#!/usr/bin/python
# multi_listener.cpp - DMI - 7-11-03

# Usage: multi_listener [ <source-one> <source-two> ]
#    (default sources are "news" and "updates")

import scop, sys, select

sock = []
for i in range(2):
	sock.append(scop.scop_open("localhost", "multi_listener"))
	
if len(sys.argv) == 3:
	scop.scop_listen(sock[0], argv[1])
	scop.scop_listen(sock[1], argv[2])
else:
	scop.scop_listen(sock[0], "news")
	scop.scop_listen(sock[1], "updates")

while 1:
	read_fds = [sock[0], sock[1]]
	r, w, e = select.select(read_fds, [], [])
	for fd in r:
		msg, rpc_flag = scop.scop_get_message(fd)
		print "Received <" + msg + "> from ",
		if fd == sock[1]:
			print "updates"
		else:
			print "news"

