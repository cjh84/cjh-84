// Transforms world coordinates to body coordinates

import java.io.*;
import java.util.*;
import javax.vecmath.*;

class Point
{
	double x, y, z;
	
	Point(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	Point()
	{
		x = y = z = 0.0;
	}
};

class Transform
{
	static final double EPSILON = 1.0e-5;
	static double fudge = 1.0;

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
		
		headingtest(data);
		System.exit(0);
		
		process(data);
		Features feat = new Features(data);
		feat.dump();
	}
	
	static void headingtest(ArrayList<Frame> data)
	{
		Vector3d v;
		double theta;
		
		System.out.println("Transformed body angles:");
		for(Frame frame: data)
		{
			theta = calcHeading(frame.body) * 180.0 / Math.PI;
			System.out.printf("%5.1f   ", theta);

			Point p = rotatePoint(frame.body.ax, frame.body.ay, frame.body.az,
					frame.body.angle,	0.0, 1.0, 0.0);
			theta = Math.atan2(p.y, p.x) * 180.0 / Math.PI;
			System.out.printf("%5.1f\n", theta);
			// System.out.printf("%7.2f%7.2f%7.2f", p.x, p.y, p.z);
		}
	}
	
	static double calcHeading(SixDOF sixdof)
	{
		double theta;
		
		// Translate from Axis-Angle to Matrix form		
		AxisAngle4d aa = new AxisAngle4d(sixdof.ax, sixdof.ay, sixdof.az,
				sixdof.angle);
		Matrix3d mat = new Matrix3d();
		mat.set(aa);
		 
		Tuple3d tup = new Vector3d(0.0, 1.0, 0.0);
		// Multiply matrix by directional vector choosen 
		mat.transform(tup);

		theta = Math.atan2(tup.y, tup.x);
		return theta;
	}

	static Point rotatePoint(double ax, double ay, double az, double angle,
			double x0, double y0, double z0)
	{
		/* ax,ay,az,angle is angle-axis rotation
			x0,y0,z0 is point to be rotated
			Inefficient if rotating multiple points by same angle-axis vector */
		
		double magnitude, s, c, t;
		Point p = new Point();
		
		if(angle < EPSILON)
			return new Point(x0, y0, z0);
		angle *= fudge;
		
		s = Math.sin(angle);
		c = Math.cos(angle);
		t = 1 - c;
		
		/* Graphics Gems (Glassner, Academic Press, 1990)
			http://www.gamedev.net/reference/articles/article1199.asp */
		
		p.x = (t*ax*ax + c)*x0 + (t*ax*ay + s*az)*y0 + (t*ax*az - s*ay)*z0;
		p.y = (t*ax*ay - s*az)*x0 + (t*ay*ay + c)*y0 + (t*ay*az + s*ax)*z0;
		p.z = (t*ax*az + s*ay)*x0 + (t*ay*az - s*ax)*y0 + (t*az*az + c)*z0;
				
		return p;
	}
	 
	static void process(ArrayList<Frame> data)
	{
		Frame.headings();
		for(Frame frame: data)
		{
			/*
			translate(frame.leftarm, frame.body);
			translate(frame.rightarm, frame.body);

			rotate(frame.leftarm, frame.body);
			rotate(frame.rightarm, frame.body);
			*/
		}
	}
};

/*
def translate(obj,axes):
    obj.tx -= axes.tx
    obj.ty -= axes.ty
    obj.tz -= axes.tz

def rotate(obj,axes):
    obj.tx, obj.ty, obj.tz = rotate_point(axes.ax, axes.ay, axes.az, \
        obj.tx, obj.ty, obj.tz)
    obj.ax, obj.ay, obj.az = rotate_point(axes.ax, axes.ay, axes.az, \
        obj.ax, obj.ay, obj.az)
*/
