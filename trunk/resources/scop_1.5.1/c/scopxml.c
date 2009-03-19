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

void vertex_des(vertex* vx)
{
	if(vx->t == VList)
	{
		if(vx->element != NULL)
		{
                        int i;
			for(i = 0; i < vx->n; i++)
				free(vx->element[i]);
			free(vx->element);
		}
		else
		{
			vertex *v = vx->head, *v_next;
			while(v != NULL)
			{
				v_next = v->next;
				free(v);
				v = v_next;
			}
		}
	}
	else if(vx->t == VString)
		free(vx->s);
	else if(vx->t == VBinary)
		free(vx->buf);
}

/* Private prototypes: */

static vertex *string_to_vertex_priv(const char *s, int *consumed);
static void vertex_to_string_priv(vertex *v, SCOP_StringBuf *sb, int indent);

/* Marshalling: */

vertex *pack_int(int n)
{
	vertex *v = malloc(sizeof(vertex));
	v->t = VInteger;
	v->n = n;
	return v;
}

vertex *pack_string(const char *s)
{
	vertex *v = malloc(sizeof(vertex));
	v->t = VString;
	v->s = malloc(strlen(s) + 1);
	strcpy(v->s, s);
	return v;
}
		
vertex *pack_double(double x)
{
	vertex *v = malloc(sizeof(vertex));
	v->t = VDouble;
	v->x = x;
	return v;
}
		
vertex *pack_bytes(byte *buf, int bytes)
{
	vertex *v = malloc(sizeof(vertex));
	v->t = VBinary;
	v->buf = malloc(bytes);
	memcpy(v->buf, buf, bytes);
	v->n = bytes;
	return v;
}

vertex *mklist()
{
	vertex *v = malloc(sizeof(vertex));
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
	vertex *v = malloc(sizeof(vertex));
        int i;
	v->t = VList;
	v->n = n;
	v->head = vert_array[0];
	v->tail = vert_array[n - 1];
	v->element = NULL;
	
	for(i = 0; i < n - 1; i++)
		vert_array[i]->next = vert_array[i + 1];
	
	vert_array[n - 1]->next = NULL;
	
	return v;
}

// Convenience functions:

vertex *pack_2(vertex *v1, vertex *v2)
{
	vertex *v = malloc(sizeof(vertex));
	v->t = VList;
	v->n = 2;
	v->head = v1;
	v->tail = v2;
	v->element = NULL;
	
	v1->next = v2;
	v2->next = NULL;
	return v;
}

vertex *pack_3(vertex *v1, vertex *v2, vertex *v3)
{
	vertex *v = malloc(sizeof(vertex));
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

vertex *pack_4(vertex *v1, vertex *v2, vertex *v3, vertex *v4)
{
	vertex *v = malloc(sizeof(vertex));
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

vertex *pack_5(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5)
{
	vertex *v = malloc(sizeof(vertex));
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

vertex *pack_6(vertex *v1, vertex *v2, vertex *v3, vertex *v4, vertex *v5,
		vertex *v6)
{
	vertex *v = malloc(sizeof(vertex));
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

char* vertex_extract_method(vertex *vx)
{
	if(vx->t != VList || vx->n != 2)
		return NULL;
	return vertex_extract_string(vx->element[0], -1);
}

vertex* vertex_extract_args(vertex *vx)
{
	if(vx->t != VList || vx->n != 2)
		return NULL;
	return vx->element[1];
}

int vertex_extract_int(vertex* vx, int item) // = -1
{
	if(item != -1)
	{
		if(vx->t != VList || item < 0 || item >= vx->n)
			return -1;
		return vertex_extract_int(vx->element[item], -1);
	}
	if(vx->t != VInteger)
		return -1;
	
	return vx->n;
}

double vertex_extract_double(vertex* vx, int item) // = -1
{
	if(item != -1)
	{
		if(vx->t != VList || item < 0 || item >= vx->n)
			return -1.0;
		return vertex_extract_double(vx->element[item], -1);
	}
	if(vx->t != VDouble)
		return -1.0;
	
	return vx->x;
}

char* vertex_extract_string(vertex* vx, int item) // = -1
{
	if(item != -1)
	{
		if(vx->t != VList || item < 0 || item >= vx->n)
			return NULL;
		return vertex_extract_string(vx->element[item], -1);
	}
	if(vx->t != VString)
		return NULL;
	
	return vx->s;
}

void* vertex_extract_bytes(vertex* vx, int item) // = -1
{
	if(item != -1)
	{
		if(vx->t != VList || item < 0 || item >= vx->n)
			return NULL;
		return vertex_extract_bytes(vx->element[item], -1);
	}
	if(vx->t != VBinary)
		return NULL;
	
	return vx->buf;
}

int vertex_count_bytes(vertex* vx, int item) // = -1
{
	if(item != -1)
	{
		if(vx->t != VList || item < 0 || item >= vx->n)
			return -1;
		return vertex_count_bytes(vx->element[item], -1);
	}
	if(vx->t != VBinary)
		return -1;
	
	return vx->n;
}

vertex* vertex_extract_item(vertex* vx, int item)
{
	if(vx->t != VList || item < 0 || item >= vx->n)
		return NULL;
	
	return vx->element[item];
}

int vertex_count_items(vertex* vx)
{
	if(vx->t != VList)
		return -1;
	
	return vx->n;
}

vertex** vertex_extract_array(vertex* vx)
{
	if(vx->t != VList)
		return NULL;
	
	return vx->element;
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

char *vertex_to_string_m(vertex *v, const char *method)
{
	return vertex_to_string(pack_2(pack_string(method), v));
}

char *vertex_to_string(vertex *v)
{
	SCOP_StringBuf *sb = malloc(sizeof(SCOP_StringBuf));
        char *s;
	vertex_to_string_priv(v, sb, -1);
	s = SCOP_StringBuf_compact(sb);
	free(sb);
	return s;
}

char *pretty_print(vertex *v)
{
	SCOP_StringBuf *sb = malloc(sizeof(SCOP_StringBuf));
        char *s;
	vertex_to_string_priv(v, sb, 0);
	s = SCOP_StringBuf_compact(sb);
	free(sb);
	return s;
}

static const char *padding = "   ";

static void vertex_to_string_priv(vertex *v, SCOP_StringBuf *sb, int indent)
{
	char str[100];

	if(indent > 0)
	{
                int i;
		for(i = 0; i < indent; i++)
			SCOP_StringBuf_cat(sb, padding);
	}
	
	if(v->t == VInteger)
	{
		sprintf(str, "<int>%d</int>", v->n); SCOP_StringBuf_cat(sb, str);
	}
	else if(v->t == VDouble)
	{
		sprintf(str, "<double>%g</double>", v->x); SCOP_StringBuf_cat(sb, str);
	}
	else if(v->t == VString)
	{
		sprintf(str, "<string %d>", strlen(v->s)); SCOP_StringBuf_cat(sb, str);
		SCOP_StringBuf_cat(sb, v->s);
		SCOP_StringBuf_cat(sb, "</string>");
	}
	else if(v->t == VBinary)
	{
                int i;
		sprintf(str, "<binary %d>", v->n); SCOP_StringBuf_cat(sb, str);
		for(i = 0; i < v->n; i++)
		{
			SCOP_StringBuf_cat_c(sb, hex_to_char(v->buf[i] / 16));
			SCOP_StringBuf_cat_c(sb, hex_to_char(v->buf[i] % 16));
		}
		SCOP_StringBuf_cat(sb, "</binary>");
	}
	else if(v->t == VList)
	{
                int i;
                vertex *w;
		sprintf(str, "<list %d>", v->n); SCOP_StringBuf_cat(sb, str);
		
		if(indent != -1)
		{
			SCOP_StringBuf_cat(sb, "\n");
		}
		w = v->head;
		for(i = 0; i < v->n; i++)
		{
			vertex_to_string_priv(w, sb, indent == -1 ? -1 : indent + 1);
			w = w->next;
		}
		if(indent > 0)
		{
			for(i = 0; i < indent; i++)
				SCOP_StringBuf_cat(sb, padding);
		}	
		SCOP_StringBuf_cat(sb, "</list>");
	}
	else
		printf("DBG: vertex_to_string unknown vertex type\n"); fflush(stdout);		
	
	if(indent != -1)
	{
		SCOP_StringBuf_cat(sb, "\n");
	}
}

static vertex *string_to_vertex_c(const char *s, int *consumed);

vertex *string_to_vertex(const char *s)
{
	int advance;
	
	return string_to_vertex_c(s, &advance);
}

static vertex *string_to_vertex_c(const char *s, int *consumed)
{
	char type[20];
	int pos;
	vertex *v = malloc(sizeof(vertex));
	
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
		float f;
		v->t = VDouble;
		sscanf(s, "%f</double>%n", &f, &pos);
		v->x = f;
		s += pos;
	}
	else if(!strncmp(type, "string", 6))
	{
		int n = atoi(type + 7);
		
		v->t = VString;
		v->s = malloc(n + 1);
		memcpy(v->s, s, n);
		v->s[n] = '\0';
		s += n;
		
		sscanf(s, "</string>%n", &pos);
		s += pos;
	}
	else if(!strncmp(type, "binary", 6))
	{
		int i, n = atoi(type + 7);
		
		v->t = VBinary;
		v->n = n;
		v->buf = malloc(n);
		
		for(i = 0; i < n; i++)
		{
			v->buf[i] = char_to_hex(s[0]) * 16 + char_to_hex(s[1]);
			s += 2;
		}
		
		sscanf(s, "</binary>%n", &pos);
		s += pos;
	}
	else if(!strncmp(type, "list", 4))
	{
		int i, n = atoi(type + 5);
		
		v->t = VList;
		v->n = n;
		v->element = malloc(sizeof(vertex*) * n);
		
		for(i = 0; i < n; i++)
		{
			v->element[i] = string_to_vertex_c(s, &pos);
			s += pos;
		}
		
		/* Fill in head, tail and next as well (these are only needed so
			that it is valid to follow a string_to_vertex call with
			vertex_to_string, which is occasionally useful for pretty-printing
			a raw XML string you have received, for debugging purposes etc:
		*/
		v->head = v->element[0];
		v->tail = v->element[n - 1];
		for(i = 0; i < n - 1; i++)
		{
			v->element[i]->next = v->element[i + 1];
		}
		v->element[n - 1]->next = NULL;
		
		sscanf(s, "</list>%n", &pos);
		s += pos;
	}
	else
	{
		printf("Unrecognised type <%s>\n", type);
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
			printf("String (length %d)\n", strlen(v->s));
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
