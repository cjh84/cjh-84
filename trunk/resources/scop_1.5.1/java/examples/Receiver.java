// Receiver.java - DMI - 20-9-02

import java.io.*;

public class Receiver
{
	public static void main(String[] args)
	{
		SCOP scop;
		String msg;

		scop = new SCOP("localhost", "Receiver");
		while(true)
		{
			msg = scop.get_message();
			System.out.println("Received <" + msg + ">");
			if(msg.equals("quit")) break;
		}

		scop.close();
	}
}