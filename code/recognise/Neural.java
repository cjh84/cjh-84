import java.io.*;
import java.util.*;

import org.joone.log.*;
import org.joone.engine.*;
import org.joone.engine.learning.*;
import org.joone.io.*;
import org.joone.net.NeuralNet;

class Neural extends Recogniser implements NeuralNetListener 
{
	static double NEURAL_THRESHOLD = -1;

	static int num_epochs = 2000;
	static int num_hidden_neurons = 20;
	static double learning_rate = 0.8;
	static double momentum = 0.3;
	static int learning_mode = 0;
	
	String output_file;
	
	LinearLayer input;
	SigmoidLayer hidden, output;
	FullSynapse synapse_IH, synapse_HO;
	NeuralNet nnet;
	Monitor monitor;
	MemoryInputSynapse inputStream, samples;
	TeachingSynapse trainer;

	double err;

	public static Gesture recognise(Person person, Features features)
	{		
		/* Uses:
			features.displacement
			features.leftarm|rightarm.get_delta(0|1|2)
			Total 7 input neurons
		*/
		
		double[] inputdata, outputdata;
		Pattern pin, pout;
		
		init(person); // Check person.nnet has been initialised

		inputdata = new double[Features.num_features];
		features.extract(inputdata);
		pin = new Pattern(inputdata);
		pin.setCount(person.neural_seq++);
		person.nnet.singleStepForward(pin);
		pout = person.netout.fwdGet();
		dump_results(pout);
		
		if(NEURAL_THRESHOLD < 0)
			NEURAL_THRESHOLD = Double.valueOf(Config.lookup("neuralthreshold"));
		
		
		double[] a = pout.getArray();
		int command = Gesture.NoMatch;
				
		for (int i = 0; i < Gesture.num_gestures; i++)
		{
			if(a[i] > NEURAL_THRESHOLD)
			{
				if(command == Gesture.NoMatch)
					command = i;
				else
					command = Gesture.MultiMatch;
			}
		}
		
		return new Gesture(command);

		// Look at pout.getArray() to decide what to do
				
		//return new Gesture(Gesture.NoMatch);
	}
	
	void train(ArrayList<Sample> sampleslist, String out_file)
	{
		output_file = out_file;
		Sample samp;
		
		int num_samples = sampleslist.size();
		double[][] inputdata = new double[num_samples][Features.num_features];
		double[][] outputdata = new double[num_samples][Gesture.num_gestures];
		for(int i = 0; i < num_samples; i++)
		{
			samp = sampleslist.get(i);
			samp.feat.extract(inputdata[i]);
			for(int j = 0; j < Gesture.num_gestures; j++)
				outputdata[i][j] = 0.0;
			if(samp.gesture.command < Gesture.num_gestures)
				outputdata[i][samp.gesture.command] = 1.0;
		}
		
		input = new LinearLayer();
		hidden = new SigmoidLayer();
		output = new SigmoidLayer();
		input.setLayerName("input");
		hidden.setLayerName("hidden");
		output.setLayerName("output");
		input.setRows(Features.num_features);
		hidden.setRows(num_hidden_neurons);
		output.setRows(Gesture.num_gestures);
		synapse_IH = new FullSynapse();
		synapse_HO = new FullSynapse();
		synapse_IH.setName("IH");
		synapse_HO.setName("HO");
		input.addOutputSynapse(synapse_IH);
		hidden.addInputSynapse(synapse_IH);
		hidden.addOutputSynapse(synapse_HO);
		output.addInputSynapse(synapse_HO);
		
		inputStream = new MemoryInputSynapse();
		inputStream.setInputArray(inputdata);
		set_columns(inputStream, 1, Features.num_features);
		input.addInputSynapse(inputStream);

		samples = new MemoryInputSynapse();
		samples.setInputArray(outputdata);
		set_columns(samples, 1, Gesture.num_gestures);
			
		trainer = new TeachingSynapse();
		trainer.setDesired(samples);
		output.addOutputSynapse(trainer);
			
		nnet = new NeuralNet();
		nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
		
		monitor = nnet.getMonitor();
		monitor.setTrainingPatterns(num_samples);
		monitor.setTotCicles(num_epochs);
		monitor.setLearningRate(learning_rate);
		monitor.setMomentum(momentum);
		monitor.setLearning(true);
		
		//Add learner
		
		monitor.addLearner(0, "org.joone.engine.BasicLearner"); // On-line
		monitor.addLearner(1, "org.joone.engine.BatchLearner"); // Batch
		monitor.addLearner(2, "org.joone.engine.RpropLearner"); // RPROP
		
		monitor.setLearningMode(learning_mode);
		
		monitor.setSingleThreadMode(true);
		monitor.addNeuralNetListener(this);
			
		nnet.go();
	}
	
	void set_columns(MemoryInputSynapse syn, int first, int last)
	{
		String cols = "";
		
		for(int i = first; i <= last; i++)
		{
			if(i == last)
				cols = cols + i;
			else
				cols = cols + i + ",";
		}
		syn.setAdvancedColumnSelector(cols);
	}
	
	void set_epochs(int epochs)
	{
		num_epochs = epochs;
	}
	
	void set_hidden_nodes(int neurons)
	{
		num_hidden_neurons = neurons;
	}
		
	// NeuralNetListener interface methods follow:
	
	public void errorChanged(NeuralNetEvent e)
	{
		int cycle;
		
		Monitor mon = (Monitor)e.getSource();
		cycle = mon.getCurrentCicle();
		if(cycle % 200 == 0 || cycle >= num_epochs - 10)
		{
			err = mon.getGlobalError();
			if (Utils.verbose)
				System.out.printf("%d epochs remaining; RMSE = %5f\n", cycle, err);
		}
	}
	
	public void netStarted(NeuralNetEvent e)
	{
		Utils.log("Training started");
	}
	
	public void netStopped(NeuralNetEvent e)
	{
		Utils.log("Training finished");
		saveNeuralNet(nnet, output_file);
	}
	
	public void netStoppedError(NeuralNetEvent e, String error)
	{
		Utils.log("Net stopped error: " + error);
	}
	
	public void cicleTerminated(NeuralNetEvent e) {}
	
	static void saveNeuralNet(NeuralNet nnet, String filename)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(stream);
			out.writeObject(nnet);
			out.close();
		}
		catch(Exception e)
		{
			Utils.error("Cannot save neural net to <" + filename + ">");
		}
	}
	
	static NeuralNet restoreNeuralNet(String filename)
	{
		try
		{
			FileInputStream stream = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(stream);
			return (NeuralNet)in.readObject();
		}
		catch(Exception e)
		{
			Utils.error("Cannot load neural net from <" + filename + ">");
		}
		return null; // Never happens
	}
	
	static void dump_results(Pattern pat)
	{
		double[] a = pat.getArray();
		Gesture gest = new Gesture(0);
		
		assert(a.length == Gesture.num_gestures);
		for(int i = 0; i < Gesture.num_gestures; i++)
		{
			gest.command = i;
			if (Utils.verbose)
				System.out.printf(gest.toString() + ": %5f\n", a[i]);
		}
	}
	
	static void init(Person p)
	{
		Layer input, output;

		if(p.nnet != null)
			return; // Already initialised
			
		p.nnet = restoreNeuralNet(p.neural_file);
		
		input = p.nnet.getInputLayer();
		input.removeAllInputs();
		
		output = p.nnet.getOutputLayer();
		output.removeAllOutputs();
		
		p.netout = new DirectSynapse();
		output.addOutputSynapse(p.netout);
		
		p.nnet.getMonitor().setLearning(false);
	}
}
