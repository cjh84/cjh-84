// A virtual Vicon system to read in recorded training data

import java.io.*;
import java.util.*;

class SixDOF
{
	double ax, ay, az, tx, ty, tz, angle;

	static final double EPSILON = 1.0e-5;

	SixDOF()
	{
		angle = 1.0;
	}
		
	void dump()
	{
		System.out.printf("%7.2f,%7.2f,%7.2f,%7.0f,%7.0f,%7.0f",
				ax, ay, az, tx, ty, tz);
	}
	
	void normalise()
	{
		angle *= Math.sqrt(ax * ax + ay * ay + az * az);
		if(angle >= EPSILON)
		{
			ax /= angle;
			ay /= angle;
			az /= angle;
		}
		else
			angle = 0.0;
	}
};

class Frame
{
	SixDOF body, left, right;
	
	void dump()
	{
		body.dump();
		System.out.print(",");
		left.dump();
		System.out.print(",");
		right.dump();
		System.out.println();
	}
	
	static void headings()
	{
		System.out.println("BodyAx BodyAy BodyAz  LArmTx LArmTy LArmTz" +
				"  RArmTx RArmTy RArmTz");
	}
};

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
	
	private static void error(String msg)
	{
		System.out.println(msg);
		System.exit(0);
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
	
	static ArrayList<Frame> getData(String filename)
	{
		ArrayList<Frame> data = new ArrayList<Frame>();
		String line;
		int bodypart = 0, frameno = 0;
		int startpart;
		SixDOF sixdof;
		char c;
		
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while((line = in.readLine()) != null)
			{
				if(line.length() == 0)
					continue;
				startpart = 0;
				c = line.charAt(0);
				if(c >= '0' && c <= '9')
				{
					sixdof = parse(line);
					if(bodypart > 1 && frameno >= data.size())
						error("Too many data points");
					if(bodypart == 1)
					{
						Frame f = new Frame();
						f.body = sixdof;
						data.add(f);
					}
					else if(bodypart == 2)
						data.get(frameno).left = sixdof;
					else if(bodypart == 3)
						data.get(frameno).right = sixdof;
					else
						error("Frame data received for unknown body part");
					frameno++;
				}
				else if(line.startsWith("Belt") || line.startsWith("Hat") ||
						line.startsWith("BeltP1") || line.startsWith("BeltP2") ||
						line.startsWith("BodyP1"))
					startpart = 1;
				else if(line.startsWith("LeftHand") ||
						line.startsWith("LeftArm") ||
						line.startsWith("LeftArmP1") ||
						line.startsWith("LeftArmP2"))
					startpart = 2;
				else if(line.startsWith("RightHand") ||
						line.startsWith("RightArm") ||
						line.startsWith("RightArmP1") ||
						line.startsWith("RightArmP2"))
					startpart = 3;
				else
					error("Invalid line in data file.");
				
				if(startpart > 0)
				{
					assert bodypart == startpart - 1: "Unexpected body part";
					bodypart++;
					frameno = 0;
					if((line = in.readLine()) == null)
						error("Unexpected end of file.");
				}
			}
			in.close();
		}
		catch(IOException e)
		{
			System.out.println("Cannot read from <" + filename + ">");
			System.exit(0);
		}
		assert bodypart == 3: "Wrong number of body parts";
		return data;
	}
};
