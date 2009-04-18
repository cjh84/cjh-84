import java.io.*;
import java.util.*;

class RecordedGesture
{
	String filename;
	ArrayList<Frame> data;
};

class SimGesture
{
	static String gesture_dir;
	
	static void usage()
	{
		System.out.println("Usage: java SimGesture <gesture-dir>");
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		if(argv.length != 1)
			usage();
		gesture_dir = argv[0];
	}
	
	static ArrayList<RecordedGesture> read_gestures(String gesture_dir)
	{
		ArrayList<RecordedGesture> gestures;
		ArrayList<Frame> data;
		String filename;
		RecordedGesture record;
		File dir;
		File[] contents;
		
		gestures = new ArrayList<RecordedGesture>();
		
		dir = new File(gesture_dir);
		if(dir.exists() == false || dir.canRead() == false ||
				dir.isDirectory() == false)
		{
			System.out.println("Cannot read directory " + gesture_dir);
			System.exit(0);
		}
		contents = dir.listFiles();
		for(File f: contents)
		{
			if(f.getName().endsWith(".csv") == false)
				continue;
			filename = f.getPath();
			// System.out.println(filename);
			record = new RecordedGesture();
			record.data = GestureReader.getData(filename);
			record.filename = filename;
			gestures.add(record);
		}
		return gestures;
	}

	static void replay_gesture(SCOP scop, RecordedGesture gesture)
	{
		Frame f;

		System.out.println("Gesture: " + gesture.filename);		
		for(int i = 0; i < gesture.data.size(); i++)
		{
			f = gesture.data.get(i);
			scop.emit(f.toString());
			delay(10);
		}
	}

	static void interpolate_gestures(SCOP scop, RecordedGesture from_gesture,
			RecordedGesture to_gesture)
	{
		;
	}
		
	public static void main(String[] argv)
	{
		ArrayList<RecordedGesture> gestures;
		SCOP scop;
		final String scopserver = "localhost";
		// final String scopserver = "www.srcf.ucam.org";
		Random random;
		int current_gesture, next_gesture;
		
		parse_args(argv);
		gestures = read_gestures(gesture_dir);		
		System.out.println("Read in " + gestures.size() + " gestures");
		scop = new SCOP(scopserver, "simulatep1");
		if(scop.connection_ok() == false)
		{
			System.out.println("Can't connect to scopserver");
			System.exit(0);
		}
		scop.set_source_hint("p1coords");
		
		random = new Random(System.currentTimeMillis());
		
		while(true)
		{
			current_gesture = random.nextInt(gestures.size());
			replay_gesture(scop, gestures.get(current_gesture));
		}
	}
	
	static void delay(int ms)
	{
		try { Thread.sleep(ms); }
		catch (InterruptedException e) {}
	}
};
