#!/usr/bin/python
# method_client.py - DMI - 11-11-03

import scop

def cent_to_faren(sock, c):
	return scop.scop_rpc(sock, "method_server", c, "ctof")

def faren_to_cent(sock, f):
	return scop.scop_rpc(sock, "method_server", f, "ftoc")

def count_uses(sock):
	return scop.scop_rpc(sock, "method_server", None, "stats")

sock = scop.scop_open("localhost", "method_client")
print str(0.0) + " deg C = " + str(cent_to_faren(sock, 0.0)) + " deg F."
print str(20.0) + " deg C = " + str(cent_to_faren(sock, 20.0)) + " deg F."
print str(60.0) + " deg F = " + str(faren_to_cent(sock, 60.0)) + " deg C."
print "The server has been accessed " + str(count_uses(sock)) + " times."
scop.scop_close(sock)
