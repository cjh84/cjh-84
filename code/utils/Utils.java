import java.io.*;
import java.util.*;

class Utils
{
	static boolean verbose = Boolean.valueOf(Config.lookup("verbose"));

	public static void error(String msg)
	{
		if (verbose)
			System.out.println(msg);
		System.exit(0);
	}

	static double square(double d)
	{
		return d * d;
	}
	
	static void delay(int ms)
	{
		try { Thread.sleep(ms); }
		catch (InterruptedException e) {}
	}
		
	public static void log(String msg)
	{
		if (verbose)
			System.out.println(msg);
	}

	static ArrayList<Sample> read_index(String gesture_dir, char[] escapechars)
	{
		/*
		* = sample not used for training (poor quality or reserved)
		# = negative examples
		*/
		
		Sample samp;
		Gesture gest;
		ArrayList<Sample> samples = new ArrayList<Sample>();
		String line;
		String[] parts;
		String index_pathname, record_filename, record_pathname;
		String slash = System.getProperty("file.separator");
		File dir;
		
		String index_filename = Config.lookup("index_filename");
		
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
				if(line.length() == 0 || in(line.charAt(0), escapechars))
					continue;
				parts = line.split(":");
				if(parts.length != 2)
					Utils.error("Invalid index file line: <" + line + ">");
				
				gest = Gesture.lookup(parts[1]);
				if(gest == null)
					Utils.error("Invalid gesture name <" + parts[1] + ">");
				
				//Remove any unescaped characters
				if (in(line.charAt(0), new char[] {'*', '#'}))
				{
					parts[0] = parts[0].substring(1);
				}
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
			Utils.error("Cannot read index from <" + index_pathname + ">");
		}
		return samples;
	}
	
	private static boolean in(char ch, char[] chars)
	{
		for (char c : chars)
		{
			if (c == ch)
				return true;
			else
				continue;
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
