// XMLClient.java - DMI - 25-12-02

/* Usage: java XMLClient [<n> <k>]    (default values n = 4, k = 2) */

import java.io.*;

public class XMLClient
{
	public static void main(String[] args)
	{
		SCOPXML scop;
		int n, k;
		Vertex v, w;

		if(args.length != 2)
		{
			n = 4;
			k = 2;
		}
		else
		{
			n = Integer.parseInt(args[0]);
			k = Integer.parseInt(args[1]);
		}

		scop = new SCOPXML("localhost", "XMLClient");	
		v = Vertex.pack(Vertex.pack(n), Vertex.pack(k));
		w = scop.rpc("XMLServer", v);
		System.out.println(n + " choose " + k + " equals " + w.extract_int()
				+ ".");
		scop.close();
	}
}
