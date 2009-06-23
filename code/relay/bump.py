#!/usr/bin/python

import scop, sys, select, time, os
from pyrobot import *

# Constants:

ROBOTS = {'mort': 1, 'princess': 2}

robotmode = True
scophost = os.getenv("SCOPCTRLSERVER", "www.srcf.ucam.org")
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

# p1bump or p2bump
endpoint = "p" + str(playerno) + "bump"
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

scop.scop_set_source_hint(sock, endpoint)

if robotmode == True:
	r = Roomba()
	r.sci.Wake()
	r.Control()
	#time.sleep(0.25)

while 1:
   if robotmode:
      r.sensors.GetAll()
      if r.sensors['bump-left']:
         scop.scop_emit(sock, "L")
      if r.sensors['bump-right']:
         scop.scop_emit(sock, "R")
      time.sleep(0.25)

scop.scop_close(sock)
