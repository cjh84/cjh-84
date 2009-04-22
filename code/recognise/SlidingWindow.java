import java.util.*;
import java.io.*;

class SlidingWindow
{
	static User user;
	static Person p;
	static Classifier classifier;
	static SCOP scopin, scopout, scopstat;
	static String player;
	
	static void init()
	{
		p = user.get_person();

		System.out.println("Using user " + user.name() +
				" and classifier " + classifier.name());
		
		String scopinserver, scopoutserver;
		scopinserver = Config.lookup("coordserver");
		scopoutserver = Config.lookup("ctrlserver");
		
		scopin = new SCOP(scopinserver, "windowp" + player);
		if(scopin.connection_ok() == false)
			Utils.error("Can't connect to scopserver <" + scopinserver + ">");
		scopin.listen("p" + player + "coords");
		
		scopout = new SCOP(scopoutserver, "windowp" + player);
		if(scopout.connection_ok() == false)
			Utils.error("Can't connect to scopserver <" + scopoutserver + ">");
		scopout.set_source_hint("p" + player + "ctrl");
		
		scopstat = new SCOP("www.srcf.ucam.org", "windowp" + player);
		if(scopstat.connection_ok() == false)
		{
			Utils.error("Can't connect to scopserver <" +
					"www.srcf.ucam.org" + ">");
		}
		scopstat.set_source_hint("p" + player + "status");		
	}
	
	static void usage()
	{
		Classifier c;
		
		System.out.println("Usage: java SlidingWindow <playernum> " +
				"[person] [classifier]");
		System.out.println("<playernum> = 1 or 2");
		System.out.println("People: " + User.all_usernames());
		System.out.println("Classifiers: " + Classifier.all_classifiers());
		System.exit(0);
	}
	
	static void parse_args(String[] argv)
	{
		int id;
		
		user = new User();
		classifier = new Classifier();
		if(argv.length < 1)
			usage();
		if(!(argv[0].equals("1")) && !(argv[0].equals("2")))
			usage();
		player = argv[0];
		for(int i = 1; i < argv.length; i++)
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
		boolean dropped_out = false, dropout;
		
		parse_args(args);
		init();
		msg = scopin.get_message();
		f = new Frame(msg);
		Transform.process(f);
		buf = new CircularBuffer(BUFFER_SIZE, f);
		start_time = System.currentTimeMillis();
		
		while(true)
		{
			before_block = System.nanoTime();
			msg = scopin.get_message();
			after_block = System.nanoTime();
			blocked_time += after_block - before_block;
			if(msg == null)
				Utils.error("Error reading message from SCOP");
			
			f = new Frame(msg);
			Transform.process(f);
			buf.add(f);
			framecounter++;
			
			dropout = f.dropout();
			if(dropout != dropped_out)
			{
				String status;
				if(dropout)
					status = "dropout";
				else
					status = "ok";
				scopstat.emit(status);
				System.out.println("New status: " + status);
				dropped_out = dropout;
			}
			
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
			scopout.emit(gesture.toAction());
			System.out.println("Recognised " + gesture.toString() +
					" between frames " + (framecounter - windowsize) +
					" and " + framecounter);
			return true;
		}
		return false;
	}
};
