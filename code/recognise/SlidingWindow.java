import java.util.*;
import java.io.*;

class SlidingWindow
{
	static User user;
	static Person p;
	static Classifier classifier;
	static SCOP scop;
	
	static void init()
	{
		p = user.get_person();

		System.out.println("Using user " + user.name() +
				" and classifier " + classifier.name());
		
		scop = new SCOP("localhost", "windowP1");
		if(scop.connection_ok() == false)
			Utils.error("Can't connect to scopserver");
		scop.listen("p1coords");
		scop.set_source_hint("p1ctrl");		
	}
	
	static void usage()
	{
		Classifier c;
		
		System.out.println("Usage: java SlidingWindow [classifier] " +
				"[person [person2]]");
		System.out.println("People: " + User.all_usernames());
		System.out.println("Classifiers: " + Classifier.all_classifiers());
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		int id;
		User user2;
		
		user = user2 = null;
		classifier = new Classifier();
		for(int i = 0; i < argv.length; i++)
		{
			id = User.lookup(argv[i]);
			if(id != -1)
			{
				if(user == null)
					user = new User(id);
				else if(user2 == null)
					user2 = new User(id);
				else
					usage();
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
		if(user == null)
			user = new User();
		if(user2 == null)
			user2 = new User();
	}
	
	public static void main(String[] args)
	{
		String msg;
		Frame f;
		final int BUFFER_SIZE = 500;
		final int MIN_WINDOW = 50, MAX_WINDOW = 350, WINDOW_STEP = 10;
		CircularBuffer buf;
		int framecounter = 0, lastrecognition = 0, laststatus = 0;
		long start_time, current_time, elapsed_time;
		long before_block, after_block, blocked_time = 0;
		
		parse_args(args);
		init();
		msg = scop.get_message();
		f = new Frame(msg);
		Transform.process(f);
		buf = new CircularBuffer(BUFFER_SIZE, f);
		start_time = System.currentTimeMillis();
		
		while(true)
		{
			before_block = System.nanoTime();
			msg = scop.get_message();
			after_block = System.nanoTime();
			blocked_time += after_block - before_block;
			if(msg == null)
				Utils.error("Error reading message from SCOP");
			
			f = new Frame(msg);
			Transform.process(f);
			buf.add(f);
			framecounter++;
			if(framecounter % 1000 == 0)
			{
				double fps, cpu;
				
				current_time = System.currentTimeMillis();
				elapsed_time = current_time - start_time;
				cpu = (double)blocked_time / 1000000.0 / (double)elapsed_time;
				cpu = 100.0 * (1.0 - cpu);
				fps = (double)(framecounter - laststatus) / (double)elapsed_time
						* 1000.0;
				System.out.printf("FPS = %.1f, CPU utilisation = %.1f%%\n",
						fps, cpu);
				
				start_time = current_time;
				blocked_time = 0;
				laststatus = framecounter;
			}
			if(framecounter % WINDOW_STEP == 0)
			{
				int availableframes = framecounter - lastrecognition;
				
				for(int windowsize = MIN_WINDOW; windowsize <= MAX_WINDOW &&
						windowsize <= availableframes; windowsize += WINDOW_STEP)
				{
					if(recognise(buf, windowsize, framecounter) == true)
					{
						lastrecognition = framecounter;
						break;
					}
				}
			}
		}

		// scop.close();
	}
	
	static boolean recognise(CircularBuffer buf, int windowsize,
			int framecounter)
	{
		Gesture gesture;
		
		// System.out.println("Trying window size " + windowsize);
		Features feat = new Features(buf, windowsize);
		gesture = classifier.recognise(p, feat);
		if(gesture.command != Gesture.NoMatch &&
				gesture.command != Gesture.MultiMatch)
		{
			scop.emit(gesture.toAction());
			System.out.println("Recognised " + gesture.toString() +
					" between frames " + (framecounter - windowsize) +
					" and " + framecounter);
			return true;
		}
		return false;
	}
};
