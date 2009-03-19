/* xmltest.cpp - DMI - 11-12-2001

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdio.h>
#include <stdlib.h>

#include "scopxml.h"

int main(int argc, char **argv)
{
	int n = 42;
	const char *s = "Hello, world!";
	int p = 12345;
	
	double x = 1.1, y = -0.007, z = 5e20;
	
	vertex *triplet = pack(pack(x), pack(y), pack(z));
	vertex *args = pack(pack(n), pack(s), pack(p), triplet);
	
	char *xml = vertex_to_string(args);
	
	printf("Raw XML:\n\n%s\n\n", xml);
	
	vertex *reply = string_to_vertex(xml);
	
	printf("y = %g\n\n", reply->extract_item(3)->extract_double(1));
	
	printf("Structured XML:\n\n%s\n", pretty_print(args));
	
	return 0;
}
