// XMLSender.java - DMI - 25-12-02

import java.io.*;

public class XMLSender
{
	public static void main(String[] args)
	{
		SCOPXML scop;
		AddressBook ab = new AddressBook(3);
		Vertex v;

		ab.set_entry(0, "Poirot", "Belgium");
		ab.set_entry(1, "Morse", "Oxford, UK");
		ab.set_entry(2, "Danger Mouse", "London, UK");
		v = ab.marshall();

		scop = new SCOPXML("localhost", "XMLSender");
		scop.send_struct("XMLReceiver", v);
		scop.close();
	}
}
