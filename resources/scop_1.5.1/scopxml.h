/* scopxml.h - DMI - 10-12-2001

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

/* Note: error checking is minimal - if you pass invalid objects to
	the parsing or conversion functions your program will probably crash. */

typedef unsigned char byte;

enum VertexType { VInteger, VString, VDouble, VBinary, VList };

class vertex
{
	public:
			
		~vertex();
		
		// Unmarshalling:

		int extract_int(int item = -1);
		double extract_double(int item = -1);
		char *extract_string(int item = -1);
		void *extract_bytes(int item = -1);
		int count_bytes(int item = -1);

		vertex *extract_item(int item);
		int count_items();

		vertex **extract_array();

		char *extract_method(); // Convenience
		vertex *extract_args(); // Convenience
		
		// Implementation details:
	
		VertexType t;
		
		int n;        // VInteger and VBinary (bytes) and VList (length)
		char *s;      // VString
		double x;     // VDouble
		byte *buf;    // VBinary
		vertex *head; // VList (First child) - for packing only
		vertex *tail; // VList (Last child)  - for packing only
		vertex **element; // VList - for extracting only
		
		vertex *next;
		// All types (Next sibling, if this node is in a list) - packing only
};

typedef vertex *vertexptr;

/* Marshalling: */

vertex *pack(int n);
vertex *pack(const char *s);
vertex *pack(double x);
vertex *pack(byte *buf, int bytes);

vertex *mklist();
vertex *append(vertex *list, vertex *v);

vertex *pack(vertex **vert_array, int n);

// Convenience functions for making short lists:
vertex *pack(vertex *v1, vertex *v2);
vertex *pack(vertex *v1, vertex *v2, vertex *v3);
vertex *pack(vertex *v1, vertex *v2, vertex *v3, vertex *v4);
vertex *pack(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5);
vertex *pack(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5,
		vertex *v6);

/* Parsing: */

char *vertex_to_string(vertex *v);
vertex *string_to_vertex(const char *s);

char *vertex_to_string(vertex *v, const char *method); // Convenience

/* Debugging: */

char *pretty_print(vertex *v);

void describe(vertex *v);
