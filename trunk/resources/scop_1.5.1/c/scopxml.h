/* scopxml.h - DMI - 10-12-2001

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

/* Note: error checking is minimal - if you pass invalid objects to
	the parsing or conversion functions your program will probably crash. */

#ifndef VERTEX_TYPE
#define VERTEX_TYPE

typedef unsigned char byte;

typedef char VertexType;
#define VInteger 1
#define VString 2
#define VDouble 3
#define VBinary 4
#define VList 5

typedef struct vertex0
{
		// Implementation details:
	
		VertexType t;
		
		int n;        // VInteger and VBinary (bytes) and VList (length)
		char *s;      // VString
		double x;     // VDouble
		byte *buf;    // VBinary
		struct vertex0 *head; // VList (First child) - for packing only
		struct vertex0 *tail; // VList (Last child)  - for packing only
		struct vertex0 **element; // VList - for extracting only
		
		struct vertex0 *next;
		// All types (Next sibling, if this node is in a list) - packing only
} vertex;
#endif

void vertex_des();
		
// Unmarshalling:

int vertex_extract_int(vertex* vx, int item); // = -1
double vertex_extract_double(vertex* vx, int item); // = -1
char* vertex_extract_string(vertex* vx, int item); // = -1
void *vertex_extract_bytes(vertex* vx, int item); // = -1
int vertex_count_bytes(vertex* vx, int item); // = -1
vertex *vertex_extract_item(vertex* vx, int item);
int vertex_count_items(vertex* vx);
vertex **vertex_extract_array(vertex* vx);
char *vertex_extract_method(vertex* vx); // Convenience
vertex *vertex_extract_args(vertex* vx); // Convenience
		

/* Marshalling: */

vertex *pack_int(int n);
vertex *pack_string(const char *s);
vertex *pack_double(double x);
vertex *pack_bytes(byte *buf, int bytes);

vertex *mklist();
vertex *append(vertex *list, vertex *v);

vertex *pack_array(vertex **vert_array, int n);

// Convenience functions for making short lists:
vertex *pack_2(vertex *v1, vertex *v2);
vertex *pack_3(vertex *v1, vertex *v2, vertex *v3);
vertex *pack_4(vertex *v1, vertex *v2, vertex *v3, vertex *v4);
vertex *pack_5(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5);
vertex *pack_6(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5,
		vertex *v6);

/* Parsing: */

char *vertex_to_string(vertex *v);
vertex *string_to_vertex(const char *s);

char *vertex_to_string_m(vertex *v, const char *method); // Convenience

/* Debugging: */

char *pretty_print(vertex *v);

void describe(vertex *v);
