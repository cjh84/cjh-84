// XMLReciever.java - DMI - 25-12-02

/* Usage: java XMLReciever [-inspect] */

import java.io.*;

public class XMLReceiver
{
	public static void main(String[] args)
	{
		SCOPXML scop;
		AddressBook ab;
		Vertex v;

		scop = new SCOPXML("localhost", "XMLReceiver");
		v = scop.get_struct();
		if(args.length == 1 && args[0].equals("-inspect"))
		{
			String s = Vertex.pretty_print(v);
			System.out.println(s);
		}
		ab = new AddressBook(v);
		ab.dump();
		scop.close();
	}
}
