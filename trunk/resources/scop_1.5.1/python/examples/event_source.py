#!/usr/bin/python
# event_source.py - DMI - 7-11-03

# Usage: event_source [ <source> ]   (default source is "news")

import scop, sys, time

count = 1
sock = scop.scop_open("localhost", "event_source")
if sock == None:
	print "Error on open"
	sys.exit()
if len(sys.argv) > 1:
	source = sys.argv[1]
else:
	source = "news"
scop.scop_set_source_hint(sock, source)
while 1:
	msg = "Item " + str(count)
	scop.scop_emit(sock, msg)
	count = count + 1
	time.sleep(1)
