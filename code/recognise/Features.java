import java.util.*;

class Features
{
	Ranges leftarm, rightarm;
	double relocation;
	
	Features(CircularBuffer buf, int windowsize)
	{
		Frame f;
		leftarm = new Ranges();
		rightarm = new Ranges();
		for(int i = 0; i < windowsize; i++)
		{
			f = buf.get(windowsize, i);
			leftarm.update(f.left);
			rightarm.update(f.right);
		}
		
		Frame first, last;
		first = buf.get(windowsize, 0);
		last = buf.get(windowsize, windowsize - 1);
		relocation = calc_relocation(first, last);
	}
	
	double calc_relocation(Frame first, Frame last)
	{
		double relocation = 0.0;
		
		relocation += Utils.square(last.left.tx - first.left.tx);
		relocation += Utils.square(last.left.ty - first.left.ty);
		relocation += Utils.square(last.left.tz - first.left.tz);
		relocation += Utils.square(last.right.tx - first.right.tx);
		relocation += Utils.square(last.right.ty - first.right.ty);
		relocation += Utils.square(last.right.tz - first.right.tz);
		/*
		relocation += Math.abs(last.left.tx - first.left.tx);
		relocation += Math.abs(last.left.ty - first.left.ty);
		relocation += Math.abs(last.left.tz - first.left.tz);
		relocation += Math.abs(last.right.tx - first.right.tx);
		relocation += Math.abs(last.right.ty - first.right.ty);
		relocation += Math.abs(last.right.tz - first.right.tz);
		*/
		
		return relocation;
	}
	
	Features(ArrayList<Frame> data)
	{
		leftarm = new Ranges();
		rightarm = new Ranges();
		for(Frame f: data)
		{
			leftarm.update(f.left);
			rightarm.update(f.right);
		}
		
		Frame first, last;
		first = data.get(0);
		last = data.get(data.size() - 1);
		relocation = calc_relocation(first, last);
	}
	
	void dump()
	{
		System.out.print("Left  Arm: ");
		leftarm.dump();
		System.out.print("Right Arm: ");
		rightarm.dump();
		System.out.printf("Relocation: %f\n", relocation);
	}
};

class Ranges
{
	private double[] min;
	private double[] max;
	
	Ranges()
	{
		min = new double[3];
		max = new double[3];
		for(int i = 0; i < 3; i++)
		{
			min[i] = 9999.0;
			max[i] = -9999.0;
		}
	}

	void update(SixDOF bodypart)
	{
		min[0] = Math.min(min[0], bodypart.tx);
		max[0] = Math.max(max[0], bodypart.tx);
		min[1] = Math.min(min[1], bodypart.ty);
		max[1] = Math.max(max[1], bodypart.ty);
		min[2] = Math.min(min[2], bodypart.tz);
		max[2] = Math.max(max[2], bodypart.tz);
	}

	double get_delta(int axis)
	{
		return (max[axis] - min[axis]);
	}
	
	void dump()
	{
		int cm = 10;
		double[] d = new double[3];
		
		for(int i = 0; i < 3; i++)
			d[i] = get_delta(i) / cm;
		System.out.printf("dx = %4.0f, dy = %4.0f, dz = %4.0f\n",
				d[0], d[1], d[2]);
	}
};
