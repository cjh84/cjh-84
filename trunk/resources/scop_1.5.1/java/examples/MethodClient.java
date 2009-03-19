// MethodClient.java - DMI - 25-12-02

import java.io.*;

public class MethodClient
{
	public static void main(String[] args)
	{
		SCOPXML scop;

		scop = new SCOPXML("localhost", "MethodClient");

		System.out.println(0.0 + " deg C = " + cent_to_faren(scop, 0.0) +
				" deg F.");
		System.out.println(20.0 + " deg C = " + cent_to_faren(scop, 20.0) +
				" deg F.");
		System.out.println(60.0 + " deg F = " + faren_to_cent(scop, 60.0) +
				" deg C.");
		System.out.println("The server has been accessed " +
				count_uses(scop) + " times.");

		scop.close();
	}

	private static double cent_to_faren(SCOPXML scop, double c)
	{
		Vertex v, w;
		double f;

		v = Vertex.pack(c);
		w = scop.rpc("MethodServer", v, "ctof");
		f = w.extract_double();
		return f;
	}

	private static double faren_to_cent(SCOPXML scop, double f)
	{
		Vertex v, w;
		double c;

		v = Vertex.pack(f);
		w = scop.rpc("MethodServer", v, "ftoc");
		c = w.extract_double();
		return c;
	}

	private static int count_uses(SCOPXML scop)
	{
		Vertex v, w;
		int n;

		v = Vertex.pack(0); // Dummy argument
		w = scop.rpc("MethodServer", v, "stats");
		n = w.extract_int();
		return n;
	}
}
