// Transforms world coordinates to body coordinates

import java.io.*;
import java.util.*;

class Transform
{
	static boolean do_processing = false, featurise = false;
	/* do_processing specifies whether we attempt rotation, or just
		take raw data */
	static String filename;
		
	static void usage()
	{
		System.out.println("Usage: java Transform [options...] <filename.csv>");
		System.out.println("Options: process   - convert arms to body " +
				"coordinate system");
		System.out.println("         features  - display features instead " +
				"of frame data");
		System.out.println("Default: display untransformed frame data");
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		if(argv.length < 1)
			usage();

		for(int i = 0; i < argv.length - 1; i++)
		{
			if(argv[i].equals("process"))
				do_processing = true;
			else if(argv[i].equals("features"))
				featurise = true;
			else
				usage();
		}
		filename = argv[argv.length - 1];		
	}
	
	public static void main(String[] argv)
	{
		ArrayList<Frame> data;

		parse_args(argv);	
		data = GestureReader.getData(filename);
		System.out.println(data.size() + " frames");
		
		if(do_processing)
			process(data);
		if(featurise)
		{
			Features feat = new Features(data);
			feat.dump();
		}
		else
			GestureReader.summarise(data);
	}
	
	static void process(ArrayList<Frame> data)
	{
		for(Frame frame: data)
			process(frame);
	}
	
	static void process(Frame f)
	{
		double theta;
		
		f.left.translate(f.body);
		f.right.translate(f.body);

		theta = f.body.calcHeading() - (Math.PI / 2.0);

		f.left.rotate(theta);
		f.right.rotate(theta);
	}
};
