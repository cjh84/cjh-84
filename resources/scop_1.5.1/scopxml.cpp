/* scopxml.cpp - DMI - 10-12-2001

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
	
#include "scopxml.h"
#include "datatype.h"

vertex::~vertex()
{
	if(t == VList)
	{
		if(element != NULL)
		{
			for(int i = 0; i < n; i++)
				delete element[i];
			delete[] element;
		}
		else
		{
			vertex *v = head, *v_next;
			while(v != NULL)
			{
				v_next = v->next;
				delete v;
				v = v_next;
			}
		}
	}
	else if(t == VString)
		delete[] s;
	else if(t == VBinary)
		delete[] buf;
}

/* Private prototypes: */

static vertex *string_to_vertex(const char *s, int *consumed, const char *orig_s);
static void vertex_to_string(vertex *v, SCOP_StringBuf *sb, int indent);

/* Marshalling: */

vertex *pack(int n)
{
	vertex *v = new vertex;
	v->t = VInteger;
	v->n = n;
	return v;
}

vertex *pack(const char *s)
{
	vertex *v = new vertex;
	v->t = VString;
	v->s = new char[strlen(s) + 1];
	strcpy(v->s, s);
	return v;
}
		
vertex *pack(double x)
{
	vertex *v = new vertex;
	v->t = VDouble;
	v->x = x;
	return v;
}
		
vertex *pack(byte *buf, int bytes)
{
	vertex *v = new vertex;
	v->t = VBinary;
	v->buf = new byte[bytes];
	memcpy(v->buf, buf, bytes);
	v->n = bytes;
	return v;
}

vertex *mklist()
{
	vertex *v = new vertex;
	v->t = VList;
	v->n = 0;
	v->head = NULL;
	v->tail = NULL;
	v->element = NULL;
	return v;
}
		
vertex *append(vertex *list, vertex *v)
{
	if(list->t != VList)
		return NULL;
	
	if(list->n == 0)
		list->head = v;
	else
		list->tail->next = v;
	list->tail = v;
	v->next = NULL;
	list->n++;
	
	return list;
}
		
vertex *pack(vertex **vert_array, int n)
{
	vertex *v = new vertex;
	v->t = VList;
	v->n = n;
	v->head = vert_array[0];
	v->tail = vert_array[n - 1];
	v->element = NULL;
	
	for(int i = 0; i < n - 1; i++)
		vert_array[i]->next = vert_array[i + 1];
	
	vert_array[n - 1]->next = NULL;
	
	return v;
}

// Convenience functions:

vertex *pack(vertex *v1, vertex *v2)
{
	vertex *v = new vertex;
	v->t = VList;
	v->n = 2;
	v->head = v1;
	v->tail = v2;
	v->element = NULL;
	
	v1->next = v2;
	v2->next = NULL;
	return v;
}

vertex *pack(vertex *v1, vertex *v2, vertex *v3)
{
	vertex *v = new vertex;
	v->t = VList;
	v->n = 3;
	v->head = v1;
	v->tail = v3;
	v->element = NULL;
	
	v1->next = v2;
	v2->next = v3;
	v3->next = NULL;
	return v;
}

vertex *pack(vertex *v1, vertex *v2, vertex *v3, vertex *v4)
{
	vertex *v = new vertex;
	v->t = VList;
	v->n = 4;
	v->head = v1;
	v->tail = v4;
	v->element = NULL;
	
	v1->next = v2;
	v2->next = v3;
	v3->next = v4;
	v4->next = NULL;
	return v;
}

vertex *pack(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5)
{
	vertex *v = new vertex;
	v->t = VList;
	v->n = 5;
	v->head = v1;
	v->tail = v5;
	v->element = NULL;
	
	v1->next = v2;
	v2->next = v3;
	v3->next = v4;
	v4->next = v5;
	v5->next = NULL;
	return v;
}

vertex *pack(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5,
		vertex *v6)
{
	vertex *v = new vertex;
	v->t = VList;
	v->n = 6;
	v->head = v1;
	v->tail = v6;
	v->element = NULL;
	
	v1->next = v2;
	v2->next = v3;
	v3->next = v4;
	v4->next = v5;
	v5->next = v6;
	v6->next = NULL;
	return v;
}

/* Unmarshalling: */

char *vertex::extract_method()
{
	if(t != VList || n != 2)
		return NULL;
	return element[0]->extract_string();
}

vertex *vertex::extract_args()
{
	if(t != VList || n != 2)
		return NULL;
	return element[1];
}

int vertex::extract_int(int item)
{
	if(item != -1)
	{
		if(t != VList || item < 0 || item >= n)
			return -1;
		return element[item]->extract_int();
	}
	if(t != VInteger)
		return -1;
	
	return n;
}

double vertex::extract_double(int item)
{
	if(item != -1)
	{
		if(t != VList || item < 0 || item >= n)
			return -1.0;
		return element[item]->extract_double();
	}
	if(t != VDouble)
		return -1.0;
	
	return x;
}

char *vertex::extract_string(int item)
{
	if(item != -1)
	{
		if(t != VList || item < 0 || item >= n)
			return NULL;
		return element[item]->extract_string();
	}
	if(t != VString)
		return NULL;
	
	return s;
}

void *vertex::extract_bytes(int item)
{
	if(item != -1)
	{
		if(t != VList || item < 0 || item >= n)
			return NULL;
		return element[item]->extract_bytes();
	}
	if(t != VBinary)
		return NULL;
	
	return buf;
}

int vertex::count_bytes(int item)
{
	if(item != -1)
	{
		if(t != VList || item < 0 || item >= n)
			return -1;
		return element[item]->count_bytes();
	}
	if(t != VBinary)
		return -1;
	
	return n;
}

vertex *vertex::extract_item(int item)
{
	if(t != VList || item < 0 || item >= n)
		return NULL;
	
	return element[item];
}

int vertex::count_items()
{
	if(t != VList)
		return -1;
	
	return n;
}

vertex **vertex::extract_array()
{
	if(t != VList)
		return NULL;
	
	return element;
}

/* Parsing: */

static int char_to_hex(char c)
{
	if(c >= '0' && c <= '9')
		return c - '0';
	else if(c >= 'A' && c <= 'F')
		return c - 'A' + 10;
	else
		return -1;
}

static char hex_to_char(int n)
{
	if(n < 0 || n > 15)
		return '?';	
	if(n < 10)
		return '0' + n;
	else
		return n - 10 + 'A';
}

char *vertex_to_string(vertex *v, const char *method)
{
	/* Fixme: this function leaks a small amount of memory,
		because the method node isn't deallocated by the user - XXX */
	
	if(v == NULL)
		return vertex_to_string(pack(pack(method), pack(0)));
	else
		return vertex_to_string(pack(pack(method), v));
}

char *vertex_to_string(vertex *v)
{
	SCOP_StringBuf *sb = new SCOP_StringBuf;
	vertex_to_string(v, sb, -1);
	char *s = sb->compact();
	delete sb;
	return s;
}

char *pretty_print(vertex *v)
{
	SCOP_StringBuf *sb = new SCOP_StringBuf;
	vertex_to_string(v, sb, 0);
	char *s = sb->compact();
	delete sb;
	return s;
}

static const char *padding = "   ";

static void vertex_to_string(vertex *v, SCOP_StringBuf *sb, int indent)
{
	char str[100];

	if(indent > 0)
	{
		for(int i = 0; i < indent; i++)
			sb->cat(padding);
	}
	
	if(v->t == VInteger)
	{
		sprintf(str, "<int>%d</int>", v->n); sb->cat(str);
	}
	else if(v->t == VDouble)
	{
		sprintf(str, "<double>%g</double>", v->x); sb->cat(str);
	}
	else if(v->t == VString)
	{
		sprintf(str, "<string %d>", (int)strlen(v->s)); sb->cat(str);
		sb->cat(v->s);
		sb->cat("</string>");
	}
	else if(v->t == VBinary)
	{
		sprintf(str, "<binary %d>", v->n); sb->cat(str);
		for(int i = 0; i < v->n; i++)
		{
			sb->cat(hex_to_char(v->buf[i] / 16));
			sb->cat(hex_to_char(v->buf[i] % 16));
		}
		sb->cat("</binary>");
	}
	else if(v->t == VList)
	{
		sprintf(str, "<list %d>", v->n); sb->cat(str);
		
		if(indent != -1)
		{
			sb->cat("\n");
		}
		vertex *w = v->head;
		for(int i = 0; i < v->n; i++)
		{
			vertex_to_string(w, sb, indent == -1 ? -1 : indent + 1);
			w = w->next;
		}
		if(indent > 0)
		{
			for(int i = 0; i < indent; i++)
				sb->cat(padding);
		}	
		sb->cat("</list>");
	}
	else
	{
		printf("DBG: vertex_to_string unknown vertex type\n");
		fflush(stdout);
	}
	
	if(indent != -1)
	{
		sb->cat("\n");
	}
}

vertex *string_to_vertex(const char *s)
{
	int advance;
	
	return string_to_vertex(s, &advance, s);
}

static vertex *string_to_vertex(const char *s, int *consumed, const char *orig_s)
{
	char type[20];
	int pos;
	vertex *v = new vertex;
	
	const char *initial_s = s;
	
	sscanf(s, "<%[^>]>%n", type, &pos);
	s += pos;
	
	if(!strcmp(type, "int"))
	{
		v->t = VInteger;
		sscanf(s, "%d</int>%n", &v->n, &pos);
		s += pos;
	}
	else if(!strcmp(type, "double"))
	{
		v->t = VDouble;
		float f;
		sscanf(s, "%f</double>%n", &f, &pos);
		v->x = f;
		s += pos;
	}
	else if(!strncmp(type, "string", 6))
	{
		int n = atoi(type + 7);
		
		v->t = VString;
		v->s = new char[n + 1];
		memcpy(v->s, s, n);
		v->s[n] = '\0';
		s += n;
		
		sscanf(s, "</string>%n", &pos);
		s += pos;
	}
	else if(!strncmp(type, "binary", 6))
	{
		int n = atoi(type + 7);
		
		v->t = VBinary;
		v->n = n;
		v->buf = new byte[n];
		
		for(int i = 0; i < n; i++)
		{
			v->buf[i] = char_to_hex(s[0]) * 16 + char_to_hex(s[1]);
			s += 2;
		}
		
		sscanf(s, "</binary>%n", &pos);
		s += pos;
	}
	else if(!strncmp(type, "list", 4))
	{
		int n = atoi(type + 5);
		
		v->t = VList;
		v->n = n;
		if(n > 0)
		{
			v->element = new vertexptr[n];

			for(int i = 0; i < n; i++)
			{
				v->element[i] = string_to_vertex(s, &pos, orig_s);
				s += pos;
			}

			/* Fill in head, tail and next as well (these are only needed so
				that it is valid to follow a string_to_vertex call with
				vertex_to_string, which is occasionally useful for pretty-printing
				a raw XML string you have received, for debugging purposes etc:
			*/
			v->head = v->element[0];
			v->tail = v->element[n - 1];
			for(int i = 0; i < n - 1; i++)
			{
				v->element[i]->next = v->element[i + 1];
			}
			v->element[n - 1]->next = NULL;
		}
		else
		{
			v->element = NULL;
			v->head = v->tail = NULL;
		}
		sscanf(s, "</list>%n", &pos);
		s += pos;
	}
	else
	{
		printf("Unrecognised type <%s> during string_to_vertex parse of:\n",
				type);
		printf("\"%s\"\n", orig_s);
		*consumed = 0;
		return NULL;
	}

	*consumed = s - initial_s;
	return v;
}

// Debugging:

void describe(vertex *v)
{
	if(v == NULL)
	{
		printf("Null pointer.\n");
		return;
	}
	switch(v->t)
	{
		case VInteger:
			printf("Integer (%d)\n", v->n);
			break;
		case VDouble:
			printf("Double (%g)\n", v->x);
			break;
		case VString:
			printf("String (length %d)\n", (int)strlen(v->s));
			break;
		case VBinary:
			printf("Binary (length %d)\n", v->n);
			break;
		case VList:
			printf("List (length %d)\n", v->n);
			break;
		default:
			printf("Unknown vertex type.\n");
			break;
	}
}
