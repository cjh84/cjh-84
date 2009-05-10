import java.util.*;
import org.joone.engine.*;

class Result 
{ 
	static final int CORRECT = 0;
	static final int FALSE_POS = 1;
	static final int FALSE_NEG = 2;
	static final int INCORRECT = 3;
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
		System.out.println("criterion = accuracy error performance");
		System.out.println("mode = training recognition recog-no-negs");		
		System.out.println("n_learner = 0{Basic} 1{Batch} 2{RProp}");
		System.out.println("n_epochs = <integer>");
		System.out.println("n_hidden_nodes = <integer>");
		System.out.println("n_learning_rate = <double>");
		System.out.println("n_momentum = <double>");
		System.out.println("n_train_on_negs = true false");
		System.out.println("m_learner = 0{Baulm-Welch} 1{K-Means}");
		System.out.println("m_hidden_states = <integer>");
		System.out.println("m_iterations = <integer>");
		System.exit(0);
	}

	static void parse_args(String[] argv)
	{
		if(argv.length < 1)
			usage();
		
		String[] attributes = new String[2];
		String arg;
		String key, value;
		
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
		user_args = args;
		dump_options();
		
		Utils.verbose = Boolean.valueOf(Config.lookup("verbose"));
		
		char[] escapedchars = new char[2];
		
		boolean nostar = mode.equalsIgnoreCase("training");
		boolean nobang = (Config.lookup("n_train_on_negs").equalsIgnoreCase("true") &&	classifier.name().equalsIgnoreCase("neural")) || 
				(mode.equalsIgnoreCase("training") && classifier.name().equalsIgnoreCase("markov")) || 
				(mode.equalsIgnoreCase("recog-no-negs") && criterion.equalsIgnoreCase("accuracy"));
				
		if (nostar && nobang)
			{ escapedchars[0] = '*'; escapedchars[1] = '!'; }
		else if (nostar)
			escapedchars[0] = '*';
		else if (nobang)
			escapedchars[0] = '!';
		else
			escapedchars = new char[0];

		Utils.log("escaping chars at eval = " + String.valueOf(escapedchars));
		
		samples = Utils.read_index(gesture_dir, escapedchars);
		num_samples = samples.size();
		
		Utils.log("Number of samples read in: " + num_samples);
				
		print_results();
	}
	
	static String[] user_args;
	
	private static void print_results()
	{
		if (criterion.equalsIgnoreCase("error"))
		{	
			double result = error();
			Utils.results("RMSE = " + String.format("%.5f", result));
		}
		else if (criterion.equalsIgnoreCase("accuracy"))
		{	
			double[] results = accuracy();
			Utils.results("Correct = " + String.format("%.3f", results[0]) + "%");
			Utils.results("False positives = " + String.format("%.3f", results[1]) + "%");
			Utils.results("False negatives = " + String.format("%.3f", results[2]) + "%");
		}
		else if (criterion.equalsIgnoreCase("performance"))
		{	
			long result = performance(); 
			Utils.results("Time in microseconds: " + String.valueOf(result/1000));
		}
		
		String[] attributes = new String[2];
		String user_options = "";
				
		for (int i = 1; i < user_args.length; i++)
		{
			attributes = user_args[i].split("=");
			if (!attributes[0].equalsIgnoreCase("verbose"))
				user_options += attributes[0] + "=" + attributes[1] + " ";
		}
		
		Utils.results(user_options);		
		
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
		if(!classifier.name().equalsIgnoreCase("neural") || !mode.equalsIgnoreCase("training"))
			Utils.error("Error criterion is only valid for " +
				"training the neural classifier.");
		
		Neural neural = new Neural();
		neural.train(samples, temp_file);
		return neural.err;
	}
	
	static double[] accuracy()
	{
		if (mode.equalsIgnoreCase("training"))
			Utils.error("Accuracy criterion is only valid for " + 
				"the recognition mode.");
		assert(mode.equalsIgnoreCase("recognition"));
				
		double[] counts = new double[4];
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
		else if (!s.gesture.equals(g))
		{
			return Result.INCORRECT;
		}
		return -1;
	} 
	
	static long performance()
	{
		long start_time = System.nanoTime();

		if (mode.equalsIgnoreCase("training"))
		{
			if (!classifier.name().equalsIgnoreCase("heuristic"))
			{
				tr = new Training(samples, classifier);
			}
			else
			{
				Utils.error("Performance criterion when training is " +
				"not valid for heuristic.");
			}
		}
		else
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
