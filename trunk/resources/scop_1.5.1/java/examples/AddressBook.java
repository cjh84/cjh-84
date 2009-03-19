// AddressBook.java - DMI - 25-12-02

import java.io.*;

public class AddressBook
{
	String[] name;
	String[] address;
	int entries;

	AddressBook(int size)
	{
		entries = size;
		name = new String[entries];
		address = new String[entries];
	}

	void set_entry(int i, String n, String a)
	{
		name[i] = n;
		address[i] = a;
	}
	
	void dump()
	{
		for(int i = 0; i < entries; i++)
			System.out.println("Name " + name[i] + ", Address " + address[i]);
	}

	Vertex marshall()
	{
		Vertex list, tuple;

		list = Vertex.mklist();
		for(int i = 0; i < entries; i++)
		{
			tuple = Vertex.pack(Vertex.pack(name[i]), Vertex.pack(address[i]));
			Vertex.append(list, tuple);
		}
		return Vertex.pack(Vertex.pack(entries), list);
	}
	
	AddressBook(Vertex v)
	{
		Vertex list, tuple;
		String n, a;

		entries = v.extract_int(0);
		name = new String[entries];
		address = new String[entries];	
		list = v.extract_item(1);
		for(int i = 0; i < entries; i++)
		{
			tuple = list.extract_item(i);
			n = tuple.extract_string(0);
			a = tuple.extract_string(1);
			name[i] = n;
			address[i] = a;
		}
	}
}
