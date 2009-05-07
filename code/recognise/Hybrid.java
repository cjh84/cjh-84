class Hybrid extends Recogniser
{
	public static Gesture recognise(Person person, Features features)
	{
		Gesture[] results = new Gesture[3];
		results[0] = Heuristic.recognise(person, features);
		results[1] = Neural.recognise(person, features);
		results[2] = Markov.recognise(person, features);
		
		Utils.log("Heuristic result: " + results[0]);
		Utils.log("Neural result: " + results[1]);
		Utils.log("Markov result: " + results[2]);
						
		//Find best of three or NoMatch otherwise
		Gesture result = new Gesture(Gesture.NoMatch);
		
		if (results[0].equals(results[1]) || results[0].equals(results[2]))
			result = results[0];
		else if (results[1].equals(results[2]))
			result = results[1];
		
		return result;
	}
};
