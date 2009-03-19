// address_book.cpp - DMI - 7-9-02

#include <stdlib.h>
#include <stdio.h>

#include <scop.h>
#include <scopxml.h>

#include "address_book.h"

typedef char *CharPtr;

AddressBook::AddressBook(int size)
{
	entries = size;
	name = new CharPtr[entries];
	address = new CharPtr[entries];
	for(int i = 0; i < entries; i++)
		name[i] = address[i] = NULL;
}

AddressBook::~AddressBook()
{
	for(int i = 0; i < entries; i++)
	{
		if(name[i]) delete[] name[i];
		if(address[i]) delete[] address[i];
	}
	delete[] name;
	delete[] address;
}

void AddressBook::set_entry(int i, const char *n, const char *a)
{
	name[i] = new char[strlen(n) + 1];
	address[i] = new char[strlen(a) + 1];
	strcpy(name[i], n);
	strcpy(address[i], a);
}

void AddressBook::dump()
{
	for(int i = 0; i < entries; i++)
		printf("Name %s, Address %s\n", name[i], address[i]);
}

vertex *AddressBook::marshall()
{
	vertex *list, *tuple;
	
	list = mklist();
	for(int i = 0; i < entries; i++)
	{
		tuple = pack(pack(name[i]), pack(address[i]));
		append(list, tuple);
	}
	return pack(pack(entries), list);
}

AddressBook::AddressBook(vertex *v)
{
	vertex *list, *tuple;
	char *n, *a;
	
	entries = v->extract_int(0);
	name = new CharPtr[entries];
	address = new CharPtr[entries];	
	list = v->extract_item(1);
	for(int i = 0; i < entries; i++)
	{
		tuple = list->extract_item(i);
		n = tuple->extract_string(0);
		a = tuple->extract_string(1);
		name[i] = new char[strlen(n) + 1];
		address[i] = new char[strlen(n) + 1];
		strcpy(name[i], n);
		strcpy(address[i], a);
	}
}
