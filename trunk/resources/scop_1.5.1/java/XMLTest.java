/* XMLTest.java - DMI - 25-12-2002

Copyright (C) 2001-02 David Ingram

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation. */

import java.io.*;
// import Vertex.*;

public class XMLTest
{
	public static void main(String[] a)
	{
		int n = 42;
		String s = "Hello, world!";
		int p = 12345;

		double x = 1.1, y = -0.007, z = 5e20;

		Vertex triplet = Vertex.pack(Vertex.pack(x), Vertex.pack(y),
				Vertex.pack(z));
		Vertex args = Vertex.pack(Vertex.pack(n), Vertex.pack(s), Vertex.pack(p),
				triplet);

		String xml = Vertex.vertex_to_string(args);
		System.out.print("Raw XML:\n\n" + xml + "\n\n");

		Vertex reply = Vertex.string_to_vertex(xml);
		System.out.print("y = " + reply.extract_item(3).extract_double(1)
				+ "\n\n");

		System.out.println("Structured XML:\n\n" + Vertex.pretty_print(args));
		System.out.println("Structured parsed XML:\n\n" + Vertex.pretty_print(reply));
	}
}
