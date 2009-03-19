#!/usr/bin/python
# rtt_client.cpp - DMI - 8-11-03

# Usage: rtt_client [ <iterations> ]   (default is 1000)

import scop, sys, time

REPEATS = 1000

if len(sys.argv) == 2:
	REPEATS = int(sys.argv[1])
sock = scop.scop_open("localhost", "rtt_client")

tv_start = time.time()
for i in range(REPEATS):
	reply = scop.scop_rpc(sock, "server", "test")
	if reply != "tteesstt":
		print "Message error."
		sys.exit()
tv_end = time.time()

diff = tv_end - tv_start
print str(REPEATS) + " round trips in " + str(diff) + " seconds."
print "Time each = " + str(diff * 1000000 / REPEATS) + " us."
print str(REPEATS / diff) + " iterations per second."
scop.scop_close(sock)
