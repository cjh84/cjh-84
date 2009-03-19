// Server.cpp - DMI - 21-9-02

import java.io.*;

public class Server
{
	public static void main(String[] args)
	{
		SCOP scop;
		String query, reply;
		char[] buf;
		int len;

		scop = new SCOP("localhost", "server");
		while(true)
		{
			query = scop.get_message();
			
			len = query.length();
			buf = new char[len * 2];
			for(int i = 0; i < len; i++)
				buf[i * 2] = buf[i * 2 + 1] = query.charAt(i);
			
			reply = new String(buf);
			scop.send_reply(reply);
		}
	}
}
