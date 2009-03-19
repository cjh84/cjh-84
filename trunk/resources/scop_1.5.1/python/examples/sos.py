#!/usr/bin/python
# sos.py - DMI - 8-11-03

import scop, sys, syslog, os

sock = scop.scop_open("localhost", "sos", 1)
if sock == None:
	print "Can't connect to scopserver."
	sys.exit()

if os.fork() > 0:
	sys.exit() # Detach

while 1:
	buf, rpc_flag = scop.scop_get_message(sock)
	if buf == None:
		syslog.syslog(syslog.LOG_INFO, "Lost connection to scopserver.")
		sys.exit()
	syslog.syslog(syslog.LOG_INFO, buf)
scop.scop_close(sock)
