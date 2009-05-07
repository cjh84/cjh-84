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
	
	//Neural options
	static int n_learner = 0; //Basic*, Batch, Rprop
	static int n_epochs = 1000, n_hidden_nodes = 20;
	static double n_learning_rate = 0.8, n_momentum = 0.3;
	static boolean n_train_on_negs = false;
	
	//Markov options
	static String m_learner = "Baulm-Welch"; //Baulm-Welch, K-Means
	static int m_hidden_states = 5, m_iterations = 10;

	static Training tr; 
	static int num_samples;
	static final String index_filename = "training.dat";
	static String gesture_dir;
	static final String temp_file = "tempfile.dat";
	static ArrayList<Sample> samples;
	
	static void usage()
	{
		System.out.println("Usage: java Evaluation **options <gesture-dir>");
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
		System.out.println("       <gesture-dir> must contain a file called " +
			index_filename);
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		if(argv.length < 1)
			usage();
		
		String[] attributes = new String[2];
		String key, value;
		String arg;
		
		for (int i = 0; i < argv.length - 1; i++)
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
				else if (key.equals("n_learner"))
				{ n_learner = Integer.parseInt(value);	}
				else if (key.equals("n_epochs"))
				{ n_epochs = Integer.parseInt(value);	}
				else if (key.equals("n_hidden_nodes"))
				{ n_hidden_nodes = Integer.parseInt(value);	}
				else if (key.equals("n_learning_rate"))
				{ n_learning_rate = Double.parseDouble(value); }
				else if (key.equals("n_momentum"))
				{ n_momentum = Double.parseDouble(value); }
				else if (key.equals("n_train_on_negs"))
				{ n_train_on_negs = Boolean.parseBoolean(value); }
				else if (key.equals("m_learner"))
				{ m_learner = value; }
				else if (key.equals("m_hidden_states"))
				{ m_hidden_states = Integer.parseInt(value); }
				else if (key.equals("m_iterations"))
				{ m_iterations = Integer.parseInt(value); }
				else
				{ usage(); }
			
			}
			catch (java.lang.ArrayIndexOutOfBoundsException e)
			{
				usage();
			}
		}
		
		gesture_dir = argv[argv.length - 1];
		
		samples = Training.read_index(gesture_dir, index_filename);
		num_samples = samples.size();
	}
	
	
	
	public static void main(String[] args)
	{
		Utils.verbose = false;
		parse_args(args);
		dump_options();
		
		System.out.println("Number of samples read in: " + num_samples);
		
		if (criterion.equals("error"))
		{	
			double result = Error();
			System.out.printf("Epochs = %6d, hidden nodes = %7d, RMSE = %.5f\n",
				n_epochs, n_hidden_nodes, result);
		}
		else if (criterion.equals("accuracy"))
		{	
			double[] results = Accuracy();
			System.out.println("Correct %: " + results[0]);
			System.out.println("False positives % = " + results[1]);
			System.out.println("False negatives % = " + results[2]);
		}
		else if (criterion.equals("performance"))
		{	
			long result = Performance(); 
			System.out.println("Time in ns: " + result);
		}
	}
	
	private static void dump_options()
	{
		Utils.verbose = true;
		Utils.log("person = " + user.name());
		Utils.log("classifier = " + classifier.name());
		Utils.log("criterion = " + criterion);
		Utils.log("mode = " + mode);
		Utils.log("n_learner = " + n_learner);
		Utils.log("n_epochs = " + n_epochs);
		Utils.log("n_hidden_nodes = " + n_hidden_nodes);
		Utils.log("n_learning_rate = " + n_learning_rate);
		Utils.log("n_momentum = " + n_momentum);
		Utils.log("n_train_on_negs = " + n_train_on_negs);
		Utils.log("m_learner = " + m_learner);
		Utils.log("m_hidden_states = " + m_hidden_states);
		Utils.log("m_iterations = " + m_iterations);
		Utils.verbose = false;
	}
	
	static double Error()
	{
		assert(classifier.name().equals("neural"));
		assert(mode.equals("training"));
		
		Neural neural = new Neural();
		
		neural.set_hidden_nodes(n_hidden_nodes);
		neural.train(samples, temp_file);
		return neural.err;
	}
	
	static double[] Accuracy()
	{
		assert(mode.equals("recognition"));
		
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
			counts[i] = counts[i] / num_samples;
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
	
	static long Performance()
	{
		long start_time = System.nanoTime();

		if (mode.equals("training"))
		{
			//assert( classifier is neural or markov only
			for (int i = 0; i < num_samples; i++)
			{
				tr = new Training(samples, classifier);
			}
		}
		else if (mode.equals("recognition"))
		{
			for (int i = 0; i < num_samples; i++)
			{
				classifier.recognise(person, samples.get(i).feat);
			}
		}		
		long end_time = System.nanoTime();
		return end_time - start_time;
	}
	
	//Billlllllllions of variables!
	
	static void markov_training()
	{
		/*
		System.out.println("Hidden markov models with different numbers of hidden states and iterations");
		Markov markov = new Markov();
		System.out.println("States Iterations RMSE");
		for (int epochs = 0; epochs < 2000; epochs += 100)
		{
			neural.set_epochs(epochs);
			for (int neurons = 2; neurons < 20; neurons += 2)
			{
				neural.set_hidden_nodes(neurons);
				neural.train(samples, temp_file);
				if ((epochs % 100 == 0) && (neurons % 2 == 0))
				{
					System.out.printf("%6d %7d %.5f\n", epochs, neurons, neural.err);
				}
			}
		}
		*/		
	}

	static void neural_training()
	{
		//TODO: Best of three because initial weights are random
		System.out.println("Neural networks with different numbers of epochs and neurons in the hidden layer");
		Neural neural = new Neural();
		System.out.println("Epochs Neurons RMSE");
		for (int epochs = 0; epochs < 2000; epochs += 100)
		{
			neural.set_epochs(epochs);
			for (int neurons = 2; neurons < 20; neurons += 2)
			{
				neural.set_hidden_nodes(neurons);
				neural.train(samples, temp_file);
				if ((epochs % 100 == 0) && (neurons % 2 == 0))
				{
					System.out.printf("%6d %7d %.5f\n", epochs, neurons, neural.err);
				}
			}
		}
	}
	
}
