class Heuristic extends Recogniser
{
	static double CLOSED_THRESHOLD = -1;
	
	public static Gesture recognise(Person person, Features features)
	{
		int command = Gesture.NoMatch;

		if(CLOSED_THRESHOLD < 0)
			CLOSED_THRESHOLD = Double.valueOf(Config.lookup("closedthreshold"));
		for(int i = 0; i < Gesture.num_gestures; i++)
		{
			if(match(person, features, i))
			{
				if(command == Gesture.NoMatch)
					command = i;
				else
					command = Gesture.MultiMatch;
			}
		}
		Gesture g = new Gesture(command);
		Utils.log("Heuristic recognised a " + g.toString());
		return g;
	}
	
	private static boolean match(Person person, Features features, int gesture)
	{
		Intervals left_range = person.left[gesture];
		Intervals right_range = person.right[gesture];
		double left_delta, right_delta;
		
		for(int axis = 0; axis < 3; axis++)
		{
			if(features.displacement > CLOSED_THRESHOLD)
				return false;
			
			left_delta = features.leftarm.get_delta(axis);
			if(left_range.get_min(axis) > left_delta ||
					left_range.get_max(axis) < left_delta)
				return false;
			
			right_delta = features.rightarm.get_delta(axis);
			if(right_range.get_min(axis) > right_delta ||
					right_range.get_max(axis) < right_delta)
				return false;
		}
		return true;
	}
};


class Intervals
{
	private double[][] range;
	int cm = 10;
	
	Intervals()
	{
		range = new double[3][2];
	}

	double get_min(int axis)
	{
		return range[axis][0];
	}
		
	double get_max(int axis)
	{
		return range[axis][1];
	}
		
	void setX(double from, double to)
	{ range[0][0] = from * cm; range[0][1] = to * cm; }
	void setY(double from, double to)
	{ range[1][0] = from * cm; range[1][1] = to * cm; }
	void setZ(double from, double to)
	{ range[2][0] = from * cm; range[2][1] = to * cm; }
	
	void stationary(int wobble)
	{
		for(int i = 0; i < 3; i++)
		{
			range[i][0] = 0;
			range[i][1] = wobble * cm;
		}
	}
	
	void stationary()
	{
		stationary(5);
	}
};
