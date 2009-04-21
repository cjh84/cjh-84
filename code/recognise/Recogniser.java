import java.util.*;

class Gesture
{
	static final int TurnLeft = 0;
	static final int TurnRight = 1;
	static final int Accelerate = 2;
	static final int Decelerate = 3;
	static final int StartStop = 4;
	static final int NoMatch = 5;
	static final int MultiMatch = 6;
	
	static final int num_gestures = 5;
	
	int command;

	public String toAction()
	{
		switch(command)
		{
			case TurnLeft:   return "l";
			case TurnRight:  return "r";
			case Accelerate: return "a";
			case Decelerate: return "d";
			case StartStop:  return "s";
			case NoMatch:    return "u";
			case MultiMatch: return "u";
		}
		return "u"; //Unknown gesture
	}
	
	public String toString()
	{
		switch(command)
		{
			case TurnLeft: return "Turn left";
			case TurnRight: return "Turn right";
			case Accelerate: return "Accelerate";
			case Decelerate: return "Decelerate";
			case StartStop: return "Start/Stop";
			case NoMatch: return "No match";
			case MultiMatch: return "Multiple matches";
		}
		return "Unknown gesture";
	}
		
	Gesture(int cmd)
	{
		command = cmd;
	}
};

class User
{
	static final int CHERYL = 0;
	static final int DAVID = 1;
	
	static final int num_users = 2;
	
	private int userid;
	
	User()
	{
		set(CHERYL);
	}

	User(int who)
	{
		set(who);
	}

	String name()
	{
		switch(userid)
		{
			case CHERYL: return "Cheryl";
			case DAVID:  return "David";
		}
		return null; // Never occurs
	}
		
	void set(int who)
	{
		if(who < 0 || who >= num_users)
			Utils.error("User ID out of range");
		userid = who;
	}
		
	Person get_person()
	{
		switch(userid)
		{
			case CHERYL: return Person.create_cheryl();
			case DAVID:  return Person.create_david();
		}
		return null; // Never occurs
	}
	
	static String all_usernames()
	{
		StringBuilder sb = new StringBuilder();
		User u = new User();
		
		for(int i = 0; i < num_users; i++)
		{
			u.set(i);
			sb.append(u.name() + " ");
		}
		return sb.toString();
	}
	
	static int lookup(String name)
	{
		User testuser = new User();
		
		for(int id = 0; id < num_users; id++)
		{
			testuser.set(id);
			if(testuser.name().equalsIgnoreCase(name))
			{
				return id;
			}
		}
		return -1;
	}
};

class Classifier
{
	static final int HEURISTIC = 0;
	static final int NEURAL = 1;
	static final int MARKOV = 2;
	static final int HYBRID = 3;
	
	static final int num_classifiers = 4;
	
	private int id;
	
	Classifier()
	{
		set(HEURISTIC);
	}
	
	Classifier(int which)
	{
		set(which);
	}
	
	String name()
	{
		switch(id)
		{
			case HEURISTIC: return "Heuristic";
			case NEURAL:    return "Neural";
			case MARKOV:    return "Markov";
			case HYBRID:    return "Hybrid";
		}
		return null; // Never occurs
	}
	
	void set(int which)
	{
		if(which < 0 || which >= num_classifiers)
			Utils.error("Classifier ID out of range");
		id = which;
	}
	
	Gesture recognise(Person person, Features features)
	{
		switch(id)
		{
			case HEURISTIC: return Heuristic.recognise(person, features);
			case NEURAL:    return Neural.recognise(person, features);
			case MARKOV:    return Markov.recognise(person, features);
			case HYBRID:    return Hybrid.recognise(person, features);
		}
		return new Gesture(Gesture.NoMatch); // Never occurs
	}

	static String all_classifiers()
	{
		StringBuilder sb = new StringBuilder();
		Classifier c = new Classifier();
		
		for(int i = 0; i < num_classifiers; i++)
		{
			c.set(i);
			sb.append(c.name() + " ");
		}
		return sb.toString();
	}	

	static int lookup(String name)
	{
		Classifier testclassifier = new Classifier();
		
		for(int id = 0; id < num_classifiers; id++)
		{
			testclassifier.set(id);
			if(testclassifier.name().equalsIgnoreCase(name))
			{
				return id;
			}
		}
		return -1;
	}	
	
};

class Recogniser
{
	static Gesture recognise(Person person, Features features) { return null; }
	
	static String filename;
	static User user;
	static Classifier classifier;
	static boolean emitresults = false;
	
	static void usage()
	{
		Classifier c;
		
		System.out.println("Usage: java Recogniser [classifier] [person] " +
				"<filename.csv>");
		System.out.println("People: " + User.all_usernames());
		System.out.println("Classifiers: " + Classifier.all_classifiers());
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		int id;
		
		user = new User();
		classifier = new Classifier();
		if(argv.length < 1)
			usage();
		for(int i = 0; i < argv.length - 1; i++)
		{
			id = User.lookup(argv[i]);
			if(id != -1)
			{
				user.set(id);
				continue;
			}

			id = Classifier.lookup(argv[i]);
			if(id != -1)
			{
				classifier.set(id);
				continue;
			}

			usage();
		}
		filename = argv[argv.length - 1];
	}
	
	public static void main(String[] argv)
	{
		ArrayList<Frame> data;
		Person p = null;
		Gesture gesture;
		SCOP scop = null;
		String scopserver = "localhost";
		// String scopserver = "www.srcf.ucam.org";

		parse_args(argv);
		System.out.println("Using user " + user.name() +
				" and classifier " + classifier.name());

		if(emitresults)
		{
			// Not normally used; hardcodes to player 1
			scop = new SCOP(scopserver, "recognisep1");
			if(scop.connection_ok() == false)
				Utils.error("Can't connect to scopserver");
			scop.set_source_hint("p1ctrl");
		}
		
		data = GestureReader.getData(filename);		
		Transform.process(data);
		Features feat = new Features(data);
		p = user.get_person();
		gesture = classifier.recognise(p, feat);
		System.out.println(filename + ": " + gesture.toString());
		if(emitresults)
			scop.emit(gesture.toAction());
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

class Person
{
	Intervals[] left;
	Intervals[] right;
	
	Person()
	{
		left = new Intervals[Gesture.num_gestures];
		right = new Intervals[Gesture.num_gestures];
		for(int i = 0; i < Gesture.num_gestures; i++)
		{
			left[i] = new Intervals();
			right[i] = new Intervals();
		}
	}

	void tabulate()
	{
		final int cm = 10;
		
		for(int i = 0; i < Gesture.num_gestures; i++)
		{
			System.out.println(new Gesture(i).toString());
			System.out.printf("L %2.0f-%2.0f %2.0f-%2.0f %2.0f-%2.0f\n",
					left[i].get_min(0) / cm, left[i].get_max(0) / cm,
					left[i].get_min(1) / cm, left[i].get_max(1) / cm,
					left[i].get_min(2) / cm, left[i].get_max(2) / cm);
			System.out.printf("R %2.0f-%2.0f %2.0f-%2.0f %2.0f-%2.0f\n",
					right[i].get_min(0) / cm, right[i].get_max(0) / cm,
					right[i].get_min(1) / cm, right[i].get_max(1) / cm,
					right[i].get_min(2) / cm, right[i].get_max(2) / cm);
			System.out.println("");
		}
	}
	
	static Person create_david()
	{
		Person dmi = new Person();
		
		dmi.left[Gesture.TurnLeft].setX(20,35);
		dmi.left[Gesture.TurnLeft].setY(0,18);
		dmi.left[Gesture.TurnLeft].setZ(65,80);
		dmi.right[Gesture.TurnLeft].stationary(7);
		
		dmi.left[Gesture.TurnRight].stationary(7);
		dmi.right[Gesture.TurnRight].setX(20,35);
		dmi.right[Gesture.TurnRight].setY(0,18);
		dmi.right[Gesture.TurnRight].setZ(65,80);
		
		dmi.left[Gesture.Accelerate].stationary();
		dmi.right[Gesture.Accelerate].setX(0,10);
		dmi.right[Gesture.Accelerate].setY(10,20);
		dmi.right[Gesture.Accelerate].setZ(25,40);
		
		dmi.left[Gesture.Decelerate].setX(0,10);
		dmi.left[Gesture.Decelerate].setY(10,20);
		dmi.left[Gesture.Decelerate].setZ(25,40);
		dmi.right[Gesture.Decelerate].stationary();
		
		dmi.left[Gesture.StartStop].setX(14,25);
		dmi.left[Gesture.StartStop].setY(20,35);
		dmi.left[Gesture.StartStop].setZ(15,30);
		dmi.right[Gesture.StartStop].setX(14,25);
		dmi.right[Gesture.StartStop].setY(20,35);
		dmi.right[Gesture.StartStop].setZ(15,30);
		
		return dmi;
	}
		
	static Person create_cheryl()
	{
		Person ch = new Person();
		
		ch.left[Gesture.TurnLeft].setX(15,30);
		ch.left[Gesture.TurnLeft].setY(0,18);
		ch.left[Gesture.TurnLeft].setZ(35,55);
		ch.right[Gesture.TurnLeft].stationary();
		
		ch.left[Gesture.TurnRight].stationary();
		ch.right[Gesture.TurnRight].setX(15,30);
		ch.right[Gesture.TurnRight].setY(0,18);
		ch.right[Gesture.TurnRight].setZ(35,55);
		
		ch.left[Gesture.Accelerate].stationary();
		ch.right[Gesture.Accelerate].setX(0,10);
		ch.right[Gesture.Accelerate].setY(10,32);
		ch.right[Gesture.Accelerate].setZ(20,35);
		
		ch.left[Gesture.Decelerate].setX(0,10);
		ch.left[Gesture.Decelerate].setY(10,32);
		ch.left[Gesture.Decelerate].setZ(20,35);
		ch.right[Gesture.Decelerate].stationary();
		
		ch.left[Gesture.StartStop].setX(14,30);
		ch.left[Gesture.StartStop].setY(15,35);
		ch.left[Gesture.StartStop].setZ(15,30);
		ch.right[Gesture.StartStop].setX(14,30);
		ch.right[Gesture.StartStop].setY(15,35);
		ch.right[Gesture.StartStop].setZ(15,30);
		
		return ch;
	}
};

class Heuristic extends Recogniser
{
	static double CLOSED_THRESHOLD = -1;
	
	// static final double CLOSED_THRESHOLD = 3000;
	// static final double CLOSED_THRESHOLD = 100;
	
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
		return new Gesture(command);
	}
	
	private static boolean match(Person person, Features features, int gesture)
	{
		Intervals left_range = person.left[gesture];
		Intervals right_range = person.right[gesture];
		double left_delta, right_delta;
		
		for(int axis = 0; axis < 3; axis++)
		{
			if(features.relocation > CLOSED_THRESHOLD)
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

class Neural extends Recogniser
{
	public static Gesture recognise(Person person, Features features)
	{
		return new Gesture(Gesture.NoMatch);
	}
};

class Hybrid extends Recogniser
{
	public static Gesture recognise(Person person, Features features)
	{
		return new Gesture(Gesture.NoMatch);
	}
};

class Markov extends Recogniser
{
	public static Gesture recognise(Person person, Features features)
	{
		return new Gesture(Gesture.NoMatch);
	}
};
