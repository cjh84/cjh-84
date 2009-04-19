// A virtual Vicon system to read in recorded training data

import java.io.*;
import java.util.*;

class GestureReader
{
	static void usage()
	{
		System.out.println("Usage: java GestureReader <filename.csv>");
		System.exit(0);
	}
	
	static void dump(ArrayList<Frame> data)
	{
		for(Frame f: data)
			f.dump();
	}
	
	/* Print average arm positions every 10 frames, relative to start, in cm: */
	static void summarise(ArrayList<Frame> data)
	{
		final int bunch = 1;
		final int cm = 10; // 10mm per cm
		int count = 0;
		double lx, ly, lz, rx, ry, rz, heading, shoulders;
		double lx0, ly0, lz0, rx0, ry0, rz0;
		boolean first = true;
		
		System.out.println("LA-Tx,LA-Ty,LA-Tz,RA-Tx,RA-Ty,RA-Tz,Body-A,Shld-A");
		lx = ly = lz = rx = ry = rz = heading = shoulders = 0.0;
		lx0 = ly0 = lz0 = rx0 = ry0 = rz0 = 0.0;
		for(Frame f: data)
		{
			lx += f.left.tx / cm;
			ly += f.left.ty / cm;
			lz += f.left.tz / cm;
			rx += f.right.tx / cm;
			ry += f.right.ty / cm;
			rz += f.right.tz / cm;
			/* Don't average heading and shoulders, because arithmetic mean
				doesn't work for angles */
			heading = f.body.calcHeading() * 180.0 / Math.PI;
			shoulders = f.shoulderAngle() * 180.0 / Math.PI;
			count++;
			if(first && false)
			{
				lx0 = lx; ly0 = ly; lz0 = lz;
				rx0 = rx; ry0 = ry; rz0 = rz;
				first = false;
			}
			if(count == bunch)
			{
				System.out.printf("%5.0f,%5.0f,%5.0f,%5.0f,%5.0f,%5.0f" +
						",%6.1f,%6.1f\n",
						lx / bunch - lx0, ly / bunch - ly0, lz / bunch - lz0,
						rx / bunch - rx0, ry / bunch - ry0, rz / bunch - rz0,
						heading, shoulders);
				lx = ly = lz = rx = ry = rz = 0.0;
				count = 0;
			}
		}
	}
	
	public static void main(String[] argv)
	{
		String filename;
		ArrayList<Frame> data;
	
		if(argv.length != 1)
			usage();
		filename = argv[0];
		
		data = getData(filename);
		dump(data);
	}
	
	static SixDOF parse(String s)
	{
		String[] values;
		SixDOF sixdof = new SixDOF();
		
		values = s.split(",");
		if(values.length != 7)
		{
			System.out.println("Parse error on line <" + s + "> (" +
					values.length + " values)");
			System.exit(0);
		}
		sixdof.ax = Double.valueOf(values[1]) * Math.PI / 180.0;
		sixdof.ay = Double.valueOf(values[2]) * Math.PI / 180.0;
		sixdof.az = Double.valueOf(values[3]) * Math.PI / 180.0;
		sixdof.tx = Double.valueOf(values[4]);
		sixdof.ty = Double.valueOf(values[5]);
		sixdof.tz = Double.valueOf(values[6]);
		sixdof.ax *= Math.PI / 180.0;
		sixdof.normalise();
		return sixdof;
	}
	
	static final int Body = 0;
	static final int LeftArm = 1;
	static final int RightArm = 2;
	
	static ArrayList<Frame> getData(String filename)
	{
		ArrayList<Frame> data = new ArrayList<Frame>();
		String line;
		int frameno = 0;
		SixDOF sixdof;
		char c;
		
		int bodypart = -1, startpart;
		
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while((line = in.readLine()) != null)
			{
				if(line.length() == 0)
					continue;
				startpart = -1;
				c = line.charAt(0);
				if(c >= '0' && c <= '9')
				{
					sixdof = parse(line);
					while(frameno > data.size() - 1)
						data.add(new Frame());
					if(bodypart == Body)
						data.get(frameno).body = sixdof;
					else if(bodypart == LeftArm)
						data.get(frameno).left = sixdof;
					else if(bodypart == RightArm)
						data.get(frameno).right = sixdof;
					else
						Utils.error("Frame data received for unknown body part");
					frameno++;
				}
				else if(line.startsWith("Belt") || line.startsWith("Hat") ||
						line.startsWith("BeltP1") || line.startsWith("BeltP2") ||
						line.startsWith("BodyP1"))
					startpart = Body;
				else if(line.startsWith("LeftHand") ||
						line.startsWith("LeftArm") ||
						line.startsWith("LeftArmP1") ||
						line.startsWith("LeftArmP2"))
					startpart = LeftArm;
				else if(line.startsWith("RightHand") ||
						line.startsWith("RightArm") ||
						line.startsWith("RightArmP1") ||
						line.startsWith("RightArmP2"))
					startpart = RightArm;
				else
					Utils.error("Invalid line in data file.");
				
				if(startpart >= 0)
				{
					// Start new body part:
					bodypart = startpart;
					frameno = 0;
					if((line = in.readLine()) == null)
						Utils.error("Unexpected end of file.");
				}
			}
			in.close();
		}
		catch(IOException e)
		{
			System.out.println("Cannot read from <" + filename + ">");
			System.exit(0);
		}
		Frame last = data.get(data.size() - 1);
		if(last.body == null || last.left == null || last.right == null)
			Utils.error("Different number of frames for different body parts.");
		return data;
	}
};
