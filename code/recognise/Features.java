import java.util.*;

class Features
{
	Ranges leftarm, rightarm;
	double displacement;
	ArrayList<Frame> rawdata;
	
	static final int num_features = 7;

	Features(ArrayList<Frame> data)
	{
		rawdata = data; //.clone()
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
		displacement = calc_displacement(first, last);
	}
	
	Features(CircularBuffer buf, int windowsize)
	{
	    rawdata = new ArrayList<Frame>();
		Frame f;
		leftarm = new Ranges();
		rightarm = new Ranges();
		for(int i = 0; i < windowsize; i++)
		{
			f = buf.get(windowsize, i);
			rawdata.add(f);
			leftarm.update(f.left);
			rightarm.update(f.right);
		}
		
		Frame first, last;
		first = buf.get(windowsize, 0);
		last = buf.get(windowsize, windowsize - 1);
		displacement = calc_displacement(first, last);
	}
	
	double calc_displacement(Frame first, Frame last)
	{
		double displacement = 0.0;
		
		displacement += Utils.square(last.left.tx - first.left.tx);
		displacement += Utils.square(last.left.ty - first.left.ty);
		displacement += Utils.square(last.left.tz - first.left.tz);
		displacement += Utils.square(last.right.tx - first.right.tx);
		displacement += Utils.square(last.right.ty - first.right.ty);
		displacement += Utils.square(last.right.tz - first.right.tz);

		return displacement;
	}

	void extract(double[] a)
	{
		a[0] = leftarm.get_delta(0);
		a[1] = leftarm.get_delta(1);
		a[2] = leftarm.get_delta(2);
		a[3] = rightarm.get_delta(0);
		a[4] = rightarm.get_delta(1);
		a[5] = rightarm.get_delta(2);
		a[6] = displacement;
	}
		
	void dump()
	{
		System.out.print("Left  Arm: ");
		leftarm.dump();
		System.out.print("Right Arm: ");
		rightarm.dump();
		System.out.printf("Displacement: %f\n", displacement);
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
