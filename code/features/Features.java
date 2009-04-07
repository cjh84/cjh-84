import java.util.*;

class Features
{
	Ranges leftarm, rightarm;
	
	Features(ArrayList<Frame> data)
	{
		leftarm = new Ranges();
		rightarm = new Ranges();
		for(Frame f: data)
		{
			leftarm.update(f.left);
			rightarm.update(f.right);
		}
	}
	
	void dump()
	{
		System.out.println("Left Arm:");
		leftarm.dump();
		System.out.println("Right Arm:");
		rightarm.dump();
	}
};

class Ranges
{
	double minx, maxx, miny, maxy, minz, maxz;
	
	Ranges()
	{
		minx = miny = minz = 9999.0;
		maxx = maxy = maxz = -9999.0;
	}

	void update(SixDOF bodypart)
	{
		minx = Math.min(minx, bodypart.tx);
		maxx = Math.max(maxx, bodypart.tx);
		miny = Math.min(miny, bodypart.ty);
		maxy = Math.max(maxy, bodypart.ty);
		minz = Math.min(minz, bodypart.tz);
		maxz = Math.max(maxz, bodypart.tz);
	}

	void dump()
	{
		double dx = maxx - minx;
		double dy = maxy - miny;
		double dz = maxz - minz;
		System.out.printf("dx = %f, dy = %f, dz = %f\n", dx, dy, dz);
	}
};
