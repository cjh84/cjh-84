#!/usr/bin/python
# xmltest.py - DMI - 10-11-03

import scopxml

n = 42
s = "Hello, world!"
p = 12345
x, y, z = 1.1, -0.007, 5e20

triplet = [x, y, z]
args = [n, s, p, triplet]
xml_s = scopxml.vertex_to_string(args)
print "Raw XML:\n\n" + xml_s + "\n"
reply = scopxml.string_to_vertex(xml_s)
print "y = " + str(reply[3][1]) + "\n"
print "Structured XML:\n\n" + scopxml.pretty_print(args)
