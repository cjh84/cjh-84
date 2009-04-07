// Transforms world coordinates to body coordinates

import java.io.*;
import java.util.*;
import javax.vecmath.*;

class Transform
{
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
		
		Vector3d v;
		for(Frame f: data)
		{
			v = calcVec(f.body, 0);
			System.out.println(v.toString());
		}
		/*
   	 headingtest(frames)
   	 sys.exit(0)
   	 feat = features.Features(frames)
   	 feat.dump()
   	 process(frames)
   	 feat = features.Features(frames)
   	 feat.dump()
		*/
	}
	
	public static Vector3d calcVec(SixDOF sixdof, int vector)
	{
		// Translate from Axis-Angle to Matrix form 
		
		AxisAngle4d aa = new AxisAngle4d(sixdof.ax, sixdof.ay, sixdof.az,
				sixdof.angle);
		Matrix3d mat = new Matrix3d();
		mat.set(aa);
		 
		double xx = 0; double yy = 0; double zz = 0; 
		if (vector == 0) xx = 1;   
		if (vector == 1) yy = 1; 
		if (vector == 2) zz = 1; 
		 
		Tuple3d tup = new Vector3d(xx, yy, zz);
		// Multiply matrix by directional vector choosen 
		mat.transform(tup);
		
		// Normalize and change to vector form 
		Vector3d vec = new Vector3d(tup);
		vec.normalize();

		return vec;	
	}

	void process(ArrayList<Frame> data)
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
EPSILON = 1.0e-5
DEBUG = False
STDOUT = True

def translate(obj,axes):
    obj.tx -= axes.tx
    obj.ty -= axes.ty
    obj.tz -= axes.tz

def rotate(obj,axes):
    obj.tx, obj.ty, obj.tz = rotate_point(axes.ax, axes.ay, axes.az, \
        obj.tx, obj.ty, obj.tz)
    obj.ax, obj.ay, obj.az = rotate_point(axes.ax, axes.ay, axes.az, \
        obj.ax, obj.ay, obj.az)

def rotate_point(ax,ay,az,x0,y0,z0):
    ''' ax,ay,az is angle-axis rotation
        x0,y0,z0 is point to be rotated
        Inefficient if rotating multiple points by same angle-axis vector '''

    magnitude = math.sqrt(ax*ax + ay*ay + az*az)

    if magnitude < EPSILON:
        return x0,y0,z0

    ax /= magnitude
    ay /= magnitude
    az /= magnitude

    if DEBUG:
        print 'ax=%f, ay=%f, az=%f, magnitude=%f' % (ax,ay,az,magnitude)
    s = math.sin(magnitude)
    c = math.cos(magnitude)
    t = 1 - c

    ''' Graphics Gems (Glassner, Academic Press, 1990) '''
    #http://www.gamedev.net/reference/articles/article1199.asp
    x1 = (t*ax*ax + c)*x0 + (t*ax*ay + s*az)*y0 + (t*ax*az - s*ay)*z0
    y1 = (t*ax*ay - s*az)*x0 + (t*ay*ay + c)*y0 + (t*ay*az + s*ax)*z0
    z1 = (t*ax*az + s*ay)*x0 + (t*ay*az - s*ax)*y0 + (t*az*az + c)*z0

    return x1,y1,z1

def headingtest(frames):
    print 'Transformed body angles:'
    for frame in frames:
        #fx,fy,fz = rotate_point(frame.body.ax, frame.body.ay, frame.body.az, \
        #    0.0, 1.0, 0.0)
        #print '%7.2f%7.2f%7.2f' % (fx,fy,fz)
        ex,ey,ez = euler.to_euler(frame.body.ax, frame.body.ay, frame.body.az)
        print '%7.2f%7.2f%7.2f' % (ex,ey,ez)

def usage():
    print "Usage: transform.py <filename>"

*/
