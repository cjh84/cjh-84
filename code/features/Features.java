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
		System.out.print("Left  Arm: ");
		leftarm.dump();
		System.out.print("Right Arm: ");
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
		int cm = 10;
		
		double dx = (maxx - minx) / cm;
		double dy = (maxy - miny) / cm;
		double dz = (maxz - minz) / cm;
		System.out.printf("dx = %4.0f, dy = %4.0f, dz = %4.0f\n", dx, dy, dz);
	}
};
