#!/usr/bin/python
# status.py - DMI - 8-11-03

import scop

sock = scop.scop_open("localhost", "status")
v = scop.scop_list(sock)
print len(v), "clients connected."
for tuple in v:
	name, interest, src_hint = tuple
	print "Client connection <" + name + "> listening to <" + interest + \
		">, source hint <" + src_hint + ">"
scop.scop_close(sock)
