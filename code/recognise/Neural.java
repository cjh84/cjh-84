import java.io.*;
import java.util.*;

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
		return new Gesture(Gesture.NoMatch);
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
	static final int NUM_INPUTS = 7;
	static String gesture_dir, output_file;
	static final String index_filename = "training.dat";
	
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
		parse_args(args);
		
		samples = read_index(gesture_dir, index_filename);
		System.exit(0); // XXXXX
				
		int num_samples = samples.size();
		double[][] data = new double[num_samples][NUM_INPUTS +
			Gesture.num_gestures + 1];
		
		Training tr = new Training(data);
	}
	
	LinearLayer input;
	SigmoidLayer hidden, output;
	FullSynapse synapse_IH, synapse_HO;
	NeuralNet nnet;
	Monitor monitor;
	MemoryInputSynapse inputStream, samples;
	TeachingSynapse trainer;
	
	Training(double[][] data)
	{
		final int epochs = 10000;
		
		input = new LinearLayer();
		hidden = new SigmoidLayer();
		output = new SigmoidLayer();
		input.setLayerName("input");
		hidden.setLayerName("hidden");
		output.setLayerName("output");
		input.setRows(NUM_INPUTS);
		hidden.setRows(20);
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
		inputStream.setInputArray(data);
		set_columns(inputStream, 1, NUM_INPUTS);
		input.addInputSynapse(inputStream);

		samples = new MemoryInputSynapse();
		samples.setInputArray(data);
		set_columns(samples, NUM_INPUTS + 1, NUM_INPUTS + Gesture.num_gestures);
			
		trainer = new TeachingSynapse();
		trainer.setDesired(samples);
		output.addOutputSynapse(trainer);
			
		nnet = new NeuralNet();
		nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
		
		monitor = nnet.getMonitor();
		monitor.setTrainingPatterns(data.length); // Should be 42
		monitor.setTotCicles(epochs);
		monitor.setLearningRate(0.8);
		monitor.setMomentum(0.3);
		monitor.setLearning(true);
		monitor.setSingleThreadMode(true);
		monitor.addNeuralNetListener(this);
		
		nnet.go(); // "async mode" (is there a synchonous way...?)
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
		if(cycle % 200 == 0)
		{
			err = mon.getGlobalError();
			System.out.println(cycle + " epochs remaining; RMSE = " + err);
		}
	}
	
	public void netStarted(NeuralNetEvent e)
	{
		System.out.println("Training started");
	}
	
	public void netStopped(NeuralNetEvent e)
	{
		System.out.println("Training finished");
	}
	
	public void netStoppedError(NeuralNetEvent e, String error)
	{
		System.out.println("Net stopped error: " + error);
	}
	
	public void cicleTerminated(NeuralNetEvent e) {}
};

