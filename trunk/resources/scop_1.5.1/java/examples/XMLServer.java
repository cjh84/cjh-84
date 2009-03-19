// XMLServer.java - DMI - 25-12-02

import java.io.*;

public class XMLServer
{
	public static void main(String[] args)
	{
		SCOPXML scop;
		Vertex v, w;

		scop = new SCOPXML("localhost", "XMLServer");
		while(true)
		{
			v = scop.get_request();
			w = Vertex.pack(combi(v.extract_int(0), v.extract_int(1)));
			scop.send_reply(w);
		}
		// scop.close();
	}

	private static int combi(int n, int k)
	{
		int result = 1;
		if(k > n || k < 0)
			return 0;

		for(int i = 0; i < k; i++)
			result *= n - i;

		for(int i = 1; i <= k; i++)
			result /= i;

		return result;
	}
}
