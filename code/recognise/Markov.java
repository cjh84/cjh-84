import java.io.*;
import java.util.*;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.*;
import be.ac.ulg.montefiore.run.jahmm.io.*;

class Markov extends Recogniser
{

	ArrayList<Learner> learners;
	ArrayList<ArrayList<ArrayList<ObservationVector>>> sequences;
	
	static final int NUM_DIMENSIONS = 9;
	static int NUM_STATES = -1;
	static int NUM_ITERATIONS = -1;
	static int LEARNER = -1;
	
	String output_root;
	
	static int SCALING_FACTOR = 100;
	
	public static Gesture recognise(Person person, Features features)
	{
		String filename;
		double[] probabilities = new double[Gesture.num_gestures];
		ArrayList<ObservationVector> framedata = toObservationVectors(features.rawdata);
		
		Hmm<ObservationVector> recog_hmm = new Hmm<ObservationVector>(5, new OpdfMultiGaussianFactory(features.rawdata.size()));
				
		for (int i = 0; i < Gesture.num_gestures; i++)
		{
			filename  = person.markov_root + new Gesture(i).toAction();
			recog_hmm = restore_hmm(filename);
			probabilities[i] = calc_prob(recog_hmm, framedata);
		}

		dump_results(probabilities);

		int command = Gesture.NoMatch;
				
		for (int i = 0; i < Gesture.num_gestures; i++)
		{
			if(!Double.isNaN(probabilities[i]))
			{
				if(command == Gesture.NoMatch)
					command = i;
				else
					command = Gesture.MultiMatch;
			}
		}

		return new Gesture(command);
	}

	private static double calc_prob(Hmm hmm, ArrayList<ObservationVector> framedata)
	{
		return hmm.lnProbability(framedata);
	}

	static void dump_results(double[] a)
	{
		Gesture gest = new Gesture(0);
		
		assert(a.length == Gesture.num_gestures);
		for(int i = 0; i < Gesture.num_gestures; i++)
		{
			gest.command = i;
			Utils.log(gest.toString() + ": " + String.format("%5f", a[i]));
		}
	}

	void train(ArrayList<Sample> samples, String out_file)
	{
		init();
		output_root = out_file;

		for (Sample sample : samples)
		{
			Learner learner = learners.get(sample.gesture.command);
			learner.add_sequence(toObservationVectors(sample.data));
			Utils.log("Assigned " + sample.pathname +
			 " to learner for " + learner.gesture.toString());
		}
		
		long timing = System.nanoTime();
		
		for (Learner learner : learners)
		{
			Utils.log("Training " + learner.gesture.toString());
			if (LEARNER == 0)
			{	
				learner.learnbw();
			}
			else if (LEARNER == 1)
			{
				learner.learnkm();
			}
			else
			{
				Utils.error("Unknown learner; valid options are " +
					"0 for Baulm-Welch and 1 for K-Means");
			}
			
			save_hmm(learner, output_root + "_" + learner.gesture.toAction());
		}
		
        timing = System.nanoTime() - timing;
        
        Utils.log("Learner hidden-states iterations ms");
        Utils.results(LEARNER + " " + NUM_STATES + " " + NUM_ITERATIONS + " " + timing);
	}

	static ArrayList<ObservationVector> toObservationVectors(ArrayList<Frame> frames)
	{
		ArrayList<ObservationVector> ovs = new ArrayList<ObservationVector>();
		double[] values;
		
		for (int i = 0; i < frames.size(); i++)
		{
			values = frames.get(i).toDoubles();
			for (double v : values)
			{
				v  = v / SCALING_FACTOR;
			}
			ovs.add(new ObservationVector(values));
		}
		//System.out.println("Done sample " + s.pathname);
		return ovs;
	}
		
	static void save_hmm(Learner learner, String filename)
	{
		try
		{
		    Utils.log("Hmm saved to " + filename);
			FileOutputStream stream = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(stream);
			out.writeObject(learner.hmm);
			out.close();
		}
		catch (IOException e)
		{
			Utils.error("Cannot save hidden markov model to <" + filename + ">");
		}
	}
	
	static Hmm restore_hmm(String filename)
	{
		try
		{
		    Utils.log("Hmm loaded from " + filename);
			FileInputStream stream = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(stream);
			return (Hmm)in.readObject();
		}
		catch (IOException e)
		{
			Utils.error("Cannot load hidden markov model from <" + filename + ">");
		}
		catch (Exception e)
		{}
		return null;
	}

	private void init()
	{
		if(NUM_STATES < 0)
			NUM_STATES = Integer.valueOf(Config.lookup("m_hidden_states"));
		if(NUM_ITERATIONS < 0)
			NUM_ITERATIONS = Integer.valueOf(Config.lookup("m_iterations"));
		if(LEARNER < 0)
			LEARNER = Integer.valueOf(Config.lookup("m_learner"));

		learners = new ArrayList<Learner>(5);

		for (int i = 0; i < Gesture.num_gestures; i++)
		{
			learners.add(new Learner(NUM_STATES, NUM_DIMENSIONS, NUM_ITERATIONS,
				new Gesture(i)));
		}
	}
};

class Learner implements Serializable
{
	Hmm<ObservationVector> hmm;
	ArrayList<ArrayList<ObservationVector>> sequences;
	BaumWelchScaledLearner bwl;
	KMeansLearner<ObservationVector> kml; 
    OpdfMultiGaussianFactory factory;
    Gesture gesture;
    
	int num_states;
	int num_dimensions;
	int num_iterations;    

	Learner(int states, int dimensions, int iterations, Gesture g)
	{
		num_states = states;
		num_dimensions = dimensions;
		num_iterations = iterations;
		gesture = g;

		factory = new OpdfMultiGaussianFactory(num_dimensions);
		hmm = new Hmm<ObservationVector>(num_states, factory);
		sequences = new ArrayList<ArrayList<ObservationVector>>();
	}
	
	void add_sequence(ArrayList<ObservationVector> seq)
	{
		sequences.add(seq);
	}
	
	void learnbw()
	{
		bwl = new BaumWelchScaledLearner();
		bwl.setNbIterations(num_iterations);
		//One iteration of KMeansLearner to initalise
		hmm = new KMeansLearner<ObservationVector>(num_states, factory, sequences).iterate();
		hmm = bwl.learn(hmm, sequences);
	}
	
	void learnkm()
	{
		kml = new KMeansLearner<ObservationVector>(num_states, factory, sequences);
		hmm = kml.learn();
	}
}
