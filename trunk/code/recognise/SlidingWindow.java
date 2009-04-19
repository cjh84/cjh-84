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
		user = new User();
		classifier = new Classifier();
		p = user.get_person();

		System.out.println("Using user " + user.name() +
				" and classifier " + classifier.name());
		
		scop = new SCOP("localhost", "windowP1");
		if(scop.connection_ok() == false)
			Utils.error("Can't connect to scopserver");
		scop.listen("p1coords");
		scop.set_source_hint("p1ctrl");		
	}
	
	public static void main(String[] args)
	{
		String msg;
		Frame f;
		static int WINDOW_SIZE = 500;
		Frame[] circ;
		
		init();
		frames = new Frame[WINDOW_SIZE];
		msg = scop.get_message();
		f = new Frame(msg);
		Transform.process(f);
		for(int i = 0; i < WINDOW_SIZE; i++)
			circ[i] = f;
		
		while(true)
		{
			msg = scop.get_message();
			f = new Frame(msg);
			Transform.process(f);
			
		}

		// scop.close();
	}
	
	static boolean recognise(ArrayList<Frame> data)
	{
		Gesture gesture;
		
		Features feat = new Features(data);
		gesture = classifier.recognise(p, feat);
		if(gesture.command != Gesture.NoMatch &&
				gesture.command != Gesture.MultiMatch)
		{
			scop.emit(gesture.toAction());
			System.out.println(gesture.toString());
			return true;
		}
		return false;
	}
};
