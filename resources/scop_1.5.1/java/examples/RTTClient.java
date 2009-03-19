// RTTClient.java - DMI - 21-9-02

/* Usage: java RTTClient [ <iterations> ]   (default is 1000) */

import java.io.*;

public class RTTClient
{
	public static void main(String[] args)
	{
		int REPEATS = 1000;
		SCOP scop;
		String reply;
		long start, end, us;

		if(args.length == 1)
			REPEATS = Integer.parseInt(args[0]);

		scop = new SCOP("localhost", "RTTClient");
		start = System.currentTimeMillis();
		for(int i = 0; i < REPEATS; i++)
		{
			reply = scop.rpc("Server", "test");
			if(!reply.equals("tteesstt"))
			{
				System.out.println("Message error.");
				System.exit(0);
			}
		}
		end = System.currentTimeMillis();
		
		us = (end - start) * 1000;
		
		System.out.println(REPEATS + " round trips in " + us +
				" us; time each = " + us / REPEATS + " us; " +
				"approx " + 1000000 / (us / REPEATS) + " per second.");

		scop.close();
	}
}
