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
		final int BUFFER_SIZE = 500;
		final int MIN_WINDOW = 50, MAX_WINDOW = 350, WINDOW_STEP = 10;
		CircularBuffer buf;
		int framecounter = 0, lastrecognition = 0;
		
		init();
		msg = scop.get_message();
		f = new Frame(msg);
		Transform.process(f);
		buf = new CircularBuffer(BUFFER_SIZE, f);
		
		while(true)
		{
			msg = scop.get_message();
			f = new Frame(msg);
			Transform.process(f);
			buf.add(f);
			framecounter++;
			if(framecounter % WINDOW_STEP == 0)
			{
				int availableframes = framecounter - lastrecognition;
				
				for(int windowsize = MIN_WINDOW; windowsize <= MAX_WINDOW &&
						windowsize <= availableframes; windowsize += WINDOW_STEP)
				{
					if(recognise(buf, windowsize) == true)
					{
						lastrecognition = framecounter;
						break;
					}
				}
			}
		}

		// scop.close();
	}
	
	static boolean recognise(CircularBuffer buf, int windowsize)
	{
		Gesture gesture;
		
		// System.out.println("Trying window size " + windowsize);
		Features feat = new Features(buf, windowsize);
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

class CircularBuffer
{
	Frame[] circ;
	int ptr; // Position for next incoming item
	int size;
	
	CircularBuffer(int size, Frame init)
	{
		this.size = size;
		circ = new Frame[size];
		for(int i = 0; i < size; i++)
			circ[i] = init;
		ptr = 0;
	}
	
	void add(Frame f)
	{
		circ[ptr] = f;
		ptr++;
		if(ptr >= size)
			ptr -= size;
	}
	
	Frame get(int windowsize, int index)
	{
		int pos;
		
		if(windowsize < 0 || windowsize > size ||
				index < 0 || index >= windowsize)
			return null;
		pos = ptr - windowsize + index;
		if(pos < 0)
			pos += size;
		return circ[pos];
	}
	
	void test()
	{
		Frame f;
		int windowsize;
				
		for(int i = 0; i < size + 100; i++)
		{
			f = new Frame();
			f.body = new SixDOF();
			f.body.tx = (double)i;
			add(f);
		}
		/* If size == 500, data layout should now be:
			500...599,100..499 */
		ptr = size / 2;
		windowsize = 2 * size / 5;
		for(int i = 0; i < windowsize; i++)
		{
			f = get(windowsize, i);
			System.out.print(f.body.tx + ", ");
		}
		System.out.print("\n\n");
		/* Should have displayed 550...599,100...249 */
		windowsize = 3 * size / 5;
		for(int i = 0; i < windowsize; i++)
		{
			f = get(windowsize, i);
			System.out.print(f.body.tx + ", ");
		}
		/* Should have displayed 450...499,500...599,100...249 */
	}
};
