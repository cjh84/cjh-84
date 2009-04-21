#!/usr/bin/python

import scop, sys, select, time, os
#from pyrobot import *

# Constants:
MAXSPEED = 5
TURNTIME = 1.0; # Turn for one second per turn instruction
ROBOTS = {'mort': 1, 'princess': 2}

def update(speed, turn):
	if robotmode == False:
		return
	if speed == 0:
		if turn > 0:
			radius = -1
			vel = (VELOCITY_MAX * 3) / MAXSPEED
		elif turn < 0:
			radius = 1
			vel = (VELOCITY_MAX * 3) / MAXSPEED
		else:
			vel = 0
			radius = RADIUS_STRAIGHT
	else:
		vel = (VELOCITY_MAX * speed) / MAXSPEED
		if turn > 0:    # Right
			radius = -150
		elif turn < 0: # Left
			radius = 150
		else:
			radius = RADIUS_STRAIGHT
	r.Drive(vel, radius)

robotmode = True
scophost = "www.srcf.ucam.org"
host = os.getenv("HOST")

if host is None:
    print "Robot host name is not set."
    print "Valid names are:",
    for k in ROBOTS.keys(): print k,
    sys.exit()

if host in ROBOTS:
    playerno = ROBOTS[host]
else:
    print "HOST environment variable must be one of:",
    for k in ROBOTS.keys(): print k,
    sys.exit()

# p1ctrl or p2ctrl
endpoint = "p" + str(playerno) + "ctrl"
print("Using endpoint " + endpoint)

for i in range(1, len(sys.argv)):
	if sys.argv[i] == "norobot":
		robotmode = False
	elif sys.argv[i] == "localscop":
	    scophost = "localhost"
	else:
		print "Usage: relay [norobot] [localscop]"
		sys.exit()

sock = scop.scop_open(scophost, host)
if sock == None:
	print "Cannot connect to scopserver"
	sys.exit()
scop.scop_listen(sock, endpoint)

speed = 0
turn = 0

if robotmode == True:
	r = Roomba()
	r.sci.Wake()
	r.Control()
	time.sleep(0.25)

while 1:

	timeout = 0.0
	if turn != 0:
		timeout = alarm - time.time()
		if timeout <= 0.0:
		    # Finished turning
			turn = 0
			update(speed, turn)
			timeout = 0.0
	if(timeout == 0.0):
		msg, rpc_flag = scop.scop_get_message(sock)
	else:
		read_fds = [sock]
		rd, wr, ex = select.select(read_fds, [], [], timeout)
		if rd == []:
			turn = 0
			update(speed, turn)
			msg = "-"
		else:
			msg, rpc_flag = scop.scop_get_message(sock)
			
	if msg == "s":
		print "Start / stop"
		if speed == 0:
			speed = 1
		else:
			speed = 0
			turn = 0
			# r.Stop()
		update(speed, turn)
	
	elif msg == "a":
		print "Accelerate"
		if speed < MAXSPEED:
			speed = speed + 1
		update(speed, turn)
		
	elif msg == "d":
		print "Deccelerate"
		if speed > -MAXSPEED:
			speed = speed - 1
		update(speed, turn)
		
	elif msg == "l":
		print "Turn left"
		# If it's already turning add one more unit of TURNTIME to alarm
		if turn < 0:
			alarm = alarm + TURNTIME
		elif turn > 0:
			turn = 0
		else:
			turn = -1
			alarm = time.time() + TURNTIME
		update(speed, turn)
		
	elif msg == "r":
		print "Turn right"
		if turn > 0:
			alarm = alarm + TURNTIME
		elif turn < 0:
			turn = 0
		else:
			turn = 1
			alarm = time.time() + TURNTIME
		update(speed, turn)

scop.scop_close(sock)

