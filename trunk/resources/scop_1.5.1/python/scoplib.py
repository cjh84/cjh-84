# scoplib.py - DMI - 29-10-03
#
# Copyright (C) 2001-03 David Ingram
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation.

import os, syslog, socket, time

SCOP_MAJOR_VERSION = 1
SCOP_MINOR_VERSION = 2
SCOP_RELEASE_NUMBER = 0

## Networking code:

def transmit(sock, buf):
	write_header(sock, len(buf))
	sock.send(buf)

def do_connect(remote_hostname):
	transport = "tcp"
	service = "scop"
	FALLBACK_PORT = 51234
	try:
		port = getservbyname(service, transport)
	except:
		port = FALLBACK_PORT
	try:
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		s.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
		s.connect((remote_hostname, port))
	except:
		log("Can't connect to " + remote_hostname)
		return None
	return s

def hex(d):
	if 0 <= d <= 9:
		return chr(d + ord('0'))
	if 10 <= d <= 15:
		return chr(d - 10 + ord('A'))
	return '0'

def dec(c):
	asc = ord(c)
	if ord('0') <= asc <= ord('9'):
		return asc - ord('0')
	if ord('A') <= asc <= ord('F'):
		return asc - ord('A') + 10
	return 0

# Note: inefficient string handling in write_header() - FIXME
def write_header(sock, len):
	buf = "sCoP "
	version = SCOP_MAJOR_VERSION << 16
	version += SCOP_MINOR_VERSION << 8
	version += SCOP_RELEASE_NUMBER
	#
	mask = 0xF00000
	shift = 20
	for i in range(6):
		buf = buf + hex((version & mask) >> shift)
		mask >>= 4
		shift -= 4
	buf = buf + ' '
	#
	mask = 0xF0000000L
	shift = 28
	for i in range(8):
		buf = buf + hex((len & mask) >> shift)
		mask >>= 4
		shift -= 4
	buf = buf + ' '
	#
	bytes = sock.send(buf)
	return bytes

def read_protocol(sock):
	len, version = 0, 0
	#
	header = fixed_read(sock, 21)
	if header == None:
		return None
	#
	if cmp(header[:4], "sCoP") != 0:
		log("Protocol magic mismatch")
		return None
	#
	shift = 20
	for i in range(6):
		version += dec(header[5 + i]) << shift
		shift -= 4
	#
	major_version = (version & 0xFF0000) >> 16
	minor_version = (version & 0x00FF00) >> 8
	if major_version != SCOP_MAJOR_VERSION or \
		minor_version != SCOP_MINOR_VERSION:
		log("Protocol version mismatch")
		return None
	#
	shift = 28
	for i in range(8):
		len += dec(header[12 + i]) << shift
		shift -= 4
	#
	buf = fixed_read(sock, len)
	return buf   # May return None if fixed_read() failed

def read_int(sock):
	c = fixed_read(sock, 8)
	if c == None:
		return -1
	shift = 28
	n = 0
	for i in range(8):
		n += dec(c[i]) << shift
		shift -= 4
	return n

def fixed_read(sock, nbytes):
	remain = nbytes
	buf = ""
	while remain > 0:
		s = sock.recv(remain)
		amount = len(s)		
		if amount <= 0:
			return None   # EOF or error
		remain -= amount
		buf = buf + s
	return buf

def log(msg):
	try:
		filename = os.environ["SCOP_LOGFILE"]
		fp = open(filename, "a")
	except (KeyError, IOError):
		fp = None
	msg = "[scoplib] " + msg
	if fp != None:
		fp.write(msg)
		f.close()
	else:
		syslog.syslog(syslog.LOG_INFO, msg)
