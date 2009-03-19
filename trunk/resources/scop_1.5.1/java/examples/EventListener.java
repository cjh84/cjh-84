// EventListener.java - DMI - 21-9-02

import java.io.*;

public class EventListener
{
	public static void main(String[] args)
	{
		SCOP scop;
		String msg;

		scop = new SCOP("localhost", "EventListener");
		scop.listen("news");
		
		while(true)
		{
			msg = scop.get_message();
			System.out.println("Received <" + msg + ">");
		}
	}
}
