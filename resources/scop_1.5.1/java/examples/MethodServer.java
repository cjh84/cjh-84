// MethodServer.java - DMI - 25-12-02

public class MethodServer
{
	static int invocations = 0;

	public static void main(String[] a)
	{
		SCOPXML scop;
		Vertex v, w, args;
		String method;

		scop = new SCOPXML("localhost", "MethodServer");
		while(true)
		{
			v = scop.get_request();
			method = v.extract_method();
			args = v.extract_args();
			w = null;
			if(method.equals("ctof"))
				w = Vertex.pack(cent_to_faren(args.extract_double()));
			else if(method.equals("ftoc"))
				w = Vertex.pack(faren_to_cent(args.extract_double()));			
			else if(method.equals("stats"))
				w = Vertex.pack(invocations);
			else
				System.exit(0);
			scop.send_reply(w);
		}
		// scop.close();
	}

	private static double cent_to_faren(double c)
	{
		invocations++;
		return (9.0 * c / 5.0) + 32.0;
	}

	private static double faren_to_cent(double f)
	{
		invocations++;
		return (f - 32.0) * 5.0 / 9.0;
	}
}
