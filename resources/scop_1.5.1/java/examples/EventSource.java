// EventSource.java - DMI - 21-9-02

/* Usage: java EventSource [ <source> ]   (default source is "news") */

import java.io.*;

public class EventSource
{
	public static void main(String[] args)
	{
		SCOP scop;
		int count = 1;

		scop = new SCOP("localhost", "EventSource");
		scop.set_source_hint(args.length > 0 ? args[0] : "news");

		while(true)
		{
			scop.emit("Item " + count);
			count++;
			try { Thread.sleep(1000); } catch(Exception e) {}
		}
	}
}
