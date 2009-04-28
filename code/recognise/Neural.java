import java.io.*;
import java.util.*;

import org.joone.log.*;
import org.joone.engine.*;
import org.joone.engine.learning.*;
import org.joone.io.*;
import org.joone.net.NeuralNet;

class Neural extends Recogniser
{
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
		
		// Look at pout.getArray() to decide what to do
		;
				
		return new Gesture(Gesture.NoMatch);
	}
	
	static void dump_results(Pattern pat)
	{
		double[] a = pat.getArray();
		Gesture gest = new Gesture(0);
		
		assert(a.length == Gesture.num_gestures);
		for(int i = 0; i < Gesture.num_gestures; i++)
		{
			gest.command = i;
			System.out.printf(gest.toString() + ": %5f\n", a[i]);
		}
	}
	
	static void init(Person p)
	{
		Layer input, output;

		if(p.nnet != null)
			return; // Already initialised
			
		p.nnet = Training.restoreNeuralNet(p.neural_file);
		
		input = p.nnet.getInputLayer();
		input.removeAllInputs();
		
		output = p.nnet.getOutputLayer();
		output.removeAllOutputs();
		
		p.netout = new DirectSynapse();
		output.addOutputSynapse(p.netout);
		
		p.nnet.getMonitor().setLearning(false);
	}
}

class Sample
{
	String pathname;
	Gesture gesture;
	ArrayList<Frame> data;
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
		System.out.println(pathname + " = " + gesture.toString());
	}
};

class Training implements NeuralNetListener
{
	static String gesture_dir, output_file;
	static final String index_filename = "training.dat";
	static final int num_epochs = 2000;
	static final int num_hidden_neurons = 20;
	
	LinearLayer input;
	SigmoidLayer hidden, output;
	FullSynapse synapse_IH, synapse_HO;
	NeuralNet nnet;
	Monitor monitor;
	MemoryInputSynapse inputStream, samples;
	TeachingSynapse trainer;
	
	static void usage()
	{
		System.out.println("Usage: java Training <gesture-dir> <output-file>");
		System.out.println("       <gesture-dir> must contain a file called " +
				index_filename);
		System.exit(0);
	}
	
	static void parse_args(String[] args)
	{
		if(args.length != 2)
			usage();
		gesture_dir = args[0];
		output_file = args[1];
	}
	
	static ArrayList<Sample> read_index(String gesture_dir,
			String index_filename)
	{
		Sample samp;
		Gesture gest;
		ArrayList<Sample> samples = new ArrayList<Sample>();
		String line;
		String[] parts;
		String index_pathname, record_filename, record_pathname;
		String slash = System.getProperty("file.separator");
		File dir;
		
		dir = new File(gesture_dir);
		if(dir.exists() == false || dir.canRead() == false ||
				dir.isDirectory() == false)
		{
			Utils.error("Cannot read directory " + gesture_dir);
		}
		index_pathname = gesture_dir + slash + index_filename;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(index_pathname));
			while((line = in.readLine()) != null)
			{
				if(line.length() == 0 || line.charAt(0) == '#')
					continue;
				parts = line.split(":");
				if(parts.length != 2)
					Utils.error("Invalid index file line: <" + line + ">");
				
				gest = Gesture.lookup(parts[1]);
				if(gest == null)
					Utils.error("Invalid gesture name <" + parts[1] + ">");
				
				record_filename = parts[0];
				record_pathname = gesture_dir + slash + record_filename;
				samp = new Sample(record_pathname, gest);
				samp.data = GestureReader.getData(record_pathname);
				Transform.process(samp.data);
				samp.feat = new Features(samp.data);
				samp.dump();
				samples.add(samp);
			}
			in.close();
		}
		catch(IOException e)
		{
			System.out.println("Cannot read index from <" + index_pathname + ">");
			System.exit(0);
		}
		return samples;
	}
	
	public static void main(String[] args)
	{
		ArrayList<Sample> samples;
		int num_samples;
		Sample samp;
		double[][] inputdata, outputdata;
		
		parse_args(args);
		
		samples = read_index(gesture_dir, index_filename);
				
		num_samples = samples.size();
		inputdata = new double[num_samples][Features.num_features];
		outputdata = new double[num_samples][Gesture.num_gestures];
		for(int i = 0; i < num_samples; i++)
		{
			samp = samples.get(i);
			samp.feat.extract(inputdata[i]);
			for(int j = 0; j < Gesture.num_gestures; j++)
				outputdata[i][j] = 0.0;
			if(samp.gesture.command < Gesture.num_gestures)
				outputdata[i][samp.gesture.command] = 1.0;
		}
		
		// dump_arrays(inputdata, outputdata);
		Training tr = new Training(inputdata, outputdata);
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
	
	Training(double[][] inputdata, double[][] outputdata)
	{
		int num_samples;
		
		num_samples = inputdata.length;
		assert(outputdata.length == num_samples);
		
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
		monitor.setLearningRate(0.8);
		monitor.setMomentum(0.3);
		monitor.setLearning(true);
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
	
	// NeuralNetListener interface methods follow:
	
	public void errorChanged(NeuralNetEvent e)
	{
		int cycle;
		double err;
		
		Monitor mon = (Monitor)e.getSource();
		cycle = mon.getCurrentCicle();
		if(cycle % 200 == 0 || cycle >= num_epochs - 10)
		{
			err = mon.getGlobalError();
			System.out.printf("%d epochs remaining; RMSE = %5f\n", cycle, err);
		}
	}
	
	public void netStarted(NeuralNetEvent e)
	{
		System.out.println("Training started");
	}
	
	public void netStopped(NeuralNetEvent e)
	{
		System.out.println("Training finished");
		saveNeuralNet(nnet, output_file);
	}
	
	public void netStoppedError(NeuralNetEvent e, String error)
	{
		System.out.println("Net stopped error: " + error);
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
};
