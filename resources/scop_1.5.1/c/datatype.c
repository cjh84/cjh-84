/* stringbuf.cpp - DMI - 23-8-2002

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "datatype.h"

/* svector */
		
svector* svector_con()
{
        svector* s = malloc(sizeof(svector));
	s->capacity = 10;
	s->data = malloc(10 * sizeof(char*));
	s->size = 0;
}

void svector_des(svector* s)
{
        int i;
	for(i = 0; i < s->size; i++)
		free(s->data[i]);
	
	free(s->data);
}

void svector_add(svector* sv, char *s)
{
	if(sv->size == sv->capacity)
		svector_expand_capacity(sv);
	
	sv->data[sv->size] = malloc(strlen(s) + 1);
	strcpy(sv->data[sv->size], s);
	sv->size++;
}

void svector_expand_capacity(svector* sv)
{
	char **new_data;
        int i;
	
	sv->capacity *= 2;
	new_data = malloc(sizeof(char *) * sv->capacity);
	for(i = 0; i < sv->size; i++)
	{
		new_data[i] = sv->data[i];
	}
	free(sv->data);
	sv->data = new_data;
}

char* svector_item(svector* sv, int n)
{
	if(n < 0 || n >= sv->size)
		return NULL;
	
	return sv->data[n];
}

int svector_count(svector* sv)
{
	return sv->size;
}

/* linefile */

int linefile_readline(FILE *fp, char *buf, int max_len)
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

linefile* linefile_con(const char *filename)
{
	FILE *fp;
	char *buf;
	int len;
	const int MAX_LINE_LEN = 1000;
	linefile* lf = malloc(sizeof(linefile));

	lf->v = svector_con();
	
	lf->file_ok = 0;
	fp = fopen(filename, "r");
	if(!fp)
		return lf;
	lf->file_ok = 1;
	
	buf = malloc(MAX_LINE_LEN);
	while(1)
	{
		len = linefile_readline(fp, buf, MAX_LINE_LEN);
		if(len == -1)
			break;
		if(len > 0)
			svector_add(lf->v, buf);
	}
	
	free(buf);
	fclose(fp);
        return lf;
}

int linefile_valid(linefile* lf)
{
	return lf->file_ok;
}

void linefile_des(linefile* lf)
{
	free(lf);
}

int linefile_count(linefile* lf)
{
	return svector_count(lf->v);
}

const char* linefile_getline(linefile* lf, int n)
{
	if(n < 0 || n >= svector_count(lf->v))
		return NULL;
	
	return svector_item(lf->v, n);
}

int linefile_search(linefile* lf, char *line)
{
        int i;
	for(i = 0; i < svector_count(lf->v); i++)
		if(!strcmp(line, svector_item(lf->v, i)))
			return 1;
	return 0;
}

/* StringBuf */

SCOP_StringBuf* SCOP_StringBuf_con()
{
        SCOP_StringBuf* sb = malloc(sizeof(SCOP_StringBuf));

	sb->capacity = 50;
	sb->buf = malloc(sb->capacity);
	sb->used = 0;
}

void SCOP_StringBuf_des(SCOP_StringBuf* sb)
{
	free(sb->buf);
        free(sb); // SH added 2002-10-09
}

void SCOP_StringBuf_clear(SCOP_StringBuf* sb)
{
	// Keep the same capacity.
	sb->used = 0;
}

int SCOP_StringBuf_length(SCOP_StringBuf* sb)
{
	return sb->used;
}

char *SCOP_StringBuf_compact(SCOP_StringBuf* sb)
{
	char *s = malloc(sb->used + 1);
	if(sb->used > 0)
		memcpy(s, sb->buf, sb->used);
	s[sb->used] = '\0';
	return s;
}

void SCOP_StringBuf_check_expand(SCOP_StringBuf* sb, int required)
{
	if(required > sb->capacity)
	{
		char *new_buf;
		int new_cap;

		new_cap = sb->capacity * 2;	
		if(new_cap < required) new_cap = required * 2;
		new_buf = malloc(new_cap);
		memcpy(new_buf, sb->buf, sb->used);
		sb->capacity = new_cap;
		free(sb->buf);
		sb->buf = new_buf;
	}
}

void SCOP_StringBuf_cat(SCOP_StringBuf* sb, const char *s)
{
	int len = strlen(s);
	SCOP_StringBuf_check_expand(sb, sb->used + len);
	memcpy(sb->buf + sb->used, s, len);
	sb->used += len;
}

void SCOP_StringBuf_cat_c(SCOP_StringBuf* sb, char c)
{
	SCOP_StringBuf_check_expand(sb, sb->used + 1);
	sb->buf[sb->used++] = c;
}
