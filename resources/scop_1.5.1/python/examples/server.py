#!/usr/bin/python
# server.py - DMI - 8-11-03

import scop

sock = scop.scop_open("localhost", "server")
while 1:
	query, rpc_flag = scop.scop_get_message(sock)
	length = len(query)
	reply = ""
	for i in range(length):
		reply = reply + query[i] + query[i]
	scop.scop_send_reply(sock, reply)
scop.scop_close(sock)
