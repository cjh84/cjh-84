#!/usr/bin/python
# Usage: relay [norobot]

import scop, sys
from pyrobot import *

robotmode = True
if len(sys.argv) > 1 and sys.argv[1] == "norobot":
	robotmode = False

# sock = scop.scop_open("www.srcf.ucam.org", "mort")
sock = scop.scop_open("localhost", "mort")
if sock == None:
	print "Cannot connect to scopserver"
	sys.exit()
scop.scop_listen(sock, "p1ctrl")

if robotmode == True:
	r = Roomba()
	r.sci.Wake()
	r.Control()
	time.sleep(0.25)

while 1:
	msg, rpc_flag = scop.scop_get_message(sock)
	# print "Received <" + msg + ">"
	if msg == "f":
		print "Drive forward"
		if robotmode == True:
			r.DriveStraight(VELOCITY_FAST)
			time.sleep(1.0)
			r.Stop()
	elif msg == "l":
		print "Turn left"
		if robotmode == True:
			r.TurnInPlace(VELOCITY_FAST, 'ccw')
			time.sleep(0.5)
			r.Stop()
	elif msg == "r":
		print "Turn right"
		if robotmode == True:
			r.TurnInPlace(VELOCITY_FAST, 'cw')
			time.sleep(0.5)
			r.Stop()
scop.scop_close(sock)
