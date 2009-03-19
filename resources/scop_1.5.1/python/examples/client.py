#!/usr/bin/python
# client.py - DMI - 8-11-03

# Usage: client [ <query> ]   (default query is "Hello world!")

import scop, sys

if len(sys.argv) > 1:
	query = sys.argv[1]
else:
	query = "Hello world!"
sock = scop.scop_open("localhost", "client")
reply = scop.scop_rpc(sock, "server", query)
print "Query <" + query + ">, Reply <" + reply + ">"
scop.scop_close(sock)
