// Geometric classes for use by both Transform and GestureReader

import java.io.*;
import java.util.*;

class SixDOF
{
	double ax, ay, az, angle, tx, ty, tz;

	static final double EPSILON = 1.0e-5;

	SixDOF()
	{
		angle = 1.0;
	}
		
	void dump()
	{
		System.out.printf("%6.2f,%6.2f,%6.2f,%5.0f,%5.0f,%5.0f,%5.0f",
				ax, ay, az, angle * 180.0 / Math.PI, tx, ty, tz);
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
		{
			angle = 0.0;
			if(ax < EPSILON)
			{
				/* Dropouts are reported as ax=ay=az=0. Make sure at least
					one co-ord is non-zero to avoid any non-unit vectors: */
				ax = 1.0;
				ay = az = 0.0;
			}
		}
	}

	void rotate(double bearing)
	{
		double c = Math.cos(bearing);
		double s = Math.sin(bearing);
		
		tx = c*tx - s*ty;
		ty = s*tx + c*ty;
		
		ax = c*ax - s*ay;
		ay = s*ax + c*ay;
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
	static final double FUDGE = -1.0;
	
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
		
		double magnitude, s, c, t;
		Point p = new Point();
		
		if(angle < EPSILON)
			return new Point(x0, y0, z0);
		angle *= FUDGE;
		
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
};
