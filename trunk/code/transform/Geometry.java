// Geometric classes for use by both Transform and GestureReader

import java.io.*;
import java.util.*;

class SixDOF
{
	double ax, ay, az, angle, tx, ty, tz;
	boolean dropout;

	static final double EPSILON = 1.0e-5;

	SixDOF()
	{
		angle = 1.0;
		dropout = false;
	}
	
	SixDOF(double[] a, int offset)
	{
		angle = 1.0;
		dropout = false;
		ax = a[offset + 0] * Math.PI / 180.0;
		ay = a[offset + 1] * Math.PI / 180.0;
		az = a[offset + 2] * Math.PI / 180.0;
		tx = a[offset + 3];
		ty = a[offset + 4];
		tz = a[offset + 5];
		normalise();
	}
	
	public String toString()
	{
		double ax1, ay1, az1;
		String s;
		
		ax1 = ax * angle * 180.0 / Math.PI;
		ay1 = ay * angle * 180.0 / Math.PI;
		az1 = az * angle * 180.0 / Math.PI;
		s = ax1 + " " + ay1 + " " + az1 + " ";
		s = s + tx + " " + ty + " " + tz;
		return s;
	}
	
	public double[] toDoubles()
	{
		//Assume everything normalised and processed
		double[] d = new double[3];
		d[0] = tx;
		d[1] = ty;
		d[2] = tz;
		return d;
	}
		
	void dump()
	{
		System.out.printf("%6.2f,%6.2f,%6.2f,%5.0f,%5.0f,%5.0f,%5.0f",
				ax, ay, az, angle * 180.0 / Math.PI, tx, ty, tz);
	}
	
	void normalise()
	{
		angle *= Math.sqrt(ax * ax + ay * ay + az * az);
		if(Math.abs(angle) >= EPSILON)
		{
			ax /= angle;
			ay /= angle;
			az /= angle;
		}
		else if(ax < EPSILON && ay < EPSILON && az < EPSILON)
		{
			/* Dropouts report a (0,0,0) axis-angle vector: record
				these with a flag and convert them to a harmless zero
				rotation around the Z-axis: */
			dropout = true;
			angle = 0.0;
			ax = ay = 0.0;
			az = 1.0;
		}
	}

	void rotate(double bearing)
	{
		double newx, newy;
		
		double c = Math.cos(-bearing);
		double s = Math.sin(-bearing);
		
		newx = c*tx - s*ty;
		newy = s*tx + c*ty;
		tx = newx;
		ty = newy;

		newx = c*ax - s*ay;
		newy = s*ax + c*ay;
		ax = newx;
		ay = newy;
	}
	
	double calcHeading()
	{
		double theta;
		
		Point p = Point.rotatePoint(ax, ay, az, angle, 0.0, 1.0, 0.0);
		theta = Math.atan2(p.y, p.x);
		return theta;
	}

	void translate(SixDOF axes)
	{
		tx -= axes.tx;
		ty -= axes.ty;
		tz -= axes.tz;
	}
};

class Frame
{
	SixDOF body, left, right;

	Frame()
	{
		body = left = right = null;
	}

	Frame(String s)
	{
		String[] values;
		double[] a;
		
		values = s.split(" ");
		if(values.length != 19)
			Utils.error("Expected 19 values");
		a = new double[18];
		for(int i = 0; i < 18; i++)
			a[i] = Double.valueOf(values[i]);
		
		body = new SixDOF(a, 0);
		left = new SixDOF(a, 6);
		right = new SixDOF(a, 12);
		
		if(values[18].equals("dropout"))
			body.dropout = true;
		else if(!values[18].equals("ok"))
			Utils.error("Unrecognised frame status");
	}

	Frame(double[] a, int offset)
	{
		if(a.length - offset < 18)
			Utils.error("Expected at least 18 doubles");
		
		body = new SixDOF(a, offset + 0);
		left = new SixDOF(a, offset + 6);
		right = new SixDOF(a, offset + 12);
	}

	boolean dropout()
	{
		if(body.dropout || left.dropout || right.dropout)
			return true;
		return false;
	}
		
	public String toString()
	{
		String status;
		
		if(dropout())
			status = "dropout";
		else
			status = "ok";
		return body.toString() + " " + left.toString() + " " +
				right.toString() + " " + status;
	}
			
	public double[] toDoubles()
	{
		/* Returns all tx,ty,tz which are already assumed to be 
		normalised and processed */
		double[] doubles = new double[9]; 
		double[] b = body.toDoubles();
		double[] l = left.toDoubles();
		double[] r = right.toDoubles();
		doubles[0] = b[0];
		doubles[1] = b[1];
		doubles[2] = b[2];
		doubles[3] = l[0];
		doubles[4] = l[1];
		doubles[5] = l[2];
		doubles[6] = r[0];
		doubles[7] = r[1];
		doubles[8] = r[2];
		return doubles;
	}
			
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
		System.out.println("BodyAx,BodyAy,BodyAz,BodyR,BodTx,BodTy,BodTz," +
				"LArmAx,LArmAy,LArmAz,LArmR,LArTx,LArTy,LArTz," +
				"RArmAx,RArmAy,RArmAz,RArmR,RArTx,RArTy,RArTz");
	}
	
	double shoulderAngle()
	{
		double dx, dy, theta;
		
		dx = right.tx - left.tx;
		dy = right.ty - left.ty;
		theta = Math.atan2(dy, dx);
		theta += Math.PI / 2.0; // Zero degrees is along the X axis
		if(theta > Math.PI)
			theta -= 2.0 * Math.PI;
		return theta;
	}
};

class Point
{
	static final double EPSILON = 1.0e-5;
	
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
	
	static Point rotatePoint(double ax, double ay, double az, double angle,
			double x0, double y0, double z0)
	{
		/* ax,ay,az,angle is angle-axis rotation
			x0,y0,z0 is point to be rotated
			Inefficient if rotating multiple points by same angle-axis vector */
		
		double s, c, t;
		Point p = new Point();
		
		if(Math.abs(angle) < EPSILON)
			return new Point(x0, y0, z0);
		
		s = Math.sin(-angle);
		c = Math.cos(-angle);
		t = 1 - c;
		
		/* Graphics Gems (Glassner, Academic Press, 1990)
			http://www.gamedev.net/reference/articles/article1199.asp */
		
		p.x = (t*ax*ax + c)*x0 + (t*ax*ay + s*az)*y0 + (t*ax*az - s*ay)*z0;
		p.y = (t*ax*ay - s*az)*x0 + (t*ay*ay + c)*y0 + (t*ay*az + s*ax)*z0;
		p.z = (t*ax*az + s*ay)*x0 + (t*ay*az - s*ax)*y0 + (t*az*az + c)*z0;
				
		return p;
	}
};
