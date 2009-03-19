#!/usr/bin/python
# xml_reciever.py - DMI - 11-11-03

# Usage: xml_reciever [-inspect]

import scop, sys
from scopxml import pretty_print

def extract(v):
	d = {}
	for pair in v:
		key, value = pair
		d[key] = value
	return d

sock = scop.scop_open("localhost", "xml_receiver")
v, rpc_flag = scop.scop_get_struct(sock)
if len(sys.argv) == 2 and sys.argv[1] == "-inspect":
	s = pretty_print(v)
	print s
ab = extract(v)
print ab
scop.scop_close(sock)
