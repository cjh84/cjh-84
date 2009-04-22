import java.io.*;

class capture
{
	public static void main (String[] args) 
	{
		DataParserR dataparser = null;
		SCOP scop1, scop2;
		double[] data;
		int points;
		String scopserver;
		
		try
		{
			dataparser = new DataParserR();
		}
		catch(IOException e)
		{
			System.out.println("Cannot open connection to Vicon");
			System.exit(0);
		}

		scopserver = Config.lookup("coordserver");
		scop1 = new SCOP(scopserver, "capturep1");
		scop2 = new SCOP(scopserver, "capturep2");
		scop1.set_source_hint("p1coords");
		scop2.set_source_hint("p2coords");

		while(true)
		{
			data = dataparser.getData();
			points = data.length;
			if(points != 18 && points != 36)
			{
				System.out.println("Received " + points + " data points; " +
					"expected 18 or 36.");
				System.exit(0);
			}
			output(data, 0, scop1, "P1");
			if(points == 36)
				output(data, 18, scop2, "P2");
		}
	}
	
	public static void output(double[] data, int startpos, SCOP scop,
			String label)
	{
		Frame f = new Frame(data, startpos);
		String s = f.toString();		
		System.out.println(label + ": " + s);
		scop.emit(s);
	}
}           
