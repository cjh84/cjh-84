// Transforms world coordinates to body coordinates

import java.io.*;
import java.util.*;

class Transform
{
	// Flag specifies whether we attempt rotation, or just dump the raw data:
	static final boolean DO_PROCESSING = false;

	static void usage()
	{
		System.out.println("Usage: java Transform <filename.csv>");
		System.exit(0);
	}
	
	public static void main(String[] argv)
	{
		String filename;
		ArrayList<Frame> data;
	
		if(argv.length != 1)
			usage();
		filename = argv[0];
		
		data = GestureReader.getData(filename);
		
		if(DO_PROCESSING)
			process(data);
		GestureReader.summarise(data);
		Features feat = new Features(data);
		feat.dump();
	}
	
	static void process(ArrayList<Frame> data)
	{
		double theta;
		
		for(Frame frame: data)
		{
			frame.left.translate(frame.body);
			frame.right.translate(frame.body);

			theta = frame.body.calcHeading();

			frame.left.rotate(theta);
			frame.right.rotate(theta);			
		}
	}
};
