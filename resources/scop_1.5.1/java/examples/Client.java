// Client.java - DMI - 21-9-02

/* Usage: java Client [ <query> ]   (default query is "Hello world!") */

import java.io.*;

public class Client
{
	public static void main(String[] args)
	{
		SCOP scop;
		String query = args.length > 0 ? args[0] : "Hello world!";
		String reply;

		scop = new SCOP("localhost", "client");
		reply = scop.rpc("server", query);
		System.out.println("Query <" + query + ">, Reply <" + reply + ">");
		scop.close();
	}
}
