// SOS.java - DMI - 27-25-02

import java.io.*;

/* Unlike the C++ version, we don't log messages to syslog or detach
	to run in the background... so this is a fairly boring example! */

public class SOS
{
	public static void main(String[] args)
	{
		SCOP scop;
		String buf;
		int rpc_flag;

		scop = new SCOP("localhost", "SOS", true);
		if(scop.connection_ok() == false)
		{
			System.out.println("Can't connect to scopserver.");
			System.exit(0);
		}

		while(true)
		{
			buf = scop.get_message();
			if(buf == null)
			{
				System.out.println("Lost connection to scopserver.");
				System.exit(0);
			}
			System.out.println(buf);
		}
		// scop.close();
	}
}
