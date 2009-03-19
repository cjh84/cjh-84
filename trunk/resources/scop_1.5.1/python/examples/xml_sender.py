#!/usr/bin/python
# xml_sender.py - DMI - 11-11-03

import scop

def marshall(dict):
	l = []
	keys = dict.keys()
	for k in keys:
		l.append([k, dict[k]])
	return l

ab = { "Poirot": "Belgium",
       "Morse": "Oxford, UK",
       "Danger Mouse": "London, UK" }
v = marshall(ab)
sock = scop.scop_open("localhost", "xml_sender")
scop.scop_send_struct(sock, "xml_receiver", v)
scop.scop_close(sock)
