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
