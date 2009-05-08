import java.util.*;
import org.joone.engine.*;

class Result 
{ 
	static final int CORRECT = 0;
	static final int FALSE_POS = 1;
	static final int FALSE_NEG = 2;
}

class Evaluation
{
	static User user = new User(); //Cheryl, David
	static Person person = user.get_person();
	static Classifier classifier = new Classifier(); //Heuristic, Neural, Markov, Hybrid
	static String criterion = "performance"; //Error, Accuracy, Performance
	static String mode = "recognition"; //Training, Recognition
	
	static Training tr; 
	static int num_samples;
	static final String index_filename = Config.lookup("index_filename");
	static String gesture_dir;
	static final String temp_file = "tempfile.dat";
	static ArrayList<Sample> samples;
	
	static void usage()
	{
		System.out.println("Usage: java Evaluation  <gesture-dir> **options");
		System.out.println("       <gesture-dir> must contain a file called " +
			index_filename);
		System.out.println("");
		System.out.println("Options are <key=value> pairs:");
		System.out.println("person = " + User.all_usernames());
		System.out.println("classifier = " + Classifier.all_classifiers());
		System.out.println("criterion = error accuracy performance");
		System.out.println("mode = training recognition");		
		System.out.println("n_learner = <integer>");
		System.out.println("n_epochs = <integer>");
		System.out.println("n_hidden_nodes = <integer>");
		System.out.println("n_learning_rate = <double>");
		System.out.println("n_momentum = <double>");
		System.out.println("n_train_on_negs = true false");
		System.out.println("m_learner = baulm-welch k-means");
		System.out.println("m_hidden_states = <integer>");
		System.out.println("m_iterations = <integer>");
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		if(argv.length < 1)
			usage();
		
		String[] attributes = new String[2];
		String key, value;
		String arg;
		
		for (int i = 1; i < argv.length; i++)
		{
			arg = argv[i];
			try
			{
				attributes = arg.split("=");
				key = attributes[0];
				value = attributes[1];

				if (key.equals("person"))
				{ if (User.lookup(value) != -1) { user.set(User.lookup(value)); person = user.get_person(); } } 
				else if (key.equals("classifier"))
				{ if (Classifier.lookup(value) != -1) { classifier.set(Classifier.lookup(value)); } } 
				else if (key.equals("criterion"))
				{ criterion = value; }
				else if (key.equals("mode"))
				{ mode = value; }
				else
				{ Config.set(key, value); }
			}
			catch (java.lang.ArrayIndexOutOfBoundsException e)
			{
				usage();
			}
		}
		gesture_dir = argv[0];
	}
	
	
	
	public static void main(String[] args)
	{
		parse_args(args);
		dump_options();
		
		Utils.verbose = Boolean.valueOf(Config.lookup("verbose"));
		
		char[] escapedchars = new char[2];
		
		boolean nostar = mode.equalsIgnoreCase("training");
		boolean nohash = Config.lookup("n_train_on_negs").equals("true") || 
			!classifier.name().equalsIgnoreCase("neural");
		if (nohash)
			assert(classifier.name().equalsIgnoreCase("neural"));
		
		if (nostar && nohash)
			{ escapedchars[0] = '*'; escapedchars[1] = '#'; }
		else if (nostar)
			escapedchars[0] = '*';
		else if (nohash)
			escapedchars[0] = '#';
		else
			escapedchars = new char[0];

		Utils.log("escaping chars at eval = " + String.valueOf(escapedchars));
		
		samples = Utils.read_index(gesture_dir, escapedchars);
		num_samples = samples.size();
		
		System.out.println("Number of samples read in: " + num_samples);
		
		if (criterion.equalsIgnoreCase("error"))
		{	
			double result = error();
			System.out.printf("RMSE = %.5f\n", result);
		}
		else if (criterion.equalsIgnoreCase("accuracy"))
		{	
			double[] results = accuracy();
			System.out.printf("Correct = %.3f %% \n", results[0]);
			System.out.printf("False positives = %.3f %% \n", results[1]);
			System.out.printf("False negatives = %.3f %% \n", results[2]);
		}
		else if (criterion.equalsIgnoreCase("performance"))
		{	
			long result = performance(); 
			System.out.println("Time in microseconds: " + String.valueOf(result/1000));
		}
	}
	
	private static void dump_options()
	{
		Utils.log("person = " + user.name());
		Utils.log("classifier = " + classifier.name());
		Utils.log("criterion = " + criterion);
		Utils.log("mode = " + mode);
		Config.dump_options();
	}
	
	static double error()
	{
		assert(classifier.name().equalsIgnoreCase("neural"));
		assert(mode.equalsIgnoreCase("training"));
		
		Neural neural = new Neural();
		neural.train(samples, temp_file);
		return neural.err;
	}
	
	static double[] accuracy()
	{
		assert(mode.equalsIgnoreCase("recognition"));
				
		double[] counts = new double[3];
		Sample s;
		Gesture g;
				
		for (int i = 0; i < num_samples; i++)
		{
			s = samples.get(i);
			g = classifier.recognise(person, s.feat);			
			counts[match(s, g)]++;
		}
		//Convert counts into %
		
		for (int i = 0; i < counts.length; i++)
		{
			counts[i] = counts[i] / num_samples * 100;
		}
		
		return counts;
	}
	
	private static int match(Sample s, Gesture g)
	{
		if (s.gesture.equals(g))
		{
			return Result.CORRECT;
		}
		else if (s.gesture.command == Gesture.NoMatch && g.command != Gesture.NoMatch)
		{
			return Result.FALSE_POS;
		}
		else if (s.gesture.command != Gesture.NoMatch && g.command == Gesture.NoMatch)
		{
			return Result.FALSE_NEG;
		}
		return -1;
	} 
	
	static long performance()
	{
		long start_time = System.nanoTime();

		if (mode.equalsIgnoreCase("training"))
		{
			assert(classifier.name().equalsIgnoreCase("neural") || 
				classifier.name().equalsIgnoreCase("markov"));
			tr = new Training(samples, classifier);
		}
		else if (mode.equalsIgnoreCase("recognition"))
		{
			for (int i = 0; i < num_samples; i++)
			{
				classifier.recognise(person, samples.get(i).feat);
			}
		}		
		long end_time = System.nanoTime();
		return end_time - start_time;
	}
	
	
		
}
