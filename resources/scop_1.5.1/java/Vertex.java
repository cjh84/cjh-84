/* Vertex.java - DMI - 21-9-2002 - SCOP Level 2 Java bindings

Copyright (C) 2002 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

import java.io.*;
import java.net.*;
import java.util.*;

public class Vertex
{
	/* Marshalling: */

	static Vertex pack(int n)
	{
		Vertex v = new Vertex();
		v.t = VInteger;
		v.n = n;
		return v;
	}
	static Vertex pack(String s)
	{
		Vertex v = new Vertex();
		v.t = VString;
		v.s = s;
		return v;
	}
	static Vertex pack(double x)
	{
		Vertex v = new Vertex();
		v.t = VDouble;
		v.x = x;
		return v;
	}
	static Vertex pack(byte[] buf)
	{
		int len = buf.length;
		Vertex v = new Vertex();
		v.t = VBinary;
		v.buf = new byte[len];
		for(int i = 0; i < len; i++)
			v.buf[i] = buf[i];
		v.n = len;
		return v;
	}
	
	static Vertex mklist()
	{
		Vertex v = new Vertex();
		v.t = VList;
		v.n = 0;
		v.head = v.tail = null;
		v.element = null;
		return v;
	}
	static Vertex append(Vertex list, Vertex v)
	{
		if(list.t != VList)
			return null;
		
		if(list.n == 0)
			list.head = v;
		else
			list.tail.next = v;
		list.tail = v;
		v.next = null;
		list.n++;

		return list;
	}
	static Vertex pack(Vertex[] vert_array)
	{
		int n = vert_array.length;
		
		Vertex v = new Vertex();
		v.t = VList;
		v.n = n;
		v.head = vert_array[0];
		v.tail = vert_array[n - 1];
		v.element = null;

		for(int i = 0; i < n - 1; i++)
			vert_array[i].next = vert_array[i + 1];
		vert_array[n - 1].next = null;

		return v;
	}

	// Convenience functions for making short lists:
	static Vertex pack(Vertex v1, Vertex v2)
	{
		Vertex v = new Vertex();
		v.t = VList;
		v.n = 2;
		v.head = v1;
		v.tail = v2;
		v.element = null;
		
		v1.next = v2;
		v2.next = null;
		return v;
	}
	static Vertex pack(Vertex v1, Vertex v2, Vertex v3)
	{
		Vertex v = new Vertex();
		v.t = VList;
		v.n = 3;
		v.head = v1;
		v.tail = v3;
		v.element = null;
		
		v1.next = v2;
		v2.next = v3;
		v3.next = null;
		return v;
	}
	static Vertex pack(Vertex v1, Vertex v2, Vertex v3, Vertex v4)
	{
		Vertex v = new Vertex();
		v.t = VList;
		v.n = 4;
		v.head = v1;
		v.tail = v4;
		v.element = null;
		
		v1.next = v2;
		v2.next = v3;
		v3.next = v4;
		v4.next = null;
		return v;
	}
	static Vertex pack(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Vertex v5)
	{
		Vertex v = new Vertex();
		v.t = VList;
		v.n = 5;
		v.head = v1;
		v.tail = v5;
		v.element = null;
		
		v1.next = v2;
		v2.next = v3;
		v3.next = v4;
		v4.next = v5;
		v5.next = null;
		return v;
	}
	static Vertex pack(Vertex v1, Vertex v2, Vertex v3, Vertex v4, Vertex v5,
			Vertex v6)
	{
		Vertex v = new Vertex();
		v.t = VList;
		v.n = 6;
		v.head = v1;
		v.tail = v6;
		v.element = null;
		
		v1.next = v2;
		v2.next = v3;
		v3.next = v4;
		v4.next = v5;
		v5.next = v6;
		v6.next = null;
		return v;
	}

	/* Unmarshalling */

	String extract_method() // Convenience
	{
		if(t != VList || n != 2)
			return null;
		return element[0].extract_string();
	}
	Vertex extract_args() // Convenience
	{
		if(t != VList || n != 2)
			return null;
		return element[1];
	}

	int extract_int()
	{
		if(t != VInteger) return -1;
		return n;
	}
	double extract_double()
	{
		if(t != VDouble) return -1.0;
		return x;
	}
	String extract_string()
	{
		if(t != VString) return null;
		return s;
	}
	byte[] extract_bytes()
	{
		if(t != VBinary) return null;
		return buf;
	}
	int count_bytes()
	{
		if(t != VBinary) return -1;
		return n;
	}

	int extract_int(int item)
	{
		if(t != VList || item < 0 || item >= n)
			return -1;
		return element[item].extract_int();
	}
	double extract_double(int item)
	{
		if(t != VList || item < 0 || item >= n)
			return -1.0;
		return element[item].extract_double();
	}
	String extract_string(int item)
	{
		if(t != VList || item < 0 || item >= n)
			return null;
		return element[item].extract_string();
	}
	byte[] extract_bytes(int item)
	{
		if(t != VList || item < 0 || item >= n)
			return null;
		return element[item].extract_bytes();
	}
	int count_bytes(int item)
	{
		if(t != VList || item < 0 || item >= n)
			return -1;
		return element[item].count_bytes();
	}

	Vertex extract_item(int item)
	{
		if(t != VList || item < 0 || item >= n)
			return null;
		return element[item];
	}
	int count_items()
	{
		if(t != VList)
			return -1;
		return n;
	}

	Vertex[] extract_array()
	{
		if(t != VList)
			return null;
		return element;
	}

	/* Parsing: */

	private static int char_to_hex(char c)
	{
		if(c >= '0' && c <= '9')
			return c - '0';
		else if(c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		else
			return -1;
	}
	private static char hex_to_char(int n)
	{
		if(n < 0 || n > 15)
			return '?';	
		if(n < 10)
			return (char)('0' + n);
		else
			return (char)(n - 10 + 'A');
	}

	static final String padding = "   ";
	static String vertex_to_string(Vertex v, String method) // Convenience
	{
		/* Fixme: this function leaks a small amount of memory,
			because the method node isn't deallocated by the user - XXX */

		if(v == null)
			return vertex_to_string(pack(pack(method), pack(0)));
		else
			return vertex_to_string(pack(pack(method), v));
	}
	static String pretty_print(Vertex v)
	{
		StringBuffer sb = new StringBuffer();
		vertex_to_string(v, sb, 0);
		String s = sb.toString();
		return s;
	}
	static String vertex_to_string(Vertex v)
	{
		StringBuffer sb = new StringBuffer();
		vertex_to_string(v, sb, -1);
		String s = sb.toString();
		return s;
	}
	static void vertex_to_string(Vertex v, StringBuffer sb, int indent)
	{
		if(indent > 0)
		{
			for(int i = 0; i < indent; i++)
				sb.append(padding);
		}

		if(v.t == VInteger)
		{
			sb.append("<int>" + v.n + "</int>");
		}
		else if(v.t == VDouble)
		{
			sb.append("<double>" + v.x + "</double>");
		}
		else if(v.t == VString)
		{
			sb.append("<string " + v.s.length() + ">" + v.s + "</string>");
		}
		else if(v.t == VBinary)
		{
			sb.append("<binary " + v.n + ">");
			for(int i = 0; i < v.n; i++)
			{
				sb.append(hex_to_char(v.buf[i] / 16));
				sb.append(hex_to_char(v.buf[i] % 16));
			}
			sb.append("</binary>");
		}
		else if(v.t == VList)
		{
			sb.append("<list " + v.n + ">");

			if(indent != -1)
			{
				sb.append("\n");
			}
			Vertex w = v.head;
			for(int i = 0; i < v.n; i++)
			{
				vertex_to_string(w, sb, indent == -1 ? -1 : indent + 1);
				w = w.next;
			}
			if(indent > 0)
			{
				for(int i = 0; i < indent; i++)
					sb.append(padding);
			}	
			sb.append("</list>");
		}
		else
		{
			System.out.println("DBG: vertex_to_string unknown vertex type");
		}

		if(indent != -1)
		{
			sb.append("\n");
		}
	}

	static class ParseResult
	{
		Vertex v;
		int offset;
	}	
	static Vertex string_to_vertex(String s)
	{
		ParseResult pr = do_string_to_vertex(s, 0);
		return pr.v;
	}
	private static ParseResult do_string_to_vertex(String s, int offset)
	{
		Vertex v = new Vertex();
		int pos;
		String tag;

		// Extract contents of opening tag and scan past it:
		pos = s.indexOf('>', offset);
		tag = s.substring(offset + 1, pos);
		offset = pos + 1;

		if(tag.equals("int"))
		{
			String endtag = "</int>";
			
			v.t = VInteger;
			pos = s.indexOf(endtag, offset);
			v.n = Integer.parseInt(s.substring(offset, pos));
			offset = pos + endtag.length();
		}
		else if(tag.equals("double"))
		{
			String endtag = "</double>";
			
			v.t = VDouble;
			pos = s.indexOf(endtag, offset);
			v.x = Double.parseDouble(s.substring(offset, pos));
			offset = pos + endtag.length();
		}
		else if(tag.startsWith("string "))
		{
			String prefix = "string ";
			String endtag = "</string>";
			
			int n = Integer.parseInt(tag.substring(prefix.length()));

			v.t = VString;
			v.s = s.substring(offset, offset + n);
			offset += n + endtag.length();
		}
		else if(tag.startsWith("binary "))
		{
			String prefix = "binary ";
			String endtag = "</binary>";
			
			int n = Integer.parseInt(tag.substring(prefix.length()));

			v.t = VBinary;
			v.n = n;
			v.buf = new byte[n];

			for(int i = 0; i < n; i++)
			{
				v.buf[i] = (byte)(char_to_hex(s.charAt(offset)) * 16 +
						char_to_hex(s.charAt(offset + 1)));
				offset += 2;
			}
			offset += endtag.length();
		}
		else if(tag.startsWith("list "))
		{
			String prefix = "list ";
			String endtag = "</list>";
			
			int n = Integer.parseInt(tag.substring(prefix.length()));

			v.t = VList;
			v.n = n;
			if(n > 0)
			{
				v.element = new Vertex[n];

				for(int i = 0; i < n; i++)
				{
					ParseResult pr = do_string_to_vertex(s, offset);
					v.element[i] = pr.v;
					offset = pr.offset;
				}

				/* Fill in head, tail and next as well (these are only needed so
					that it is valid to follow a string_to_vertex call with
					vertex_to_string, which is occasionally useful for pretty-printing
					a raw XML string you have received, for debugging purposes etc:
				*/
				v.head = v.element[0];
				v.tail = v.element[n - 1];
				for(int i = 0; i < n - 1; i++)
				{
					v.element[i].next = v.element[i + 1];
				}
				v.element[n - 1].next = null;
			}
			else
			{
				v.element = null;
				v.head = v.tail = null;
			}
			offset += endtag.length();
		}
		else
		{
			System.out.println("Unrecognised tag <" + tag + ">");
			return null;
		}

		ParseResult pr = new ParseResult();
		pr.offset = offset;
		pr.v = v;
		return pr;
	}
	
	/* Debugging: */

	static void describe(Vertex v)
	{
		if(v == null)
		{
			System.out.println("Null pointer.");
			return;
		}
		switch(v.t)
		{
			case VInteger:
				System.out.println("Integer (" + v.n + ")");
				break;
			case VDouble:
				System.out.println("Double (" + v.x + ")");
				break;
			case VString:
				System.out.println("String (length " + v.s.length() + ")");
				break;
			case VBinary:
				System.out.println("Binary (length " + v.n + ")");
				break;
			case VList:
				System.out.println("List (length " + v.n + ")");
				break;
			default:
				System.out.println("Unknown vertex type.");
				break;
		}
	}

	/* Implementation details */

	// Vertex type enumeration:
	private static final int VInteger = 0;
	private static final int VString = 1;
	private static final int VDouble = 2;
	private static final int VBinary = 3;
	private static final int VList = 4;
	
	private int t; // Vertex type

	private int n;        // VInteger and VBinary (bytes) and VList (length)
	private String s;     // VString
	private double x;     // VDouble
	private byte[] buf;   // VBinary
	private Vertex head;  // VList (First child) - for packing only
	private Vertex tail;  // VList (Last child)  - for packing only
	private Vertex[] element; // VList - for extracting only

	private Vertex next;
	// All types (Next sibling, if this node is in a list) - packing only
}
