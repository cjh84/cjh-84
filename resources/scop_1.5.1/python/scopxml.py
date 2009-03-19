# scopxml.py - DMI - 9-11-03
#
# Copyright (C) 2001-03 David Ingram
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation.

from types import *

## Parsing:

def vertex_to_string(v, method = None):
	if method != None: # Convenience option to encode method name
		if v == None:
			v = [method, 0]
		else:
			v = [method, v]
	l = []
	do_vertex_to_string(v, l, -1)
	s = "".join(l)
	return s

def string_to_vertex(s):
	v, offset = do_string_to_vertex(s, 0)
	return v

## Debugging:

def pretty_print(v):
	l = []
	do_vertex_to_string(v, l, 0)
	s = "".join(l)
	return s

## Internal:

padding = "   "

def char_to_hex(c):
	o = ord(c)
	if ord('0') <= o <= ord('9'):
		return o - ord('0')
	elif ord('A') <= o <= ord('F'):
		return o - ord('A') + 10
	else:
		return -1

def hex_to_char(n):
	if n < 0 or n > 15:
		return '?'
	if n < 10:
		return chr(ord('0') + n)
	return chr(n - 10 + ord('A'))

def isprintable(s):
	for c in s:
		o = ord(c)
		if o < 32 or o > 126:
			return False
	return True

def do_vertex_to_string(v, sb, indent):
	if indent > 0:
		for i in range(indent):
			sb.append(padding)
	t = type(v)
	if t is IntType:
		sb.append("<int>" + str(v) + "</int>")
	elif t is FloatType:
		sb.append("<double>" + str(v) + "</double>")
	elif t is StringType:
		if isprintable(v):
			sb.append("<string " + str(len(v)) + ">" + v + "</string>")
		else: # Binary
			sb.append("<binary " + str(len(v)) + ">")
			for c in v:
				sb.append(hex_to_char(ord(c) / 16))
				sb.append(hex_to_char(ord(c) % 16))
			sb.append("</binary>")
	elif t is ListType or t is TupleType:
		sb.append("<list " + str(len(v)) + ">")
		if indent != -1:
			sb.append("\n")
		for w in v:
			if indent == -1:
				do_vertex_to_string(w, sb, -1)
			else:
				do_vertex_to_string(w, sb, indent + 1)
		if indent > 0:
			for i in range(indent):
				sb.append(padding)
		sb.append("</list>")
	else:
		print "DBG: vertex_to_string unknown vertex type."
	if indent != -1:
		sb.append("\n")

def do_string_to_vertex(s, offset):
	# Extract contents of opening tag and scan past it:
	pos = s.find(">", offset)	
	tag = s[offset + 1:pos]
	offset = pos + 1
	if tag == "int":
		endtag = "</int>"
		pos = s.find(endtag, offset)
		v = int(s[offset:pos])
		offset = pos + len(endtag)
	elif tag == "double":
		endtag = "</double>"
		pos = s.find(endtag, offset)
		v = float(s[offset:pos])
		offset = pos + len(endtag)
	elif tag.startswith("string "):
		prefix = "string "
		endtag = "</string>"
		n = int(tag[len(prefix):])
		v = s[offset:offset + n]
		offset += n + len(endtag)
	elif tag.startswith("binary "): # Convert to string
		prefix = "binary "
		endtag = "</binary>"
		n = int(tag[len(prefix):])
		l = []
		for i in range(n):
			l.append(chr(char_to_hex(s[offset]) * 16 + \
				char_to_hex(s[offset + 1])))
			offset += 2
		v = "".join(l)
		offset += len(endtag)
	elif tag.startswith("list "):
		prefix = "list "
		endtag = "</list>"
		n = int(tag[len(prefix):])
		v = []
		if n > 0:
			for i in range(n):
				w, offset = do_string_to_vertex(s, offset)
				v.append(w)
		offset += len(endtag)
	else:
		print "Unrecognised tag <" + tag + ">"
		return None
	return v, offset
