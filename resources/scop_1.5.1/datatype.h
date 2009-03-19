/* datatype.h - DMI - 23-8-2002

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

#include <stdlib.h>
#include <string.h>

class SCOP_StringBuf
{
	public:
			
		SCOP_StringBuf();
		~SCOP_StringBuf();
	
		char *compact();
		/* The string returned by compact() must be deleted by the caller,
			and is *not* removed when the StringBuf itself is deleted. */
					
		void cat(const char *s);
		void cat(char c);
		
		void clear();
		int length();
		
	private:
			
		char *buf; // Internal buffer - not null terminated
		int used, capacity;
		
		void check_expand(int required);
};

/* svector's allocate storage for strings which are added, perform
	deep copies, and frees them when it is destroyed:
*/

class SCOP_svector
{
	public:
			
		SCOP_svector();
		~SCOP_svector();
		
		void add(char *s);
		char *item(int n);
		
		int count();
	
	private:
			
		int capacity;
		int size;
		char **data;
		
		void expand_capacity();
};

class SCOP_linefile
{
	public:
			
		SCOP_linefile(const char *filename);
		~SCOP_linefile();
		
		int valid();
		int count();
		/* The valid() method (which tests the private file_ok flag set by
			the constructor) shows if the file could be opened. If not, the
			other methods will still work since v will exist anyway
			(but contain no items). */
		
		const char *getline(int n);
		int search(const char *line);
	
	private:
			
		SCOP_svector *v;
		int file_ok;
	
		int readline(FILE *fp, char *buf, int max_len);
};

