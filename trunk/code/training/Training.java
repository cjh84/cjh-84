import java.util.*;

import org.joone.log.*;
import org.joone.engine.*;
import org.joone.engine.learning.*;
import org.joone.io.*;
import org.joone.net.NeuralNet;

class Sample
{
	String pathname;
	Gesture gesture;
	ArrayList<Frame> data; //Processed and normalised
	Features feat;
	
	Sample(String pathname, Gesture g)
	{
		this.pathname = pathname;
		gesture = g;
		data = null;
		feat = null;
	}
	
	void dump()
	{
		Utils.log(pathname + " = " + gesture.toString());
	}
};

class Training
{
	static User user;
	static Classifier classifier;

	static String gesture_dir, output_file = "temp_file.out";
	
	static void usage()
	{
		String index_filename = Config.lookup("index_filename");
		
		System.out.println("Usage: java Training [classifier] [person] <gesture-dir>");
		System.out.println("       <gesture-dir> must contain a file called " +
				index_filename);
		System.out.println("People: " + User.all_usernames());
		System.out.println("Classifiers: Neural Markov");
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		if(argv.length < 1)
			usage();
			
		int id;
		
		user = new User();
		classifier = new Classifier();

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
		gesture_dir = argv[argv.length - 1];
		
		output_file = user.name() + "_" + classifier.name() + ".out";
		
		
	}
	
	public static void main(String[] args)
	{
		ArrayList<Sample> samples;		
		parse_args(args);
		Utils.log("Training using user " + user.name() +
				" and classifier " + classifier.name());
					
		char[] escapechars = {'#', '*'};
		samples = Utils.read_index(gesture_dir, escapechars);
		
		Training tr = new Training(samples, classifier);
				
	}
	
	static void dump_arrays(double[][] inputdata, double[][] outputdata)
	{
		int num_samples;
		
		num_samples = inputdata.length;
		assert(outputdata.length == num_samples);
		
		for(int i = 0; i < num_samples; i++)
		{
			System.out.printf("Sample %d features: ", i);
			for(int j = 0; j < Features.num_features; j++)
				System.out.print(inputdata[i][j] + " ");
			System.out.println("");
			System.out.printf("Outputs: ", i);
			for(int j = 0; j < Gesture.num_gestures; j++)
				System.out.print(outputdata[i][j] + " ");
			System.out.println("");
		}
	}
	
	Training(ArrayList<Sample> samples, Classifier clf)
	{
		clf.train(samples, output_file);
	}
	

};
