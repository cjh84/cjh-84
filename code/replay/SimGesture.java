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
	static final int FRAME_RATE = 100;
	
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

		System.out.println("Replaying gesture " + gesture.filename +
				" (" + gesture.data.size() + " frames)");		
		for(int i = 0; i < gesture.data.size(); i++)
		{
			f = gesture.data.get(i);
			scop.emit(f.toString());
			delay(1000 / FRAME_RATE);
		}
	}

	static double interpolate_angle(double from, double to, double w)
	{
		double v = 1 - w;
		double x1, y1, x2, y2, x, y;
		double theta;
		
		x1 = Math.sin(from) * v;
		y1 = Math.cos(from) * v;
		x2 = Math.sin(to) * w;
		y2 = Math.cos(to) * w;
		x = x1 + x2;
		y = y1 + y2;
		theta = Math.atan2(y, x);
		return theta;
	}
	
	static SixDOF interpolate_sixdof(SixDOF from, SixDOF to, double w)
	{
		// "w" is the weighting for the "to" body part (0.0 - 1.0)
		SixDOF sixdof = new SixDOF();
		double v = 1 - w;
		
		sixdof.ax = v * from.ax + w * to.ax;
		sixdof.ay = v * from.ay + w * to.ay;
		sixdof.az = v * from.az + w * to.az;
		sixdof.angle = interpolate_angle(from.angle, to.angle, w);
		
		sixdof.tx = v * from.tx + w * to.tx;
		sixdof.ty = v * from.ty + w * to.ty;
		sixdof.tz = v * from.tz + w * to.tz;
		return sixdof;
	}
	
	static Frame interpolate_frames(Frame from, Frame to, double weight)
	{
		// "weight" is the weighting for the "to" frame (0.0 - 1.0)
		Frame f = new Frame();
		
		f.body = interpolate_sixdof(from.body, to.body, weight);
		f.left = interpolate_sixdof(from.left, to.left, weight);
		f.right = interpolate_sixdof(from.right, to.right, weight);
		return f;
	}
	
	static void interpolate_gestures(SCOP scop, RecordedGesture from_gesture,
			RecordedGesture to_gesture, int duration)
	{
		int num_frames;
		Frame from, to, f;
		
		System.out.println("Interpolating for " + duration + " ms");
		from = from_gesture.data.get(from_gesture.data.size() - 1);
		to = to_gesture.data.get(0);
		num_frames = (duration * FRAME_RATE) / 1000;
		for(int i = 0; i < num_frames; i++)
		{
			f = interpolate_frames(from, to,
					(double)(i + 1) / (double)(num_frames + 1));
			f.dump();
			scop.emit(f.toString());
			delay(1000 / FRAME_RATE);
		}
		System.exit(0); // XXXXX
	}
		
	public static void main(String[] argv)
	{
		ArrayList<RecordedGesture> gestures;
		SCOP scop;
		final String scopserver = "localhost";
		// final String scopserver = "www.srcf.ucam.org";
		Random random;
		int current_gesture, next_gesture, duration;
		
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
		
		current_gesture = random.nextInt(gestures.size());
		while(true)
		{
			replay_gesture(scop, gestures.get(current_gesture));
			next_gesture = random.nextInt(gestures.size());
			// Inter-gesture time min 0.1s, max 3s:
			duration = 100 + random.nextInt(2900);
			interpolate_gestures(scop, gestures.get(current_gesture),
					gestures.get(next_gesture), duration);
			current_gesture = next_gesture;
		}
	}
	
	static void delay(int ms)
	{
		try { Thread.sleep(ms); }
		catch (InterruptedException e) {}
	}
};
