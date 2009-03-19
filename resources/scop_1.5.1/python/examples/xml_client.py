#!/usr/bin/python
# xml_client.py - DMI - 11-11-03

# Usage: xml_client [<n> <k>]    (default values n = 4, k = 2)

import scop, sys

if len(sys.argv) != 3:
	n, k = 4, 2
else:
	n, k = int(sys.argv[1]), int(sys.argv[2])
sock = scop.scop_open("localhost", "xml_client")
v = [n, k]
w = scop.scop_rpc(sock, "xml_server", v)
print str(n) + " choose " + str(k) + " equals " + str(w) + "."
scop.scop_close(sock)
