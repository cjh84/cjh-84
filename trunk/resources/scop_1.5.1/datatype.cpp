/* datatype.cpp - DMI - 23-8-2002

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "datatype.h"

typedef char *charptr;

/* svector */
		
SCOP_svector::SCOP_svector()
{
	capacity = 10;
	data = new charptr[10];
	size = 0;
}

SCOP_svector::~SCOP_svector()
{
	for(int i = 0; i < size; i++)
		delete[] data[i];
	
	delete[] data;
}

void SCOP_svector::add(char *s)
{
	if(size == capacity)
		expand_capacity();
	
	data[size] = new char[strlen(s) + 1];
	strcpy(data[size], s);
	size++;
}

void SCOP_svector::expand_capacity()
{
	char **new_data;
	
	capacity *= 2;
	new_data = new charptr[capacity];
	for(int i = 0; i < size; i++)
	{
		new_data[i] = data[i];
	}
	delete[] data;
	data = new_data;
}

char *SCOP_svector::item(int n)
{
	if(n < 0 || n >= size)
		return NULL;
	
	return data[n];
}

int SCOP_svector::count()
{
	return size;
}

/* linefile */

int SCOP_linefile::readline(FILE *fp, char *buf, int max_len)
{
	int len;
	
	if(feof(fp))
		return -1;
	buf[0] = '\0';
	fgets(buf, max_len, fp);
	len = strlen(buf);
	if(len > 0 && buf[len - 1] == '\n')
		buf[--len] = '\0';
	return len;
}

SCOP_linefile::SCOP_linefile(const char *filename)
{
	FILE *fp;
	char *buf;
	int len;
	const int MAX_LINE_LEN = 1000;
	
	v = new SCOP_svector();
	
	file_ok = 0;
	fp = fopen(filename, "r");
	if(!fp)
		return;
	file_ok = 1;
	
	buf = new char[MAX_LINE_LEN];
	while(1)
	{
		len = readline(fp, buf, MAX_LINE_LEN);
		if(len == -1)
			break;
		if(len > 0)
			v->add(buf);
	}
	
	delete[] buf;
	fclose(fp);
}

int SCOP_linefile::valid()
{
	return file_ok;
}

SCOP_linefile::~SCOP_linefile()
{
	delete v;
}

int SCOP_linefile::count()
{
	return v->count();
}

const char *SCOP_linefile::getline(int n)
{
	if(n < 0 || n >= v->count())
		return NULL;
	
	return v->item(n);
}

int SCOP_linefile::search(const char *line)
{
	for(int i = 0; i < v->count(); i++)
		if(!strcmp(line, v->item(i)))
			return 1;
	return 0;
}

/* StringBuf */

SCOP_StringBuf::SCOP_StringBuf()
{
	capacity = 50;
	buf = new char[capacity];
	used = 0;
}

SCOP_StringBuf::~SCOP_StringBuf()
{
	delete[] buf;
}

void SCOP_StringBuf::clear()
{
	// Keep the same capacity.
	used = 0;
}

int SCOP_StringBuf::length()
{
	return used;
}

char *SCOP_StringBuf::compact()
{
	char *s = new char[used + 1];
	if(used > 0)
		memcpy(s, buf, used);
	s[used] = '\0';
	return s;
}

void SCOP_StringBuf::check_expand(int required)
{
	if(required > capacity)
	{
		char *new_buf;
		int new_cap;

		new_cap = capacity * 2;	
		if(new_cap < required) new_cap = required * 2;
		new_buf = new char[new_cap];
		memcpy(new_buf, buf, used);
		capacity = new_cap;
		delete[] buf;
		buf = new_buf;
	}
}

void SCOP_StringBuf::cat(const char *s)
{
	int len = strlen(s);
	check_expand(used + len);
	memcpy(buf + used, s, len);
	used += len;
}

void SCOP_StringBuf::cat(char c)
{
	check_expand(used + 1);
	buf[used++] = c;
}
