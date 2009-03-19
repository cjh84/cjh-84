#!/usr/bin/python
# xml_server.cpp - DMI - 11-11-03

import scop

def combi(n, k):
	result = 1;
	if k > n or k < 0:
		return 0	
	for i in range(k):
		result *= n - i
	for i in range (1, k + 1):
		result /= i
	return result

sock = scop.scop_open("localhost", "xml_server")
while 1:
	v = scop.scop_get_request(sock)
	w = combi(v[0], v[1])
	scop.scop_send_reply(sock, w)
scop.scop_close(sock)
