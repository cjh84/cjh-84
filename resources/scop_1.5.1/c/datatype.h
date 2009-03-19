/* stringbuf.cpp - DMI - 23-8-2002

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <string.h>

typedef struct SCOP_StringBuf0
{
		char *buf; // Internal buffer - not null terminated
		int used, capacity;
		
} SCOP_StringBuf;

SCOP_StringBuf* SCOP_StringBuf_con();
void SCOP_StringBuf_des(SCOP_StringBuf*);
	
char* SCOP_StringBuf_compact(SCOP_StringBuf*);
		/* The string returned by compact() must be deleted by the caller,
			and is *not* removed when the StringBuf itself is deleted. */

void SCOP_StringBuf_cat(SCOP_StringBuf*, const char *s);
void SCOP_StringBuf_cat_c(SCOP_StringBuf*, char c);
		
void SCOP_StringBuf_clear(SCOP_StringBuf*);
int SCOP_StringBuf_length(SCOP_StringBuf*);
void SCOP_StringBuf_check_expand(SCOP_StringBuf*, int required);
		

/* svector's allocate storage for strings which are added, perform
	deep copies, and frees them when it is destroyed:
*/

typedef struct svector0
{
		int capacity;
		int size;
		char **data;
		
} svector;
			
svector* svector_con();
void svector_des(svector*);
		
void svector_add(svector*, char *s);
char* svector_item(svector*, int n);
void svector_expand_capacity(svector*);
		
int svector_count(svector*);
			

typedef struct linefile0
{
		svector *v;
		int file_ok;
	
} linefile;

linefile* linefile_con(const char *filename);
void linefile_des(linefile*);
int linefile_valid(linefile*);
int linefile_count(linefile*);
		/* The valid() method (which tests the private file_ok flag set by
			the constructor) shows if the file could be opened. If not, the
			other methods will still work since v will exist anyway
			(but contain no items). */
		
const char *linefile_getline(linefile*, int n);
int linefile_search(linefile*, char *line);
	
int linefile_readline(FILE *fp, char *buf, int max_len);		
