import java.io.*;

class Capture
{
	static int FRAME_RATE = -1;
	static boolean dump = false;
	
	static void usage()
	{
		System.out.println("Usage: java Capture [downsample] [dump]");
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		for(int i = 0; i < argv.length; i++)
		{
			if(argv[i].equals("downsample"))
				FRAME_RATE = Integer.valueOf(Config.lookup("framerate"));
			else if(argv[i].equals("dump"))
				dump = true;
			else
				usage();
		}
	}
	
	public static void main (String[] args) 
	{
		DataParserR dataparser = null;
		SCOP scop1, scop2;
		double[] data;
		int points;
		String scopserver;
		int framecounter = 0, lastfps = 0;
		long init_time, start_time, current_time, elapsed_time;
		
		try
		{
			dataparser = new DataParserR();
		}
		catch(IOException e)
		{
			System.out.println("Cannot open connection to Vicon");
			System.exit(0);
		}

		parse_args(args);
		
		scopserver = Config.lookup("coordserver");
		scop1 = new SCOP(scopserver, "capturep1");
		scop2 = new SCOP(scopserver, "capturep2");
		scop1.set_source_hint("p1coords");
		scop2.set_source_hint("p2coords");

		init_time = start_time = System.currentTimeMillis();
		while(true)
		{
			data = dataparser.getData();
			if(framecounter - lastfps > 1000)
			{
				current_time = System.currentTimeMillis();
				elapsed_time = current_time - start_time;
				System.out.printf("FPS = %.1f\n", (double)(framecounter - lastfps)
						/ (double)elapsed_time * 1000.0);
				lastfps = framecounter;
				start_time = current_time;
			}
			points = data.length;
			if(points != 18 && points != 36)
			{
				System.out.println("Received " + points + " data points; " +
					"expected 18 or 36.");
				System.exit(0);
			}
			current_time = System.currentTimeMillis();
			elapsed_time = current_time - init_time;
			if(FRAME_RATE < 0 || elapsed_time > (framecounter * 1000) / FRAME_RATE)
			{
				framecounter++;
				output(data, 0, scop1, "P1");
				if(points == 36)
					output(data, 18, scop2, "P2");
			}
		}
	}
	
	public static void output(double[] data, int startpos, SCOP scop,
			String label)
	{
		Frame f = new Frame(data, startpos);
		String s = f.toString();
		if(dump)
			System.out.println(label + ": " + s);
		scop.emit(s);
	}
}           
