# scop.py - DMI - 24-10-03 - SCOP Library API
#
# Copyright (C) 2001-03 David Ingram
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation.

import socket, scoplib, scopxml, time
from types import *

## Connection setup:

def scop_open(remote_hostname, name, unique = 0):
	sock = scoplib.do_connect(remote_hostname)
	if sock == None:
		return None
	if name != None:
		if unique:
			scop_clear(sock, name)
		buf = "register " + name
		scoplib.transmit(sock, buf)
	return sock

def scop_close(sock):
	sock.close()

def scop_listen(sock, interest, unique = 0):
	if unique:
		scop_clear(sock, interest)
	buf = "listen " +  interest
	scoplib.transmit(sock, buf)

## Messaging:

def scop_send_message(sock, endpoint, message, verify = 0):
	if verify:
		buf = "verify " + endpoint + "! " + message
	else:
		buf = "message " + endpoint + "! " + message
	scoplib.transmit(sock, buf)
	if verify:
		status = scoplib.read_int(sock)
		return status
	else:
		return -1

SCOP_RPC_CALL = "scop-rpc-call "

def scop_get_message(sock):
	s = scoplib.read_protocol(sock)
	if s == None:
		return None
	if s.startswith(SCOP_RPC_CALL):
		return s[len(SCOP_RPC_CALL):], True
	else:
		return s, False
	
## Predefined event sources:

def scop_set_source_hint(sock, endpoint):
	buf = "set-source-hint " + endpoint
	scoplib.transmit(sock, buf)

def scop_emit(sock, message, verify = 0):
	buf = "emit " + message
	scoplib.transmit(sock, buf)
	status = scoplib.read_int(sock)
	if verify:
		return status
	else:
		return 0

## Admin:

def scop_query(sock, endpoint):
	buf = "query " + endpoint
	scoplib.transmit(sock, buf)
	answer = scoplib.read_int(sock)
	return answer

def scop_clear(sock, endpoint):
	buf = "clear " + endpoint
	scoplib.transmit(sock, buf)

def scop_set_log(sock, log_level):
	buf = "log " + str(log_level)
	transmit(sock, buf)

def scop_terminate(sock):
	scoplib.transmit(sock, "terminate")
	
def scop_reconfigure(sock):
	scoplib.transmit(sock, "reconfigure")

def scop_list(sock):
	scoplib.transmit(sock, "list")
	s = scoplib.read_protocol(sock)
	sarr = s.split("!")
	n = len(sarr)
	if n % 3 != 1:
		log("List parsing sanity check failed!")
		return None
	v = []
	for i in range((n - 1) / 3):
		name = sarr[i * 3]
		interest = sarr[i * 3 + 1]
		src_hint = sarr[i * 3 + 2]
		tuple = name, interest, src_hint
		v.append(tuple)
	return v

## Cookies:

def scop_set_plain_cookie(sock, text):
	buf = "set-cookie " + text
	transmit(sock, buf)

def scop_get_plain_cookie(sock, name):
	buf = "get-cookie " + name
	transmit(sock, buf)
	reply = scoplib.read_protocol(sock)
	return reply

## XML Cookies:

def scop_get_cookie(sock, name):
	s = scop_get_plain_cookie(sock, name)
	if s == None:
		return None
	v = string_to_vertex(s)
	return v

def scop_set_cookie(sock, data):
	text = scopxml.vertex_to_string(data)
	scop_set_plain_cookie(sock, text)

## XML messaging:

def scop_send_struct(sock, endpoint, args, method = None):
	if method != None:
		msg = scopxml.vertex_to_string(args, method)
	else:
		msg = scopxml.vertex_to_string(args)
	scop_send_message(sock, endpoint, msg)

def scop_get_struct(sock):
	buf, rpc_flag = scop_get_message(sock)
	if buf == None:
		return None
	v = scopxml.string_to_vertex(buf);
	return v, rpc_flag

## RPC (including XML versions):

def scop_rpc(sock, endpoint, args, method = None): # String or XML
	if type(args) is StringType:
		xmlmode = 0
		s = args
	else:
		xmlmode = 1
		if method != None:
			s = scopxml.vertex_to_string(args, method)
		else:
			s = scopxml.vertex_to_string(args)
	buf = "call " + endpoint + "! " + s
	scoplib.transmit(sock, buf)
	reply, rpc_flag = scop_get_message(sock)
	if reply == "scop-rpc-error":
		return None
	if xmlmode:
		return scopxml.string_to_vertex(reply)
	else:
		return reply

def scop_send_reply(sock, reply): # String or XML
	if type(reply) is StringType:
		buf = "reply " + reply
	else:
		buf = "reply " + scopxml.vertex_to_string(reply)
	scoplib.transmit(sock, buf)

def scop_get_request(sock): # XML only
	buf, rpc_flag = scop_get_message(sock)
	if buf == None or not rpc_flag:
		return None
	v = scopxml.string_to_vertex(buf)
	return v
