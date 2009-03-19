// Sender.java - DMI - 20-9-02

/* Usage: java Sender [ <message> ]   (default message is "Hello world!") */

import java.io.*;

public class Sender
{
	public static void main(String[] args)
	{
		SCOP scop;
		String msg = args.length > 0 ? args[0] : "Hello world!";
		
		scop = new SCOP("localhost", "Sender");
		scop.send_message("Receiver", msg);
		scop.close();
	}
}
