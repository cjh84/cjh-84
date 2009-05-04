import java.io.*;
import java.util.*;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.*;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;

class Markov extends Recogniser
{
	public static Gesture recognise(Person person, Features features)
	{
		return new Gesture(Gesture.NoMatch);
	}

	public static void main(String[] argv)
	{

		OpdfMultiGaussianFactory ogf = new OpdfMultiGaussianFactory(1);

		Hmm<ObservationInteger>hmm1 = new Hmm<ObservationInteger>(5, new OpdfIntegerFactory(10));
		Hmm<ObservationVector> hmm = new Hmm<ObservationVector>(5, ogf);
		
		

		
		//hmm = new Hmm<ObservationVector>(2);//, new OpdfGaussianFactory());
		List<List<ObservationVector>> sequences;
		
	}
};
